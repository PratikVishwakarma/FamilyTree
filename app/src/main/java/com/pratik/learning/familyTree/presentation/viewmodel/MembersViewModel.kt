package com.pratik.learning.familyTree.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.data.local.model.MemberFilter
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.navigation.AddMember
import com.pratik.learning.familyTree.navigation.MemberDetailsRoute
import com.pratik.learning.familyTree.presentation.UIState
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.RELATION_TYPE_BROTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_DAUGHTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_HUSBAND
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SON
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE
import com.pratik.learning.familyTree.utils.SyncPrefs.setIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.calculateAgeFromDob
import com.pratik.learning.familyTree.utils.logger
import com.pratik.learning.familyTree.utils.validateMemberData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembersViewModel @Inject constructor(
    private val familyTreeRepository: FamilyTreeRepository,
    @ApplicationContext val context: Context
) : BaseViewModel() {

    var relationType = ""

    var relatedMembers = emptyList<Int>()

    private val _filter = MutableStateFlow(MemberFilter())
    val filter: StateFlow<MemberFilter> = _filter

    private val _query = MutableStateFlow("")
    var query: StateFlow<String> = _query

    private val _isUnmarried = MutableStateFlow(false)
    private val _isMale = MutableStateFlow(false)
    var isMale: StateFlow<Boolean> = _isMale

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        _filter.update { it.copy(query = newQuery) }
    }

    private val _error = MutableStateFlow("")
    var error: StateFlow<String> = _error

    private val _uiState = MutableStateFlow<UIState>(UIState.IdealUIState)
    var uiState: StateFlow<UIState> = _uiState


    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val filterResult: Flow<PagingData<MemberWithFather>> =
        combine(
            _query.debounce(500),
            _isUnmarried
        ) { query, isUnmarried ->
            query to isUnmarried
        }
            .flatMapLatest { (query, isUnmarried) ->
                familyTreeRepository.getPagedMembersForSearchByName(query, isUnmarried)
                    .map { pagingData ->
                        pagingData
                            // filter the relation type by gender
                            .filter { member ->
                                matchesRelationType(member = member)
                            }
                            // filter out already related members like spouse, parents, siblings, grandparents etc.
                            .filter { member ->
                                relatedMembers.isEmpty() || member.memberId !in relatedMembers
                            }.filter {
                                // additional filter in case of unmarried + not dead + younger than 45 years
                                if (isUnmarried) {
                                    it.isLiving && calculateAgeFromDob(it.dob) < 45
                                } else true
                            }
                    }
            }.cachedIn(viewModelScope)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val filterResult2 : Flow<PagingData<MemberWithFather>> =
        filter
            .debounce(500)
            .flatMapLatest { filter ->
                familyTreeRepository.getPagedMembersForSearchByFilter(
                    name = filter.query,
                    filterMatrix = filter
                ).map { pagingData ->
                    pagingData
                        .filter { member ->
                            matchesRelationType(member)
                        }
                        .filter { member ->
                            relatedMembers.isEmpty() ||
                                    member.memberId !in relatedMembers
                        }
                        .filter { member ->
                            applyAdditionalFilters(member, filter)
                        }
                }
            }.cachedIn(viewModelScope)


    private fun matchesRelationType(member: MemberWithFather): Boolean =
        when (relationType) {
            RELATION_TYPE_MOTHER,
            RELATION_TYPE_DAUGHTER,
            RELATION_TYPE_SISTER,
            RELATION_TYPE_WIFE -> member.gender == "F"

            RELATION_TYPE_FATHER,
            RELATION_TYPE_SON,
            RELATION_TYPE_BROTHER,
            RELATION_TYPE_HUSBAND -> member.gender == "M"

            else -> true
        }

    private fun applyAdditionalFilters(
        member: MemberWithFather,
        filter: MemberFilter
    ): Boolean {

        // unmarried rule
        if (filter.isUnmarried) {
            if (!member.isLiving) return false
            if (calculateAgeFromDob(member.dob) >= 45) return false
        }

        filter.isLeaving?.let {
            return member.isLiving == it
        }
        // gender filter (future-safe)
        filter.isMale?.let {
            if ((member.gender == "M") != it) return false
        }

        // city
        if (filter.city.isNotBlank() &&
            !member.city.equals(filter.city, ignoreCase = true)
        ) return false

        // gotra
        if (filter.gotra.isNotBlank() &&
            !member.gotra.equals(filter.gotra, ignoreCase = true)
        ) return false

        return true
    }
    fun navigateToAddMember(navController: NavController) {
        navController.navigate(route = AddMember)
    }

    fun addMember(member: MemberFormState, navController: NavController) {
        logger("addMember:: $member")


        viewModelScope.launch {
            val familyMember = FamilyMember(
                fullName = member.fullName.trim(),
                gotra = member.gotra.trim(),
                dob = member.dob,
                gender = member.gender,
                isLiving = member.isLiving,
                dod = member.dod,
                city = member.city.trim(),
                state = member.state,
                mobile = member.mobile,
                isNewEntry = true
            )
            val validateMemberData = validateMemberData(member)
            if (validateMemberData.isNotEmpty()) {
                _error.value = validateMemberData
                return@launch
            }

            val insertMember = familyTreeRepository.insertMember(familyMember)
            if (insertMember != -1L) {
                navController.popBackStack()
                setIsDataUpdateRequired(context, true)
                navController.navigate(MemberDetailsRoute(insertMember.toInt()))
            }
        }
    }

    fun onUnmarriedCheck(checked: Boolean) {
        _isUnmarried.value = checked
        _filter.update { it.copy(isUnmarried = checked) }
    }

    fun onIsLivingCheck(checked: Boolean) {
        _filter.update { it.copy(isLeaving = checked) }
    }

    fun onSortByChange(newValue: String) {
        _filter.update { it.copy(sortBy = newValue) }
    }

    fun showExpandedFilter() {
        if (_uiState.value is UIState.FilterExpandedUIState)
            return
        _uiState.value = UIState.FilterExpandedUIState
    }

    fun dismissExpandedMember() {
        if (_uiState.value is UIState.IdealUIState)
            return
        _uiState.value = UIState.IdealUIState
    }

    fun showExpandedMember(member: MemberWithFather) {
        val expandedUI = UIState.ExpandViewUIState(member)
        if (_uiState.value is UIState.IdealUIState)
            _uiState.value = expandedUI
    }


    fun dismissFilterExpand() {
        if (_uiState.value is UIState.IdealUIState)
            return
        _uiState.value = UIState.IdealUIState
    }


}

