package com.pratik.learning.familyTree.presentation

import androidx.annotation.Keep
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather

@Keep
sealed class UIState {
    data object IdealUIState : UIState()
    data object
    FilterExpandedUIState : UIState()
    data class ConfirmationUIState(val title: String, val message: String) : UIState()
    data class ExpandViewUIState(val member: MemberWithFather) : UIState()
}