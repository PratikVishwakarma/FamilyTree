package com.pratik.learning.familyTree.presentation.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pratik.learning.familyTree.navigation.AncestryRoute
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.DualFamilyTreeView
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.logger

@Composable
fun AncestryScreen(navController: NavController, viewModel: MemberDetailsViewModel) {

    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        Log.d("DetailsScreen", "LaunchedEffect")
        viewModel.fetchAncestry()
    }

    val familyTree = viewModel.familyTree.collectAsState().value
    val descendantTree = viewModel.descendantTree.collectAsState().value
    Container(
        title = "Family Tree",
        rightButton = null
    ) {
        LazyColumn {
            item {
                familyTree?.let {
                    val onMemberClick: (Int) -> Unit = { memberId ->
                        navController.navigate(route = AncestryRoute(memberId))
                    }
                    DualFamilyTreeView(
                        it,
                        descendantTree,
                        onMemberClick = onMemberClick,
                        onFinalSize = { width, height ->
                            logger("DualFamilyTreeView: width: $width  || height: $height")
                            if (width > 1000 || height > 2000) {
                                logger("DualFamilyTreeView: width: showing toast")
                                Toast.makeText(
                                    context, "Please scroll left or down to see whole family tree".inHindi(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }
}