package com.buildagent.shared.di

import com.buildagent.shared.api.BuildAgentClient
import org.koin.dsl.module

fun sharedModule(baseUrl: String, tokenProvider: suspend () -> String) = module {
    single { BuildAgentClient(baseUrl, tokenProvider) }
}
