package com.pratik.learning.familyTree.presentation.component

import android.annotation.SuppressLint
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
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import com.pratik.learning.familyTree.data.local.dto.TimelineEvent
import com.pratik.learning.familyTree.data.local.dto.TimelineEventType
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.calculateAgeFromDob
import com.pratik.learning.familyTree.utils.formatIsoDate
import com.pratik.learning.familyTree.utils.getIcon
import com.pratik.learning.familyTree.utils.getSpouseRelation
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.logger
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun AnimatedAppLogo() {
    var atEnd by remember { mutableStateOf(false) }

    val animatedVector = AnimatedImageVector.animatedVectorResource(
        R.drawable.ic_family_tree_animated
    )

    Image(
        painter = rememberAnimatedVectorPainter(animatedVector, atEnd),
        contentDescription = "Family Tree Logo",
        modifier = Modifier.size(220.dp)
    )

    LaunchedEffect(Unit) {
        while (true) {
            atEnd = true
            delay(1400)
            atEnd = false
            delay(900)
        }
    }
}


@Composable
fun Container(
    title: String = "Family Tree",
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, 8.dp),
    isFilter: Boolean = false,
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
                    .padding(paddingValues), // Add horizontal padding for content
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                rightButton?.let {
                    it()
                }
            }
        }) { padding ->
        if (!isFilter)
            Column(Modifier.padding(padding)) {
                content()
            }
        else {
            Box(Modifier.padding(padding)) {
                content()
            }
        }
    }
}

@Composable
fun MemberSmallTile(
    title: String,
    description: String,
    onSinglePress: () -> Unit,
    onLongPress: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = 0.3f,
            stiffness = 350f
        ),
        label = "popScale"
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .background(MaterialTheme.colorScheme.background)
                .combinedClickable(
                    onClick = onSinglePress,
                    onLongClick = {
                        isPressed = true
                        scope.launch {
                            delay(300)
                            onLongPress()
                            isPressed = false
                        }
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Divider(
            color = MaterialTheme.colorScheme.surfaceVariant,
            thickness = 0.6.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
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
fun MemberInfoSectionSmall(
    member: MemberFormState,
    alignment: Alignment = Alignment.CenterEnd,
    relation: String = ""
) {
    val iconModifier = Modifier
        .size(18.dp)
        .padding(end = 6.dp)
    val textStyle =
        MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val roundedCornerShape = if (alignment == Alignment.CenterStart) RoundedCornerShape(
        bottomStart = 20.dp,
        bottomEnd = 20.dp
    )
    else RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = roundedCornerShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = member.fullName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
            )
            if (relation.isNotEmpty()) {
                Text(
                    text = relation,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
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
                    modifier = Modifier
                        .fillMaxWidth()
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
                                val relatedName =
                                    if (!member.husbandFullName.isNullOrEmpty()) "पति - श्री ${member.husbandFullName}" else if (!member.fatherFullName.isNullOrEmpty()) "पिता - श्री ${member.fatherFullName}" else ""
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
                                    .border(
                                        BorderStroke(1.dp, color = Color.LightGray),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = false) {},
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = "${it.key.fullName} \n ${it.key.city}",
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


@Composable
fun ThemedMiniSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = if (checked)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceContainerLow

    val thumbColor = MaterialTheme.colorScheme.onPrimary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = modifier
                .width(36.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(50))
                .background(trackColor)
                .border(1.dp, MaterialTheme.colorScheme.inversePrimary, RoundedCornerShape(14.dp))
                .clickable(
                    role = Role.Switch,
                    onClick = { onCheckedChange(!checked) }
                )
                .padding(2.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(
                        if (checked) Alignment.CenterEnd else Alignment.CenterStart
                    )
                    .background(thumbColor, CircleShape)
            )
        }
        Box(
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }

    }
}

@Composable
fun CircularCardCarousel(
    members: Map<FamilyMember, Pair<String, String>>,
    cardWidth: Dp = 320.dp,
    cardHeight: Dp = 200.dp
) {
    val realCount = members.size
    val virtualCount = Int.MAX_VALUE
    val startIndex = virtualCount / 2

    val listState = rememberLazyListState(startIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LazyRow(
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(virtualCount) { index ->
            val realIndex = (index % realCount + realCount) % realCount
            val member = members.keys.elementAt(realIndex)
            val relation = members.values.elementAt(realIndex)

            CarouselCard(
                member = member,
                relations = relation,
                index = index,
                listState = listState,
                width = cardWidth,
                height = cardHeight
            )
        }
    }
}

@SuppressLint("FrequentlyChangingValue")
@Composable
private fun CarouselCard(
    member: FamilyMember,
    relations: Pair<String, String>,
    index: Int,
    listState: LazyListState,
    width: Dp,
    height: Dp
) {
    val layoutInfo = listState.layoutInfo
    val viewportCenter =
        (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }

    val distanceFromCenter = itemInfo?.let {
        kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
    } ?: Int.MAX_VALUE

    val scale by animateFloatAsState(
        targetValue = if (distanceFromCenter < (itemInfo?.size ?: 0) / 2) 1f else 0.75f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = kotlin.math.abs(distanceFromCenter) * 0.03f
            }
            .zIndex(if (distanceFromCenter == 0) 1f else 0f)
            .width(width)
            .height(height)
            .background(
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            logger("relations card $relations")
            Text(
                text = "${getIcon(relations.first)} ${relations.first.inHindi()}",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = "${member.fullName} \n ${member.city}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )

            Text(
                text = "${getIcon(relations.second)} ${relations.second.inHindi()}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

//@Preview
@Composable
fun FilterView(
    onDismiss: () -> Unit = {},
    onSortBy: (String) -> Unit = {},
    currentSortBy: String = "IDUP",
    onMarriedStatusChanged: (Boolean) -> Unit = {},
    currentMarriedStatus: Boolean = true,
    onIsLeavingStatusChanged: (Boolean) -> Unit = {},
    currentIsLeavingStatus: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Intentionally empty – just consume clicks
            }
            .padding(20.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Image(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Close Filter",
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        onDismiss()
                    }
            )
        }

        Spacer(Modifier.height(28.dp))

        // Sort section
        SectionTitle("Sort By")

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 3
        ) {
            FilterChip(
                text = "By Name ⇑",
                value = "NAMEUP",
                selected = currentSortBy == "NAMEUP",
                { onSortBy(it) })
            FilterChip(
                text = "By Name ⇓",
                value = "NAMEDOWN",
                selected = currentSortBy == "NAMEDOWN",
                { onSortBy(it) })
            FilterChip(
                text = "By ID ⇑",
                value = "IDUP",
                selected = currentSortBy == "IDUP",
                { onSortBy(it) })
            FilterChip(
                text = "By ID ⇓",
                value = "IDDOWN",
                selected = currentSortBy == "IDDOWN",
                { onSortBy(it) })
        }

        Spacer(Modifier.height(28.dp))

        // Filter section
        SectionTitle("Filter By")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemedMiniSwitch(
                title = "Unmarried".inHindi(),
                checked = currentMarriedStatus,
                onCheckedChange = { onMarriedStatusChanged(it) }
            )


            ThemedMiniSwitch(
                title = "IsLeaving".inHindi(),
                checked = currentIsLeavingStatus,
                onCheckedChange = { onIsLeavingStatusChanged(it) }
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
fun FilterChip(
    text: String,
    value: String,
    selected: Boolean,
    onClick: (String) -> Unit = {}
) {
    val bgColor =
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant

    val textColor =
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant

    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick(value) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun CircleIconButton(
    iconRes: Int,
    contentDescription: String?,
    size: Dp = 40.dp,
    iconSize: Dp = (size.value * 0.65).dp,
    borderWidth: Dp = 1.5.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .border(
                width = borderWidth,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun TimelineRow(
    event: TimelineEvent,
    isFirst: Boolean,
    isLast: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // LEFT: Line + Dot
        Column(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(if (!isFirst) colorScheme.primary else Color.Transparent)
            )
            // Dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        color = eventDotColor(event.type, colorScheme),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(if (!isLast) colorScheme.primary else Color.Transparent)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // RIGHT: Event Card
        EventCard(event)
    }

}


@Composable
fun EventCard(event: TimelineEvent) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column {
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "${eventIcon(event.type)}  ${event.title}",
                    style = typography.titleMedium,
                    color = colorScheme.onSurface
                )

                if (event.subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.subtitle,
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                if (event.date.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = event.date,
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

fun eventIcon(type: TimelineEventType): String =
    when (type) {
        TimelineEventType.BIRTH -> "🎂"
        TimelineEventType.MARRIAGE -> "❤️"
        TimelineEventType.SON_BIRTH -> "👶"
        TimelineEventType.DAUGHTER_BIRTH -> "👧"
        TimelineEventType.DEATH -> "🕯️"
    }


fun eventDotColor(
    type: TimelineEventType,
    colorScheme: ColorScheme
): Color =
    when (type) {
        TimelineEventType.BIRTH ->
            colorScheme.primary

        TimelineEventType.MARRIAGE ->
            colorScheme.secondary

        TimelineEventType.SON_BIRTH,
        TimelineEventType.DAUGHTER_BIRTH ->
            colorScheme.tertiary

        TimelineEventType.DEATH ->
            colorScheme.outline
    }


@Preview(showBackground = false)
@Composable
fun Pre() {
    CircleIconButton(
        iconRes = R.drawable.ic_tree,
        contentDescription = "See Ancestry",
        onClick = {
//            navController.navigate(route = AncestryRoute(viewModel.memberId))
        }
    )

//    CircleIconButton(
//        iconRes = R.drawable.ic_compare,
//        contentDescription = "Compare Members",
//        onClick = {
////            viewModel.resetSecondMemberDetails()
////            navController.navigate(route = MembersCompareRoute(viewModel.memberId))
//        }
//    )
//    FilterView()
//    CommonRelativesItem("Pratik", "Father", "Mother")
}





