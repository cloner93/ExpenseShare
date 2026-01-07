package org.milad.expense_share.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProfileScreen(
//            viewModel: ProfileViewModel = koinViewModel()
    onLogout: () -> Unit,
) {
    Scaffold {paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Screen under construction",
                style = AppTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.primary,
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    onLogout()
                }
            ) {
                Text("Logout")
            }
        }
    }

}

@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(onLogout = {})
}