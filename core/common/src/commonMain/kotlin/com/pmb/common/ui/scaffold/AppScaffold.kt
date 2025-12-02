package com.pmb.common.ui.scaffold

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.window.core.layout.WindowHeightSizeClass
import com.pmb.common.ui.scaffold.model.NavigationContentPosition
import com.pmb.common.ui.scaffold.navigation.BottomNavigationBar
import com.pmb.common.ui.scaffold.navigation.NavigationRailLayout
import com.pmb.common.ui.scaffold.navigation.drawer.ModalDrawerContent
import com.pmb.common.ui.scaffold.navigation.drawer.PermanentDrawerContent
import kotlinx.coroutines.launch

/**
 * Main scaffold component that adapts navigation UI based on screen size
 *
 * Navigation types:
 * - NavigationBar: Bottom navigation for compact screens
 * - NavigationRail: Side rail for medium screens
 * - NavigationDrawer: Permanent drawer for expanded screens
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppScaffold(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    onAddGroupClick: () -> Unit,
    showAddGroupButton: Boolean = true,
    content: @Composable (NavigationSuiteType) -> Unit,
) {
    BoxWithConstraints {
        val adaptiveInfo = currentWindowAdaptiveInfo()
        val coroutineScope = rememberCoroutineScope()

        val navLayoutType = calculateAppScreenSize(adaptiveInfo, maxWidth)

        val navContentPosition = when (adaptiveInfo.windowSizeClass.windowHeightSizeClass) {
            WindowHeightSizeClass.COMPACT -> NavigationContentPosition.TOP
            WindowHeightSizeClass.MEDIUM,
            WindowHeightSizeClass.EXPANDED -> NavigationContentPosition.CENTER
            else -> NavigationContentPosition.TOP
        }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val gesturesEnabled = drawerState.isOpen || navLayoutType == NavigationSuiteType.NavigationRail

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = gesturesEnabled,
            drawerContent = {
                ModalDrawerContent(
                    contentPosition = navContentPosition,
                    selectedItem = selectedItem,
                    showAddGroupButton = showAddGroupButton,
                    onItemSelected = onItemSelected,
                    onAddGroupClick = onAddGroupClick,
                    onDrawerClicked = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    },
                )
            }
        ) {
            NavigationSuiteScaffoldLayout(
                layoutType = navLayoutType,
                navigationSuite = {
                    when (navLayoutType) {
                        NavigationSuiteType.NavigationBar -> {
                            BottomNavigationBar(
                                selectedItem = selectedItem,
                                onItemSelected = onItemSelected
                            )
                        }

                        NavigationSuiteType.NavigationRail -> {
                            NavigationRailLayout(
                                selectedItem = selectedItem,
                                onItemSelected = onItemSelected,
                                showAddGroupButton = showAddGroupButton,
                                onAddGroupClick = onAddGroupClick,
                                onDrawerClicked = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }
                            )
                        }

                        NavigationSuiteType.NavigationDrawer -> {
                            PermanentDrawerContent(
                                contentPosition = navContentPosition,
                                selectedItem = selectedItem,
                                onItemSelected = onItemSelected,
                                showAddGroupButton = showAddGroupButton,
                                onAddGroupClick = onAddGroupClick,
                            )
                        }
                    }
                }
            ) {
                content(navLayoutType)
            }
        }
    }
}