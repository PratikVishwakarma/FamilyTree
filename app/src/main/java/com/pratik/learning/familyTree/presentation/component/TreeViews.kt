package com.pratik.learning.familyTree.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratik.learning.familyTree.data.local.dto.AncestorNode
import com.pratik.learning.familyTree.data.local.dto.DescendantNode
import com.pratik.learning.familyTree.data.local.dto.DualAncestorTree
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.utils.getCombinedName
import com.pratik.learning.familyTree.utils.logger

@Composable
fun DualFamilyTreeView(
    tree: DualAncestorTree,
    descendantNode: DescendantNode?,
    onMemberClick: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .horizontalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 🌳 Title
                    Text(
                        text = "🌳 परिवार वंश वृक्ष",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 💞 Current Couple
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (tree.spouse != null) "💞 वर्तमान दंपत्ति" else "वर्तमान सदस्य",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(8.dp))

                        if (tree.self != null) {
                            if (tree.spouse != null) {
                                CoupleCard(
                                    member = tree.self,
                                    spouse = tree.spouse,
                                    relationWithMember = ""
                                )
                            } else {
                                MemberCard(member = tree.self)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 👨👩 Ancestor Sides
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 👨 Paternal Side
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "👨 पितृ पक्ष",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            tree.paternalLineRoot?.let {
                                AncestorNodeViewWithLines(it, onMemberClick)
                            } ?: Text(
                                text = "— कोई डेटा नहीं —",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // 👩 Maternal Side
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "👩 मातृ पक्ष",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            tree.maternalLineRoot?.let {
                                AncestorNodeViewWithLines(it, onMemberClick)
                            } ?: Text(
                                text = "— कोई डेटा नहीं —",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // 🌿 Descendant Tree
                    descendantNode?.let {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "🌿 परिवार वंशज वृक्ष",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        HorizontalFamilyTree(
                            node = it,
                            onMemberClick = onMemberClick
                        )
                    }
                }
            }

}

@Composable
fun AncestorNodeViewWithLines(node: AncestorNode, onMemberClick: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        // Show current couple (merged card)
        CoupleCard(
            member = node.member,
            spouse = node.spouse,
            node.relationWithMember,
            onMemberClick
        )

        // Draw children (parents of current generation)
        if (node.parents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                for (parent in node.parents) {
                    AncestorNodeViewWithLines(parent, onMemberClick)
                }
            }
        }
    }
}


@Composable
fun MemberCard(member: FamilyMember, onMemberClick: ((Int) -> Unit)? = null) {
    val bgColor = if (member.isLiving) Color(0xFFE0F2F1) else Color(0xFFFFEBEE)
    val textColor = Color.Black

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .padding(4.dp)
            .widthIn(min = 160.dp)
            .shadow(3.dp, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .clickable {
                    if (onMemberClick != null) {
                        onMemberClick(member.memberId)
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = member.fullName,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                if (!member.isLiving) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "(स्वर्गवासी)",
                        fontSize = 12.sp,
                        color = Color(0xFFB71C1C),
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = if (member.gender == "M") "पुरुष" else "महिला",
                fontSize = 13.sp,
                color = textColor
            )
            if (member.city.isNotEmpty()) {
                Text(
                    text = member.city,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}


@Composable
fun CoupleCard(
    member: FamilyMember,
    spouse: FamilyMember?,
    relationWithMember: String,
    onMemberClick: ((Int) -> Unit)? = null
) {
    val background = Color(0xFFE7F0FF)
    val textColor = Color.Black

    Card(
        modifier = Modifier
            .padding(4.dp)
            .widthIn(min = 180.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        val combinedSurname = member.fullName.getCombinedName(spouse?.fullName ?: "")
        println("CoupleCard: combinedSurname: $combinedSurname")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = combinedSurname,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    onMemberClick?.let { it(member.memberId) }
                }
            )

            // Show relation
            if (relationWithMember.isNotEmpty()) {
                Text(relationWithMember, color = textColor.copy(alpha = 0.7f), fontSize = 13.sp)
            }

            // Show living status
            val isLiving = member.isLiving && (spouse?.isLiving != false)
            if (!isLiving) {
                Text(
                    text = "स्वर्गवासी",
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}



@Composable
fun DescendantTreeView(
    node: DescendantNode,
    onMemberClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = MaterialTheme.colorScheme.background

    Row(horizontalArrangement = Arrangement.Start) {
        // Vertical guide line
        VerticalDivider(
            thickness = 12.dp,
            color = dividerColor
        )
        if (node.level == 2) {
            Row(
                modifier = modifier
                    .background(backgroundColor)
                    .wrapContentWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // --- Current Member + Spouse ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    repeat((node.level - 1) * 2) { Text("⎯⎯", color = dividerColor) }

                    FamilyCoupleCard(node = node, onMemberClick = onMemberClick)
                }

                // --- Recursively draw children ---
                if (node.children.isNotEmpty()) {
                    node.children.forEach { child ->
                        DescendantTreeView(child, onMemberClick)
                    }
                }
            }
        } else {
            Column(
                modifier = modifier
                    .background(backgroundColor)
                    .wrapContentWidth()
                    .padding(vertical = 8.dp)
            ) {
                // --- Current Member + Spouse ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    repeat((node.level - 1) * 2) { Text("⎯⎯", color = dividerColor) }

                    FamilyCoupleCard(node = node, onMemberClick = onMemberClick)
                }

                // --- Recursively draw children ---
                if (node.children.isNotEmpty()) {
                    node.children.forEach { child ->
                        DescendantTreeView(child, onMemberClick)
                    }
                }
            }
        }
    }
}

@Composable
fun FamilyCoupleCard(
    node: DescendantNode,
    onMemberClick: ((Int) -> Unit)? = null
) {
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    val textColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .padding(4.dp)
            .wrapContentSize()
            .clickable(enabled = onMemberClick != null) {
                onMemberClick?.invoke(node.member.memberId)
            },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val combinedName = node.member.fullName.getCombinedName(node.spouse?.fullName ?: "")
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            // Couple Name
            Text(
                text = node.level.toString() + combinedName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Relation Label
            if (node.relationWithMember.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = node.relationWithMember,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HorizontalFamilyTree(
    node: DescendantNode,
    level: Int = 0,
    onMemberClick: (Int) -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 0.dp)
    ) {
        FamilyNodeCard(node, onMemberClick)
        logger("HorizontalFamilyTree: ${node.member.fullName}  total children: ${node.children.size}")
        // Bottom vertical line
        if (node.children.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(16.dp)
                    .background(lineColor)
            )
        }
        if (node.children.isNotEmpty()) {
            Spacer(Modifier.height(0.dp))


            // ─ Draw children inside a Box so we can measure their width
            Box {
//                var childrenWidth by remember { mutableFloatStateOf(0f) }
                val childrenWidth = if (node.children.size == 1) 2.dp else (node.children.size * 220).dp

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Horizontal line (only drawn once width is known)
                    if (childrenWidth > 0.dp) {
                        Canvas(
                            modifier = Modifier
                                .width(childrenWidth)
                                .height(2.dp)
                        ) {
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 3f
                            )
                        }
                    }

                    Spacer(Modifier.height(0.dp))

                    // Children Row that reports its width
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
//                                childrenWidth = coordinates.size.width.toFloat() / 2
                            }
                    ) {
                        node.children.forEach { child ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(lineColor)
                                )

                                HorizontalFamilyTree(
                                    node = child,
                                    level = level + 1,
                                    onMemberClick = onMemberClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FamilyNodeCard(node: DescendantNode, onMemberClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .wrapContentSize()
            .clickable { onMemberClick(node.member.memberId) },
        colors = CardDefaults.cardColors(
            containerColor = if (node.member.gender == "F")
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = node.member.fullName.getCombinedName(node.spouse?.fullName ?: ""),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (node.member.city.isNotEmpty()) {
                Text(
                    text = node.relationWithMember,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}