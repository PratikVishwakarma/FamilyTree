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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.genders
import com.pratik.learning.familyTree.utils.showDatePicker

@Composable
fun AddMemberScreen(
    viewModel: MembersViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var formState by remember { mutableStateOf(MemberFormState()) }

    var expanded by remember { mutableStateOf(false) }

    // Helper lambda to launch the date picker dialog and update state
    val openDatePicker: (Boolean) -> Unit = { isDob ->
        showDatePicker(context) { newDate ->
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
                .padding(16.dp) // Add default padding for inner content
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // 1. Full Name
            OutlinedTextField(
                value = formState.fullName,
                onValueChange = { formState = formState.copy(fullName = it) },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // 2. Date of Birth (Clickable to open Date Picker)
            OutlinedTextField(
                value = formState.dob,
                onValueChange = { /* Read-only field */ },
                label = { Text("Date of Birth") },
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

            // 3. Gender Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formState.gender,
                    onValueChange = { /* Read-only value */ },
                    label = { Text("Gender") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Gender",
                            Modifier.clickable { expanded = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    genders.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender) },
                            onClick = {
                                formState = formState.copy(gender = gender)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // 4. Living Status Toggle
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
                    onValueChange = { /* Read-only field */ },
                    label = { Text("Date of Death") },
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
                onValueChange = { formState = formState.copy(city = it) },
                label = { Text("City / Place") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // 7. Mobile
            OutlinedTextField(
                value = formState.mobile,
                onValueChange = { formState = formState.copy(mobile = it.filter { it.isDigit() }) },
                label = { Text("Mobile Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
            Spacer(Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.addMember(formState, navController)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save Member")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
