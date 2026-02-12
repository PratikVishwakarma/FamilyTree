package com.pratik.learning.familyTree.utils

import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.MemberRelationAR
import com.pratik.learning.familyTree.data.local.dto.MemberWithSpouseDto


fun MemberRelationAR.toRelationMap(): Map<Int, String> {

    val relationMap = mutableMapOf<Int, String>()

    fun addRelation(id: Int?, relation: String) {
        if (id == null) return
        relationMap[id] = relation
    }

    // Handle List<Pair<String, FamilyMember>>
    fun handleFamilyList(list: List<Pair<String, FamilyMember>>) {
        list.forEach { (relation, member) ->
            addRelation(member.memberId, relation)
        }
    }

    // Handle List<Pair<String, MemberWithSpouseDto>>
    fun handleMemberWithSpouseList(list: List<Pair<String, MemberWithSpouseDto>>) {
        list.forEach { (relation, dto) ->
            addRelation(dto.innerMember.memberId, relation)
            addRelation(dto.spouseId, relation.getSpouseRelation().relationTextInHindi())
        }
    }

    // Direct relations
    handleFamilyList(parents)
    handleFamilyList(inLaws)
    handleFamilyList(grandchildren)
    handleFamilyList(grandParentsFather)
    handleFamilyList(grandParentsMother)

    spouse?.let { (relation, member) ->
        addRelation(member.memberId, relation)
    }

    // DTO relations
    handleMemberWithSpouseList(siblings)
    handleMemberWithSpouseList(spouseSiblings)
    handleMemberWithSpouseList(children)
    handleMemberWithSpouseList(uncleAuntFatherSide)
    handleMemberWithSpouseList(uncleAuntMotherSide)

    return relationMap
}


fun String.getRelationShipInHindiWithIcon(): String {
    return getIcon(this) + " " + this.inHindi()
}

