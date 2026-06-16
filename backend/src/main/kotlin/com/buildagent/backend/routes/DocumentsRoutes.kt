package com.buildagent.backend.routes

import com.buildagent.backend.auth.AgentPrincipal
import com.buildagent.backend.services.DocumentsService
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.documentsRoutes(service: DocumentsService) {
    authenticate("local-auth") {
        route("/admin/documents") {
            get {
                val principal = call.principal<AgentPrincipal>()!!
                val targetType = call.request.queryParameters["targetType"]
                val targetId = call.request.queryParameters["targetId"]
                val docType = call.request.queryParameters["docType"]
                val docs = service.list(UUID.fromString(principal.agencyId), targetType, targetId, docType)
                call.respond(ApiResponse(docs))
            }

            post {
                val principal = call.principal<AgentPrincipal>()!!
                val multipart = call.receiveMultipart()

                var fileBytes: ByteArray? = null
                var fileName = "upload"
                var mimeType = "application/octet-stream"
                var targetType = ""
                var targetId = ""
                var docType = ""
                var notes: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName ?: "upload"
                            mimeType = part.contentType?.toString() ?: "application/octet-stream"
                            fileBytes = part.streamProvider().readBytes()
                        }
                        is PartData.FormItem -> when (part.name) {
                            "targetType" -> targetType = part.value
                            "targetId" -> targetId = part.value
                            "docType" -> docType = part.value
                            "notes" -> notes = part.value
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                val bytes = fileBytes ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No file provided"))
                val (fileUrl, fileSize) = service.saveFile(bytes, fileName)

                val doc = service.create(
                    agencyId = UUID.fromString(principal.agencyId),
                    uploadedBy = UUID.fromString(principal.userId),
                    targetType = targetType,
                    targetId = targetId,
                    docType = docType,
                    notes = notes,
                    fileName = fileName,
                    fileSize = fileSize,
                    mimeType = mimeType,
                    fileUrl = fileUrl
                )
                call.respond(HttpStatusCode.Created, ApiResponse(doc))
            }

            get("/{id}") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val doc = service.getById(UUID.fromString(principal.agencyId), id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Document not found"))
                call.respond(ApiResponse(doc))
            }

            patch("/{id}") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val req = call.receive<PatchDocumentRequest>()
                val doc = service.patch(UUID.fromString(principal.agencyId), id, req.docType, req.notes)
                    ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Document not found"))
                call.respond(ApiResponse(doc))
            }

            delete("/{id}") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val deleted = service.delete(UUID.fromString(principal.agencyId), id)
                if (!deleted) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Document not found"))
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }

        route("/tenant/documents") {
            get {
                val principal = call.principal<AgentPrincipal>()!!
                val docs = service.listForTenant(UUID.fromString(principal.agencyId), UUID.fromString(principal.userId))
                call.respond(ApiResponse(docs))
            }

            post {
                val principal = call.principal<AgentPrincipal>()!!
                val multipart = call.receiveMultipart()

                var fileBytes: ByteArray? = null
                var fileName = "upload"
                var mimeType = "application/octet-stream"

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName ?: "upload"
                            mimeType = part.contentType?.toString() ?: "application/octet-stream"
                            fileBytes = part.streamProvider().readBytes()
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                val bytes = fileBytes ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No file provided"))
                val (fileUrl, fileSize) = service.saveFile(bytes, fileName)

                val doc = service.create(
                    agencyId = UUID.fromString(principal.agencyId),
                    uploadedBy = UUID.fromString(principal.userId),
                    targetType = "tenant",
                    targetId = principal.userId,
                    docType = "tenant_upload",
                    notes = null,
                    fileName = fileName,
                    fileSize = fileSize,
                    mimeType = mimeType,
                    fileUrl = fileUrl
                )
                call.respond(HttpStatusCode.Created, ApiResponse(doc))
            }
        }
    }
}
