package org.milad.expense_share.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun FriendsScreen(
//            viewModel: FriendsViewModel = koinViewModel(),
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()



    ListDetailPaneScaffold(
        modifier = Modifier.background(color = AppTheme.colors.background),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
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
            }
        },
        detailPane = {

        }
    )
}

@Preview
@Composable
fun FriendsScreenPreview() {
    FriendsScreen()
}