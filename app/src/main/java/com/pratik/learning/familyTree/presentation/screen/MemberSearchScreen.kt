package com.pratik.learning.familyTree.presentation.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.pratik.learning.familyTree.R
import com.pratik.learning.familyTree.navigation.MemberDetailsRoute
import com.pratik.learning.familyTree.presentation.UIState
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.FilterView
import com.pratik.learning.familyTree.presentation.component.MemberInfoOverlay
import com.pratik.learning.familyTree.presentation.component.MemberSmallTile
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.isAdmin
import com.pratik.learning.familyTree.utils.logger

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MemberSearchScreen(
    navController: NavController,
    viewModel: MembersViewModel
) {

    val members = viewModel.filterResult2.collectAsLazyPagingItems()
    val query by viewModel.query.collectAsState()
    val filterMatrix = viewModel.filter.collectAsState().value
    val uiState = viewModel.uiState.collectAsState().value

    BackHandler {
        if (query.isNotEmpty())
            viewModel.onQueryChanged("")
        if (uiState is UIState.FilterExpandedUIState)
            viewModel.dismissFilterExpand()
        else
            navController.popBackStack()
    }
    Container(
        title = "Members",
        isFilter = true,
        rightButton = {
            if (viewModel.relationType.isEmpty() && isAdmin)
                Text(
                    "Add Member".inHindi(),
                    modifier = Modifier.clickable { viewModel.navigateToAddMember(navController) },
                    style = MaterialTheme.typography.titleMedium
                )
        }
    ) {
        when (uiState) {
            is UIState.ExpandViewUIState -> {
                logger("ExpandViewUIState", "Long press for member: ${uiState.member.fullName}")
                MemberInfoOverlay(uiState.member) {
                    viewModel.dismissExpandedMember()
                }
            }

            else -> {
                // nothing required
            }
        }
        AnimatedVisibility(
            visible = uiState is UIState.FilterExpandedUIState,
            enter = slideInHorizontally(
                initialOffsetX = { it }, // slide in from right to left
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it }, // slide out from left to right
                animationSpec = tween(durationMillis = 400)
            ),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f)
                .background(color = Color.Transparent)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp, bottom = 30.dp, start = 30.dp, end = 0.dp)
            ) {
                FilterView(
                    onDismiss = { viewModel.dismissFilterExpand() },
                    currentSortBy = filterMatrix.sortBy,
                    onSortBy = viewModel::onSortByChange,
                    onMarriedStatusChanged = viewModel::onUnmarriedCheck,
                    currentIsLeavingStatus = filterMatrix.isLeaving ?: false,
                    onIsLeavingStatusChanged = viewModel::onIsLivingCheck,
                    currentMarriedStatus = filterMatrix.isUnmarried,
                )
            }
        }
        Column(Modifier.padding(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = query, // type: String ✅
                    onValueChange = { viewModel.onQueryChanged(it) }, // type: (String) -> Unit ✅
                    maxLines = 1,
                    singleLine = true,
                    label = { Text("Search".inHindi()) },
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = "filter",
                    modifier = Modifier.size(28.dp).clickable{
                        viewModel.showExpandedFilter()
                    }
                )
            }

//            if (viewModel.relationType.isEmpty()) {
//                Spacer(Modifier.height(8.dp))
//                Row(
//                    modifier = Modifier.wrapContentSize(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Spacer(Modifier.weight(1f))
//                    Text(
//                        "Open Filter",
//                        modifier = Modifier.clickable {
//                            viewModel.showExpandedFilter()
//                        }
//                    )
//                }
//            }
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(members.itemCount) { index ->
                    val mem = members[index]
                    mem?.let { member ->
                        val relatedName =
                            if (!member.husbandFullName.isNullOrEmpty()) "पति - श्री ${member.husbandFullName}" else if (!member.fatherFullName.isNullOrEmpty()) "पिता - श्री ${member.fatherFullName}" else ""
                        val description =
                            if (relatedName.isNotEmpty()) "$relatedName - ${member.city}" else member.city
                        MemberSmallTile(
                            title = member.fullName,
                            description = description,
                            onSinglePress = {
                                navController.navigate(route = MemberDetailsRoute(member.memberId))
                            },
                            onLongPress = {
                                viewModel.showExpandedMember(member)
                                logger("onLong Click on member: ${member.fullName}")
                            }
                        )
                    }
                }
                members.apply {
                    when {
                        loadState.refresh is LoadState.Loading -> {
                            // Initial load
                            item { CircularProgressIndicator() }
                        }

                        loadState.append is LoadState.Loading -> {
                            // Pagination load
                            item { CircularProgressIndicator() }
                        }

                        loadState.append is LoadState.Error -> {
                            // Handle initial load error
                            item {
                                Text("Load error. Try again.")
                            }
                        }
                    }
                }
            }
        }
    }
}