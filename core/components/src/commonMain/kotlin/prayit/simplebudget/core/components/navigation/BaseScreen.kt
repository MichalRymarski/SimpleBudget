package prayit.simplebudget.core.components.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import prayit.simplebudget.core.utils.DeviceClass

data class NavigationItem(
    val label: @Composable () -> Unit,
    val icon: @Composable () -> Unit = {},
    val selected: Boolean,
    val onClick: () -> Unit,
)

@Composable
fun BaseScreen(
    deviceClass: DeviceClass,
    items: List<NavigationItem> = emptyList(),
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit,
) {
    if (deviceClass.isCompact) {
        Scaffold(
            bottomBar = {
                if (items.isNotEmpty()) {
                    NavigationBar {
                        items.forEach { item ->
                            NavigationBarItem(
                                icon = item.icon,
                                label = item.label,
                                selected = item.selected,
                                onClick = item.onClick,
                            )
                        }
                    }
                }
            },
            floatingActionButton = { floatingActionButton?.invoke() },
        ) { paddingValues ->
            content(Modifier.padding(paddingValues))
        }
    } else {
        Scaffold(
            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
            floatingActionButton = { floatingActionButton?.invoke() },
        ) { paddingValues ->
            Row(Modifier.padding(paddingValues)) {
                if (items.isNotEmpty()) {
                    NavigationRail(Modifier.fillMaxHeight()) {
                        items.forEach { item ->
                            NavigationRailItem(
                                icon = item.icon,
                                label = item.label,
                                selected = item.selected,
                                onClick = item.onClick,
                            )
                        }
                    }
                }
                content(Modifier.weight(1f))
            }
        }
    }
}
