package com.pratik.learning.familyTree.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pratik.learning.familyTree.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val auth: FirebaseAuth
): ViewModel() {
    private val _closeAppEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val closeAppEvent = _closeAppEvent.asSharedFlow()

    fun closeApp() {
        logger("closeApp called")
        viewModelScope.launch {
            _closeAppEvent.emit(Unit)
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}