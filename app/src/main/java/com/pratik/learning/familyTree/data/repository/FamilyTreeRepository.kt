package com.pratik.learning.familyTree.data.repository

import androidx.paging.PagingData
import com.pratik.learning.familyTree.data.local.dto.ChildWithSpouseDto
import com.pratik.learning.familyTree.data.local.dto.DescendantNode
import com.pratik.learning.familyTree.data.local.dto.DualAncestorTree
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import com.pratik.learning.familyTree.data.local.dto.FullFamilyTree
import com.pratik.learning.familyTree.data.local.dto.MemberRelationAR
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.data.local.dto.TimelineEvent
import com.pratik.learning.familyTree.data.local.model.MemberFilter
import kotlinx.coroutines.flow.Flow

interface FamilyTreeRepository {

    val allMembers: Flow<List<FamilyMember>>

    suspend fun downloadDataFromServer(): Boolean

    suspend fun syncDataToFirebase(isAlsoDownload: Boolean = false)

    suspend fun insertAllMembers(members: List<FamilyMember>)

    suspend fun insertMember(member: FamilyMember): Long

    suspend fun updateMember(member: FamilyMember, isGotraChanged: Boolean = false, spouseId: Int = -1)

    suspend fun deleteMember(id: Int)

    suspend fun deleteAllRelations(id: Int)

    // Relations operations
    suspend fun getRelationsForMember(memberId: Int): Flow<List<FamilyRelation>>

    suspend fun insertRelation(relation: FamilyRelation)

    suspend fun insertAllRelation(relation: List<FamilyRelation>)

    suspend fun deleteRelation(m1Id: Int, m2Id: Int, type: String)

    suspend fun getMemberById(id: Int): FamilyMember?

    fun getPagedMembersForSearchByName(name: String, isUnmarried: Boolean = false): Flow<PagingData<MemberWithFather>>
    fun getPagedMembersForSearchByFilter(name: String, filterMatrix: MemberFilter): Flow<PagingData<MemberWithFather>>

    suspend fun getSpouse(memberId: Int): FamilyMember?

    suspend fun getChildren(memberId: Int): List<FamilyMember>

    suspend fun getChildrenWithSpouse(memberId: Int): List<ChildWithSpouseDto>

    suspend fun getFullAncestorTree(memberId: Int): DualAncestorTree?

    suspend fun getCompleteFamilyTree(memberId: Int): FullFamilyTree?

    suspend fun getFullDescendantTree(memberId: Int): DescendantNode?

    suspend fun getParentsWithMemberId(memberId: Int): List<Pair<String, FamilyMember>>

    suspend fun verifyInternetAccess(): Boolean

    suspend fun isNoDataAndNoInternet(): Boolean

    suspend fun getMemberRelatives(memberId: Int): MemberRelationAR

    suspend fun getMembersBetweenRelations(member1Relatives: MemberRelationAR, member2Relatives: MemberRelationAR): Pair<String, String>
    suspend fun getMemberSmallBio(memberRelatives: MemberRelationAR): String
    suspend fun createMemberTimeline(memberRelatives: MemberRelationAR): List<TimelineEvent>
}