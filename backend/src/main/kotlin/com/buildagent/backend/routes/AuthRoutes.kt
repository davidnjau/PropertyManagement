package com.buildagent.backend.routes

import com.buildagent.backend.auth.AgentPrincipal
import com.buildagent.backend.services.AuthService
import com.buildagent.shared.models.ApiResponse
import com.buildagent.shared.models.ForgotPasswordRequest
import com.buildagent.shared.models.LoginRequest
import com.buildagent.shared.models.RegisterRequest
import com.buildagent.shared.models.VerifyOtpRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(service: AuthService) {
    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            val result = service.register(req)
            call.respond(HttpStatusCode.Created, ApiResponse(result))
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val result = service.login(req)
            call.respond(ApiResponse(result))
        }

        post("/signin") {
            val req = call.receive<LoginRequest>()
            val result = service.login(req)
            call.respond(ApiResponse(result))
        }

        post("/signup") {
            val req = call.receive<RegisterRequest>()
            val result = service.register(req)
            call.respond(HttpStatusCode.Created, ApiResponse(result))
        }

        post("/forgot-password") {
            val req = call.receive<ForgotPasswordRequest>()
            service.forgotPassword(req.email)
            call.respond(ApiResponse<Unit>(message = "OTP sent"))
        }

        post("/verify-otp") {
            val req = call.receive<VerifyOtpRequest>()
            service.verifyOtp(req.email, req.otp)
            call.respond(ApiResponse<Unit>(message = "OTP verified"))
        }

        authenticate("local-auth") {
            get("/me") {
                val principal = call.principal<AgentPrincipal>()!!
                val user = service.me(principal.userId)
                call.respond(ApiResponse(user))
            }
        }
    }
}
