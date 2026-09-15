package prayit.simplebudget.feature.home.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import prayit.simplebudget.core.components.button.fab.AppFloatingActionButton
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.DeviceClass
import prayit.simplebudget.core.utils.PhonePreviews


@Composable
fun HomeFAB(
    onClick: () -> Unit,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    content: @Composable (() -> Unit),
) {
    AppFloatingActionButton(deviceClass = deviceClass, onClick = onClick) { content() }
}

@PhonePreviews
@Composable
private fun HomeFABPreview() {
    MParafiaTheme {
        HomeFAB(onClick = {}) {
            Text("Go")
        }
    }
}