package com.pratik.learning.familyTree.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.MemberInfoSectionSmall
import com.pratik.learning.familyTree.presentation.component.TimelineRow
import com.pratik.learning.familyTree.presentation.component.eventIcon
import com.pratik.learning.familyTree.presentation.viewmodel.AppViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.utils.HiText
import com.pratik.learning.familyTree.utils.inHindi


@Composable
fun AboutAppScreen(appVM: AppViewModel) {
    BackHandler {
        appVM.closeApp()
    }
    Container(
        title = HiText.TIMELINE,
        paddingValues = PaddingValues(horizontal = 16.dp, 0.dp),
        rightButton = {

        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("About App details")
        }
    }
}