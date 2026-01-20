package org.milad.expense_share.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmb.common.loading.FullScreenLoading
import com.pmb.common.theme.AppTheme
import com.pmb.common.ui.emptyState.EmptyListState
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.paris
import kotlinx.serialization.Serializable
import model.User
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.milad.expense_share.dashboard.group.components.FakeDate.userNiloufar
import org.milad.expense_share.dashboard.group.components.FakeDate.userReza
import org.milad.expense_share.dashboard.group.components.GroupDropdownMenu

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = koinViewModel(),
) {
    val state by viewModel.viewState.collectAsState()

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()
    val loading by mutableStateOf(false)

    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded && navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
    val isDetailVisible =
        state.isDetailVisible || navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

    ListDetailPaneScaffold(
        modifier = Modifier.background(color = AppTheme.colors.background),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Friends(
                    friends = state.friends.map { Friend(it, FriendRelationStatus.ACCEPTED) })
                if (loading) FullScreenLoading()
            }
        },
        detailPane = {

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Friends(friends: List<Friend>) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("Friends name") }, navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }, actions = {
                GroupDropdownMenu(
                    isGroupOwner = true, onAction = {})
            })
        }, floatingActionButton = {}) {
            FriendsContent(
                modifier = Modifier.padding(it), friends = friends
            )
        }
    }
}

@Composable
fun FriendsContent(modifier: Modifier, friends: List<Friend>) {

    if (friends.isNotEmpty()) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(friends) { item ->
                FriendRow(
                    user = item,
                    isSelected = item.user == userReza,
                    isOpened = item.user == userNiloufar
                )
            }
        }
    } else {
        EmptyListState()
    }
}

@Composable
fun FriendRow(user: Friend, isSelected: Boolean = false, isOpened: Boolean = false) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { selected = isSelected }
            .clip(CardDefaults.shape)
            .combinedClickable(
                onClick = {},
                onLongClick = {},
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppTheme.colors.primaryContainer
            else if (isOpened) AppTheme.colors.secondaryContainer
            else AppTheme.colors.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.paris),
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CardDefaults.shape),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(horizontal = 8.dp).weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.user.username,
                        color = AppTheme.colors.onSurface,
                        style = AppTheme.typography.titleMedium
                    )
                    Spacer(Modifier.width(8.dp))

                    FriendState(user.state)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = user.user.phone.phoneMapToView(),
                    color = AppTheme.colors.onSurfaceVariant,
                    style = AppTheme.typography.labelLarge
                )
            }
            IconButton(onClick = { }, enabled = true) {
                Icon(
                    imageVector = Icons.Default.ArrowRight,
                    tint = AppTheme.colors.onSurfaceVariant,
                    contentDescription = null,
                )
            }
        }
    }
}

fun String.phoneMapToView(): String {
    if (this.length != 11)
        return this

    val a = this.take(4)
    val b = this.drop(4).take(3)
    val c = this.drop(7)

    return "$a $b $c"
}

@Composable
fun FriendState(state: FriendRelationStatus) {
    val containerColor: Color
    val contentColor: Color
    val icon: ImageVector
    when (state) {
        FriendRelationStatus.ACCEPTED -> {
            containerColor = AppTheme.colors.successContainer
            contentColor = AppTheme.colors.onSuccessContainer
            icon = Icons.Default.Check
        }

        FriendRelationStatus.PENDING -> {
            containerColor = AppTheme.colors.secondaryContainer
            contentColor = AppTheme.colors.onSecondaryContainer
            icon = Icons.Default.Nightlight
        }

        FriendRelationStatus.REJECTED -> {
            containerColor = AppTheme.colors.background
            contentColor = AppTheme.colors.onBackground
            icon = Icons.Default.Cancel
        }

        FriendRelationStatus.BLOCKED -> {
            containerColor = AppTheme.colors.errorContainer
            contentColor = AppTheme.colors.onErrorContainer
            icon = Icons.Default.Block
        }
    }

    Column(
        modifier = Modifier.clip(CardDefaults.shape).background(containerColor)
            .padding(vertical = 2.dp, horizontal = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.name.run {
                    this.take(1).uppercase() + this.lowercase().drop(1)
                },
                style = AppTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = icon,
                modifier = Modifier.size(12.dp),
                contentDescription = null,
                tint = contentColor
            )
        }
    }
}

@Preview
@Composable
fun FriendsScreenPreview() {
    AppTheme { FriendsScreen() }
}

@Preview
@Composable
fun FriendsScreenPreview2() {
    AppTheme(darkTheme = true) { FriendsScreen() }
}

data class Friend(
    val user: User,
    val state: FriendRelationStatus,
)

@Serializable
enum class FriendRelationStatus {
    ACCEPTED, PENDING, REJECTED, BLOCKED,
}
