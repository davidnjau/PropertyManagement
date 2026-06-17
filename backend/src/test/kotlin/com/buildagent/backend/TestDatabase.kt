package com.buildagent.backend

import com.buildagent.backend.db.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Sets up an H2 in-memory database for backend integration tests.
 * Call [init] before any test suite and [resetAll] between tests.
 */
object TestDatabase {

    private val ALL_TABLES = arrayOf(
        AgenciesTable, UsersTable, UserCredentialsTable, ClientsTable, BuildingsTable,
        UnitsTable, TenantsTable, LeasesTable, PaymentsTable,
        MaintenanceRequestsTable, AuditEventsTable,
        PaymentMethodsConfigTable, BankConfigTable, MpesaConfigTable, PaypalConfigTable,
        DocumentsTable, AlertsTable, AlertRecipientsTable, AlertTenantIdsTable,
        LeaseExtensionRequestsTable, ContactInquiriesTable
    )

    fun init() {
        Database.connect(
            url = "jdbc:h2:mem:buildagent_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;" +
                  "DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )
        create()
    }

    fun create() = transaction { SchemaUtils.create(*ALL_TABLES) }

    fun resetAll() = transaction {
        SchemaUtils.drop(*ALL_TABLES.reversedArray())
        SchemaUtils.create(*ALL_TABLES)
    }
}
