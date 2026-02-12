package com.pratik.learning.familyTree.presentation

import androidx.compose.ui.graphics.vector.ImageVector
import com.pratik.learning.familyTree.navigation.AppRoute

data class FancyBottomItem(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector
)
