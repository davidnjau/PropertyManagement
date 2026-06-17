package com.buildagent.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.buildagent.ui.App
import com.buildagent.ui.di.appModules
import org.koin.core.context.startKoin

private var koinStarted = false

fun MainViewController() = ComposeUIViewController {
    if (!koinStarted) {
        koinStarted = true
        startKoin {
            modules(appModules(baseUrl = "http://localhost:3001"))
        }
    }
    App()
}
