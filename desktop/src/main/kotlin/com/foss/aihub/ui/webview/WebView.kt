package com.foss.aihub.pc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.graphics.Color
import io.github.jan.suparasync.html.webview.WebView
import io.github.jan.suparasync.html.webview.WebViewState
import io.github.jan.suparasync.html.webview.rememberWebViewState
import com.foss.aihub.pc.models.AiService

@Composable
fun rememberWebViewState(): WebViewState = rememberWebViewState(
    WebViewState().copy()
)

@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier
) {
    WebView(
        state = state,
        modifier = modifier,
        onAttached = { window ->
            window.settings.javaScriptEnabled = true
            window.settings.domStorageEnabled = true
            window.settings.useWideViewPort = true
            window.settings.loadWithOverviewMode = true
        }
    )
}