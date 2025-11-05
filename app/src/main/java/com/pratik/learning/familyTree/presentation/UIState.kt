package com.pratik.learning.familyTree.presentation

import com.pratik.learning.familyTree.data.local.dto.MemberWithFather

sealed class UIState {
    data object IdealUIState : UIState()
    data class ConfirmationUIState(val title: String, val message: String) : UIState()
    data class ExpandViewUIState(val member: MemberWithFather) : UIState()
}