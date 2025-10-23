package com.pratik.learning.familyTree.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.navigation.AddMember
import com.pratik.learning.familyTree.navigation.MemberDetailsRoute
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.RELATION_TYPE_BROTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_DAUGHTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_HUSBAND
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SON
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE
import com.pratik.learning.familyTree.utils.SyncPrefs.getIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.SyncPrefs.setIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.SyncPrefs.shouldSync
import com.pratik.learning.familyTree.utils.isAdmin
import com.pratik.learning.familyTree.utils.logger
import com.pratik.learning.familyTree.utils.validateMemberData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembersViewModel @Inject constructor(
    private val familyTreeRepository: FamilyTreeRepository,
    @ApplicationContext val context: Context
) : ViewModel() {

    var relationType = ""

    private val _isDataLoaded = MutableStateFlow(false)
    var isDataLoaded: StateFlow<Boolean> = _isDataLoaded

    private val _query = MutableStateFlow("")
    var query: StateFlow<String> = _query

    private val _isUnmarried = MutableStateFlow(false)
    var isUnmarried: StateFlow<Boolean> = _isUnmarried

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    private val _error = MutableStateFlow("")
    var error: StateFlow<String> = _error

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
                        pagingData.filter { member ->
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
                        }
                    }
            }.cachedIn(viewModelScope)

    fun navigateToAddMember(navController: NavController) {
        viewModelScope.launch(Dispatchers.IO) {
            familyTreeRepository.downloadDataFromServer()
            return@launch
        }
        navController.navigate(route = AddMember)
    }

    fun uploadDataOnServer() {
        viewModelScope.launch(Dispatchers.IO) {
            familyTreeRepository.syncDataToFirebase()
        }
    }

    private fun downloadDataFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            _isDataLoaded.value = familyTreeRepository.downloadDataFromServer()
        }
    }

    fun addMember(member: MemberFormState, navController: NavController) {
        Log.d("MembersViewModel", "addMember: $member")


        viewModelScope.launch {
            val familyMember = FamilyMember(
                fullName = member.fullName,
                gotra = member.gotra,
                dob = member.dob,
                gender = member.gender,
                isLiving = member.isLiving,
                dod = member.dod,
                city = member.city,
                state = member.state,
                mobile = member.mobile
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
    }
}

