package com.pratik.learning.familyTree.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.MemberRelationAR
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.navigation.MemberDetailsRoute
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.FilterChip
import com.pratik.learning.familyTree.presentation.component.MemberInfoSection
import com.pratik.learning.familyTree.presentation.component.MemberSearchPicker
import com.pratik.learning.familyTree.presentation.component.MemberSmallTile
import com.pratik.learning.familyTree.presentation.component.RectangleButton
import com.pratik.learning.familyTree.presentation.component.RelationGroup
import com.pratik.learning.familyTree.presentation.component.RelationGroupDivider
import com.pratik.learning.familyTree.presentation.component.RelationGroupWithSpouse
import com.pratik.learning.familyTree.presentation.viewmodel.AppViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.MySpaceUIState
import com.pratik.learning.familyTree.presentation.viewmodel.MySpaceViewModel
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.logger


@Composable
fun MySpaceScreen(
    membersViewModel: MembersViewModel = hiltViewModel<MembersViewModel>(),
    mySpaceVM: MySpaceViewModel = hiltViewModel<MySpaceViewModel>(),
    appVM: AppViewModel,
    navController: NavController
) {

    val onMemberClick: (Int) -> Unit = { memberId ->
        navController.navigate(route = MemberDetailsRoute(memberId))
    }

    val mySpaceMember by mySpaceVM.member.collectAsState()
    val relatives by mySpaceVM.relatives.collectAsState()
    var showPicker by remember { mutableStateOf(false) }
    val myFavMembers = mySpaceVM.favMember.collectAsLazyPagingItems()

    val uiState by mySpaceVM.uiState.collectAsStateWithLifecycle()

    BackHandler {
        appVM.closeApp()
    }
    Container(
        title = "My Space",
        paddingValues = PaddingValues(horizontal = 16.dp, 0.dp),
        rightButton = {

        }
    ) {
        if (showPicker) {
            MemberSearchPicker(
                title = "Select User",
                viewModel = membersViewModel,
                onMemberSelected = {
                    mySpaceVM.setMySpaceUserId(it.memberId)
                },
                onDismissRequest = { showPicker = false }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            if (mySpaceMember == null) {
                FilterChip(
                    text = "Select yourself".inHindi(),
                    value = "",
                    selected = true,
                    onClick = { _ ->
                        showPicker = true
                    }
                )
            } else {
                mySpaceMember?.let { it ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RectangleButton(
                                modifier = Modifier.height(48.dp).weight(1f),
                                text = "My Profile".inHindi(),
                                selected = uiState is MySpaceUIState.MyProfile,
                                onClick = {
                                    mySpaceVM.switchSection(MySpaceUIState.MyProfile())
                                }
                            )
                            VerticalDivider( color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), modifier = Modifier.height(48.dp), thickness = 1.dp)
                            RectangleButton(
                                modifier = Modifier.height(48.dp).weight(1f),
                                text = "My Relatives".inHindi(),
                                selected = uiState is MySpaceUIState.MyRelatives,
                                onClick = {
                                    mySpaceVM.switchSection(MySpaceUIState.MyRelatives())
                                }
                            )
                            VerticalDivider( color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), modifier = Modifier.height(48.dp), thickness = 1.dp)
                            RectangleButton(
                                modifier = Modifier.height(48.dp).weight(1f),
                                text = "My List".inHindi(),
                                selected = uiState is MySpaceUIState.MyFavList,
                                onClick = {
                                    mySpaceVM.switchSection(MySpaceUIState.MyFavList())
                                }
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        logger("MySpaceScreen UiState: $uiState")
                        when (uiState) {
                            is MySpaceUIState.MyProfile -> {
                                MyProfile(it, mySpaceVM.memberSmallBio, {
                                    mySpaceVM.setMySpaceUserId(-1)
                                })
                            }

                            is MySpaceUIState.MyRelatives -> {
                                MyRelatives(relatives, { memberId ->
                                    onMemberClick(memberId)
                                })
                            }

                            is MySpaceUIState.MyFavList -> {
                                MyFavMemberList(myFavMembers, { memberId ->
                                    onMemberClick(memberId)
                                }, mySpaceVM)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyProfile(memberDetails: FamilyMember, bio: String = "", onNotMeClick: () -> Unit, ) {
    LazyColumn {
        item {
            MemberInfoSection(
                MemberFormState(
                    fullName = memberDetails.fullName,
                    gotra = memberDetails.gotra,
                    dob = memberDetails.dob,
                    gender = memberDetails.gender,
                    isLiving = memberDetails.isLiving,
                    dod = memberDetails.dod,
                    city = memberDetails.city,
                    state = memberDetails.state,
                    mobile = memberDetails.mobile
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = bio.inHindi(),
                style = typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            FilterChip(
                text = "It's not me".inHindi(),
                value = "",
                selected = true,
                onClick = { _ ->
                    onNotMeClick()
                }
            )
        }
    }
}


@Composable
fun MyRelatives(relatives: MemberRelationAR, onMemberClick: (Int) -> Unit) {
    LazyColumn {
        item {
            RelationGroup("Parents ", relatives.parents, onMemberClick)
            RelationGroup(
                "Spouse",
                relatives.spouse?.let { listOf(it) } ?: emptyList(),
                onMemberClick)
            RelationGroupWithSpouse("Siblings", relatives.siblings.distinct(), onMemberClick)
            RelationGroupWithSpouse("Children", relatives.children.distinct(), onMemberClick)
            RelationGroup("father-grandparents", relatives.grandParentsFather.distinct(), onMemberClick)
            RelationGroup("Grandchildren", relatives.grandchildren.distinct(), onMemberClick)
            RelationGroupWithSpouse("father-uncle and aunt", relatives.uncleAuntFatherSide.distinct(), onMemberClick)

            if (relatives.grandParentsMother.isNotEmpty()) {
                RelationGroupDivider("mother side".inHindi())
                RelationGroup(
                    "mother-grandparents",
                    relatives.grandParentsMother.distinct(),
                    onMemberClick
                )
                RelationGroupWithSpouse(
                    "mother-uncle and aunt",
                    relatives.uncleAuntMotherSide.distinct(),
                    onMemberClick
                )
            }

            if (relatives.inLaws.isNotEmpty()) {
                RelationGroupDivider("in-laws side".inHindi())
                RelationGroup("In-Laws", relatives.inLaws.distinct(), onMemberClick)
                RelationGroupWithSpouse(
                    "Spouse siblings",
                    relatives.spouseSiblings.distinct(),
                    onMemberClick
                )
            }
        }
    }
}

@Composable
fun MyFavMemberList(members: LazyPagingItems<MemberWithFather>, onMemberClick: (Int) -> Unit, mySpaceVM: MySpaceViewModel) {
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
                    relation = mySpaceVM.toRelationMap[member.memberId] ?: "",
                    onSinglePress = {
                        onMemberClick(member.memberId)
                    },
                    onLongPress = {

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