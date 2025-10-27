package com.pratik.learning.familyTree.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.utils.genders
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.logger
import com.pratik.learning.familyTree.utils.showDatePicker
import com.pratik.learning.familyTree.utils.states


@Composable
fun EditMemberScreen(
    viewModel: MemberDetailsViewModel,
    navController: NavController
) {

    val error = viewModel.error.collectAsState().value
    LaunchedEffect(Unit) {
        logger("EditMemberScreen", "LaunchedEffect")
        viewModel.fetchDetails()
    }

    val context = LocalContext.current
    val formState = viewModel.member.collectAsState().value

    var genderExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }

    // Helper lambda to launch the date picker dialog and update state
    val openDatePicker: (Boolean) -> Unit = { isDob ->
        showDatePicker(context, date = if (isDob) formState.dob else formState.dod, maxDate = if (isDob) "" else formState.dob) { newDate ->
            if (isDob) {
                viewModel.onDOBChanged(newDate)
            } else {
                viewModel.onDODChanged(newDate)
            }
        }
    }

    Container(
        title = "Edit Member Details",
        rightButton = null
    ) {
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
                onValueChange = { viewModel.onFullNameChanged(it)},
                label = { Text("Full Name".inHindi()) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Gotra
            OutlinedTextField(
                value = formState.gotra,
                maxLines = 1,
                singleLine = true,
                onValueChange = { viewModel.onGotraChanged(it)},
                label = { Text("Gotra".inHindi()) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Gender Dropdown
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
                                viewModel.onGenderChanged(gender)
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
                        viewModel.onIsLivingStatusChanges(isLiving)
                        viewModel.onDODChanged(if (isLiving) "" else formState.dod)
                    }
                )
            }
            Spacer(Modifier.height(16.dp))

            // 5. Date of Death (Conditional and Clickable Field)
            if (!formState.isLiving) {
                OutlinedTextField(
                    value = formState.dod,
                    maxLines = 1,
                    singleLine = true,
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
                onValueChange = {
                    viewModel.onCityChanged(it)
                },
                label = { Text("City / Place") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // 7. State Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formState.state,
                    maxLines = 1,
                    singleLine = true,
                    onValueChange = { /* Read-only value */ },
                    label = { Text("State") },
                    readOnly = true,
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
                                viewModel.onStateChanged(state)
                                stateExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = formState.mobile,
                maxLines = 1,
                singleLine = true,
                onValueChange = {
                    viewModel.onMobileChanged(it)
                },
                label = { Text("Mobile Number".inHindi()) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
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
                    viewModel.updateMember(navController)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Update Member")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}


