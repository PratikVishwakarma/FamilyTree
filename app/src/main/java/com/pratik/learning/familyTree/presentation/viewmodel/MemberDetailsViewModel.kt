package com.pratik.learning.familyTree.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.pratik.learning.familyTree.data.local.dto.DescendantNode
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.RelationFormState
import com.pratik.learning.familyTree.data.local.dto.DualAncestorTree
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import com.pratik.learning.familyTree.data.local.dto.FullFamilyTree
import com.pratik.learning.familyTree.data.local.dto.MemberRelations
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.presentation.component.ConfirmationPopup
import com.pratik.learning.familyTree.utils.GENDER_TYPE_FEMALE
import com.pratik.learning.familyTree.utils.GENDER_TYPE_MALE
import com.pratik.learning.familyTree.utils.RELATION_TYPE_BROTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_CHILD
import com.pratik.learning.familyTree.utils.RELATION_TYPE_DAUGHTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER_IN_LAW
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDFATHER_F
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDFATHER_M
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDMOTHER_F
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDMOTHER_M
import com.pratik.learning.familyTree.utils.RELATION_TYPE_HUSBAND
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER_IN_LAW
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SON
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE
import com.pratik.learning.familyTree.utils.SyncPrefs.setIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.logger
import com.pratik.learning.familyTree.utils.relationTextInHindi
import com.pratik.learning.familyTree.utils.validateMemberData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MemberDetailsViewModel @Inject constructor(
    private val familyTreeRepository: FamilyTreeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    var memberId = -1

    private val _uiState = MutableStateFlow<UIState>(UIState.IdealUIState)
    var uiState: StateFlow<UIState> = _uiState

    private val _familyTree = MutableStateFlow<DualAncestorTree?>(null)
    var familyTree: StateFlow<DualAncestorTree?> = _familyTree

    private val _descendantTree = MutableStateFlow<DescendantNode?>(null)
    var descendantTree: StateFlow<DescendantNode?> = _descendantTree

    private var currentMember: FamilyMember? = null

    private val _member = MutableStateFlow(MemberFormState())
    var member: StateFlow<MemberFormState> = _member

    private val _relations = MutableStateFlow(MemberRelations())
    var relations: StateFlow<MemberRelations> = _relations

    private val _relationList = MutableStateFlow(ArrayList<String>())
    var relationList: StateFlow<ArrayList<String>> = _relationList

    private val _error = MutableStateFlow("")
    var error: StateFlow<String> = _error

    fun fetchDetails() {
        fetchMemberDetails()
        fetchMemberRelations()
    }

    fun fetchAncestry() {
        getFamilyHistory(memberId)
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

    private fun fetchMemberDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            currentMember = familyTreeRepository.getMemberById(memberId)
            if (currentMember != null) {
               logger("Member Details: $currentMember")
                _member.value = MemberFormState(
                    fullName = currentMember!!.fullName,
                    gotra = currentMember!!.gotra,
                    dob = currentMember!!.dob,
                    gender = currentMember!!.gender,
                    isLiving = currentMember!!.isLiving,
                    dod = currentMember!!.dod,
                    city = currentMember!!.city,
                    state = currentMember!!.state,
                    mobile = currentMember!!.mobile
                )
            }
        }
    }

    private fun fetchMemberRelations() {
        _relations.value = MemberRelations()
        viewModelScope.launch(Dispatchers.IO) {
            familyTreeRepository.getRelationsForMember(memberId).onEach { relations ->
                getRelations(relations)
            }.launchIn(viewModelScope)
        }
    }


    /**
     * helper function to label the actual relations of the member
     * */
    private suspend fun getRelations(relations: List<FamilyRelation>) {
        relations.forEach { relation ->
            val memberDetails = familyTreeRepository.getMemberById(relation.relatedMemberId)
           logger("getRelations for relation: $relation")
            memberDetails?.let {
                when (relation.relationType) {
                    RELATION_TYPE_FATHER -> {
                        _relations.update { current ->
                            current.copy(
                                parents = (current.parents + Pair(
                                    RELATION_TYPE_FATHER,
                                    it
                                )).distinct()
                            )
                        }
                        fetchGrandParents(it, isParental = true)
                    }

                    RELATION_TYPE_MOTHER -> {
                        _relations.update { current ->
                            current.copy(
                                parents = (current.parents + Pair(
                                    RELATION_TYPE_MOTHER,
                                    it
                                )).distinct()
                            )
                        }
                        fetchGrandParents(it, isParental = false)
                    }

                    RELATION_TYPE_WIFE -> {
                        _relations.update { current ->
                            current.copy(
                                spouse = Pair(
                                    RELATION_TYPE_WIFE,
                                    it
                                )
                            )
                        }
                    }

                    RELATION_TYPE_HUSBAND -> {
                        _relations.update { current ->
                            current.copy(
                                spouse = Pair(
                                    RELATION_TYPE_HUSBAND,
                                    it
                                )
                            )
                        }
                    }
                }
            }
        }
        _relations.value.spouse?.let { spouse ->
            fetchInLawsDetails(spouse.second)
            fetchChildren()
        }
        fetchSiblings()
    }

    /**
     * this will fetch the member's In-Laws details
     * */
    private suspend fun fetchInLawsDetails(spouse: FamilyMember) {
       logger("fetchInLawsDetails for spouse: $spouse")
        val parentsWithMemberId = familyTreeRepository.getParentsWithMemberId(spouse.memberId)
       logger("parentsWithMemberId: $parentsWithMemberId")
        parentsWithMemberId.forEach { parent ->
            when (parent.first) {
                RELATION_TYPE_FATHER -> _relations.update { current ->
                    current.copy(
                        inLaws = (current.inLaws + Pair(
                            RELATION_TYPE_FATHER_IN_LAW,
                            parent.second
                        )).distinct()
                    )
                }

                RELATION_TYPE_MOTHER -> _relations.update { current ->
                    current.copy(
                        inLaws = (current.inLaws + Pair(
                            RELATION_TYPE_MOTHER_IN_LAW,
                            parent.second
                        )).distinct()
                    )
                }
            }
        }
    }

    private suspend fun fetchGrandParents(member: FamilyMember, isParental: Boolean = true) {
        logger("fetchGrandParents for isParental: $isParental  member: $member")
        val parentsWithMemberId = familyTreeRepository.getParentsWithMemberId(member.memberId)
       logger("fetchGrandParents: $parentsWithMemberId")
        parentsWithMemberId.forEach { parent ->
            when (parent.first) {
                RELATION_TYPE_FATHER -> _relations.update { current ->
                    if (isParental) {
                        current.copy(
                            grandParentsFather = (current.grandParentsFather + Pair(
                                RELATION_TYPE_GRANDFATHER_F,
                                parent.second
                            )).distinct()
                        )
                    } else {
                        current.copy(
                            grandParentsMother = (current.grandParentsMother + Pair(
                                RELATION_TYPE_GRANDFATHER_M,
                                parent.second
                            )).distinct()
                        )
                    }
                }

                RELATION_TYPE_MOTHER -> _relations.update { current ->
                    if (isParental) {
                        current.copy(
                            grandParentsFather = (current.grandParentsFather + Pair(
                                RELATION_TYPE_GRANDMOTHER_F,
                                parent.second
                            )).distinct()
                        )
                    } else {
                        current.copy(
                            grandParentsMother = (current.grandParentsMother + Pair(
                                RELATION_TYPE_GRANDMOTHER_M,
                                parent.second
                            )).distinct()
                        )
                    }
                }
            }
        }
    }


    private suspend fun fetchSiblings() {
        logger(
            "fetchSiblings for member: ${member.value}"
        )
        relations.value.parents.forEach {
           logger("FetchSiblings: for member: $it")
            val siblings = familyTreeRepository.getChildren(it.second.memberId)
           logger("siblings: $siblings")
            siblings.forEach { sibling ->
                if (sibling.memberId != memberId) {
                   logger("sibling: $sibling")
                    when (sibling.gender) {
                        GENDER_TYPE_MALE -> _relations.update { current ->
                            current.copy(
                                siblings = (current.siblings + Pair(
                                    RELATION_TYPE_BROTHER,
                                    sibling
                                )).distinct()
                            )
                        }

                        GENDER_TYPE_FEMALE -> _relations.update { current ->
                            current.copy(
                                siblings = (current.siblings + Pair(
                                    RELATION_TYPE_SISTER,
                                    sibling
                                )).distinct()
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchChildren() {
        logger(
            "fetchChildren for member: ${member.value}"
        )
       logger("fetchChildren: for member: $memberId")
        val children = familyTreeRepository.getChildren(memberId)
       logger("total children: $children")
        children.forEach { child ->
            if (child.memberId != memberId) {
               logger("child: $child")
                when (child.gender) {
                    GENDER_TYPE_MALE -> _relations.update { current ->
                        current.copy(
                            children = (current.children + Pair(
                                RELATION_TYPE_SON,
                                child
                            )).distinct()
                        )
                    }

                    GENDER_TYPE_FEMALE -> _relations.update { current ->
                        current.copy(
                            children = (current.children + Pair(
                                RELATION_TYPE_DAUGHTER,
                                child
                            )).distinct()
                        )
                    }
                }
            }
        }
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
                    relationText.add("${member.value.fullName} ${if (isHusband) "Wife".relationTextInHindi() else "Husband".relationTextInHindi()} - ${member.value.fullName}")
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
        val mRelations = relations.value
        // Add parents
        mRelations.parents.forEach { (_, member) -> ids.add(member.memberId) }

        // Add spouse
        mRelations.spouse?.let { (_, member) -> ids.add(member.memberId) }

        // Add in-laws
        mRelations.inLaws.forEach { (_, member) -> ids.add(member.memberId) }

        // Add siblings
        mRelations.siblings.forEach { (_, member) -> ids.add(member.memberId) }

        // Add children
        mRelations.children.forEach { (_, member) -> ids.add(member.memberId) }

        // Add grandchildren
        mRelations.grandchildren.forEach { (_, member) -> ids.add(member.memberId) }

        // Add grandparents (father side)
        mRelations.grandParentsFather.forEach { (_, member) -> ids.add(member.memberId) }

        // Add grandparents (mother side)
        mRelations.grandParentsMother.forEach { (_, member) -> ids.add(member.memberId) }
        ids.add(memberId)

        return ids.toList()
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
}


sealed class UIState {
    data object IdealUIState : UIState()
    data class ConfirmationUIState(val title: String, val message: String) : UIState()
}