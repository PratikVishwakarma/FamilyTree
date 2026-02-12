package com.pratik.learning.familyTree.presentation.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pratik.learning.familyTree.FamilyTreeApp.Companion.isAdmin
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.navigation.Home
import com.pratik.learning.familyTree.utils.SyncPrefs.getIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: FamilyTreeRepository,
    @ApplicationContext val context: Context
): BaseViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        _isLoggedIn.value = getCurrentUser() != null
    }


    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun login(email: String, password: String) {
        logger("login initiated for email: $email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                _isLoggedIn.value = it.isSuccessful
                if (it.isSuccessful) {
                    // do nothing move to
                    isAdmin = true
                } else {
                    logger("signInWithEmail:failure: ${it.exception}")
                    Toast.makeText(context, "Incorrect Email or Password", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun logOut(navController: NavController) {
        viewModelScope.launch {
            if (!getIsDataUpdateRequired(context)) {
                auth.signOut()
                _isLoggedIn.value = false
                isAdmin = false
                navController.navigate(route = Home)
            } else {
                repository.syncDataToFirebase()
                Toast.makeText(context, "Please save data before logging out", Toast.LENGTH_LONG).show()
            }
        }
    }
}