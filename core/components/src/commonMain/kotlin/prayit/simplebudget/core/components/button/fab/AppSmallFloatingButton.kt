package prayit.simplebudget.core.components.button.fab

import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable


@Composable
internal fun AppSmallFloatingButton(onClick: () -> Unit, content: @Composable (() -> Unit)) {
    FloatingActionButton(onClick = onClick) { content() }
}