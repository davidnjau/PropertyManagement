package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.ContactInquiriesTable
import com.buildagent.shared.models.ContactInquiry
import com.buildagent.shared.models.ContactInquiryRequest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

class ContactService {

    suspend fun submit(req: ContactInquiryRequest): ContactInquiry = dbQuery {
        val now: Instant = Clock.System.now()
        val id = ContactInquiriesTable.insertAndGetId {
            it[fullName] = req.fullName
            it[workEmail] = req.workEmail
            it[company] = req.company
            it[units] = req.units
            it[message] = req.message
            it[createdAt] = now
        }
        val row = ContactInquiriesTable.selectAll().where { ContactInquiriesTable.id eq id }.single()
        ContactInquiry(
            id = row[ContactInquiriesTable.id].value.toString(),
            fullName = row[ContactInquiriesTable.fullName],
            workEmail = row[ContactInquiriesTable.workEmail],
            company = row[ContactInquiriesTable.company],
            units = row[ContactInquiriesTable.units],
            message = row[ContactInquiriesTable.message],
            createdAt = row[ContactInquiriesTable.createdAt].toString()
        )
    }
}
