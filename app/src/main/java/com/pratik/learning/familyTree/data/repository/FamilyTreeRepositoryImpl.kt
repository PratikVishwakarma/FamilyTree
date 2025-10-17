package com.pratik.learning.familyTree.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pratik.learning.familyTree.data.local.dao.FamilyTreeDao
import com.pratik.learning.familyTree.data.local.dto.AncestorNode
import com.pratik.learning.familyTree.data.local.dto.DualAncestorTree
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER
import kotlinx.coroutines.flow.Flow

class FamilyTreeRepositoryImpl(
    private val dao: FamilyTreeDao
): FamilyTreeRepository  {
    // Member Flow for UI updates
    override val allMembers: Flow<List<FamilyMember>> = dao.getAllMembers()

    override suspend fun insertAllMembers(members: List<FamilyMember>) {
        dao.insertAllMembers(members)
    }

    override suspend fun insertMember(member: FamilyMember): Long {
        Log.d("FamilyTreeRepositoryImpl", "insertMember: $member")
        return dao.insertMember(member)
    }

    override suspend fun updateMember(member: FamilyMember) {
        Log.d("FamilyTreeRepositoryImpl", "updateMember: $member")
        dao.updateMember(member)
    }

    override suspend fun deleteMember(id: Int) {
        dao.deleteMember(id)
    }

    // Relation operations
    override suspend fun getRelationsForMember(memberId: Int): Flow<List<FamilyRelation>> {
        return dao.getRelationsForMember(memberId)
    }

    override suspend fun insertRelation(relation: FamilyRelation) {
        dao.insertRelation(relation)
    }

    override suspend fun insertAllRelation(relation: List<FamilyRelation>) {
        Log.d("FamilyTreeRepositoryImpl", "insertAllRelation: $relation")
        dao.insertAllRelations(relation)
    }

    override suspend fun deleteRelation(m1Id: Int, m2Id: Int, type: String) {
        dao.deleteRelation(m1Id, m2Id, type)
    }

    override suspend fun getMemberById(id: Int): FamilyMember? {
        return dao.getMemberById(id)
    }

    override fun getPagedMembersForSearchByName(name: String): Flow<PagingData<MemberWithFather>> {
        Log.d("InterviewPagingSource", "Loading page with matched: $name")
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.getAllMembersBySearchQuery(name) }
        ).flow
    }

    // The complex, derived relationship logic (like reciprocal and inferred relations)
    // should live here or in a separate 'TreeLogicManager' service, not in the DAO.

    private suspend fun buildAncestryNode(
        member: FamilyMember,
        level:Int,
        familyTreeDao: FamilyTreeDao,
        isParental: Boolean = true
    ): AncestorNode {
        Log.d("buildAncestryNode", "called for member = ${member.fullName},  isParental = $isParental")
        // 1. Get Spouse using getSpouse()
        val spouse = familyTreeDao.getSpouse(member.memberId)

        // 2. Recursively find Father's line
        val father = familyTreeDao.getImmediateParentByType(member.memberId, "Father")
        val fatherNode = father?.let { buildAncestryNode(it, level = level + 1, familyTreeDao, isParental) }

        // 3. Recursively find Mother's line
        val mother = familyTreeDao.getImmediateParentByType(member.memberId, "Mother")
        val motherNode = mother?.let { buildAncestryNode(it, level = level + 1, familyTreeDao, isParental) }
        Log.d("buildAncestryNode", "member = ${member.fullName}, father = $fatherNode, mother = $motherNode  isParental = $isParental")

        // 4. Construct the AncestorNode
        val parents = mutableListOf<AncestorNode>()
        if (fatherNode != null) parents.add(fatherNode)
        if (motherNode != null) parents.add(motherNode)

        return AncestorNode(
            member = member,
            spouse = spouse,
            parents = parents,
            level = level + 1,
            relationWithMember = getRelationWithMember(level+1, isParental)
        )
    }

    override suspend fun getFullAncestorTree(memberId: Int): DualAncestorTree? {
        // 1. Get the starting member (e.g., Pratik)
        val startingMember = dao.getMemberById(memberId) ?: return null

        // 2. Find the root of the paternal line
        val father = dao.getImmediateParentByType(startingMember.memberId, "Father")

        // 3. Find the root of the maternal line
        val mother = dao.getImmediateParentByType(startingMember.memberId, "Mother")

        // 4. Build the full tree recursively for each line
        val paternalTree = father?.let { buildAncestryNode(it, level = 1, dao, isParental = true) }
        val maternalTree = mother?.let { buildAncestryNode(it, level = 1, dao, isParental = false) }

        // 5. Find the current couple
        val currentSpouse = dao.getSpouse(startingMember.memberId)

        return DualAncestorTree(
            self = startingMember,
            spouse = currentSpouse,
            paternalLineRoot = paternalTree,
            maternalLineRoot = maternalTree
        )
    }

    private fun getRelationWithMember(level: Int, isParental: Boolean): String {
        Log.d("getRelationWithMember", "level = $level, isParental = $isParental")
        return when (level) {
            1 -> ""
            2 -> "पिता - माता"
            3 -> if (isParental) "दादा जी - दादी जी" else "नाना जी : नानी जी"
            4 -> if (isParental) "परदादा जी - परदादी जी" else "परनाना जी : परनानी जी"
            else -> if (isParental) "पर...परदादा जी - पर...परदादी जी" else "पर...परनाना जी : पर...परनानी जी"
        }
    }

    override suspend fun getParentsWithMemberId(memberId: Int): List<Pair<String, FamilyMember>> {
        Log.d("getParentsWithMemberId", "memberId = $memberId")
        val parents = mutableListOf<Pair<String, FamilyMember>>()
        val father = dao.getImmediateParentByType(memberId, RELATION_TYPE_FATHER)
        father?.let {
            parents.add(Pair("Father", father))
        }
        val mother = dao.getImmediateParentByType(memberId, RELATION_TYPE_MOTHER)
        mother?.let {
            parents.add(Pair("Mother", mother))
        }
        return parents
    }

    override suspend fun getSpouse(memberId: Int): FamilyMember? {
        Log.d("getSpouse", "memberId = $memberId")
        return dao.getSpouse(memberId)
    }

    override suspend fun getChildren(memberId: Int): List<FamilyMember> {
        Log.d("getChildren", "memberId = $memberId")
        return dao.getChildren(memberId)?: emptyList()
    }
}