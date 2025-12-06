package org.milad.expense_share.dashboard.groups.expense

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmb.common.ui.emptyState.EmptyListState
import model.Transaction
import model.User

@Composable
fun ExpenseList(expenses: List<Transaction>, currentUser: User?) {
    val grouped = expenses.groupBy { it.status }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }


    if (expenses.isNotEmpty()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            grouped.forEach { (label, list) ->
                item {
                    Text(
                        text = label.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(list) { item ->
                    ExpandableExpenseCard(
                        item,
                        currentUser,
                        isExpanded = selectedTransaction == item,
                        onExpandClick = {
                            if (selectedTransaction == it) {
                                selectedTransaction = null
                            } else
                                selectedTransaction = it
                        }

                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    } else {
        EmptyListState()
    }
}