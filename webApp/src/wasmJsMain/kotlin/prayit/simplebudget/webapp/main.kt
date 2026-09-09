package prayit.simplebudget.webapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import prayit.simplebudget.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val container = document.getElementById("app") ?: document.body!!
    ComposeViewport(container) {
        Box(Modifier.fillMaxSize()) {
            App()
        }
    }
}
