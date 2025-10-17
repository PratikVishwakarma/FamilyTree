package com.pratik.learning.familyTree.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.utils.RELATION_TYPE_BROTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_DAUGHTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_HUSBAND
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SON
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembersViewModel @Inject constructor(
    private val familyTreeRepository: FamilyTreeRepository,
) : ViewModel() {

    var relationType = ""

    private val _query = MutableStateFlow("")
    var query: StateFlow<String> = _query

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val filterResult: Flow<PagingData<MemberWithFather>> = _query
        .debounce(500)
//        .filter { it.isNotBlank() } // comment if you want to see all members
        .flatMapLatest { query ->
            familyTreeRepository.getPagedMembersForSearchByName(query)
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

    fun addMember(member: MemberFormState, navController: NavController) {
        viewModelScope.launch {
            val familyMember = FamilyMember(
                fullName = member.fullName,
                dob = member.dob,
                gender = member.gender,
                isLiving = member.isLiving,
                dod = member.dod,
                city = member.city,
                mobile = member.mobile
            )
            val insertMember = familyTreeRepository.insertMember(familyMember)
            if (insertMember != -1L) {
                navController.popBackStack()
            }
        }
    }
}

