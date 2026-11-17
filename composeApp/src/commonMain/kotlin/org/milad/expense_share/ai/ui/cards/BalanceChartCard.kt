package org.milad.expense_share.ai.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*

/**
 * BalanceChartCard:
 * Native A2UI component rendering financial balance overview and charts directly in chat.
 */
@Composable
fun BalanceChartCard(
    summary: String,
    data: JsonObject,
    modifier: Modifier = Modifier
) {
    val title = data["title"]?.jsonPrimitive?.contentOrNull ?: summary.ifBlank { "نمودار وضعیت بدهی‌ها و بستانکاری‌ها" }
    val totalBalance = data["totalBalance"]?.jsonPrimitive?.longOrNull ?: 0L
    val balancesArray = data["balances"]?.jsonArray ?: JsonArray(emptyList())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (balancesArray.isNotEmpty()) {
                balancesArray.forEach { item ->
                    val itemObj = item.jsonObject
                    val name = itemObj["name"]?.jsonPrimitive?.contentOrNull ?: "عضو"
                    val amount = itemObj["amount"]?.jsonPrimitive?.longOrNull ?: 0L
                    val isPositive = amount >= 0

                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (isPositive) "+" else ""}$amount تومان",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            val fraction = (kotlin.math.abs(amount).toFloat() / 1000000f).coerceIn(0.1f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isPositive) Color(0xFF4CAF50) else Color(0xFFE53935))
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "وضعیت کل حساب‌ها: ${if (totalBalance >= 0) "بستانکار" else "بدهکار"} ($totalBalance تومان)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
