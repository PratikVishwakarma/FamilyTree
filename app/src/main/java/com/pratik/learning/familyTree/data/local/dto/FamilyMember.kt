package com.pratik.learning.familyTree.data.local.dto


import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines the main table for storing family members.
 */
@Keep
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

@Keep
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
@Keep
data class MemberWithSpouseDto(
    @Embedded val innerMember: FamilyMember,
    val spouseId: Int?,
    val spouseFullName: String?
)

@Keep
data class DualAncestorTree(
    val self: FamilyMember?,
    val spouse: FamilyMember?,
    val paternalLineRoot: AncestorNode?,
    val maternalLineRoot: AncestorNode?)
@Keep
data class AncestorNode(
    val level: Int,
    val member: FamilyMember,
    val spouse: FamilyMember?,
    val parents: List<AncestorNode>,
    val relationWithMember: String = ""
)
@Keep
data class FullFamilyTree(
    val self: FamilyMember,
    val spouse: FamilyMember? = null,
    val ancestors: DualAncestorTree? = null,
    val descendants: DescendantNode? = null
)
@Keep
data class DescendantNode(
    val member: FamilyMember,
    val spouse: FamilyMember? = null,
    val children: List<DescendantNode> = emptyList(),
    val level: Int,
    val relationWithMember: String
)


@Keep
data class MemberRelationAR(
    var member: FamilyMember? = null,
    var spouse: Pair<String, FamilyMember>? = null,
    val parents: ArrayList<Pair<String, FamilyMember>> = arrayListOf(),
    val inLaws: ArrayList<Pair<String, FamilyMember>> = arrayListOf(),
    val siblings: ArrayList<Pair<String, MemberWithSpouseDto>> = arrayListOf(),
    val spouseSiblings: ArrayList<Pair<String, MemberWithSpouseDto>> = arrayListOf(),
    val children: ArrayList<Pair<String, MemberWithSpouseDto>> = arrayListOf(),
    val grandchildren: ArrayList<Pair<String, FamilyMember>> = arrayListOf(),
    val grandParentsFather: ArrayList<Pair<String, FamilyMember>> = arrayListOf(),
    val grandParentsMother: ArrayList<Pair<String, FamilyMember>> = arrayListOf(),
    val uncleAuntFatherSide: ArrayList<Pair<String, MemberWithSpouseDto>> = arrayListOf(),
    val uncleAuntMotherSide: ArrayList<Pair<String, MemberWithSpouseDto>> = arrayListOf(),
)


@Keep
enum class TimelineEventType {
    BIRTH,
    MARRIAGE,
    SON_BIRTH,
    DAUGHTER_BIRTH,
    DEATH
}

@Keep
data class TimelineEvent(
    val type: TimelineEventType,
    val title: String,
    val subtitle: String,
    val date: String
)

@Keep
data class AncestryLevel(
    val level: Int,
    val name: String,
    val relation: String
)

