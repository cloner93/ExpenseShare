package org.milad.expense_share.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.Group
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    groups: List<Group>,
    onGroupClick: (Group) -> Unit,
) {

    val totalOwe: Double = 123.12
    val totalOwed: Double = 321.21

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") }
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            BalanceSummaryRow(
                modifier = Modifier,
                totalOwe = totalOwe,
                totalOwed = totalOwed
            )

            GroupSection(groups = groups, onGroupClick = onGroupClick)
        }
    }
}

@Composable
fun BalanceSummaryRow(
    modifier: Modifier = Modifier,
    totalOwe: Double,
    totalOwed: Double,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BalanceCard(
            modifier = Modifier.weight(1f),
            title = "You owe",
            amount = totalOwe,
            backgroundColor = Color(0xFFFFE5E5),
            textColor = Color(0xFFD32F2F)
        )

        BalanceCard(
            modifier = Modifier.weight(1f),
            title = "You are owed",
            amount = totalOwed,
            backgroundColor = Color(0xFFE5FFEC),
            textColor = Color(0xFF2E7D32)
        )
    }
}

@Composable
private fun BalanceCard(
    modifier: Modifier,
    title: String,
    amount: Double,
    backgroundColor: Color,
    textColor: Color,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$${"%.2f".format(amount)}",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
@Composable
fun GroupSection(
    groups: List<Group>,
    onGroupClick: (Group) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Groups",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(groups) { group ->
                GroupItem(
                    group = group,
                    onClick = { onGroupClick(group) }
                )
            }
        }
    }
}

@Composable
private fun GroupItem(
    group: Group,
    onClick: () -> Unit,
) {
    val balanceColor = if (true) Color(0xFF2E7D32) else Color(0xFFD32F2F)
//    val balanceColor = if (group.isOwed) Color(0xFF2E7D32) else Color(0xFFD32F2F)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Image(
//                imageVector = vectorResource(Res.drawable.paris),
//                contentDescription = null,
//                modifier = Modifier
//                    .size(56.dp)
//                    .clip(RoundedCornerShape(12.dp)),
//                contentScale = ContentScale.Crop
//            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = "${group.members.size+1} members",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
                Text(
                    text = "group.balanceText",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = balanceColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Preview
@Composable
fun BalancePrev() {
}

