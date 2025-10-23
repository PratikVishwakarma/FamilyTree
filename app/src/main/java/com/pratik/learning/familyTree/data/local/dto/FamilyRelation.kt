package com.pratik.learning.familyTree.data.local.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Defines the table for storing relationships (edges).
 * This table uses a composite primary key to ensure uniqueness for each directed relation.
 * (e.g., Member A -> Member B, Type 'Father')
 */
@Entity(
    tableName = "relations",
    primaryKeys = ["relatesToMemberId", "relatedMemberId", "relationType"],
    foreignKeys = [
        ForeignKey(
            entity = FamilyMember::class,
            parentColumns = ["memberId"],
            childColumns = ["relatesToMemberId"],
            onDelete = ForeignKey.CASCADE // If relatesToMemberId is deleted, remove this relation
        ),
        ForeignKey(
            entity = FamilyMember::class,
            parentColumns = ["memberId"],
            childColumns = ["relatedMemberId"],
            onDelete = ForeignKey.CASCADE // If relatedMemberId is deleted, remove this relation
        )
    ],
    // Indexing for faster lookups based on the subject of the relation
    indices = [Index(value = ["relatesToMemberId"]), Index(value = ["relatedMemberId"])]
)
data class FamilyRelation(
    // Subject of the relation (e.g., the child in a 'Father' relation). This member 'relates to' another.
    val relatesToMemberId: Int,

    // Object of the relation (e.g., the parent in a 'Father' relation). This is the 'related member'.
    val relatedMemberId: Int,

    // Type of relation (e.g., "Father", "Mother", "Husband", "Sibling")
    val relationType: String,

    val updatedAt: String = System.currentTimeMillis().toString(),
    val updatedBy: String = ""
)
