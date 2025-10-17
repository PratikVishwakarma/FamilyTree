package com.pratik.learning.familyTree.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyTreeDao {

    // --- MEMBER OPERATIONS ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMembers(members: List<FamilyMember>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember): Long // Returns the new row ID

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Query("SELECT * FROM members ORDER BY memberId DESC")
    fun getAllMembers(): Flow<List<FamilyMember>>


    @Query("SELECT * FROM members WHERE memberId = :id")
    suspend fun getMemberById(id: Int): FamilyMember?

    @Query("DELETE FROM members WHERE memberId = :id")
    suspend fun deleteMember(id: Int)

    // --- RELATION OPERATIONS ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: FamilyRelation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRelations(relations: List<FamilyRelation>)

    /**
     * Finds all relations where the given member is the subject (member1).
     * Used for displaying relations list and building the tree.
     */
    @Query("SELECT * FROM relations WHERE relatesToMemberId = :memberId")
    fun getRelationsForMember(memberId: Int): Flow<List<FamilyRelation>>

    /**
     * Finds all relations where the given member is the object (member2).
     * Used for finding children or spouses pointing to the member.
     */
    @Query("SELECT * FROM relations WHERE relatesToMemberId = :memberId")
    fun getInverseRelationsForMember(memberId: Int): Flow<List<FamilyRelation>>

    /**
     * Deletes a specific, directed relation.
     */
    @Query("DELETE FROM relations WHERE relatesToMemberId = :m1Id AND relatesToMemberId = :m2Id AND relationType = :type")
    suspend fun deleteRelation(m1Id: Int, m2Id: Int, type: String)

    /**
     * Deletes all relations involving a member (used before deleting the member itself,
     * although the ForeignKey rule also handles this).
     */
    @Query("DELETE FROM relations WHERE relatesToMemberId = :memberId OR relatesToMemberId = :memberId")
    suspend fun deleteAllRelationsForMember(memberId: Int)


    // complex queries

    /**
     * Retrieves a single parent of a specific type (e.g., 'Father' or 'Mother') for a child.
     * This is crucial for isolating the paternal and maternal lines for the DualAncestorTree.
     */
    @Query("""
        SELECT 
            m2.*
        FROM 
            relations r 
        INNER JOIN 
            members m2 ON r.relatedMemberId = m2.memberId
        WHERE
            r.relatesToMemberId = :childId 
            AND r.relationType = :relationType
        LIMIT 1
    """)
    suspend fun getImmediateParentByType(childId: Int, relationType: String): FamilyMember?

    /**
     * Retrieves the spouse of a given member ID.
     * This query looks for a reciprocal 'Husband' or 'Wife' relation.
     */
    @Query("""
        SELECT 
            m2.*
        FROM 
            relations r 
        INNER JOIN 
            members m2 ON r.relatedMemberId = m2.memberId
        WHERE
            r.relatesToMemberId = :memberId 
            AND r.relationType IN ('Husband', 'Wife')
        LIMIT 1
    """)
    suspend fun getSpouse(memberId: Int): FamilyMember?

    /**
     * Retrieves the children of a given member ID.
     * This query looks for a reciprocal 'Son' or 'Daughter' relation.
     */
    @Query("""
        SELECT 
            m2.*
        FROM 
            relations r 
        INNER JOIN 
            members m2 ON r.relatedMemberId = m2.memberId
        WHERE
            r.relatesToMemberId = :memberId 
            AND r.relationType IN ('Son', 'Daughter')
    """)
    suspend fun getChildren(memberId: Int): List<FamilyMember>?


    @Query("""
        SELECT 
            m1.*, 
            m2.fullName AS fatherFullName
        FROM 
            members m1
        LEFT JOIN 
            relations r 
        ON 
            m1.memberId = r.relatesToMemberId AND r.relationType = 'Father'
        LEFT JOIN 
            members m2 
        ON 
            r.relatedMemberId = m2.memberId
        WHERE
            m1.fullName LIKE '%' || :name || '%'
        ORDER BY 
            m1.memberId DESC
    """)
    fun getAllMembersBySearchQuery(name: String): PagingSource<Int, MemberWithFather>

}
