package com.buildagent.ui.di

import com.buildagent.shared.di.sharedModule
import org.koin.dsl.module

fun appModules(baseUrl: String, tokenProvider: suspend () -> String) = listOf(
    sharedModule(baseUrl, tokenProvider),
    module {
        // Additional UI-layer singletons can be registered here
    }
)
