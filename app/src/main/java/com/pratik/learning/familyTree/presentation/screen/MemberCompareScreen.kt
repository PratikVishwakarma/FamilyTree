package com.pratik.learning.familyTree.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pratik.learning.familyTree.presentation.component.CircularCardCarousel
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.MemberInfoSectionSmall
import com.pratik.learning.familyTree.presentation.component.MemberSearchPicker
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel
import com.pratik.learning.familyTree.utils.inHindi


@Composable
fun MemberCompareScreen(
    memberDetailsViewModel: MemberDetailsViewModel,
    membersViewModel: MembersViewModel
) {
    val firstMember by memberDetailsViewModel.member.collectAsState()
    val secondMember by memberDetailsViewModel.secondMember.collectAsState()
    val commonRelatives by memberDetailsViewModel.commonRelatives.collectAsState()
    val membersBetweenRelations by memberDetailsViewModel.membersBetweenRelations.collectAsState()
    var showPicker by remember { mutableStateOf(false) }

    Container(
        title = "Compare Members".inHindi(),
        paddingValues = PaddingValues(horizontal = 16.dp, 0.dp),
        rightButton = {

        }
    ) {
        if (showPicker) {
            membersViewModel.relatedMembers =
                listOf(memberDetailsViewModel.memberId, memberDetailsViewModel.secondMemberId)
            MemberSearchPicker(
                title = "second Member",
                viewModel = membersViewModel,
                onMemberSelected = {
                    memberDetailsViewModel.secondMemberId = it.memberId
                    memberDetailsViewModel.fetchMemberDetails(isFirstMember = false)
                },
                onDismissRequest = { showPicker = false }
            )
        }
        Column(
            Modifier.fillMaxSize()
        ) {
            MemberInfoSectionSmall(firstMember, alignment = Alignment.CenterStart, membersBetweenRelations.first)
            if (memberDetailsViewModel.secondMemberId != -1) {
                if (!commonRelatives.isNullOrEmpty()) {
                    commonRelatives?.let { relatives ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
//                        VerticalDivider(Modifier
//                            .width(6.dp)
//                            .fillMaxHeight()
//                            .background(Color.Green, shape = CircleShape))
//                        LazyColumn(
//                            modifier = Modifier
//                                .matchParentSize()
//                                .padding(vertical = 0.dp, horizontal = 16.dp),
//                            verticalArrangement = Arrangement.Center,
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            item {
//                                CompareDivider(relatives)
//                            }
//                        }
                            CircularCardCarousel(members = relatives)
                        }
                    }
                }
                else
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No common relative found...",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                MemberInfoSectionSmall(secondMember, relation = membersBetweenRelations.second)
            } else {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        showPicker = true
                    },
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(horizontal = 20.dp)
                        .height(50.dp)
                ) {
                    Text("Select other member".inHindi())
                }
            }
        }
    }
}