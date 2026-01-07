package com.pmb.common.ui.scaffold.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme

/**
 * Compact FAB for Navigation Rail
 */
@Composable
fun CompactAddGroupButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(top = 8.dp, bottom = 32.dp),
        containerColor = AppTheme.colors.primary,
        contentColor = AppTheme.colors.onPrimary,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Group",
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * Extended FAB for Drawer layouts
 */
@Composable
fun ExtendedAddGroupButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 40.dp),
        containerColor = AppTheme.colors.primary,
        contentColor = AppTheme.colors.onPrimary,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Group",
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = "Add Group",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
    }
}