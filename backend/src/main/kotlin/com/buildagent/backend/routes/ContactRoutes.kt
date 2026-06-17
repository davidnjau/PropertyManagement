package com.buildagent.backend.routes

import com.buildagent.backend.services.ContactService
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.contactRoutes(service: ContactService) {
    route("/contact") {
        post("/inquiries") {
            val req = call.receive<ContactInquiryRequest>()
            val inquiry = service.submit(req)
            call.respond(HttpStatusCode.Created, ApiResponse(inquiry))
        }
    }
}
