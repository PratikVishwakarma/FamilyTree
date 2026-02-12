package com.pratik.learning.familyTree.presentation.screen

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.room.util.TableInfo
import com.pratik.learning.familyTree.navigation.AddMember
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.FilterChip
import com.pratik.learning.familyTree.presentation.component.MemberInfoSectionSmall
import com.pratik.learning.familyTree.presentation.component.TimelineRow
import com.pratik.learning.familyTree.presentation.component.eventIcon
import com.pratik.learning.familyTree.presentation.viewmodel.AdminViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.AppViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.utils.HiText
import com.pratik.learning.familyTree.utils.inHindi


@Composable
fun AdminScreen(
    navController: NavController,
    appVM: AppViewModel,
    adminVM: AdminViewModel = hiltViewModel<AdminViewModel>()
) {
    val isLoggedIn by adminVM.isLoggedIn.collectAsState()

    BackHandler {
        appVM.closeApp()
    }
    Container(
        title = "Admin".inHindi(),
        paddingValues = PaddingValues(horizontal = 16.dp, 0.dp),
        rightButton = {

        }
    ) {
        if (isLoggedIn) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FilterChip(
                    text = "Add Member".inHindi(),
                    value = "",
                    selected = true,
                    onClick = { _ -> navController.navigate(route = AddMember) }
                )
                FilterChip(
                    text = "Log out".inHindi(),
                    value = "",
                    selected = true,
                    onClick = { _ -> adminVM.logOut(navController) }
                )
            }
        } else {


            var email by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }

            var emailError by remember { mutableStateOf<String?>(null) }
            var passwordError by remember { mutableStateOf<String?>(null) }

            fun validate(): Boolean {
                var isValid = true

                if (email.isBlank()) {
                    emailError = "Email cannot be empty"
                    isValid = false
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = "Invalid email format"
                    isValid = false
                } else {
                    emailError = null
                }

                if (password.isBlank()) {
                    passwordError = "Password cannot be empty"
                    isValid = false
                } else if (password.length <= 4) {
                    passwordError = "Password must be greater than 4 characters"
                    isValid = false
                } else {
                    passwordError = null
                }

                return isValid
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = { Text("Email") },
                    isError = emailError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                emailError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = { Text("Password") },
                    isError = passwordError != null,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                passwordError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                FilterChip(
                    text = "Login".inHindi(),
                    value = "",
                    selected = true,
                    onClick = { _ ->
                        if (validate()) {
                            adminVM.login(email, password)
//                            adminVM.login("pratik@mailinator.com", "krpratik1434")
                        }
                    }
                )
            }
        }
    }
}