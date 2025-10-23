package com.pratik.learning.familyTree.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.utils.SyncPrefs.getIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.SyncPrefs.setIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.SyncPrefs.shouldSync
import com.pratik.learning.familyTree.utils.isAdmin
import com.pratik.learning.familyTree.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val familyTreeRepository: FamilyTreeRepository,
    @ApplicationContext val context: Context
) : ViewModel() {

    var relationType = ""

    private val _isDataLoaded = MutableStateFlow(false)
    var isDataLoaded: StateFlow<Boolean> = _isDataLoaded

    private val _error = MutableStateFlow("")
    var error: StateFlow<String> = _error

    private val _isInternetRequired = MutableStateFlow(false)
    val isInternetRequired = _isInternetRequired.asStateFlow()

    private suspend fun checkInternetAndSync() {
        val isRequired = familyTreeRepository.isNoDataAndNoInternet()
        logger("checkInternetAndSync", "isRequired = $isRequired")
        if (isRequired) {
            _isInternetRequired.value = true
        } else {
            _isInternetRequired.value = false
            if (shouldSync(context) && familyTreeRepository.verifyInternetAccess()) {
                Log.d("MembersViewModel", "Downloading data from server is required")
                downloadDataFromServer()
            } else {
                delay(3000)
                _isDataLoaded.value = true
            }
        }
    }

    fun retryDataSync() {
        _isInternetRequired.value = false
        viewModelScope.launch {
            checkInternetAndSync()
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("MembersViewModel", "init called")
            if (isAdmin && getIsDataUpdateRequired(context) && familyTreeRepository.verifyInternetAccess()) {
                Log.d("MembersViewModel", "Uploading data to server is required")
                uploadDataOnServer()
                setIsDataUpdateRequired(context, false)
            }
            checkInternetAndSync()
        }
    }

    private fun uploadDataOnServer() {
        viewModelScope.launch(Dispatchers.IO) {
            familyTreeRepository.syncDataToFirebase()
        }
    }

    private fun downloadDataFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            _isDataLoaded.value = familyTreeRepository.downloadDataFromServer()
        }
    }

}

