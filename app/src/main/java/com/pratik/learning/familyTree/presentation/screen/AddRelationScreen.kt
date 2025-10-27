package com.pratik.learning.familyTree.presentation.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.MemberSearchPicker
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel
import com.pratik.learning.familyTree.utils.RelationFormState
import com.pratik.learning.familyTree.utils.getAvailableRelationsForGender
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.relationTextInHindi

@Composable
fun AddRelationScreen(
    membersViewModel: MembersViewModel,
    detailViewModel: MemberDetailsViewModel,
    navController: NavController,
) {
    var formState by remember { mutableStateOf(RelationFormState()) }
    val member = detailViewModel.member.collectAsState().value
    val error = detailViewModel.error.collectAsState().value
    val relationList = detailViewModel.relationList.collectAsState().value

    var expanded by remember { mutableStateOf(false) }

    var showPicker by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<MemberWithFather?>(null) }

    LaunchedEffect(selectedMember) {
        detailViewModel.checkRelationValidity(
            formState.relation,
            selectedMember
        )
        if (selectedMember?.memberId == detailViewModel.memberId)
            return@LaunchedEffect
        formState = formState.copy(
            relatedToFullName = selectedMember?.fullName ?: "",
            relatedToMemberId = selectedMember?.memberId ?: -1,
            relatesToFullName = member.fullName,
            relatesToMemberId = detailViewModel.memberId
        )
        detailViewModel.createRelation(formState)
    }

    Container(
        title = "Add Relation".inHindi(),
        rightButton = null
    ) {
        if (showPicker) {
            membersViewModel.relationType = formState.relation
            membersViewModel.relatedMembers = detailViewModel.getAllRelatedMemberIds()
            MemberSearchPicker(
                title = "${member.fullName}' ${formState.relation.relationTextInHindi()}",
                viewModel = membersViewModel,
                onMemberSelected = {
                    selectedMember = it
                },
                onDismissRequest = { showPicker = false }
            )
        }
        // Use a Column to hold the content, relying on the parent container for padding/Scaffold
        Column(
            modifier = Modifier
                .padding(16.dp) // Add default padding for inner content
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            // 1. Full Name
            Text(
                text = member.fullName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.height(16.dp))

            // 3. Gender Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if(formState.relation.isEmpty()) formState.relation else member.fullName+" "+formState.relation.relationTextInHindi(),
                    onValueChange = { /* Read-only value */ },
                    label = { Text("Relation".inHindi()) },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Relation",
                            Modifier.clickable { expanded = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    getAvailableRelationsForGender(member.gender).forEach { relation ->
                        DropdownMenuItem(
                            text = { Text(relation.relationTextInHindi()) },
                            onClick = {
                                if (relation == formState.relation) return@DropdownMenuItem
                                formState = formState.copy(relation = relation)
                                detailViewModel.checkRelationValidity(relation = relation)

                                selectedMember = null
                                formState = formState.copy(relatedToFullName = "")
                                formState = formState.copy(relatedToMemberId = -1)
                                expanded = false
                                detailViewModel.onClearRelationList()
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (formState.relation.isNotEmpty()) {

                OutlinedTextField(
                    value = selectedMember?.fullName ?: "Select Member".inHindi(),
                    onValueChange = { /* Read-only value */ },
                    label = { Text("Member") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Select Member",
                            Modifier.clickable {
                                showPicker = true
//                                navController.navigate(Home(formState.relation))
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (error.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }

            if (relationList.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column (horizontalAlignment = Alignment.Start){
                    Text("Relation Going to Add")
                    Spacer(Modifier.height(8.dp))
                    relationList.forEach { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Log.d("AddRelationScreen"," error: $error")
            // Save Relation
            if (formState.relatedToMemberId != -1 && formState.relation.isNotEmpty() && error.isEmpty()) {
                Button(
                    onClick = {
                        detailViewModel.checkRelationValidity(
                            formState.relation,
                            selectedMember
                        )

                        formState = formState.copy(
                            relatedToFullName = selectedMember?.fullName ?: "",
                            relatedToMemberId = selectedMember?.memberId ?: -1,
                            relatesToFullName = member.fullName,
                            relatesToMemberId = detailViewModel.memberId
                        )
                        detailViewModel.createRelation(formState, isCreate = true)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Save Relation")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}