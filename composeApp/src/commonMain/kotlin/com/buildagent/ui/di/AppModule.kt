package com.buildagent.ui.di

import com.buildagent.shared.di.sharedModule
import com.buildagent.ui.screens.admin.AdminAlertsViewModel
import com.buildagent.ui.screens.admin.AdminDocumentsViewModel
import com.buildagent.ui.screens.admin.AdminLeaseExtensionsViewModel
import com.buildagent.ui.screens.admin.PaymentMethodsViewModel
import com.buildagent.ui.screens.maintenance.MaintenanceViewModel
import com.buildagent.ui.screens.payments.PaymentsViewModel
import com.buildagent.ui.screens.portfolio.PortfolioViewModel
import com.buildagent.ui.screens.tenant.TenantPortalViewModel
import com.buildagent.ui.screens.tenancy.TenancyViewModel
import org.koin.dsl.module

fun appModules(baseUrl: String, tokenProvider: suspend () -> String) = listOf(
    sharedModule(baseUrl, tokenProvider),
    module {
        factory { PortfolioViewModel(get()) }
        factory { TenancyViewModel(get()) }
        factory { PaymentsViewModel(get()) }
        factory { MaintenanceViewModel(get()) }
        factory { AdminAlertsViewModel(get()) }
        factory { AdminDocumentsViewModel(get()) }
        factory { PaymentMethodsViewModel(get()) }
        factory { AdminLeaseExtensionsViewModel(get()) }
        factory { TenantPortalViewModel(get()) }
    }
)
