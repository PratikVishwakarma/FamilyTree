package com.pratik.learning.familyTree.data.repository

import androidx.paging.PagingData
import com.pratik.learning.familyTree.data.local.dto.DualAncestorTree
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import kotlinx.coroutines.flow.Flow

interface FamilyTreeRepository {

    val allMembers: Flow<List<FamilyMember>>

    suspend fun insertAllMembers(members: List<FamilyMember>)

    suspend fun insertMember(member: FamilyMember): Long

    suspend fun updateMember(member: FamilyMember)

    suspend fun deleteMember(id: Int)

    // Relations operations
    suspend fun getRelationsForMember(memberId: Int): Flow<List<FamilyRelation>>

    suspend fun insertRelation(relation: FamilyRelation)

    suspend fun insertAllRelation(relation: List<FamilyRelation>)

    suspend fun deleteRelation(m1Id: Int, m2Id: Int, type: String)

    suspend fun getMemberById(id: Int): FamilyMember?

    fun getPagedMembersForSearchByName(name: String): Flow<PagingData<MemberWithFather>>

    suspend fun getSpouse(memberId: Int): FamilyMember?

    suspend fun getChildren(memberId: Int): List<FamilyMember>

    suspend fun getFullAncestorTree(memberId: Int): DualAncestorTree?

    suspend fun getParentsWithMemberId(memberId: Int): List<Pair<String, FamilyMember>>

}