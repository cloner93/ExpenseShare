@file:OptIn(ExperimentalMaterial3Api::class)

package org.milad.expense_share.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.paris
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AddGroupScreen(
    onBackClick: () -> Unit,
    onAddClick: ( String,  List<Int>) -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    "Add Group", style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
        )
    }, bottomBar = {
        Button(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
            onClick = {
                onAddClick(
                    "Test Group",
                    listOf(1, 2, 3)
                )
            }
        ) {
            Text("Save")
        }

    }) { paddingValues ->
        Column(
            modifier = Modifier.padding(16.dp).padding(paddingValues).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = "",
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { },
                label = { Text("Group name (Trip, Dinner)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = false,
                supportingText = null
            )
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Members",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MemberRow("Liam")
                    MemberRow("Tom")
                }

                OutlinedButton(
                    onClick = { }) {
                    Icon(Icons.Default.Add, contentDescription = "Back")
                    Text("Add Member")
                }
            }
        }
    }
}

@Composable
private fun MemberRow(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(Res.drawable.paris),
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(36.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            modifier = Modifier.padding(horizontal = 8.dp).weight(1f), text = name
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = Color.Gray
        )
    }
}

@Preview
@Composable
fun AddGroupScreenPreview() {
//    AddGroupScreen({}) {}
}