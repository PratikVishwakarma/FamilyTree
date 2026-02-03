package com.pratik.learning.familyTree.data.local.model

import androidx.annotation.Keep
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.MemberRelationAR
import com.pratik.learning.familyTree.utils.GENDER_TYPE_MALE

@Keep
data class RelationContext(
    val m1: FamilyMember,
    val m2: FamilyMember,
    val m1Rel: MemberRelationAR,
    val m2Rel: MemberRelationAR
) {
    val m1Id = m1.memberId
    val m2Id = m2.memberId
    val isM1Male = m1.gender == GENDER_TYPE_MALE
    val isM2Male = m2.gender == GENDER_TYPE_MALE
}