package com.pratik.learning.familyTree.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.pratik.learning.familyTree.presentation.component.Container
import com.pratik.learning.familyTree.presentation.component.TopicTile
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MemberSearchScreen(
    onMemberSelected: (Pair<Int, String>) -> Unit,
    onAddMemberClick: () -> Unit,
    viewModel: MembersViewModel
) {

    val members = viewModel.filterResult.collectAsLazyPagingItems()
    val query by viewModel.query.collectAsState()

    Container(
        title = "Members",
        rightButton = {
            if (viewModel.relationType.isEmpty())
                Text(
                    "Add Member",
                    modifier = Modifier.clickable { onAddMemberClick() },
                    style = MaterialTheme.typography.titleMedium
                )
        }
    ) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = query, // type: String ✅
                onValueChange = { viewModel.onQueryChanged(it) }, // type: (String) -> Unit ✅
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            LazyColumn {
                items(members.itemCount) { index ->
                    val member = members[index]
                    member?.let {
                        val fatherName = member.fatherFullName ?: ""
                        val description =
                            if (fatherName.isNotEmpty()) "पिता - श्री $fatherName - ${member.city}" else member.city
                                ?: ""
                        TopicTile(
                            title = member.fullName,
                            description = description,
                            onClick = {
                                onMemberSelected(
                                    Pair(
                                        member.memberId,
                                        member.fullName
                                    )
                                )
                            }
                        )
                    }
                }
                members.apply {
                    when {
                        loadState.refresh is LoadState.Loading -> {
                            // Initial load
                            item { CircularProgressIndicator() }
                        }

                        loadState.append is LoadState.Loading -> {
                            // Pagination load
                            item { CircularProgressIndicator() }
                        }

                        loadState.append is LoadState.Error -> {
                            // Handle initial load error
                            item {
                                Text("Load error. Try again.")
                            }
                        }
                    }
                }
            }
        }
    }
}