package org.milad.expense_share.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import com.pmb.common.theme.ThemeMode
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.app_name_display
import expenseshare.composeapp.generated.resources.app_version_label
import expenseshare.composeapp.generated.resources.cancel
import expenseshare.composeapp.generated.resources.ic_app_logo
import expenseshare.composeapp.generated.resources.logout
import expenseshare.composeapp.generated.resources.logout_confirm_message
import expenseshare.composeapp.generated.resources.logout_confirm_title
import expenseshare.composeapp.generated.resources.profile_about_section
import expenseshare.composeapp.generated.resources.profile_account_section
import expenseshare.composeapp.generated.resources.profile_appearance_section
import expenseshare.composeapp.generated.resources.profile_copy_phone_desc
import expenseshare.composeapp.generated.resources.profile_guest
import expenseshare.composeapp.generated.resources.profile_member_id
import expenseshare.composeapp.generated.resources.profile_phone_copied
import expenseshare.composeapp.generated.resources.profile_phone_label
import expenseshare.composeapp.generated.resources.profile_title
import expenseshare.composeapp.generated.resources.profile_username_label
import expenseshare.composeapp.generated.resources.theme_dark
import expenseshare.composeapp.generated.resources.theme_light
import expenseshare.composeapp.generated.resources.theme_system
import kotlinx.coroutines.launch
import model.User
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.milad.expense_share.components.UserAvatar
import org.milad.expense_share.friends.friendsList.phoneMapToView
import org.milad.expense_share.friends.friendsList.upperFirstChar

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onLogout: () -> Unit,
) {
    val state by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(Res.string.profile_phone_copied)

    LaunchedEffect(Unit) {
        viewModel.viewEvent.collect { event ->
            when (event) {
                is ProfileEvent.CopyPhoneToClipboard -> {
                    clipboardManager.setText(AnnotatedString(event.phone))
                    scope.launch {
                        snackbarHostState.showSnackbar(copiedMessage)
                    }
                }

                ProfileEvent.LoggedOut -> onLogout()
            }
        }
    }

    ProfileContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::handle,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileState,
    snackbarHostState: SnackbarHostState,
    onAction: (ProfileAction) -> Unit,
) {
    Scaffold(
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.profile_title),
                        style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
                    titleContentColor = AppTheme.colors.onBackground,
                ),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                IdentityHeroCard(user = state.user)

                AccountInfoCard(
                    user = state.user,
                    onCopyPhone = { onAction(ProfileAction.CopyPhone) },
                )

                AppearanceCard(
                    selectedMode = state.themeMode,
                    onSelectMode = { onAction(ProfileAction.SelectThemeMode(it)) },
                )

                AboutCard()

                Spacer(modifier = Modifier.height(4.dp))

                FilledTonalButton(
                    onClick = { onAction(ProfileAction.RequestLogout) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AppTheme.colors.errorContainer,
                        contentColor = AppTheme.colors.onErrorContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.logout),
                        style = AppTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (state.showLogoutConfirm) {
        LogoutConfirmationBottomSheet(
            onDismiss = { onAction(ProfileAction.DismissLogout) },
            onConfirm = { onAction(ProfileAction.ConfirmLogout) },
        )
    }
}

@Composable
private fun IdentityHeroCard(user: User) {
    val displayName = user.username.trim().upperFirstChar()
        .ifBlank { stringResource(Res.string.profile_guest) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                name = displayName,
                size = 68.dp,
                shape = CircleShape,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = displayName,
                    style = AppTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (user.id > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppTheme.colors.primaryContainer,
                    ) {
                        Text(
                            text = stringResource(Res.string.profile_member_id, user.id),
                            style = AppTheme.typography.labelMedium,
                            color = AppTheme.colors.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountInfoCard(
    user: User,
    onCopyPhone: () -> Unit,
) {
    val formattedPhone = user.phone.takeIf { it.isNotBlank() }?.phoneMapToView() ?: "—"
    val usernameText = user.username.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "—"

    ProfileSectionCard(title = stringResource(Res.string.profile_account_section)) {
        ProfileInfoRow(
            icon = Icons.Outlined.Phone,
            label = stringResource(Res.string.profile_phone_label),
            value = formattedPhone,
            trailing = if (user.phone.isNotBlank()) {
                {
                    IconButton(onClick = onCopyPhone) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = stringResource(Res.string.profile_copy_phone_desc),
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            } else {
                null
            },
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = AppTheme.colors.outlineVariant.copy(alpha = 0.5f),
        )

        ProfileInfoRow(
            icon = Icons.Outlined.Badge,
            label = stringResource(Res.string.profile_username_label),
            value = usernameText,
        )
    }
}

@Composable
private fun AppearanceCard(
    selectedMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
) {
    val options = listOf(
        Triple(ThemeMode.SYSTEM, stringResource(Res.string.theme_system), Icons.Outlined.BrightnessAuto),
        Triple(ThemeMode.LIGHT, stringResource(Res.string.theme_light), Icons.Outlined.LightMode),
        Triple(ThemeMode.DARK, stringResource(Res.string.theme_dark), Icons.Outlined.DarkMode),
    )

    ProfileSectionCard(title = stringResource(Res.string.profile_appearance_section)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = AppTheme.colors.surfaceContainerHighest,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                options.forEach { (mode, label, icon) ->
                    val isSelected = selectedMode == mode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectMode(mode) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AppTheme.colors.primaryContainer else Color.Transparent,
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) AppTheme.colors.onPrimaryContainer else AppTheme.colors.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = AppTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                ),
                                color = if (isSelected) AppTheme.colors.onPrimaryContainer else AppTheme.colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutCard() {
    ProfileSectionCard(title = stringResource(Res.string.profile_about_section)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(Res.string.app_name_display),
                    style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(Res.string.app_version_label),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = AppTheme.typography.labelLarge,
                color = AppTheme.colors.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
            )
            Text(
                text = value,
                style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = AppTheme.colors.onSurface,
            )
        }

        if (trailing != null) {
            trailing()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogoutConfirmationBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.logout_confirm_title),
                style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = AppTheme.colors.onSurface,
            )
            Text(
                text = stringResource(Res.string.logout_confirm_message),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(Res.string.cancel))
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.error,
                        contentColor = AppTheme.colors.onError,
                    ),
                ) {
                    Text(stringResource(Res.string.logout))
                }
            }
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    AppTheme {
        ProfileContent(
            state = ProfileState(
                user = User(id = 42, username = "milad", phone = "09123456789"),
                themeMode = ThemeMode.SYSTEM,
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}
