package com.pratik.learning.familyTree.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratik.learning.familyTree.utils.calculateAgeFromDob
import com.pratik.learning.familyTree.data.local.dto.AncestorNode
import com.pratik.learning.familyTree.data.local.dto.DualAncestorTree
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.utils.MemberFormState
import com.pratik.learning.familyTree.utils.formatIsoDate
import com.pratik.learning.familyTree.utils.getFirstName
import com.pratik.learning.familyTree.utils.getIcon
import com.pratik.learning.familyTree.utils.getRelationInHindi
import com.pratik.learning.familyTree.utils.getSurname


@Composable
fun Container(
    title: String = "Family Tree",
    rightButton: @Composable (() -> Unit)? = null, // Optional right button
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                // This modifier pushes the content down by the height of the status bar
                modifier = Modifier
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
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


@Composable
fun DualFamilyTreeViewOriginal(tree: DualAncestorTree, onMemberClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🌳 परिवार वंश वृक्ष",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 💞 Center Couple
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (tree.spouse!= null) "💞 वर्तमान दंपत्ति" else "वर्तमान", fontWeight = FontWeight.SemiBold, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                if (tree.self != null) {
                    if(tree.spouse != null)
                        CoupleCard(
                            member = tree.self,
                            spouse = tree.spouse,
                            relationWithMember = ""
                        )
                    else
                        MemberCard(member = tree.self)
                }
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 👨 Paternal Side
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👨 पितृ पक्ष", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Spacer(Modifier.height(8.dp))
                    tree.paternalLineRoot?.let { AncestorNodeViewWithLines(it, onMemberClick) }
                }

                // 👩 Maternal Side
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👩 मातृ पक्ष", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Spacer(Modifier.height(8.dp))
                    tree.maternalLineRoot?.let { AncestorNodeViewWithLines(it, onMemberClick) }
                }
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
            modifier = Modifier.padding(10.dp)
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
            if (!member.city.isNullOrEmpty()) {
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
        // 1. Extract details
        val memberFirstName = member.fullName.getFirstName()
        val memberSurname = member.fullName.getSurname()

        val spouseFirstName = spouse?.fullName?.getFirstName()
        val spouseSurname = spouse?.fullName?.getSurname()

        val combinedSurname = if (spouse != null && memberSurname == spouseSurname && memberSurname.isNotEmpty()) {
            memberSurname // Surnames match, display once
        } else {
            null // Surnames differ or no spouse, display full names
        }
        println("CoupleCard: memberSurname: $memberSurname, spouseSurname: $spouseSurname, combinedSurname: $combinedSurname")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = if (spouse != null)
                    "$memberFirstName 💞 $spouseFirstName"
                else
                    member.fullName,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    onMemberClick?.let { it(member.memberId) }
                }
            )
            if (spouse != null) {
                Text(
                    text = "$combinedSurname",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Show relation
            if (member.city?.isNotEmpty() == true) {
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
fun MemberInfoSection(member: MemberFormState) {
    val iconModifier = Modifier.size(18.dp).padding(end = 6.dp)
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)

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

        InfoRow("👤", "Gender: ${member.gender}", textStyle, iconModifier)
        InfoRow("🎂", "DOB: ${formatIsoDate(member.dob)} (${calculateAgeFromDob(member.dob)} years)", textStyle, iconModifier)
        if (!member.isLiving) {
            InfoRow("🕯️", "DOD: ${formatIsoDate(member.dod)}", textStyle, iconModifier)
        }
        InfoRow("📍", "City: ${member.city}", textStyle, iconModifier)
        if (member.mobile.isNotEmpty()) {
            InfoRow("📞", "Mobile: ${member.mobile}", textStyle, iconModifier)
        }
        InfoRow(
            if (member.isLiving) "💚" else "🕊️",
            "Status: ${if (member.isLiving) "Living" else "Deceased"}",
            textStyle, iconModifier
        )
    }
}

@Composable
fun InfoRow(icon: String, text: String, style: TextStyle, modifier: Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = style)
    }
}


@Composable
fun RelationGroup(
    title: String,
    members: List<Pair<String, FamilyMember>>,
    onMemberClick: (FamilyMember) -> Unit
) {
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
        members.forEachIndexed { index, member ->
            RelationItem(member, onMemberClick)
            if (index < members.lastIndex)
                Divider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 32.dp))
        }
    }
}


@Composable
fun RelationItem(member: Pair<String, FamilyMember>, onClick: (FamilyMember) -> Unit) {
    val icon = getIcon(member.first)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(member.second) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, style =/**/ MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = "${getRelationInHindi(member.first)} - ${member.second.fullName}", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = member.second.city?:"",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}
