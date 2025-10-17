package com.pratik.learning.familyTree.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute

@Serializable
data class Home(val relation: String = "") : AppRoute

@Serializable
data object AddMember : AppRoute

@Serializable
data object MemberDetailsGraph : AppRoute

@Serializable
data class DetailsRoute(val memberId: Int) : AppRoute

@Serializable
data class EditMemberRoute(val memberId: Int) : AppRoute

@Serializable
data class AncestryRoute(val memberId: Int) : AppRoute

@Serializable
data class AddRelationRoute(val memberId: Int) : AppRoute

@Serializable
data object ProductRoute : AppRoute


