package com.buildagent.ui.di

import com.buildagent.shared.di.sharedModule
import com.buildagent.ui.screens.admin.AdminAlertsViewModel
import com.buildagent.ui.screens.admin.AdminDocumentsViewModel
import com.buildagent.ui.screens.admin.AdminLeaseExtensionsViewModel
import com.buildagent.ui.screens.admin.AdminUsersViewModel
import com.buildagent.ui.screens.admin.PaymentMethodsViewModel
import com.buildagent.ui.screens.dashboard.DashboardViewModel
import com.buildagent.ui.screens.maintenance.MaintenanceViewModel
import com.buildagent.ui.screens.payments.PaymentsViewModel
import com.buildagent.ui.screens.portfolio.PortfolioViewModel
import com.buildagent.ui.screens.tenant.TenantPortalViewModel
import com.buildagent.ui.screens.tenancy.TenancyViewModel
import com.buildagent.ui.state.TokenStore
import org.koin.dsl.module

fun appModules(baseUrl: String) = listOf(
    sharedModule(baseUrl) { TokenStore.token },
    module {
        factory { DashboardViewModel(get()) }
        factory { PortfolioViewModel(get()) }
        factory { TenancyViewModel(get()) }
        factory { PaymentsViewModel(get()) }
        factory { MaintenanceViewModel(get()) }
        factory { AdminAlertsViewModel(get()) }
        factory { AdminDocumentsViewModel(get()) }
        factory { PaymentMethodsViewModel(get()) }
        factory { AdminLeaseExtensionsViewModel(get()) }
        factory { TenantPortalViewModel(get()) }
        factory { AdminUsersViewModel(get()) }
    }
)
