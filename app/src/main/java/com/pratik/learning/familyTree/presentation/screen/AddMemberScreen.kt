package com.pratik.learning.familyTree.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.genders
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.showDatePicker
import com.pratik.learning.familyTree.utils.states

@Composable
fun AddMemberScreen(
    viewModel: MembersViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var formState by remember { mutableStateOf(MemberFormState()) }

    var genderExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }
    val error = viewModel.error.collectAsState().value
    // Helper lambda to launch the date picker dialog and update state
    val openDatePicker: (Boolean) -> Unit = { isDob ->
        showDatePicker(context, date = if (isDob) formState.dob else formState.dod, maxDate = if (isDob) "" else formState.dob) { newDate ->
            formState = if (isDob) {
                formState.copy(dob = newDate)
            } else {
                formState.copy(dod = newDate)
            }
        }
    }
    Container(
        title = "Member Details",
        rightButton = null
    ) {
        // Use a Column to hold the content, relying on the parent container for padding/Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // 1. Full Name
            OutlinedTextField(
                value = formState.fullName,
                maxLines = 1,
                singleLine = true,
                onValueChange = { formState = formState.copy(fullName = it) },
                label = { Text("Full Name".inHindi()) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            // 1. Gotra
            OutlinedTextField(
                value = formState.gotra,
                maxLines = 1,
                singleLine = true,
                onValueChange = { formState = formState.copy(gotra = it) },
                label = { Text("Gotra".inHindi()) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // 3. Gender Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formState.gender.inHindi(),
                    onValueChange = { /* Read-only value */ },
                    maxLines = 1,
                    singleLine = true,
                    label = { Text("Gender") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Gender",
                            Modifier.clickable { genderExpanded = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genders.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender.inHindi()) },
                            onClick = {
                                formState = formState.copy(gender = gender)
                                genderExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            // Date of Birth (Clickable to open Date Picker)
            OutlinedTextField(
                value = formState.dob,
                maxLines = 1,
                singleLine = true,
                onValueChange = { /* Read-only field */ },
                label = { Text("Date of Birth".inHindi()) },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Open Date Picker",
                        Modifier.clickable { openDatePicker(true) }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            // Living Status Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Living Status", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = formState.isLiving,
                    onCheckedChange = { isLiving ->
                        formState = formState.copy(
                            isLiving = isLiving,
                            dod = if (isLiving) "" else formState.dod
                        )
                    }
                )
            }
            Spacer(Modifier.height(16.dp))

            // 5. Date of Death (Conditional and Clickable Field)
            if (!formState.isLiving) {
                OutlinedTextField(
                    value = formState.dod,
                    maxLines = 1,
                    onValueChange = { /* Read-only field */ },
                    label = { Text("Date of Death".inHindi()) },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Open Date Picker",
                            Modifier.clickable { openDatePicker(false) }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }

            // 6. City
            OutlinedTextField(
                value = formState.city,
                maxLines = 1,
                singleLine = true,
                onValueChange = { formState = formState.copy(city = it) },
                label = { Text("City / Place") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // 7. State Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formState.state,
                    onValueChange = { /* Read-only value */ },
                    label = { Text("State") },
                    readOnly = true,
                    maxLines = 1,
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select State",
                            Modifier.clickable { stateExpanded = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = stateExpanded,
                    onDismissRequest = { stateExpanded = false }
                ) {
                    states.forEach { state ->
                        DropdownMenuItem(
                            text = { Text(state) },
                            onClick = {
                                formState = formState.copy(state = state)
                                stateExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // 8. Mobile
            OutlinedTextField(
                value = formState.mobile,
                onValueChange = { formState = formState.copy(mobile = it.filter { text -> text.isDigit() }) },
                label = { Text("Mobile Number".inHindi()) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true,
            )
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
            Spacer(Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.addMember(formState, navController)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Add Member".inHindi())
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
