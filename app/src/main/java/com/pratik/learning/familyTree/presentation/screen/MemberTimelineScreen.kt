package com.pratik.learning.familyTree.presentation.screen

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
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.utils.HiText
import com.pratik.learning.familyTree.utils.inHindi


@Composable
fun MemberTimelineScreen(
    memberDetailsViewModel: MemberDetailsViewModel
) {
    val firstMember by memberDetailsViewModel.member.collectAsState()
    val timeline by memberDetailsViewModel.memberTimeline.collectAsState()
    val memberBio by memberDetailsViewModel.memberBio.collectAsState()

    Container(
        title = HiText.TIMELINE,
        paddingValues = PaddingValues(horizontal = 16.dp, 0.dp),
        rightButton = {

        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                MemberInfoSectionSmall(firstMember)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = memberBio,
                    style = typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(6.dp))
            }
            itemsIndexed(timeline) { index, event ->
                TimelineRow(
                    event = event,
                    isFirst = index == 0,
                    isLast = index == timeline.lastIndex
                )
            }
        }

    }
}