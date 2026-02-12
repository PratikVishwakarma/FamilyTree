package com.pratik.learning.familyTree.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pratik.learning.familyTree.FamilyTreeApp.Companion.isAdmin
import com.pratik.learning.familyTree.R
import com.pratik.learning.familyTree.navigation.AddRelationRoute
import com.pratik.learning.familyTree.navigation.AncestryRoute
import com.pratik.learning.familyTree.navigation.EditMemberRoute
import com.pratik.learning.familyTree.navigation.MemberDetailsRoute
import com.pratik.learning.familyTree.navigation.MemberTimelineRoute
import com.pratik.learning.familyTree.navigation.MembersCompareRoute
import com.pratik.learning.familyTree.presentation.UIState
import com.pratik.learning.familyTree.presentation.component.CircleIconButton
import com.pratik.learning.familyTree.presentation.component.ConfirmationPopup
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.MemberInfoSection
import com.pratik.learning.familyTree.presentation.component.RelationGroup
import com.pratik.learning.familyTree.presentation.component.RelationGroupWithSpouse
import com.pratik.learning.familyTree.presentation.viewmodel.AppViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.utils.SyncPrefs.isMemberInMyFavListFlow
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.logger

@Composable
fun MemberDetailsScreen(navController: NavController, viewModel: MemberDetailsViewModel, appVM: AppViewModel) {

    val onMemberClick: (Int) -> Unit = { memberId ->
        if (memberId != viewModel.memberId) {
            navController.navigate(route = MemberDetailsRoute(memberId))
        }
    }

    LaunchedEffect(Unit) {
        logger("MemberDetailsScreen LaunchedEffect")
        viewModel.fetchDetails()
    }
    val uiState = viewModel.uiState.collectAsState().value
    val member by viewModel.member.collectAsState()
    val relatives by viewModel.relatives.collectAsState()
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    val isFav by isMemberInMyFavListFlow(LocalContext.current, viewModel.memberId)
        .collectAsStateWithLifecycle(initialValue = false)
    Container(
        title = "Member Details".inHindi(),
        rightButton = {
            if (isAdmin)
                Text(
                    "Edit".inHindi(),
                    modifier = Modifier.clickable { navController.navigate(EditMemberRoute(viewModel.memberId)) },
                    style = MaterialTheme.typography.titleMedium
                )
        }
    ) {
        when(uiState) {
            is UIState.ConfirmationUIState -> {
                ConfirmationPopup(
                    title = uiState.title.inHindi(),
                    message = uiState.message.inHindi(),
                    onDismiss = { viewModel.dismissConfirmationPopup() },
                    onConfirm = {
                        when (uiState.title) {
                            "Delete Member" -> {
                                logger("MemberDetailsScreen", "onConfirm: Delete Member")
                                viewModel.deleteMember(navController)
                            }
                            "Delete all relations" -> {
                                logger("MemberDetailsScreen", "onConfirm: Delete all relations")
                                viewModel.deleteAllRelations()
                            }
                        }
                    }
                )
            }
            else -> {
                // nothing required
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            // 🧍 Member Info
            // Member Info Section
            item {
                MemberInfoSection(member, isFav, {
                    viewModel.addRemoveFromFavList(isFav)
                })
                HorizontalDivider(
                    color = dividerColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Relations
            item {
                RelationGroup("Parents ", relatives.parents, onMemberClick)
                RelationGroup(
                    "Spouse",
                    relatives.spouse?.let { listOf(it) } ?: emptyList(),
                    onMemberClick)
                RelationGroup("In-Laws", relatives.inLaws.distinct(), onMemberClick)
                RelationGroupWithSpouse("Siblings", relatives.siblings.distinct(), onMemberClick)
                RelationGroupWithSpouse("Children", relatives.children.distinct(), onMemberClick)
                RelationGroup("Grandchildren", relatives.grandchildren.distinct(), onMemberClick)
                RelationGroup("Grandparents ", relatives.grandParentsFather.distinct(), onMemberClick)
                RelationGroup("Grandparents ", relatives.grandParentsMother.distinct(), onMemberClick)
            }
            item {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(Modifier.height(16.dp))
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    CircleIconButton(
                        size = 60.dp,
                        iconRes = R.drawable.ic_timeline,
                        contentDescription = "Time line",
                        onClick = {
                            navController.navigate(route = MemberTimelineRoute(viewModel.memberId))
                        }
                    )

                    CircleIconButton(
                        size = 60.dp,
                        iconRes = R.drawable.ic_tree,
                        contentDescription = "See Ancestry",
                        onClick = {
                            navController.navigate(route = AncestryRoute(viewModel.memberId))
                        }
                    )

                    CircleIconButton(
                        size = 60.dp,
                        iconRes = R.drawable.ic_compare,
                        contentDescription = "Compare Members",
                        onClick = {
                            viewModel.resetSecondMemberDetails()
                            navController.navigate(route = MembersCompareRoute(viewModel.memberId))
                        }
                    )
                }
//                Button(
//                    onClick = {
//                        navController.navigate(route = AncestryRoute(viewModel.memberId))
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(50.dp)
//                ) {
//                    Text("See Ancestry".inHindi())
//                }
//                Spacer(Modifier.height(16.dp))
//                Button(
//                    onClick = {
//                        viewModel.resetSecondMemberDetails()
//                        navController.navigate(route = MembersCompareRoute(viewModel.memberId))
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(50.dp)
//                ) {
//                    Text("Compare Members".inHindi())
//                }
                // Admin options
                Spacer(Modifier.height(16.dp))
                if (isAdmin) {
                    Spacer(Modifier.height(32.dp))
                    Row {
                        Button(
                            onClick = {
                                navController.navigate(route = AddRelationRoute(viewModel.memberId))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text("Add Relation".inHindi())
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = {
                                viewModel.showDeleteRelationShipClearPopup()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text("Delete all relations".inHindi())
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.showDeleteMemberPopup()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("❌ "+"Delete Member".inHindi())
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
