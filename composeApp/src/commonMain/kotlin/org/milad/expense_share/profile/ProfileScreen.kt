package org.milad.expense_share.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.milad.expense_share.ui.AppScreenSize

@Composable
fun ProfileScreen(
    appScreenSize: AppScreenSize,
//            viewModel: ProfileViewModel = koinViewModel()
    onLogout: () -> Unit,
) {
    Scaffold {paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Profile & Settings")
            Spacer(Modifier.height(16.dp))


            Spacer(Modifier.height(32.dp))

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
