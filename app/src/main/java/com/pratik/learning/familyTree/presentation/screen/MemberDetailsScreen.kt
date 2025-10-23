package com.pratik.learning.familyTree.presentation.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.navigation.AddRelationRoute
import com.pratik.learning.familyTree.navigation.AncestryRoute
import com.pratik.learning.familyTree.navigation.EditMemberRoute
import com.pratik.learning.familyTree.navigation.MemberDetailsRoute
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.MemberInfoSection
import com.pratik.learning.familyTree.presentation.component.RelationGroup
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.utils.isAdmin

@Composable
fun MemberDetailsScreen(navController: NavController, viewModel: MemberDetailsViewModel) {

    val onMemberClick: (FamilyMember) -> Unit = { member ->
        if (member.memberId != viewModel.memberId) {
            navController.navigate(route = MemberDetailsRoute(member.memberId))
        }
    }
    LaunchedEffect(Unit) {
        Log.d("DetailsScreen", "LaunchedEffect")
        viewModel.fetchDetails()
    }
    val member = viewModel.member.collectAsState().value
    val relations = viewModel.relations.collectAsState().value
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    Container(
        title = "Member Details",
        rightButton = {
            if (isAdmin)
                Text(
                    "Edit",
                    modifier = Modifier.clickable { navController.navigate(EditMemberRoute(viewModel.memberId)) },
                    style = MaterialTheme.typography.titleMedium
                )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            // 🧍 Member Info
            // Member Info Section
            item {
                MemberInfoSection(member)
                Divider(
                    color = dividerColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Relations
            item {
                RelationGroup("Parents ", relations.parents, onMemberClick)
                RelationGroup(
                    "Spouse",
                    relations.spouse?.let { listOf(it) } ?: emptyList(),
                    onMemberClick)
                RelationGroup("In-Laws", relations.inLaws, onMemberClick)
                RelationGroup("Siblings", relations.siblings, onMemberClick)
                RelationGroup("Children", relations.children, onMemberClick)
                RelationGroup("Grandchildren", relations.grandchildren, onMemberClick)
                RelationGroup("Grandparents ", relations.grandParentsFather, onMemberClick)
                RelationGroup("Grandparents ", relations.grandParentsMother, onMemberClick)
            }
            // Add Relation
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        navController.navigate(route = AncestryRoute(viewModel.memberId))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("See Ancestry")
                }
                Spacer(Modifier.height(16.dp))
                if (isAdmin) {
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            navController.navigate(route = AddRelationRoute(viewModel.memberId))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Add Relation")
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.deleteMember(navController)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("❌ Delete Member")
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}