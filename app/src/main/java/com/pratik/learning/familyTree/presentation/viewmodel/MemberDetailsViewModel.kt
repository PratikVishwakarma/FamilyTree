package com.pratik.learning.familyTree.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.RelationFormState
import com.pratik.learning.familyTree.data.local.dto.DualAncestorTree
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import com.pratik.learning.familyTree.data.local.dto.MemberRelations
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.utils.GENDER_TYPE_FEMALE
import com.pratik.learning.familyTree.utils.GENDER_TYPE_MALE
import com.pratik.learning.familyTree.utils.RELATION_TYPE_BROTHER
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
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SIBLING
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SON
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val familyTreeRepository: FamilyTreeRepository
) : ViewModel() {
    var memberId = -1

    private val _familyTree = MutableStateFlow<DualAncestorTree?>(null)
    var familyTree: StateFlow<DualAncestorTree?> = _familyTree

    private var currentMember: FamilyMember? = null

    private val _member = MutableStateFlow(MemberFormState())
    var member: StateFlow<MemberFormState> = _member

    private val _relations = MutableStateFlow(MemberRelations())
    var relations: StateFlow<MemberRelations> = _relations

    private val _relationList = MutableStateFlow(ArrayList<String>())
    var relationList: StateFlow<ArrayList<String>> = _relationList


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
        }
    }

    private fun fetchMemberDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            currentMember = familyTreeRepository.getMemberById(memberId)
            if (currentMember != null) {
                Log.d("MemberDetailsViewModel", "Member Details: $currentMember")
                _member.value = MemberFormState(
                    fullName = currentMember!!.fullName,
                    dob = currentMember!!.dob,
                    gender = currentMember!!.gender,
                    isLiving = currentMember!!.isLiving,
                    dod = currentMember!!.dod ?: "",
                    city = currentMember!!.city ?: "",
                    mobile = currentMember!!.mobile ?: ""
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
            memberDetails?.let {
                when (relation.relationType) {
                    RELATION_TYPE_FATHER -> {
                        _relations.update { current ->
                            current.copy(
                                parents = current.parents + Pair(
                                    RELATION_TYPE_FATHER,
                                    it
                                )
                            )
                        }
                        fetchGrandParents(it, isParental = true)
                    }

                    RELATION_TYPE_MOTHER -> {
                        _relations.update { current ->
                            current.copy(
                                parents = current.parents + Pair(
                                    RELATION_TYPE_MOTHER,
                                    it
                                )
                            )
                        }
                        fetchGrandParents(it, isParental = false)
                    }

                    RELATION_TYPE_SIBLING -> {
                        if (it.memberId == memberId) return
                        val broOrSis =
                            if (it.gender == "M") RELATION_TYPE_BROTHER else RELATION_TYPE_SISTER
                        _relations.update { current ->
                            current.copy(
                                siblings = current.siblings + Pair(
                                    broOrSis,
                                    it
                                )
                            )
                        }
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
                        fetchInLawsDetails(it)
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

                    RELATION_TYPE_SON -> {
                        _relations.update { current ->
                            current.copy(
                                children = current.children + Pair(
                                    RELATION_TYPE_SON,
                                    it
                                )
                            )
                        }
                    }

                    RELATION_TYPE_DAUGHTER -> {
                        _relations.update { current ->
                            current.copy(
                                children = current.children + Pair(
                                    RELATION_TYPE_DAUGHTER,
                                    it
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * this will fetch the member's In-Laws details
     * */
    private suspend fun fetchInLawsDetails(spouse: FamilyMember) {
        Log.d("MemberDetailsViewModel", "fetchInLawsDetails for spouse: $spouse")
        val parentsWithMemberId = familyTreeRepository.getParentsWithMemberId(spouse.memberId)
        Log.d("MemberDetailsViewModel", "parentsWithMemberId: $parentsWithMemberId")
        parentsWithMemberId.forEach { parent ->
            when (parent.first) {
                RELATION_TYPE_FATHER -> _relations.update { current ->
                    current.copy(
                        inLaws = current.inLaws + Pair(
                            RELATION_TYPE_FATHER_IN_LAW,
                            parent.second
                        )
                    )
                }

                RELATION_TYPE_MOTHER -> _relations.update { current ->
                    current.copy(
                        inLaws = current.inLaws + Pair(
                            RELATION_TYPE_MOTHER_IN_LAW,
                            parent.second
                        )
                    )
                }
            }
        }
    }

    private suspend fun fetchGrandParents(member: FamilyMember, isParental: Boolean = true) {
        Log.d(
            "MemberDetailsViewModel",
            "fetchGrandParents for isParental: $isParental  member: $member"
        )
        val parentsWithMemberId = familyTreeRepository.getParentsWithMemberId(member.memberId)
        Log.d("MemberDetailsViewModel", "fetchGrandParents: $parentsWithMemberId")
        parentsWithMemberId.forEach { parent ->
            when (parent.first) {
                RELATION_TYPE_FATHER -> _relations.update { current ->
                    if (isParental) {
                        current.copy(
                            grandParentsFather = current.grandParentsFather + Pair(
                                RELATION_TYPE_GRANDFATHER_F,
                                parent.second
                            )
                        )
                    } else {
                        current.copy(
                            grandParentsFather = current.grandParentsMother + Pair(
                                RELATION_TYPE_GRANDFATHER_M,
                                parent.second
                            )
                        )
                    }
                }

                RELATION_TYPE_MOTHER -> _relations.update { current ->
                    if (isParental) {
                        current.copy(
                            grandParentsFather = current.grandParentsFather + Pair(
                                RELATION_TYPE_GRANDMOTHER_F,
                                parent.second
                            )
                        )
                    } else {
                        current.copy(
                            grandParentsFather = current.grandParentsMother + Pair(
                                RELATION_TYPE_GRANDMOTHER_M,
                                parent.second
                            )
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


    fun updateMember(navController: NavController) {
        viewModelScope.launch(Dispatchers.IO) {
            val familyMember = FamilyMember(
                memberId = memberId,
                fullName = member.value.fullName,
                dob = member.value.dob,
                gender = member.value.gender,
                isLiving = member.value.isLiving,
                dod = member.value.dod,
                city = member.value.city,
                mobile = member.value.mobile
            )
            familyTreeRepository.updateMember(familyMember)
            withContext(Dispatchers.Main) {
                navController.navigateUp()
            }
        }
    }

    fun onClearRelationList() {
        _relationList.value.clear()
    }


    fun createRelation(formState: RelationFormState, isCreate: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val newRelations = ArrayList<FamilyRelation>()
            val relationText = ArrayList<String>()
            _relationList.value = ArrayList()
            when (formState.relation) {
                RELATION_TYPE_FATHER, RELATION_TYPE_MOTHER -> {
                    // add parents relation
                    val isFather = formState.relation == RELATION_TYPE_FATHER

                    // member's -> Father/Mother -> related Member
                    relationText.add("${member.value.fullName}'s ${formState.relation} - ${formState.relatedToFullName}")
                    Log.d(
                        "MemberDetailsViewModel",
                        "createRelation ${member.value.fullName}'s ${formState.relation} ${formState.relatedToFullName}"
                    )
                    newRelations.add(
                        FamilyRelation(
                            relatesToMemberId = memberId,
                            relationType = formState.relation,
                            relatedMemberId = formState.relatedToMemberId
                        )
                    )

                    // related Member's -> Son/Daughter -> Member
                    val childRelation = if (member.value.gender == GENDER_TYPE_FEMALE) RELATION_TYPE_DAUGHTER else RELATION_TYPE_SON
                    relationText.add("${formState.relatedToFullName}'s $childRelation - ${member.value.fullName}")
                    Log.d(
                        "MemberDetailsViewModel",
                        "createRelation ${formState.relatedToFullName}'s $childRelation - ${member.value.fullName}"
                    )
                    newRelations.add(
                        FamilyRelation(
                            relatesToMemberId = formState.relatedToMemberId,
                            relationType = childRelation,
                            relatedMemberId = memberId
                        )
                    )

                    val spouse = familyTreeRepository.getSpouse(formState.relatedToMemberId)
                    //check is father's spouse exist, then add mother relations also or vice versa
                    spouse?.let {
                        relationText.add("${member.value.fullName}'s ${if (isFather) RELATION_TYPE_MOTHER else RELATION_TYPE_FATHER} - ${it.fullName}")
                        Log.d(
                            "MemberDetailsViewModel",
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

                        // related Member's -> Son/Daughter -> Member' spouse
                        relationText.add("${it.fullName}'s $childRelation - ${member.value.fullName}")
                        Log.d(
                            "MemberDetailsViewModel",
                            "createRelation ${it.fullName}'s $childRelation - ${member.value.fullName}"
                        )
                        newRelations.add(
                            FamilyRelation(
                                relatesToMemberId = it.memberId,
                                relationType = childRelation,
                                relatedMemberId = memberId
                            )
                        )
                    }


                    // check is father's children exist, then add all children relations as sibling
                    val children = familyTreeRepository.getChildren(formState.relatedToMemberId) + relations.value.children.map { it.second }
                    if (children.isNotEmpty()) {
                        children.forEach { sibling ->
                            // member's -> Sibling -> related Member
                            relationText.add("${member.value.fullName}'s Sibling - ${sibling.fullName}")
                            Log.d(
                                "MemberDetailsViewModel",
                                "createRelation ${member.value.fullName}'s sibling ${sibling.fullName}"
                            )
                            newRelations.add(
                                FamilyRelation(
                                    relatesToMemberId = memberId,
                                    relationType = RELATION_TYPE_SIBLING,
                                    relatedMemberId = sibling.memberId
                                )
                            )
                            // member's -> Sibling -> related Member
                            relationText.add("${sibling.fullName}'s Sibling - ${member.value.fullName}")
                            Log.d(
                                "MemberDetailsViewModel",
                                "createRelation ${sibling.fullName}'s Sibling - ${member.value.fullName}"
                            )
                            newRelations.add(
                                FamilyRelation(
                                    relatesToMemberId = sibling.memberId,
                                    relationType = RELATION_TYPE_SIBLING,
                                    relatedMemberId = memberId
                                )
                            )
                        }
                    } else {
                        Log.d(
                            "MemberDetailsViewModel",
                            "${member.value.fullName}'s have no siblings to add"
                        )
                    }
                }

                RELATION_TYPE_WIFE, RELATION_TYPE_HUSBAND -> {
                    // add spouse relation
                    val isHusband = formState.relation == RELATION_TYPE_HUSBAND

                    // Member's -> Husband/Wife -> related Member
                    relationText.add("${formState.relation} - ${formState.relatedToFullName}")
                    Log.d(
                        "MemberDetailsViewModel",
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
                    relationText.add("${formState.relatedToFullName}'s ${if (isHusband) "Wife" else "Husband"} - ${member.value.fullName}")
                    Log.d(
                        "MemberDetailsViewModel",
                        "createRelation ${formState.relatedToFullName}'s ${if (isHusband) "Wife" else "Husband"} ${member.value.fullName}"
                    )
                    newRelations.add(
                        FamilyRelation(
                            relatesToMemberId = formState.relatedToMemberId,
                            relationType = if (isHusband) RELATION_TYPE_WIFE else RELATION_TYPE_HUSBAND,
                            relatedMemberId = memberId
                        )
                    )


                    //don't need this now until spouse is not added, then can't add children
                      // check if any member have children then add all children to other if not already
//
//                    // check is father's children exist, then add all children relations as sibling
//                    val children = familyTreeRepository.getChildren(formState.relatedToMemberId) + relations.value.children.map { it.second }
//                    if (children.isNotEmpty()) {
//                        children.forEach { child ->
//                            val relationWithChild =
//                                if (child.gender == "M") RELATION_TYPE_SON else RELATION_TYPE_DAUGHTER
//                            relationText.add("$relationWithChild - ${child.fullName}")
//                            Log.d(
//                                "MemberDetailsViewModel",
//                                "createRelation ${member.value.fullName}'s $relationWithChild ${child.fullName}"
//                            )
//                            newRelations.add(
//                                FamilyRelation(
//                                    relatesToMemberId = memberId,
//                                    relationType = relationWithChild,
//                                    relatedMemberId = child.memberId
//                                )
//                            )
//                        }
//                    } else {
//                        Log.d(
//                            "MemberDetailsViewModel",
//                            "${member.value.fullName}'s have no children to add"
//                        )
//                    }
                }

                RELATION_TYPE_DAUGHTER, RELATION_TYPE_SON -> {
                    // member's -> Son/Daughter -> related Member
                    relationText.add("${member.value.fullName}'s ${formState.relation} - ${formState.relatedToFullName}")
                    Log.d(
                        "MemberDetailsViewModel",
                        "createRelation ${member.value.fullName}'s ${formState.relation} - ${formState.relatedToFullName}"
                    )
                    newRelations.add(
                        FamilyRelation(
                            relatesToMemberId = memberId,
                            relationType = formState.relation,
                            relatedMemberId = formState.relatedToMemberId
                        )
                    )

                    // related Member's -> Father/Mother -> Member
                    val mfRelation = if (member.value.gender == "M") RELATION_TYPE_FATHER else RELATION_TYPE_MOTHER
                    relationText.add("${formState.relatedToFullName}'s $mfRelation - ${member.value.fullName}")
                    Log.d(
                        "MemberDetailsViewModel",
                        "${formState.relatedToFullName}'s $mfRelation - ${member.value.fullName}"
                    )
                    newRelations.add(
                        FamilyRelation(
                            relatesToMemberId = formState.relatedToMemberId,
                            relationType = mfRelation,
                            relatedMemberId = memberId
                        )
                    )

                    // now add other parent relations
                    // ex Pratik's -> Son -> Shritik then also add Pratik's spouse -> Son -> Shritik

                    if (relations.value.spouse != null) {
                        val spouse = relations.value.spouse!!.second
                        val mfRelationInner = if (spouse.gender == "M") RELATION_TYPE_FATHER else RELATION_TYPE_MOTHER
                        relationText.add("${formState.relatedToFullName}'s $mfRelationInner - ${spouse.fullName}")
                        Log.d(
                            "MemberDetailsViewModel",
                            "createRelation ${formState.relatedToFullName}'s $mfRelationInner - ${spouse.fullName}"
                        )
                        // member's spouse -> Father/Mother -> related Member
                        newRelations.add(
                            FamilyRelation(
                                relatesToMemberId = spouse.memberId,
                                relationType = mfRelationInner,
                                relatedMemberId = formState.relatedToMemberId
                            )
                        )

                        // member's spouse -> Son/Daughter -> Member
                        relationText.add("${spouse.fullName}'s ${formState.relation} - ${formState.relatedToFullName}")
                        newRelations.add(
                            FamilyRelation(
                                relatesToMemberId = spouse.memberId,
                                relationType = mfRelation,
                                relatedMemberId = formState.relatedToMemberId
                            )
                        )
                    }

                    // now add other children relations
                    // ex Pratik's -> Son -> Shritik and Pratik's already have a daughter Shritika then also add Shritik's -> Sibling -> Shritika

                    if (relations.value.children.isNotEmpty()) {
                        val children = relations.value.children.distinct()
                        children.forEach { child ->
                            val sibling = child.second
                            val relationWithChild =
                                if (sibling.gender == "M") RELATION_TYPE_BROTHER else RELATION_TYPE_SISTER

                            // member's children -> Sibling -> related member
                            relationText.add("${formState.relatedToFullName}'s $relationWithChild ${sibling.fullName}")
                            Log.d(
                                "MemberDetailsViewModel",
                                "createRelation ${formState.relatedToFullName}'s $relationWithChild ${sibling.fullName}"
                            )
                            newRelations.add(
                                FamilyRelation(
                                    relatesToMemberId = formState.relatedToMemberId,
                                    relationType = RELATION_TYPE_SIBLING,
                                    relatedMemberId = sibling.memberId
                                )
                            )

                            // related member -> Sibling ->  member's children
                            relationText.add("${sibling.fullName}'s $relationWithChild ${formState.relatedToFullName}")
                            Log.d(
                                "MemberDetailsViewModel",
                                "createRelation ${sibling.fullName}'s $relationWithChild ${formState.relatedToFullName}"
                            )
                            newRelations.add(
                                FamilyRelation(
                                    relatesToMemberId = sibling.memberId,
                                    relationType = RELATION_TYPE_SIBLING,
                                    relatedMemberId = formState.relatedToMemberId
                                )
                            )
                        }
                    } else {
                        Log.d(
                            "MemberDetailsViewModel",
                            "${member.value.fullName}'s have no sibling to add"
                        )
                    }
                }
            }
            _relationList.value = relationText
            if (isCreate) {
                val filter = newRelations.filter { it.relatedMemberId != it.relatesToMemberId }
                familyTreeRepository.insertAllRelation(filter)
            }
        }
    }


    fun checkRelationValidity(
        relation: String,
        selectedPerson: Pair<Int, String>? = null
    ): String {

        if (relation.isEmpty()) {
            return "Please select a relation"
        }
        if (relation == RELATION_TYPE_SON || relation == RELATION_TYPE_DAUGHTER) {
            if (relations.value.spouse == null) {
                return "Please add wife first before adding children"
            }
        }
        if (selectedPerson == null) {
            return "Please select a related person"
        }
        if (memberId == selectedPerson.first) {
            return "Person cannot be related to themselves"
        }

        return ""
    }
}