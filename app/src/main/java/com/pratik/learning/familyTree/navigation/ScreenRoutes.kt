package com.pratik.learning.familyTree.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute

@Serializable
data object MemberListGraph : AppRoute

@Serializable
data object SplashRoute : AppRoute

@Serializable
data object Home : AppRoute

@Serializable
data object AddMember : AppRoute

@Serializable
data object MemberDetailsGraph : AppRoute

@Serializable
data class MemberDetailsRoute(val memberId: Int) : AppRoute

@Serializable
data class EditMemberRoute(val memberId: Int) : AppRoute

@Serializable
data class AncestryRoute(val memberId: Int) : AppRoute

@Serializable
data class AddRelationRoute(val memberId: Int) : AppRoute

@Serializable
data class MembersCompareRoute(val memberId: Int) : AppRoute

@Serializable
data class MemberTimelineRoute(val memberId: Int) : AppRoute

@Serializable data object MySpace : AppRoute
@Serializable data object Admin : AppRoute
@Serializable data object About : AppRoute


