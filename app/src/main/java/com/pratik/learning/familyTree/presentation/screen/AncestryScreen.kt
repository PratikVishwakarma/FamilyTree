package com.pratik.learning.familyTree.presentation.screen

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.pratik.learning.familyTree.navigation.AncestryRoute
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.DualFamilyTreeView
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel

@Composable
fun AncestryScreen(navController: NavController, viewModel: MemberDetailsViewModel) {

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

            item{
                familyTree?.let {
                    val onMemberClick: (Int) -> Unit = { memberId ->
                        navController.navigate(route = AncestryRoute(memberId))
                    }
                    DualFamilyTreeView(it, descendantTree, onMemberClick)
                }
            }
        }
    }
}