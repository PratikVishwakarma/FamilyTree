package com.pratik.learning.familyTree.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.drawToBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.pratik.learning.familyTree.data.local.dto.DescendantNode
import com.pratik.learning.familyTree.data.local.dto.DualAncestorTree
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import com.pratik.learning.familyTree.data.local.dto.MemberRelationAR
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.presentation.UIState
import com.pratik.learning.familyTree.utils.GENDER_TYPE_MALE
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.RELATION_TYPE_CHILD
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_HUSBAND
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE
import com.pratik.learning.familyTree.utils.RelationFormState
import com.pratik.learning.familyTree.utils.SyncPrefs.setIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.logger
import com.pratik.learning.familyTree.utils.relationTextInHindi
import com.pratik.learning.familyTree.utils.validateMemberData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MemberDetailsViewModel @Inject constructor(
    private val familyTreeRepository: FamilyTreeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    var memberId = -1
    var secondMemberId = -1

    private val _uiState = MutableStateFlow<UIState>(UIState.IdealUIState)
    var uiState: StateFlow<UIState> = _uiState

    private val _familyTree = MutableStateFlow<DualAncestorTree?>(null)
    var familyTree: StateFlow<DualAncestorTree?> = _familyTree

    private val _descendantTree = MutableStateFlow<DescendantNode?>(null)
    var descendantTree: StateFlow<DescendantNode?> = _descendantTree

    private var currentMember: FamilyMember? = null
    private var comparedMember: FamilyMember? = null

    private val _member = MutableStateFlow(MemberFormState())
    var member: StateFlow<MemberFormState> = _member

    private val _relatives = MutableStateFlow(MemberRelationAR())
    var relatives: StateFlow<MemberRelationAR> = _relatives

    private val _relationList = MutableStateFlow(ArrayList<String>())
    var relationList: StateFlow<ArrayList<String>> = _relationList

    private val _error = MutableStateFlow("")
    var error: StateFlow<String> = _error


    private val _secondMember = MutableStateFlow(MemberFormState())
    var secondMember: StateFlow<MemberFormState> = _secondMember

    private val _secondMemberRelatives = MutableStateFlow(MemberRelationAR())
    var secondMemberRelatives: StateFlow<MemberRelationAR> = _secondMemberRelatives

    private val _commonRelatives: MutableStateFlow<Map<FamilyMember, Pair<String, String>>?> = MutableStateFlow(null)
    var commonRelatives: StateFlow<Map<FamilyMember, Pair<String, String>>?> = _commonRelatives

    private val _membersBetweenRelations: MutableStateFlow<Pair<String, String>> = MutableStateFlow(Pair("", ""))
    var membersBetweenRelations: StateFlow<Pair<String, String>> = _membersBetweenRelations



    fun fetchDetails() {
        logger("fetchDetails:: called")
        fetchMemberDetails()
        fetchMemberRelations()
    }

    fun fetchAncestry() {
        getFamilyHistory(memberId)
    }


    /**
     * resetting the related values for existing second member before going for compression
     * */
    fun resetSecondMemberDetails() {
        secondMemberId = -1
        _secondMemberRelatives.value = MemberRelationAR()
        _secondMember.value = MemberFormState()
        _commonRelatives.value = null
    }

    private fun getFamilyHistory(memberId: Int) {
        println("getFamilyHistory for member: $memberId")
        viewModelScope.launch(Dispatchers.IO) {
            _familyTree.value = familyTreeRepository.getFullAncestorTree(memberId)
            println("full tree ${_familyTree.value}")
            _descendantTree.value = familyTreeRepository.getFullDescendantTree(memberId)
            println("Complete descendant tree ${_descendantTree.value}")
        }
    }

    fun fetchMemberDetails(isFirstMember: Boolean = true) {
        val memberId = if (isFirstMember) this.memberId else secondMemberId
        logger("fetchMemberDetails:: isFirstMember = $isFirstMember  memberId = $memberId")
        viewModelScope.launch(Dispatchers.IO) {
            val mMember = familyTreeRepository.getMemberById(memberId)
            mMember?.let {
                logger("Member Details: $it")
                val mMemberFormState = MemberFormState(
                    fullName = it.fullName,
                    gotra = it.gotra,
                    dob = it.dob,
                    gender = it.gender,
                    isLiving = it.isLiving,
                    dod = it.dod,
                    city = it.city,
                    state = it.state,
                    mobile = it.mobile
                )
                if (isFirstMember) {
                    _member.value = mMemberFormState
                    currentMember = it
                    logger("pratik:: -2 = member ${_relatives.value.member}  || $it")
                } else {
                    _secondMember.value = mMemberFormState
                    comparedMember = it
                    logger("pratik:: -1 = member ${_secondMemberRelatives.value.member}  || $it")
                    fetchMemberRelations(isFirstMember = false)
                }
            }
        }
    }

    private fun fetchMemberRelations(isFirstMember: Boolean = true) {
        val memberId = if (isFirstMember) memberId else secondMemberId
        logger("fetchMemberRelations:: isFirstMember = $isFirstMember  memberId = $memberId")

        viewModelScope.launch(Dispatchers.IO) {
            if (isFirstMember) {
                _relatives.value = familyTreeRepository.getMemberRelatives(memberId = memberId)
            } else {
                _secondMemberRelatives.value =
                    familyTreeRepository.getMemberRelatives(memberId = memberId)
            }
            delay(1000)
            logger("fetchMemberRelations:: isFirstMember = $isFirstMember  memberId = $memberId _relatives.value = ${_relatives.value}")
            if (!isFirstMember) {
                _commonRelatives.value = getCommonRelatives()
                _membersBetweenRelations.value = familyTreeRepository.getMembersBetweenRelations(
                    _relatives.value,
                    _secondMemberRelatives.value
                )
                logger("fetchMemberRelations:: firstMember = ${_relatives.value.member} || secondMember = ${_secondMemberRelatives.value.member} ")
                logger("fetchMemberRelations:: getMembersBetweenRelations = $membersBetweenRelations  memberId = $memberId")
            }
        }
    }


    fun getCommonRelatives(): Map<FamilyMember, Pair<String, String>> {
        logger("getCommonRelatives")
        val firstMemberRelatives = getAllRelatedMember()
        val secondMemberRelatives = getAllRelatedMember(isFirstMember = false)

        val firstMemberIds = firstMemberRelatives.keys
        val secondMemberIds = secondMemberRelatives.keys
        val intersect = firstMemberIds.intersect(secondMemberIds)
        logger("getCommonRelatives intersect: $intersect")

        val commonMembers = mutableMapOf<FamilyMember, Pair<String, String>>()

        intersect.forEach {
            val firstMember = firstMemberRelatives[it]
            val secondMember = secondMemberRelatives[it]
            commonMembers[firstMember?.second ?: secondMember!!.second] = Pair(firstMember?.first ?:"", secondMember?.first ?:"")
        }
        logger("getCommonRelatives commonMembers: $commonMembers")
        return commonMembers

    }



    fun onMobileChanged(newValue: String) {
        _member.value = _member.value.copy(mobile = newValue.filter { it.isDigit() })
    }

    fun onFullNameChanged(newValue: String) {
        _member.value = _member.value.copy(fullName = newValue)
    }

    fun onGotraChanged(newValue: String) {
        _member.value = _member.value.copy(gotra = newValue)
    }

    fun onGenderChanged(newValue: String) {
        _member.value = _member.value.copy(gender = newValue)
    }

    fun onDOBChanged(newValue: String) {
        _member.value = _member.value.copy(dob = newValue)
    }

    fun onDODChanged(newValue: String) {
        _member.value = _member.value.copy(dod = newValue)
    }

    fun onIsLivingStatusChanges(newValue: Boolean) {
        _member.value = _member.value.copy(isLiving = newValue)
    }

    fun onCityChanged(newValue: String) {
        _member.value = _member.value.copy(city = newValue)
    }

    fun onStateChanged(newValue: String) {
        _member.value = _member.value.copy(state = newValue)
    }


    fun deleteMember(navController: NavController) {
        logger("deleteMember: ${member.value.fullName}")
        viewModelScope.launch(Dispatchers.IO) {
            familyTreeRepository.deleteMember(memberId)
            setIsDataUpdateRequired(context, true)
            withContext(Dispatchers.Main) {
                navController.popBackStack()
            }
        }
    }


    fun deleteAllRelations() {
        logger("deleteAllRelations for member: ${member.value.fullName}")
        viewModelScope.launch(Dispatchers.IO) {
            familyTreeRepository.deleteAllRelations(memberId)
            setIsDataUpdateRequired(context, true)
            fetchDetails()
        }
    }

    fun updateMember(navController: NavController) {
        logger("updateMember: ${member.value}")
        viewModelScope.launch(Dispatchers.IO) {
            val familyMember = FamilyMember(
                memberId = memberId,
                fullName = member.value.fullName,
                gotra = member.value.gotra,
                dob = member.value.dob,
                gender = member.value.gender,
                isLiving = member.value.isLiving,
                dod = member.value.dod,
                city = member.value.city,
                state = member.value.state,
                mobile = member.value.mobile,
                updatedAt = System.currentTimeMillis().toString()
            )
            val validateMemberData = validateMemberData(member.value)
            if (validateMemberData.isNotEmpty()) {
                _error.value = validateMemberData
                return@launch
            }

            if (currentMember?.gotra != member.value.gotra && currentMember?.gender == GENDER_TYPE_MALE) {
                // gotra of the male user has been changes so need to update the spouse and descendants gotra as well
                familyTreeRepository.updateMember(familyMember, true, relatives.value.spouse?.second?.memberId ?: -1)
            } else
                familyTreeRepository.updateMember(familyMember)
            withContext(Dispatchers.Main) {
                setIsDataUpdateRequired(context, true)
                navController.navigateUp()
            }
        }
    }

    fun onClearRelationList() {
        _relationList.value.clear()
    }


    fun createRelation(formState: RelationFormState, isCreate: Boolean = false) {
        logger("createRelation: $formState")
        if (formState.relatedToMemberId == -1) return
        viewModelScope.launch(Dispatchers.IO) {
            val newRelations = ArrayList<FamilyRelation>()
            val relationText = ArrayList<String>()
            _relationList.value = ArrayList()
            when (formState.relation) {
                RELATION_TYPE_CHILD -> {

                }

                RELATION_TYPE_FATHER, RELATION_TYPE_MOTHER -> {
                    // add parents relation
                    val isFather = formState.relation == RELATION_TYPE_FATHER

                    // member's -> Father/Mother -> related Member
                    relationText.add("${member.value.fullName} ${formState.relation.relationTextInHindi()} - ${formState.relatedToFullName}")
                    logger(
                        "createRelation ${member.value.fullName}'s ${formState.relation} ${formState.relatedToFullName}"
                    )
                    newRelations.add(
                        FamilyRelation(
                            relatesToMemberId = memberId,
                            relationType = formState.relation,
                            relatedMemberId = formState.relatedToMemberId
                        )
                    )

                    val spouse = familyTreeRepository.getSpouse(formState.relatedToMemberId)
                    if (spouse == null) {
                        updateError("${formState.relatedToFullName} विवाहित नहीं है या उनका जीवनसाथी अभी तक नहीं जोड़ा गया है|")
                        return@launch
                    }
                    //check is father's spouse exist, then add mother relations also or vice versa
                    spouse.let {
                        relationText.add("${member.value.fullName} ${if (isFather) RELATION_TYPE_MOTHER.relationTextInHindi() else RELATION_TYPE_FATHER.relationTextInHindi()} - ${it.fullName}")
                        logger(
                            "createRelation ${member.value.fullName}'s ${if (isFather) RELATION_TYPE_MOTHER else RELATION_TYPE_FATHER} ${it.fullName}"
                        )
                        // Member's spouse -> Father/Mother -> related Member
                        newRelations.add(
                            FamilyRelation(
                                relatesToMemberId = memberId,
                                relationType = if (isFather) RELATION_TYPE_MOTHER else RELATION_TYPE_FATHER,
                                relatedMemberId = it.memberId
                            )
                        )
                    }
                }

                RELATION_TYPE_WIFE, RELATION_TYPE_HUSBAND -> {
                    // add spouse relation
                    val isHusband = formState.relation == RELATION_TYPE_HUSBAND

                    // Member's -> Husband/Wife -> related Member
                    relationText.add("${member.value.fullName} ${formState.relation.relationTextInHindi()} - ${formState.relatedToFullName}")
                    logger(
                        "createRelation ${member.value.fullName}'s ${formState.relation} ${formState.relatedToFullName}"
                    )
                    newRelations.add(
                        FamilyRelation(
                            relatesToMemberId = memberId,
                            relationType = formState.relation,
                            relatedMemberId = formState.relatedToMemberId
                        )
                    )

                    // vice versa relations if adding husband of member then add wife to member also
                    // ex if adding Shivani's -> Husband -> Pratik then auto add Pratik's -> Wife -> Shivani

                    // Related Member's -> Husband/Wife -> Member
                    relationText.add("${formState.relatedToFullName} ${if (isHusband) "Wife".relationTextInHindi() else "Husband".relationTextInHindi()} - ${member.value.fullName}")
                    logger(
                        "createRelation ${formState.relatedToFullName}'s ${if (isHusband) "Wife" else "Husband"} ${member.value.fullName}"
                    )
                    newRelations.add(
                        FamilyRelation(
                            relatesToMemberId = formState.relatedToMemberId,
                            relationType = if (isHusband) RELATION_TYPE_WIFE else RELATION_TYPE_HUSBAND,
                            relatedMemberId = memberId
                        )
                    )
                }
            }
            _relationList.value = relationText
            if (isCreate) {
                val filter = newRelations.filter { it.relatedMemberId != it.relatesToMemberId }
                filter.map { it.isNewEntry = true }
                familyTreeRepository.insertAllRelation(filter)
                setIsDataUpdateRequired(context, true)
            }
        }
    }


    fun checkRelationValidity(
        relation: String,
        selectedPerson: MemberWithFather? = null
    ) {
        var error = ""
        if (relation.isEmpty()) {
            error = "Please select a relation".inHindi()
            _relationList.value = ArrayList()
        }
        if (selectedPerson == null) {
            error = "Please select a related person".inHindi()
            _relationList.value = ArrayList()
        }
        if (memberId == selectedPerson?.memberId) {
            error = "Person cannot be related to themselves".inHindi()
            _relationList.value = ArrayList()
        }
        updateError(error)
    }

    private fun updateError(error: String) {
        _error.value = error
    }

    fun getAllRelatedMemberIds(): List<Int> {
        val ids = mutableSetOf<Int>() // use a set to avoid duplicates
        val mRelations = relatives.value
        // Add parents
        mRelations.parents.forEach { (_, member) -> ids.add(member.memberId) }

        // Add spouse
        mRelations.spouse?.let { (_, member) -> ids.add(member.memberId) }

        // Add in-laws
        mRelations.inLaws.forEach { (_, member) -> ids.add(member.memberId) }

        // Add siblings
        mRelations.siblings.forEach { (_, member) -> ids.add(member.memberId) }

        // Add children
        mRelations.children.forEach { (_, member) ->
            ids.add(member.child.memberId)
            member.spouseId?.let {  ids.add(it) }
        }

        // Add grandchildren
        mRelations.grandchildren.forEach { (_, member) -> ids.add(member.memberId) }

        // Add grandparents (father side)
        mRelations.grandParentsFather.forEach { (_, member) -> ids.add(member.memberId) }

        // Add grandparents (mother side)
        mRelations.grandParentsMother.forEach { (_, member) -> ids.add(member.memberId) }
        ids.add(memberId)

        return ids.toList()
    }


    fun getAllRelatedMember(isFirstMember: Boolean = true): Map<Int, Pair<String, FamilyMember>> {
        val members = mutableMapOf<Int, Pair<String, FamilyMember>>()
        val mRelations = if (isFirstMember) relatives.value else secondMemberRelatives.value
        logger("getAllRelatedMember: $mRelations")

        // Add parents
        mRelations.parents.forEach {
            members[it.second.memberId] = Pair(it.first, it.second)
        }
        // Add spouse
        mRelations.spouse?.let {
            members[it.second.memberId] = Pair(it.first, it.second)
        }
        // Add in-laws
        mRelations.inLaws.forEach {
            members[it.second.memberId] = Pair(it.first, it.second)
        }
        // Add siblings
        mRelations.siblings.forEach {
            members[it.second.memberId] = Pair(it.first, it.second)
        }
        // Add children
        mRelations.children.forEach {
            members[it.second.child.memberId] = Pair(it.first, it.second.child)
        }

        // Add grandchildren
        mRelations.grandchildren.forEach {
            members[it.second.memberId] = Pair(it.first, it.second)
        }

        // Add grandparents (father side)
        mRelations.grandParentsFather.forEach {
            members[it.second.memberId] = Pair(it.first, it.second)
        }

        // Add grandparents (mother side)
        mRelations.grandParentsMother.forEach {
            members[it.second.memberId] = Pair(it.first, it.second)
        }

        return members
    }

    fun dismissConfirmationPopup() {
        if (_uiState.value == UIState.IdealUIState)
            return
        _uiState.value = UIState.IdealUIState
    }

    fun showDeleteMemberPopup() {
        val popupUIState = UIState.ConfirmationUIState(
            title = "Delete Member",
            message = "Are you sure you want to delete this member?"
        )
        if (_uiState.value is UIState.IdealUIState)
            _uiState.value = popupUIState
    }

    fun showDeleteRelationShipClearPopup() {
        val popupUIState = UIState.ConfirmationUIState(
            title = "Delete all relations",
            message = "Are you sure you want to delete all the relations for member?"
        )
        if (_uiState.value is UIState.IdealUIState)
            _uiState.value = popupUIState
    }


    suspend fun captureComposableAsBitmap(
        context: Context,
        content: @Composable () -> Unit
    ): Bitmap = withContext(Dispatchers.Main) {
        val composeView = ComposeView(context)
        composeView.setContent {
            Box(Modifier.background(Color.White)) { content() }
        }

        // Measure & layout
        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
        )
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        composeView.drawToBitmap()
    }

}

