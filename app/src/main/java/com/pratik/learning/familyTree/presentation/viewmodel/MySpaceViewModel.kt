package com.pratik.learning.familyTree.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.MemberRelationAR
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.utils.SyncPrefs.getMyFavMemberIDs
import com.pratik.learning.familyTree.utils.SyncPrefs.getMyUerID
import com.pratik.learning.familyTree.utils.SyncPrefs.setMyUserID
import com.pratik.learning.familyTree.utils.logger
import com.pratik.learning.familyTree.utils.toRelationMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MySpaceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FamilyTreeRepository,
) : BaseViewModel() {
    var mySpaceId: Int = -1


    // to fetch relatives
    private val _relatives = MutableStateFlow(MemberRelationAR())
    var relatives: StateFlow<MemberRelationAR> = _relatives

    private val _member = MutableStateFlow<FamilyMember?>(null)
    var member: StateFlow<FamilyMember?> = _member

    private val _uiState = MutableStateFlow<MySpaceUIState>(MySpaceUIState.MyProfile())
    var uiState: StateFlow<MySpaceUIState> = _uiState


    private val _memberNameSearch = MutableStateFlow("")
    var memberNameSearch: StateFlow<String> = _memberNameSearch

    var myFavMemberIDs = emptyList<Int>()

    var toRelationMap: Map<Int, String> = emptyMap()
    var memberSmallBio = ""

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val favMember: Flow<PagingData<MemberWithFather>> =
        _memberNameSearch
            .debounce(500)
            .flatMapLatest { name ->
                repository.getPagedFavMembersForSearchByFilter(name, myFavMemberIDs)
            }
            .map { pagingData ->
                pagingData.map { member ->
                    logger("my fav person: ${member.fullName}")
                    member   // IMPORTANT: return item
                }
            }
            .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            mySpaceId = getMyUerID(context)
            logger("current user id: $mySpaceId")
            if (mySpaceId != -1) {
                fetchMySpaceUserDetails()
            }
            myFavMemberIDs = getMyFavMemberIDs(context)
        }
    }

    /**
     * to fetch/get the user details based on the id
     * */
    fun fetchMySpaceUserDetails() {
        logger("fetchCurrentUserDetails initiated for id: $mySpaceId")
        viewModelScope.launch(Dispatchers.IO) {
            _member.value = repository.getMemberById(mySpaceId)
            fetchMySpaceUserRelatives()
        }
    }

    /**
     * to fetch/get the user other relatives based on the id
     * */
    fun fetchMySpaceUserRelatives() {
        logger("fetchMySpaceUserRelatives initiated for id: $mySpaceId")
        viewModelScope.launch(Dispatchers.IO) {
            _relatives.value = repository.getMemberRelatives(memberId = mySpaceId)
            toRelationMap = _relatives.value.toRelationMap()
            memberSmallBio = repository.getMemberSmallBio(_relatives.value, true)
        }
    }


    /**
     * to set the mySpace user id and fetch the details
     * and also to clear the current member
     * */
    fun setMySpaceUserId(id: Int) {
        logger("setMySpaceUserId initiated for id: $id")
        viewModelScope.launch(Dispatchers.IO) {
            mySpaceId = id
            setMyUserID(context, id)
            if (id == -1) _member.value = null
            else fetchMySpaceUserDetails()
        }
    }

    /**
     *
     * */
    fun switchSection(section: MySpaceUIState) {
        logger("switchSection initiated for section: $section")
        if (_uiState.value is MySpaceUIState.MyProfile && section is MySpaceUIState.MyProfile) return
        if (_uiState.value is MySpaceUIState.MyRelatives && section is MySpaceUIState.MyRelatives) return
        if (_uiState.value is MySpaceUIState.MyFavList && section is MySpaceUIState.MyFavList) return
        _uiState.value = section
    }
}

sealed class MySpaceUIState {
    class MyProfile : MySpaceUIState()
    class MyRelatives : MySpaceUIState()
    class MyFavList : MySpaceUIState()
}