package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.DocumentsTable
import com.buildagent.shared.models.Document
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.io.File
import java.util.UUID

class DocumentsService {

    suspend fun list(agencyId: UUID, targetType: String?, targetId: String?, docType: String?): List<Document> = dbQuery {
        var query = DocumentsTable.selectAll()
            .where { (DocumentsTable.agencyId eq agencyId) and DocumentsTable.deletedAt.isNull() }
        if (targetType != null) query = query.andWhere { DocumentsTable.targetType eq targetType }
        if (targetId != null) query = query.andWhere { DocumentsTable.targetId eq UUID.fromString(targetId) }
        if (docType != null) query = query.andWhere { DocumentsTable.docType eq docType }
        query.orderBy(DocumentsTable.uploadedAt, SortOrder.DESC).map { it.toDocument() }
    }

    suspend fun getById(agencyId: UUID, docId: UUID): Document? = dbQuery {
        DocumentsTable.selectAll()
            .where {
                (DocumentsTable.id eq docId) and
                (DocumentsTable.agencyId eq agencyId) and
                DocumentsTable.deletedAt.isNull()
            }
            .firstOrNull()?.toDocument()
    }

    suspend fun create(
        agencyId: UUID,
        uploadedBy: UUID,
        targetType: String,
        targetId: String,
        docType: String,
        notes: String?,
        fileName: String,
        fileSize: Long,
        mimeType: String,
        fileUrl: String
    ): Document = dbQuery {
        val now: Instant = Clock.System.now()
        val id = DocumentsTable.insertAndGetId {
            it[DocumentsTable.agencyId] = agencyId
            it[DocumentsTable.targetType] = targetType
            it[DocumentsTable.targetId] = UUID.fromString(targetId)
            it[DocumentsTable.docType] = docType
            it[DocumentsTable.fileName] = fileName
            it[DocumentsTable.fileSize] = fileSize
            it[DocumentsTable.mimeType] = mimeType
            it[DocumentsTable.notes] = notes
            it[DocumentsTable.uploadedBy] = uploadedBy
            it[DocumentsTable.fileUrl] = fileUrl
            it[DocumentsTable.uploadedAt] = now
        }
        DocumentsTable.selectAll().where { DocumentsTable.id eq id }.single().toDocument()
    }

    suspend fun patch(agencyId: UUID, docId: UUID, docType: String?, notes: String?): Document? = dbQuery {
        val existing = DocumentsTable.selectAll()
            .where { (DocumentsTable.id eq docId) and (DocumentsTable.agencyId eq agencyId) and DocumentsTable.deletedAt.isNull() }
            .firstOrNull() ?: return@dbQuery null

        DocumentsTable.update({ DocumentsTable.id eq docId }) {
            if (docType != null) it[DocumentsTable.docType] = docType
            if (notes != null) it[DocumentsTable.notes] = notes
        }
        DocumentsTable.selectAll().where { DocumentsTable.id eq docId }.single().toDocument()
    }

    suspend fun delete(agencyId: UUID, docId: UUID): Boolean = dbQuery {
        val existing = DocumentsTable.selectAll()
            .where { (DocumentsTable.id eq docId) and (DocumentsTable.agencyId eq agencyId) }
            .firstOrNull() ?: return@dbQuery false
        DocumentsTable.deleteWhere { DocumentsTable.id eq docId }
        true
    }

    suspend fun listForTenant(agencyId: UUID, tenantId: UUID): List<Document> = dbQuery {
        DocumentsTable.selectAll()
            .where {
                (DocumentsTable.agencyId eq agencyId) and
                (DocumentsTable.targetType eq "tenant") and
                (DocumentsTable.targetId eq tenantId) and
                DocumentsTable.deletedAt.isNull()
            }
            .orderBy(DocumentsTable.uploadedAt, SortOrder.DESC)
            .map { it.toDocument() }
    }

    fun saveFile(bytes: ByteArray, fileName: String): Pair<String, Long> {
        val uploadsDir = File("uploads")
        if (!uploadsDir.exists()) uploadsDir.mkdirs()
        val uniqueName = "${UUID.randomUUID()}_$fileName"
        val file = File(uploadsDir, uniqueName)
        file.writeBytes(bytes)
        return "uploads/$uniqueName" to bytes.size.toLong()
    }

    private fun ResultRow.toDocument() = Document(
        id = this[DocumentsTable.id].value.toString(),
        agencyId = this[DocumentsTable.agencyId].value.toString(),
        targetType = this[DocumentsTable.targetType],
        targetId = this[DocumentsTable.targetId].toString(),
        docType = this[DocumentsTable.docType],
        fileName = this[DocumentsTable.fileName],
        fileSize = this[DocumentsTable.fileSize],
        mimeType = this[DocumentsTable.mimeType],
        notes = this[DocumentsTable.notes],
        uploadedBy = this[DocumentsTable.uploadedBy].value.toString(),
        fileUrl = this[DocumentsTable.fileUrl],
        uploadedAt = this[DocumentsTable.uploadedAt].toString()
    )
}
