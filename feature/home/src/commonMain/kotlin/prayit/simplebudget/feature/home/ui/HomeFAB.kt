package prayit.simplebudget.feature.home.ui

import androidx.compose.runtime.Composable
import prayit.simplebudget.core.components.button.fab.AppFloatingActionButton
import prayit.simplebudget.core.utils.DeviceClass


@Composable
fun HomeFAB(
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onClick: () -> Unit,
    content: @Composable (() -> Unit),
) {
    AppFloatingActionButton(deviceClass= deviceClass,onClick = onClick) { content() }
}