package com.pratik.learning.familyTree.data.local.model

import androidx.annotation.Keep

@Keep
data class MemberFilter(
    val query: String = "",
    val isUnmarried: Boolean = false,
    val isMale: Boolean? = null,
    val isLeaving: Boolean? = null,
    val sortBy: String = "IDDOWN",
    val city: String = "",
    val gotra: String = ""
)