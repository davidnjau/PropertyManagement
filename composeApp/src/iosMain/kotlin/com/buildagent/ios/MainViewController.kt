package com.buildagent.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.buildagent.ui.App
import com.buildagent.ui.di.appModules
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
    startKoin {
        modules(appModules(
            baseUrl = "http://localhost:3001",
            tokenProvider = { "" }
        ))
    }
    App()
}
