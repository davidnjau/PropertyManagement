package com.buildagent.backend.services

import org.slf4j.LoggerFactory

/**
 * Sends SMS/email notifications to tenants.
 * Currently logs; swap the log calls for Twilio/SendGrid when credentials are available.
 */
class NotificationService {

    private val log = LoggerFactory.getLogger("NotificationService")

    fun sendOtp(phone: String?, email: String, otp: String, fullName: String) {
        val message = "Hi $fullName, welcome to BuildAgent. Your one-time login code is: $otp"
        log.info("[OTP] to=$email phone=$phone otp=$otp msg=\"$message\"")
        // TODO: Twilio SMS → phone, SendGrid email → email
    }

    fun sendLeaseCreatedNotification(phone: String?, email: String, fullName: String, leaseId: String) {
        val message = "Hi $fullName, a new lease has been created for you (ref: $leaseId). " +
                "Log in to your tenant portal to view the details."
        log.info("[LEASE_CREATED] to=$email phone=$phone leaseId=$leaseId msg=\"$message\"")
        // TODO: Twilio SMS → phone
    }

    fun sendLeaseAddedToExistingUserNotification(phone: String?, email: String, fullName: String, leaseId: String) {
        val message = "Hi $fullName, you have been added as a tenant on a new lease (ref: $leaseId). " +
                "Your existing login credentials remain unchanged."
        log.info("[LEASE_ADDED_EXISTING_USER] to=$email phone=$phone leaseId=$leaseId msg=\"$message\"")
        // TODO: Twilio SMS → phone
    }
}
