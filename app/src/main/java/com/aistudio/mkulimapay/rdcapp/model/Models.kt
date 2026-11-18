package com.aistudio.mkulimapay.rdcapp.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AppLanguage(val code: String, val displayLabel: String) {
    FR("fr", "Français"),
    SW("sw", "Kiswahili")
}

enum class ThemePreference {
    AUTO,
    LIGHT,
    DARK
}

enum class IdentityStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED
}

enum class CreditStatus {
    IN_PROGRESS,
    ACCEPTED,
    REJECTED
}

enum class RiskLevel {
    FAIBLE,
    MOYEN,
    ELEVE
}

enum class IdDocumentType {
    VOTER_CARD,
    PASSPORT,
    DRIVER_LICENSE,
    ATTESTATION
}

enum class AgriculturalSector {
    VIVRIER,
    MARAICHAGE,
    CAFE_CACAO,
    ELEVAGE,
    AGROFORESTERIE,
    AUTRE
}

enum class CropType {
    MAIS,
    MANIOC,
    HARICOT,
    RIZ,
    CAFE,
    BANANE,
    AUTRE
}

enum class CreditPurpose {
    SEMENCES,
    OUTILS,
    ENGRAIS,
    STOCKAGE,
    MAIN_OEUVRE
}

enum class RiskIconType {
    RATIO,
    CROP,
    SEASON,
    EXPERIENCE
}

enum class TransactionType {
    DISBURSEMENT,
    REPAYMENT
}

enum class TransactionStatus {
    COMPLETED,
    PENDING
}

@Keep
@IgnoreExtraProperties
data class FarmerProfile(
    val userId: String = "",
    val email: String? = null,
    val nom: String = "",
    val postnom: String = "",
    val prenom: String = "",
    val dateOfBirth: String = "",
    val sex: String = "M",
    val phone: String = "",
    val province: String = "",
    val location: String = "",
    val agriculturalSector: AgriculturalSector = AgriculturalSector.VIVRIER,
    val idType: IdDocumentType = IdDocumentType.VOTER_CARD,
    val idNumber: String = "",
    val localIdPhotoUri: String? = null,
    val localSelfieUri: String? = null,
    val hasIdPhoto: Boolean = false,
    val hasSelfie: Boolean = false,
    val identityStatus: IdentityStatus = IdentityStatus.UNVERIFIED,
    val lastVerificationScore: Int? = null,
    val verifiedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val fullName: String
        get() = listOf(prenom, nom, postnom).filter { it.isNotBlank() }.joinToString(" ")

    val formattedLocation: String
        get() = listOf(location.trim(), province.trim()).filter { it.isNotBlank() }.joinToString(", ")

    val hasEnrolledProfile: Boolean
        get() = userId.isNotBlank() && nom.isNotBlank() && prenom.isNotBlank() && hasSelfie

    val enrolledAtMillis: Long
        get() = createdAt?.toDate()?.time ?: 0L
}

typealias UserProfile = FarmerProfile

@Keep
@IgnoreExtraProperties
data class CreditIntakeData(
    val crop: CropType = CropType.MAIS,
    val areaHectares: Float = 1.5f,
    val harvestMonthIndex: Int = 6,
    val experienceYears: Int = 3,
    val monthlyIncomeUsd: Int = 150,
    val requestedAmountUsd: Int = 300,
    val durationMonths: Int = 6,
    val purpose: CreditPurpose = CreditPurpose.SEMENCES
)

@Keep
@IgnoreExtraProperties
data class RiskReason(
    val title: String = "",
    val detail: String = "",
    val positive: Boolean = true,
    val iconType: RiskIconType = RiskIconType.RATIO
)

@Keep
@IgnoreExtraProperties
data class RepaymentInstallment(
    val monthLabel: String = "",
    val amountUsd: Int = 0,
    val harvestPeak: Boolean = false,
    val note: String = ""
)

@Keep
@IgnoreExtraProperties
data class CreditRequest(
    @DocumentId val requestId: String = "",
    val userId: String = "",
    val data: CreditIntakeData = CreditIntakeData(),
    val riskLevel: RiskLevel = RiskLevel.MOYEN,
    val riskScore: Int = 0,
    val monthlyInstallmentUsd: Int = 0,
    val reasons: List<RiskReason> = emptyList(),
    val schedule: List<RepaymentInstallment> = emptyList(),
    val advice: List<String> = emptyList(),
    val status: CreditStatus = CreditStatus.IN_PROGRESS,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val createdAtMillis: Long
        get() = createdAt?.toDate()?.time ?: 0L
}

@Keep
@IgnoreExtraProperties
data class FinancialTransaction(
    @DocumentId val txId: String = "",
    val userId: String = "",
    val requestId: String = "",
    val type: TransactionType = TransactionType.DISBURSEMENT,
    val amountUsd: Int = 0,
    val channel: String = "M-Pesa",
    val reference: String = "",
    val description: String = "",
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val createdAtMillis: Long
        get() = createdAt?.toDate()?.time ?: 0L
}

enum class ChatWidgetType {
    NONE,
    CROP_SELECTOR,
    AREA_SLIDER,
    HARVEST_MONTH_SELECTOR,
    EXPERIENCE_SELECTOR,
    INCOME_INPUT,
    AMOUNT_SLIDER,
    DURATION_SELECTOR,
    PURPOSE_SELECTOR
}

data class ChatMessage(
    val id: String,
    val isFromAssistant: Boolean,
    val text: String,
    val timestamp: String,
    val widgetType: ChatWidgetType = ChatWidgetType.NONE,
    val isOfflineFallback: Boolean = false
)

data class AppSettings(
    val language: AppLanguage = AppLanguage.FR,
    val themePreference: ThemePreference = ThemePreference.AUTO,
    val largeText: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false
)

enum class CreditNotificationType {
    CREDIT_ACCEPTED,
    CREDIT_IN_PROGRESS,
    CREDIT_REJECTED,
    DISBURSEMENT,
    REPAYMENT
}

data class CreditNotificationItem(
    val id: String,
    val requestId: String,
    val type: CreditNotificationType,
    val titleFr: String,
    val titleSw: String,
    val messageFr: String,
    val messageSw: String,
    val timestampMillis: Long
) {
    fun localizedTitle(language: AppLanguage): String =
        if (language == AppLanguage.FR) titleFr else titleSw

    fun localizedMessage(language: AppLanguage): String =
        if (language == AppLanguage.FR) messageFr else messageSw
}

data class OfflineCacheSnapshot(
    val userId: String = "",
    val fullName: String = "",
    val prenom: String = "",
    val formattedLocation: String = "",
    val agriculturalSector: AgriculturalSector = AgriculturalSector.VIVRIER,
    val identityStatus: IdentityStatus = IdentityStatus.UNVERIFIED,
    val lastVerificationScore: Int? = null,
    val verifiedAtMillis: Long? = null,
    val hasEnrolledProfile: Boolean = false,
    val notifications: List<CreditNotificationItem> = emptyList(),
    val lastSyncedAtMillis: Long = 0L
)

