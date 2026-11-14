package org.milad.expense_share.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pmb.common.theme.AppTheme
import kotlin.math.absoluteValue

@Composable
private fun avatarColorPair(seed: String): Pair<Color, Color> {
    val palette = listOf(
        AppTheme.colors.primaryContainer to AppTheme.colors.onPrimaryContainer,
        AppTheme.colors.tertiaryContainer to AppTheme.colors.onTertiaryContainer,
        AppTheme.colors.secondaryContainer to AppTheme.colors.onSecondaryContainer,
        AppTheme.colors.surfaceContainerHighest to AppTheme.colors.primary,
    )
    val index = seed.trim().lowercase().hashCode().absoluteValue % palette.size
    return palette[index]
}

private fun extractInitials(label: String): String {
    val parts = label.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "?"
    if (parts.size == 1) return parts[0].take(2).uppercase()
    return "${parts.first().first()}${parts.last().first()}".uppercase()
}

/**
 * Deterministic monogram avatar for a user, styled from [AppTheme.colors].
 */
@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    shape: Shape = CircleShape,
) {
    val (bg, fg) = avatarColorPair(name)
    val initials = extractInitials(name)
    val fontSize = (size.value * 0.36f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = fg,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/**
 * Deterministic avatar badge for a group using initials when named or a group icon fallback.
 */
@Composable
fun GroupAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(14.dp),
) {
    val (bg, fg) = avatarColorPair(name)
    val trimmed = name.trim()

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        if (trimmed.isNotEmpty()) {
            Text(
                text = extractInitials(trimmed),
                color = fg,
                fontSize = (size.value * 0.34f).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Groups,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
