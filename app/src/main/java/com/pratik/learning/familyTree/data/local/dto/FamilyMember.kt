package com.pratik.learning.familyTree.data.local.dto


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines the main table for storing family members.
 */
@Entity(tableName = "members")
data class FamilyMember(
    // Primary key, auto-generated for new members
    @PrimaryKey
    var memberId: Int = 0,
    val fullName: String,
    val gender: String, // "M" or "F"
    val dob: String, // Date of Birth (ISO format "YYYY-MM-DD")
    val isLiving: Boolean,
    val dod: String="", // Date of Death (Nullable)
    val city: String="",
    val state: String ="",
    val mobile: String ="",
    val gotra: String ="",
    val updatedAt: String = System.currentTimeMillis().toString(),
    val updatedBy: String = "",
    var isNewEntry: Boolean = false
)


/**
 * Data class to hold the result of the query: a member's full details
 * joined with their father's full name.
 */
data class MemberWithFather(
    // Fields from the FamilyMember entity
    val memberId: Int,
    val fullName: String,
    val gender: String,
    val dob: String,
    val isLiving: Boolean,
    val dod: String,
    val city: String="",
    val state: String ="",
    val mobile: String ="",
    val gotra: String ="",

    // Field from the JOIN operation (the father's name)
    val fatherFullName: String?,
    // Field from the JOIN operation (the father's name)
    val husbandFullName: String?
)

data class ChildWithSpouseDto(
    @Embedded val child: FamilyMember,
    val spouseId: Int?,
    val spouseFullName: String?
)


data class DualAncestorTree(
    val self: FamilyMember?,
    val spouse: FamilyMember?,
    val paternalLineRoot: AncestorNode?,
    val maternalLineRoot: AncestorNode?)

data class AncestorNode(
    val level: Int,
    val member: FamilyMember,
    val spouse: FamilyMember?,
    val parents: List<AncestorNode>,
    val relationWithMember: String = ""
)

data class FullFamilyTree(
    val self: FamilyMember,
    val spouse: FamilyMember? = null,
    val ancestors: DualAncestorTree? = null,
    val descendants: DescendantNode? = null
)

data class DescendantNode(
    val member: FamilyMember,
    val spouse: FamilyMember? = null,
    val children: List<DescendantNode> = emptyList(),
    val level: Int,
    val relationWithMember: String
)


data class MemberRelations(
    val parents: List<Pair<String, FamilyMember>> = emptyList(),
    val spouse: Pair<String, FamilyMember>? = null,
    val inLaws: List<Pair<String, FamilyMember>> = emptyList(),
    val siblings: List<Pair<String, FamilyMember>> = emptyList(),
    val children: List<Pair<String, ChildWithSpouseDto>> = emptyList(),
    val grandchildren: List<Pair<String, FamilyMember>> = emptyList(),
    val grandParentsFather: List<Pair<String, FamilyMember>> = emptyList(),
    val grandParentsMother: List<Pair<String, FamilyMember>> = emptyList(),
)

