package com.pratik.learning.familyTree.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.pratik.learning.familyTree.R
import com.pratik.learning.familyTree.data.local.dto.ChildWithSpouseDto
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.calculateAgeFromDob
import com.pratik.learning.familyTree.utils.formatIsoDate
import com.pratik.learning.familyTree.utils.getIcon
import com.pratik.learning.familyTree.utils.getSpouseRelation
import com.pratik.learning.familyTree.utils.inHindi
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun Container(
    title: String = "Family Tree",
    modifier: Modifier = Modifier,
    rightButton: @Composable (() -> Unit)? = null, // Optional right button
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                // This modifier pushes the content down by the height of the status bar
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer) // Use a background color
                    .windowInsetsPadding(WindowInsets.statusBars) // <-- THE CRITICAL MODIFIER
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ), // Add horizontal padding for content
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                rightButton?.let {
                    it()
                }
            }
        }) { padding ->
        Column(Modifier.padding(padding)) {
            content()
        }
    }
}

@Composable
fun TopicTile(
    title: String,
    description: String,
    onSinglePress: () -> Unit,
    onLongPress: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Smooth pop animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f,    // makes it bouncy
            stiffness = 300f
        ),
        label = "popScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .scale(scale)
            .combinedClickable(
                onClick = onSinglePress,
                onLongClick = {
                    isPressed = true
                    onLongPress()

                    // launch coroutine safely from event handler
                    scope.launch {
                        delay(150)
                        isPressed = false
                    }
                }
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun InfoRow(icon: String, text: String, style: TextStyle, modifier: Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = style)
    }
}

@Composable
fun MemberInfoSection(member: MemberFormState) {
    val iconModifier = Modifier
        .size(18.dp)
        .padding(end = 6.dp)
    val textStyle =
        MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = member.fullName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        InfoRow("🌳", "${"Gotra".inHindi()}: ${member.gotra}", textStyle, iconModifier)
        InfoRow("👤", "Gender: ${member.gender.inHindi()}", textStyle, iconModifier)
        InfoRow(
            "🎂",
            "${"DOB".inHindi()}: ${formatIsoDate(member.dob)} (${calculateAgeFromDob(member.dob)} years)",
            textStyle,
            iconModifier
        )
        if (!member.isLiving) {
            InfoRow(
                "🕯️",
                "${"DOD".inHindi()}: ${formatIsoDate(member.dod)}",
                textStyle,
                iconModifier
            )
        }
        InfoRow("📍", "Place: ${member.city}", textStyle, iconModifier)
        if (member.mobile.isNotEmpty()) {
            InfoRow("📞", "Mobile: ${member.mobile}", textStyle, iconModifier)
        }
        InfoRow(
            if (member.isLiving) "💚" else "🕊️",
            "Status: ${if (member.isLiving) "Living".inHindi() else "Deceased".inHindi()}",
            textStyle, iconModifier
        )
    }
}


@Composable
fun MemberInfoSectionSmall(member: MemberFormState, alignment: Alignment = Alignment.CenterEnd, relation: String = "") {
    val iconModifier = Modifier
        .size(18.dp)
        .padding(end = 6.dp)
    val textStyle =
        MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)

    Box(modifier =  Modifier
        .fillMaxWidth()
        .padding(12.dp),
        contentAlignment = alignment
    ){
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = member.fullName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = relation,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            InfoRow("🌳", "${"Gotra".inHindi()}: ${member.gotra}", textStyle, iconModifier)
        }
    }

}


@Composable
fun MemberInfoOverlay(
    member: MemberWithFather,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Trigger bounce when dialog first appears
    LaunchedEffect(Unit) {
        visible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        // Dark background (no blur)
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Bounce animation
            AnimatedVisibility(visible = visible) {
                val scale by animateFloatAsState(
                    targetValue = if (visible) 1f else 0.8f,
                    animationSpec = spring(
                        dampingRatio = 0.5f,
                        stiffness = 250f
                    ),
                    label = "overlayScale",
                    finishedListener = {
                        coroutineScope.launch {
                            onDismiss()
                        }
                    }
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight()
                        .scale(scale),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MemberInfoSection(
                            MemberFormState(
                                fullName = member.fullName,
                                gotra = member.gotra,
                                dob = member.dob,
                                gender = member.gender,
                                isLiving = member.isLiving,
                                dod = member.dod,
                                city = member.city,
                                state = member.state,
                                mobile = member.mobile
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                visible = false
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RelationGroup(
    title: String,
    members: List<Pair<String, FamilyMember>>,
    onMemberClick: (Int) -> Unit
) {
    println("RelationGroup: $title  members: $members")
    if (members.isEmpty()) return

    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val labelColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        members.sortedBy { it.second.dob }.forEachIndexed { index, member ->
            RelationItem(
                relation = member.first,
                memberName = member.second.fullName,
                city = member.second.city,
                memberId = member.second.memberId,
                onMemberClick
            )
            if (index < members.lastIndex)
                HorizontalDivider(
                    modifier = Modifier.padding(start = 32.dp),
                    thickness = 0.5.dp,
                    color = dividerColor
                )
        }
    }
}


@Composable
fun RelationGroupWithSpouse(
    title: String,
    members: List<Pair<String, ChildWithSpouseDto>>,
    onMemberClick: (Int) -> Unit
) {
    println("RelationGroup: $title  members: $members")
    if (members.isEmpty()) return

    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val labelColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        members.sortedBy { it.second.child.dob }.forEachIndexed { index, member ->
            RelationItem(
                relation = member.first,
                memberName = member.second.child.fullName,
                city = member.second.child.city,
                memberId = member.second.child.memberId,
                onMemberClick
            )
            member.second.spouseId?.let { spouseId ->
                Spacer(modifier = Modifier.height(4.dp))
                RelationItem(
                    relation = member.first.getSpouseRelation(),
                    memberName = member.second.spouseFullName ?: "",
                    city = member.second.child.city,
                    memberId = spouseId,
                    onMemberClick
                )
            }
            if (index < members.lastIndex)
                HorizontalDivider(
                    modifier = Modifier.padding(start = 32.dp),
                    thickness = 0.5.dp,
                    color = dividerColor
                )
        }
    }
}


@Composable
fun RelationItem(
    relation: String,
    memberName: String,
    city: String,
    memberId: Int,
    onClick: (Int) -> Unit
) {
    val icon = getIcon(relation)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(memberId) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "${relation.inHindi()} - $memberName",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = city,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}


@Composable
fun NoInternetScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E88E5))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_no_internet), // add icon in drawable
                contentDescription = "No Internet",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Internet Connection, Please check your connection",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}


@Composable
fun ConfirmationPopup(
    title: String = "Confirm Action",
    message: String,
    confirmText: String = "Yes".inHindi(),
    cancelText: String = "Cancel".inHindi(),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isConfirmed by remember { mutableStateOf(false) }
    val tickAlpha by animateFloatAsState(targetValue = if (isConfirmed) 1f else 0f)

    // 🕒 Auto-dismiss after animation completes
    LaunchedEffect(isConfirmed) {
        if (isConfirmed) {
            delay(1600) // animation duration + short pause
            onConfirm()
            onDismiss()
        }
    }

    Dialog(onDismissRequest = { if (!isConfirmed) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isConfirmed,
                    contentAlignment = Alignment.Center
                ) { confirmed ->
                    if (confirmed) {
                        TickAnimation(
                            modifier = Modifier.size(90.dp),
                            alpha = tickAlpha
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(cancelText)
                                }
                                Button(
                                    onClick = { isConfirmed = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF4CAF50
                                        )
                                    )
                                ) {
                                    Text(confirmText, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TickAnimation(
    modifier: Modifier = Modifier,
    alpha: Float
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = LinearOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier.alpha(alpha)) {
        val start = Offset(size.width * 0.25f, size.height * 0.55f)
        val mid = Offset(size.width * 0.45f, size.height * 0.75f)
        val end = Offset(size.width * 0.75f, size.height * 0.35f)

        val total = 2f
        val phase = progress.value * total

        if (phase <= 1f) {
            val x = start.x + (mid.x - start.x) * phase
            val y = start.y + (mid.y - start.y) * phase
            drawLine(
                color = Color(0xFF4CAF50),
                start = start,
                end = Offset(x, y),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
        } else {
            drawLine(
                color = Color(0xFF4CAF50),
                start = start,
                end = mid,
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
            val secondPhase = phase - 1f
            val x = mid.x + (end.x - mid.x) * secondPhase
            val y = mid.y + (end.y - mid.y) * secondPhase
            drawLine(
                color = Color(0xFF4CAF50),
                start = mid,
                end = Offset(x, y),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
        }
    }
}


@Composable
fun MemberSearchPicker(
    title: String = "Search Members",
    viewModel: MembersViewModel,
    onMemberSelected: (MemberWithFather) -> Unit,
    onDismissRequest: () -> Unit
) {
    val query by viewModel.query.collectAsState()
    val pagingItems = viewModel.filterResult.collectAsLazyPagingItems()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        // wait for first frame
        awaitFrame()
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    // Background overlay (semi-transparent)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .clickable(onClick = onDismissRequest)
    ) {
        // Card for picker
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) {},
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Header title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Search field
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.onQueryChanged(it) },
                    label = { Text("Search by name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(focusRequester)
                        .focusable(true),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search results list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    items(pagingItems.itemCount) { index ->
                        pagingItems[index]?.let { member ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onMemberSelected(member)
                                        onDismissRequest()
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    member.fullName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val relatedName = if(!member.husbandFullName.isNullOrEmpty()) "पति - श्री ${member.husbandFullName}" else if(!member.fatherFullName.isNullOrEmpty()) "पिता - श्री ${member.fatherFullName}" else ""
                                val description =
                                    if (relatedName.isNotEmpty()) "$relatedName - ${member.city}" else member.city
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }

                    // Handle Paging states
                    pagingItems.apply {
                        when {
                            loadState.refresh is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            loadState.append is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            loadState.refresh is LoadState.Error -> {
                                item {
                                    Text(
                                        text = "Failed to load members",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommonRelativesItem(
    memberName: String,
    firstRelation: String,
    secondRelation: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    text = "${getIcon(firstRelation)} ${firstRelation.inHindi()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = memberName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${getIcon(secondRelation)} ${secondRelation.inHindi()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun CompareDivider(relatives: Map<FamilyMember, Pair<String, String>>) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            relatives.entries.forEachIndexed { index, it ->
                key(it.key) {
                    var visible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(index * 200L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = scaleIn(
                            initialScale = 0.2f,
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(animationSpec = tween(180)),
                        exit = ExitTransition.None
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${getIcon(it.value.first)} ${it.value.first.inHindi()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentHeight()
                                    .padding(12.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(BorderStroke(1.dp, color = Color.LightGray), shape = RoundedCornerShape(8.dp))
                                    .clickable(enabled = false) {},
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = it.key.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            Text(
                                text = "${it.value.second.inHindi()} ${getIcon(it.value.second)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                  Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}


@Preview(showBackground = false)
@Composable
fun Pre() {
    CommonRelativesItem("Pratik", "Father", "Mother")
}





