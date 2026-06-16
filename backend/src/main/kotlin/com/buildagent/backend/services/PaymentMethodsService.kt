package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.*
import com.buildagent.shared.models.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

private val KENYAN_BANKS = listOf(
    "kcb" to "KCB Bank",
    "equity" to "Equity Bank",
    "cooperative" to "Co-operative Bank",
    "absa" to "Absa Bank",
    "standard_chartered" to "Standard Chartered",
    "ncba" to "NCBA Bank",
    "stanbic" to "Stanbic Bank",
    "im_bank" to "I&M Bank",
    "dtb" to "DTB Bank",
    "family_bank" to "Family Bank",
    "hf_group" to "HF Group",
    "prime_bank" to "Prime Bank",
    "bank_of_africa" to "Bank of Africa",
    "sidian" to "Sidian Bank",
    "gulf_african" to "Gulf African Bank",
    "nbk" to "NBK",
    "dbk" to "DBK",
    "consolidated" to "Consolidated Bank"
)

private val DEFAULT_METHODS = listOf("mpesa", "paypal", "bank_transfer")

class PaymentMethodsService {

    suspend fun getConfig(agencyId: UUID): PaymentMethodsConfig = dbQuery {
        seedMethodsIfAbsent(agencyId)
        seedBanksIfAbsent(agencyId)

        val methods = PaymentMethodsConfigTable.selectAll()
            .where { PaymentMethodsConfigTable.agencyId eq agencyId }
            .map { PaymentMethod(it[PaymentMethodsConfigTable.methodId], it[PaymentMethodsConfigTable.enabled]) }

        val mpesaRow = MpesaConfigTable.selectAll()
            .where { MpesaConfigTable.agencyId eq agencyId }
            .firstOrNull()
        val mpesaConfig = mpesaRow?.let {
            MpesaConfig(
                businessNo = it[MpesaConfigTable.businessNo],
                accountNo = it[MpesaConfigTable.accountNo],
                instructions = it[MpesaConfigTable.instructions]
            )
        }

        val paypalRow = PaypalConfigTable.selectAll()
            .where { PaypalConfigTable.agencyId eq agencyId }
            .firstOrNull()
        val paypalConfig = paypalRow?.let {
            PaypalConfig(
                email = it[PaypalConfigTable.email],
                instructions = it[PaypalConfigTable.instructions]
            )
        }

        val banks = BankConfigTable.selectAll()
            .where { BankConfigTable.agencyId eq agencyId }
            .map { BankConfig(it[BankConfigTable.bankId], it[BankConfigTable.bankName], it[BankConfigTable.enabled]) }

        PaymentMethodsConfig(methods = methods, mpesaConfig = mpesaConfig, paypalConfig = paypalConfig, banks = banks)
    }

    suspend fun toggleMethod(agencyId: UUID, methodId: String, enabled: Boolean) = dbQuery {
        seedMethodsIfAbsent(agencyId)
        val now: Instant = Clock.System.now()
        PaymentMethodsConfigTable.update({
            (PaymentMethodsConfigTable.agencyId eq agencyId) and
            (PaymentMethodsConfigTable.methodId eq methodId)
        }) {
            it[PaymentMethodsConfigTable.enabled] = enabled
            it[updatedAt] = now
        }
    }

    suspend fun updateMpesaConfig(agencyId: UUID, req: UpdateMpesaConfigRequest) = dbQuery {
        val existing = MpesaConfigTable.selectAll()
            .where { MpesaConfigTable.agencyId eq agencyId }
            .firstOrNull()
        if (existing == null) {
            MpesaConfigTable.insert {
                it[MpesaConfigTable.agencyId] = agencyId
                it[businessNo] = req.businessNo
                it[accountNo] = req.accountNo
                it[instructions] = req.instructions
            }
        } else {
            MpesaConfigTable.update({ MpesaConfigTable.agencyId eq agencyId }) {
                it[businessNo] = req.businessNo
                it[accountNo] = req.accountNo
                it[instructions] = req.instructions
            }
        }
    }

    suspend fun updatePaypalConfig(agencyId: UUID, req: UpdatePaypalConfigRequest) = dbQuery {
        val existing = PaypalConfigTable.selectAll()
            .where { PaypalConfigTable.agencyId eq agencyId }
            .firstOrNull()
        if (existing == null) {
            PaypalConfigTable.insert {
                it[PaypalConfigTable.agencyId] = agencyId
                it[email] = req.email
                it[instructions] = req.instructions
            }
        } else {
            PaypalConfigTable.update({ PaypalConfigTable.agencyId eq agencyId }) {
                it[email] = req.email
                it[instructions] = req.instructions
            }
        }
    }

    suspend fun toggleBank(agencyId: UUID, bankId: String, enabled: Boolean) = dbQuery {
        BankConfigTable.update({
            (BankConfigTable.agencyId eq agencyId) and
            (BankConfigTable.bankId eq bankId)
        }) {
            it[BankConfigTable.enabled] = enabled
        }
    }

    suspend fun bulkUpdateBanks(agencyId: UUID, banks: List<BankToggle>) = dbQuery {
        banks.forEach { bank ->
            BankConfigTable.update({
                (BankConfigTable.agencyId eq agencyId) and
                (BankConfigTable.bankId eq bank.id)
            }) {
                it[enabled] = bank.enabled
            }
        }
    }

    private fun seedMethodsIfAbsent(agencyId: UUID) {
        val existing = PaymentMethodsConfigTable.selectAll()
            .where { PaymentMethodsConfigTable.agencyId eq agencyId }
            .count()
        if (existing == 0L) {
            val now: Instant = Clock.System.now()
            DEFAULT_METHODS.forEach { methodId ->
                PaymentMethodsConfigTable.insert {
                    it[PaymentMethodsConfigTable.agencyId] = agencyId
                    it[PaymentMethodsConfigTable.methodId] = methodId
                    it[enabled] = false
                    it[updatedAt] = now
                }
            }
        }
    }

    private fun seedBanksIfAbsent(agencyId: UUID) {
        val existing = BankConfigTable.selectAll()
            .where { BankConfigTable.agencyId eq agencyId }
            .count()
        if (existing == 0L) {
            KENYAN_BANKS.forEach { (bankId, bankName) ->
                BankConfigTable.insert {
                    it[BankConfigTable.agencyId] = agencyId
                    it[BankConfigTable.bankId] = bankId
                    it[BankConfigTable.bankName] = bankName
                    it[enabled] = false
                }
            }
        }
    }
}
