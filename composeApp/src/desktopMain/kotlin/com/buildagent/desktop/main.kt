package com.buildagent.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.buildagent.ui.App
import com.buildagent.ui.di.appModules
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(appModules(baseUrl = System.getenv("API_URL") ?: "http://localhost:3001"))
    }
    Window(onCloseRequest = ::exitApplication, title = "BuildAgent") {
        App()
    }
}
