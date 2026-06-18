package com.buildagent.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.buildagent.ui.App
import com.buildagent.ui.di.appModules
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(appModules(baseUrl = "http://localhost:3001"))
    }
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
