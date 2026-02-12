package com.pratik.learning.familyTree.data.local.dao

import android.util.Log
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pratik.learning.familyTree.data.local.dto.MemberWithSpouseDto
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyTreeDao {

    // --- MEMBER OPERATIONS ---
    @Query("SELECT * FROM members ORDER BY memberId DESC")
    fun getAllMembersForServer(): List<FamilyMember>

    @Query("SELECT * FROM relations")
    fun getAllRelationsForServer(): List<FamilyRelation>

    @Delete
    suspend fun deleteMembers(members: List<FamilyMember>)

    @Delete
    suspend fun deleteRelations(relations: List<FamilyRelation>)

    @Update
    suspend fun updateRelation(relation: FamilyRelation)

    @Transaction
    suspend fun insertAllMembersAndRelations(
        members: List<FamilyMember>,
        relations: List<FamilyRelation>
    ) {
        Log.w(
            "Sync",
            "🚀 Starting sync with ${members.size} members and ${relations.size} relations"
        )

        // --- Step 1: Fetch current local data
        val localMembers = getAllMembersForServer()
        val localRelations = getAllRelationsForServer()

        // --- Step 2: Prepare lookup maps
        val localMemberMap = localMembers.associateBy { it.memberId }
        val serverMemberIds = members.map { it.memberId }.toSet()

        // --- Step 3: Insert or update members
        for (member in members) {
            val local = localMemberMap[member.memberId]
            if (local == null) {
                insertMember(member.copy(isNewEntry = false))
                Log.i("Sync", "🟢 Inserted new member ${member.fullName} (ID=${member.memberId})")
            } else if (member.updatedAt.toLong() > local.updatedAt.toLong()) {
                updateMember(member.copy(isNewEntry = false))
                Log.i("Sync", "🟡 Updated member ${member.fullName} (ID=${member.memberId})")
            } else {
                Log.d(
                    "Sync",
                    "⚪ Skipped unchanged member ${member.fullName} (ID=${member.memberId})"
                )
            }
        }

        // --- Step 4: Delete local members missing on server (but only if not isNewEntry)
        val toDeleteMembers = localMembers.filter {
            it.memberId !in serverMemberIds && !it.isNewEntry
        }
        if (toDeleteMembers.isNotEmpty()) {
            deleteMembers(toDeleteMembers)
            Log.w(
                "Sync",
                "🔴 Deleted ${toDeleteMembers.size} members missing on server (non-new entries only)"
            )
        }

        // --- Step 5: Handle relations
        val localRelationMap = localRelations.associateBy {
            Triple(it.relatesToMemberId, it.relatedMemberId, it.relationType)
        }
        val serverRelationKeys = relations.map {
            Triple(it.relatesToMemberId, it.relatedMemberId, it.relationType)
        }.toSet()

        for (relation in relations) {
            val key =
                Triple(relation.relatesToMemberId, relation.relatedMemberId, relation.relationType)
            val local = localRelationMap[key]

            if (local == null) {
                insertRelation(relation.copy(isNewEntry = false))
                Log.i(
                    "Sync",
                    "🟢 Inserted new relation ${relation.relationType} (${relation.relatesToMemberId}→${relation.relatedMemberId})"
                )
            } else if (relation.updatedAt.toLong() > local.updatedAt.toLong()) {
                updateRelation(relation.copy(isNewEntry = false))
                Log.i(
                    "Sync",
                    "🟡 Updated relation ${relation.relationType} (${relation.relatesToMemberId}→${relation.relatedMemberId})"
                )
            } else {
                Log.d("Sync", "⚪ Skipped unchanged relation ${relation.relationType}")
            }
        }

        // --- Step 6: Delete local relations missing on server (but only if not isNewEntry)
        val relationsToDelete = localRelations.filter {
            Triple(
                it.relatesToMemberId,
                it.relatedMemberId,
                it.relationType
            ) !in serverRelationKeys &&
                    !it.isNewEntry
        }
        if (relationsToDelete.isNotEmpty()) {
            deleteRelations(relationsToDelete)
            Log.w(
                "Sync",
                "🔴 Deleted ${relationsToDelete.size} relations missing on server (non-new entries only)"
            )
        }

        // --- Step 7: Reset isNewEntry flag after successful sync
        markAllAsSynced()

        Log.w("Sync", "✅ Sync completed successfully")
    }

    private suspend fun markAllAsSynced() {
        markAllMembersAsSynced()
        markAllRelationsAsSynced()
    }

    @Query("UPDATE members SET isNewEntry = 0 WHERE isNewEntry = 1")
    suspend fun markAllMembersAsSynced()

    @Query("UPDATE relations SET isNewEntry = 0 WHERE isNewEntry = 1")
    suspend fun markAllRelationsAsSynced()

    @Query(
        """
        DELETE FROM relations 
        WHERE relatesToMemberId = :memberId 
           OR relatedMemberId = :memberId
    """
    )
    suspend fun deleteRelationsForMember(memberId: Int)

    @Transaction
    suspend fun deleteMemberWithRelations(memberId: Int) {
        // Delete all relations related to this member
        deleteRelationsForMember(memberId)

        // Delete the member
        deleteMember(memberId)
    }

    @Query("SELECT MAX(memberId) FROM members")
    suspend fun getMaxMemberId(): Int?

    @Query("SELECT COUNT(*) FROM members")
    suspend fun getMemberCount(): Int

    @Query("DELETE FROM members")
    suspend fun clearMembers()

    @Query("DELETE FROM relations")
    suspend fun clearRelations()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMembers(members: List<FamilyMember>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember): Long // Returns the new row ID

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Query(
        """
    UPDATE members 
    SET gotra = :gotra, 
        updatedAt = :updatedAt 
    WHERE memberId = :memberId
"""
    )
    suspend fun updateGotra(
        memberId: Int,
        gotra: String,
        updatedAt: Long = System.currentTimeMillis()
    )


    @Query("SELECT * FROM members ORDER BY memberId DESC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM relations")
    fun getAllRelations(): Flow<List<FamilyRelation>>

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
    @Query(
        """
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
    """
    )
    suspend fun getImmediateParentByType(childId: Int, relationType: String): FamilyMember?

    /**
     * Retrieves the spouse of a given member ID.
     * This query looks for a reciprocal 'Husband' or 'Wife' relation.
     */
    @Query(
        """
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
    """
    )
    suspend fun getSpouse(memberId: Int): FamilyMember?

    /**
     * Retrieves the children of a given member ID.
     * This query looks for a reciprocal 'Son' or 'Daughter' relation.
     */
    @Query(
        """
        SELECT 
            m2.*
        FROM 
            relations r 
        INNER JOIN 
            members m2 ON r.relatesToMemberId = m2.memberId
        WHERE
            r.relatedMemberId = :memberId 
            AND r.relationType IN ('Father', 'Mother')
        ORDER BY
            m2.dob ASC
    """
    )
    suspend fun getChildren(memberId: Int): List<FamilyMember>?


    @Query(
        """
    SELECT 
        c.*, 
        s.fullName AS spouseFullName, 
        s.memberId AS spouseId
    FROM 
        relations rParent
    INNER JOIN 
        members c ON rParent.relatesToMemberId = c.memberId
    LEFT JOIN 
        relations rSpouse ON 
            (
                (rSpouse.relatesToMemberId = c.memberId AND rSpouse.relationType IN ('Husband', 'Wife'))
                OR
                (rSpouse.relatedMemberId = c.memberId AND rSpouse.relationType IN ('Husband', 'Wife'))
            )
    LEFT JOIN 
        members s ON 
            (CASE 
                WHEN rSpouse.relatesToMemberId = c.memberId THEN rSpouse.relatedMemberId 
                ELSE rSpouse.relatesToMemberId 
            END) = s.memberId
    WHERE
        rParent.relatedMemberId = :memberId
        AND rParent.relationType IN ('Father', 'Mother')
    GROUP BY 
        c.memberId
    ORDER BY 
        c.dob ASC
"""
    )
    suspend fun getChildrenWithSpouse(memberId: Int): List<MemberWithSpouseDto>

//
//@Query("""
//     SELECT
//    m1.*,
//    m2.fullName AS fatherFullName
//FROM
//    members m1
//LEFT JOIN
//    relations rFather
//    ON m1.memberId = rFather.relatesToMemberId AND rFather.relationType = 'Father'
//LEFT JOIN
//    members m2
//    ON rFather.relatedMemberId = m2.memberId
//WHERE
//    m1.fullName LIKE '%' || :name || '%'
//    AND (
//        :isUnmarried = 0
//        OR (
//            NOT EXISTS (
//                SELECT 1
//                FROM relations r
//                WHERE r.relationType IN ('Husband', 'Wife')
//                  AND (r.relatesToMemberId = m1.memberId OR r.relatedMemberId = m1.memberId)
//            )
//            AND (julianday('now') - julianday(m1.dob)) / 365.25 >= 18
//        )
//    )
//ORDER BY
//    m1.memberId DESC
//""")


    @Query(
        """
SELECT 
    m1.*,
    mFather.fullName AS fatherFullName,
    mHusband.fullName AS husbandFullName

FROM members m1

-- Father join
LEFT JOIN relations rFather
    ON rFather.relatesToMemberId = m1.memberId 
    AND rFather.relationType = 'Father'
LEFT JOIN members mFather
    ON mFather.memberId = rFather.relatedMemberId

-- Husband join (for female only)
LEFT JOIN relations rHusband
    ON (
        (m1.gender = 'F' AND rHusband.relatesToMemberId = m1.memberId AND rHusband.relationType = 'Husband')
        OR (m1.gender = 'F' AND rHusband.relatedMemberId = m1.memberId AND rHusband.relationType = 'Wife')
    )
LEFT JOIN members mHusband
    ON (
        CASE 
            WHEN rHusband.relatesToMemberId = m1.memberId THEN rHusband.relatedMemberId
            ELSE rHusband.relatesToMemberId
        END
    ) = mHusband.memberId

WHERE
    m1.fullName LIKE '%' || :name || '%'
    AND (
        :isUnmarried = 0
        OR (
            NOT EXISTS (
                SELECT 1
                FROM relations r
                WHERE r.relationType IN ('Husband', 'Wife')
                  AND (r.relatesToMemberId = m1.memberId OR r.relatedMemberId = m1.memberId)
            )
            AND (julianday('now') - julianday(m1.dob)) / 365.25 >= 18
        )
    )
GROUP BY 
    m1.memberId
ORDER BY
    CASE WHEN :sortBy = 'NAMEUP' THEN m1.fullName END ASC,
    CASE WHEN :sortBy = 'NAMEDOWN' THEN m1.fullName END DESC,
    CASE WHEN :sortBy = 'IDUP' THEN m1.memberId END ASC,
    CASE WHEN :sortBy = 'IDDOWN' THEN m1.memberId END DESC,
    CASE WHEN :sortBy = 'DOB' THEN m1.dob END ASC,
    CASE WHEN :sortBy = 'DOB' THEN m1.dob END DESC,
    m1.memberId DESC
"""
    )

    fun getAllMembersBySearchQuery(
        name: String,
        isUnmarried: Boolean,
        sortBy: String = "IDDOWN"
    ): PagingSource<Int, MemberWithFather>


    @Query(
        """
SELECT 
    m1.*,
    mFather.fullName AS fatherFullName,
    mHusband.fullName AS husbandFullName

FROM members m1

-- Father join
LEFT JOIN relations rFather
    ON rFather.relatesToMemberId = m1.memberId 
    AND rFather.relationType = 'Father'
LEFT JOIN members mFather
    ON mFather.memberId = rFather.relatedMemberId

-- Husband join (for female only)
LEFT JOIN relations rHusband
    ON (
        (m1.gender = 'F' AND rHusband.relatesToMemberId = m1.memberId AND rHusband.relationType = 'Husband')
        OR (m1.gender = 'F' AND rHusband.relatedMemberId = m1.memberId AND rHusband.relationType = 'Wife')
    )
LEFT JOIN members mHusband
    ON (
        CASE 
            WHEN rHusband.relatesToMemberId = m1.memberId THEN rHusband.relatedMemberId
            ELSE rHusband.relatesToMemberId
        END
    ) = mHusband.memberId

WHERE
    m1.memberId IN (:ids)
    AND m1.fullName LIKE '%' || :name || '%'

GROUP BY 
    m1.memberId

ORDER BY
    CASE WHEN :sortBy = 'NAMEUP' THEN m1.fullName END ASC,
    CASE WHEN :sortBy = 'NAMEDOWN' THEN m1.fullName END DESC,
    CASE WHEN :sortBy = 'IDUP' THEN m1.memberId END ASC,
    CASE WHEN :sortBy = 'IDDOWN' THEN m1.memberId END DESC,
    CASE WHEN :sortBy = 'DOB' THEN m1.dob END ASC,
    CASE WHEN :sortBy = 'DOB' THEN m1.dob END DESC,
    m1.memberId DESC
"""
    )
    fun getMyFavMembersBySearchQuery(
        name: String,
        ids: List<Int>,
        sortBy: String = "IDDOWN"
    ): PagingSource<Int, MemberWithFather>

}
