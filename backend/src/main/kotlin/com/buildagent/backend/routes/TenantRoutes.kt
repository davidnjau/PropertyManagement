package com.buildagent.backend.routes

import com.buildagent.backend.auth.requireRole
import com.buildagent.backend.auth.userPrincipal
import com.buildagent.backend.services.TenantService
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tenantRoutes(tenantService: TenantService) {
    authenticate("local-auth") {
        route("/tenants") {
            get {
                val p = call.userPrincipal()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val (tenants, total) = tenantService.listTenants(p.agencyId, page, limit)
                call.respond(ApiResponse(
                    data = tenants,
                    meta = PaginationMeta(total, page, limit, (total + limit - 1) / limit)
                ))
            }
            post {
                val p = call.userPrincipal()
                p.requireRole("ADMIN", "AGENT")
                val tenant = tenantService.createTenant(p.agencyId, call.receive<CreateTenantRequest>())
                call.respond(HttpStatusCode.Created, ApiResponse(data = tenant))
            }
            get("/{id}") {
                val p = call.userPrincipal()
                val tenantDetail = tenantService.getTenantEnriched(p.agencyId, call.parameters["id"]!!)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Tenant not found"))
                call.respond(ApiResponse(data = tenantDetail))
            }
            patch("/{id}") {
                val p = call.userPrincipal()
                p.requireRole("ADMIN", "AGENT")
                val tenant = tenantService.updateTenant(p.agencyId, call.parameters["id"]!!, call.receive<CreateTenantRequest>())
                call.respond(ApiResponse(data = tenant))
            }
            delete("/{id}") {
                val p = call.userPrincipal()
                p.requireRole("ADMIN", "AGENT")
                val deleted = tenantService.deleteTenant(p.agencyId, call.parameters["id"]!!)
                if (!deleted) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "Cannot delete tenant with an active lease"))
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}
