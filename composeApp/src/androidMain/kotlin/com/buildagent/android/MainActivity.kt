package com.buildagent.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.buildagent.ui.App
import com.buildagent.ui.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startKoin {
            androidContext(this@MainActivity)
            modules(appModules(
                baseUrl = "http://10.0.2.2:3001",
                tokenProvider = { "" }
            ))
        }
        setContent { App() }
    }
}
