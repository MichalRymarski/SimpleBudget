package prayit.simplebudget.core.components.button.fab

import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.runtime.Composable
import prayit.simplebudget.core.utils.DeviceClass

@Composable
fun AppFloatingActionButton(
    deviceClass: DeviceClass,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    when (deviceClass) {
        DeviceClass.TabletPortrait,
        DeviceClass.TabletLandscape,
        DeviceClass.LargeTabletDesktop -> LargeFloatingActionButton(onClick = onClick) { content() }
        else -> AppSmallFloatingButton(onClick = onClick) { content() }
    }
}
