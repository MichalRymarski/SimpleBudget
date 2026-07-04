@file:Suppress("DEPRECATION")

package prayit.simplebudget.navigation

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowWidthSizeClass
import prayit.simplebudget.core.utils.DeviceClass

@Composable
fun rememberDeviceClass(): DeviceClass {
    val info = currentWindowAdaptiveInfo()
    return remember(info) { resolveDeviceClass(info) }
}

private fun resolveDeviceClass(info: WindowAdaptiveInfo): DeviceClass {
    val widthClass = info.windowSizeClass.windowWidthSizeClass

    return when {
        widthClass == WindowWidthSizeClass.COMPACT -> DeviceClass.PhonePortrait
        widthClass == WindowWidthSizeClass.MEDIUM -> DeviceClass.TabletPortrait
        widthClass == WindowWidthSizeClass.EXPANDED -> DeviceClass.TabletLandscape
        else -> DeviceClass.LargeTabletDesktop
    }
}
