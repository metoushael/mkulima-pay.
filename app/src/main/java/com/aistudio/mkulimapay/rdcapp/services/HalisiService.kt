package com.aistudio.mkulimapay.rdcapp.services

import android.graphics.BitmapFactory
import android.util.Log
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.CreditIntakeData
import com.aistudio.mkulimapay.rdcapp.model.CropType
import com.aistudio.mkulimapay.rdcapp.model.RepaymentInstallment
import com.aistudio.mkulimapay.rdcapp.model.RiskIconType
import com.aistudio.mkulimapay.rdcapp.model.RiskLevel
import com.aistudio.mkulimapay.rdcapp.model.RiskReason
import com.aistudio.mkulimapay.rdcapp.model.UserProfile
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val TAG = "MockHalisiService"

enum class FaceFailureReason {
    NONE,
    NO_FACE,
    MULTIPLE_FACES,
    EYES_CLOSED,
    LIVENESS_FAILED,
    MOCK_FORCED_FAILURE
}

enum class MockMode {
    NORMAL,
    ECHEC_VISAGE,
    ECHEC_RESEAU,
    LENT
}

object MockConfig {
    @Volatile
    var mode: MockMode = MockMode.NORMAL
}

data class FaceAnalysisResult(
    val faceCount: Int,
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?
)

data class EnrollmentResponse(
    val userId: String,
    val status: String
)

data class FaceVerificationResponse(
    val match: Boolean,
    val score: Int,
    val livenessPassed: Boolean,
    val failureReason: FaceFailureReason = FaceFailureReason.NONE,
    val faceCount: Int = 0
)

data class CreditEvaluationResponse(
    val requestId: String,
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val monthlyInstallmentUsd: Int,
    val reasons: List<RiskReason>,
    val schedule: List<RepaymentInstallment>,
    val advice: List<String>
)

class HalisiServiceException(message: String) : Exception(message)

interface HalisiService {
    suspend fun enrollUser(
        profile: UserProfile,
        selfieBytes: ByteArray
    ): Result<EnrollmentResponse>

    suspend fun verifyFace(
        userId: String,
        referenceSelfieBytes: ByteArray?,
        capturedSelfieBytes: ByteArray
    ): Result<FaceVerificationResponse>

    suspend fun submitCreditRequest(
        userId: String,
        data: CreditIntakeData,
        language: AppLanguage
    ): Result<CreditEvaluationResponse>
}

// TODO: remplacer par HalisiApiService quand l'API Halisi est disponible
fun provideHalisiService(): HalisiService = MockHalisiService()

/**
 * SIMULATION locale en attendant l'API Halisi réelle.
 * - Utilise ML Kit Face Detection sur l'appareil pour détecter le nombre de visages et l'ouverture des yeux.
 * - Calcule ensuite un score de correspondance simulé (80–98 %) si les contrôles passent.
 * - Permet au développeur de simuler des échecs et latences via [MockConfig.mode].
 */
// TODO: remplacer par HalisiApiService quand l'API Halisi est disponible
class MockHalisiService(
    private val faceAnalyzer: suspend (ByteArray) -> FaceAnalysisResult = ::analyzeFaceWithMlKit
) : HalisiService {

    private suspend fun simulateNetworkLatencyAndFailures(): HalisiServiceException? {
        val currentMode = MockConfig.mode
        val delayMs = if (currentMode == MockMode.LENT) 3200L else 1200L
        delay(delayMs)
        return if (currentMode == MockMode.ECHEC_RESEAU) {
            HalisiServiceException("NETWORK_SIMULATION_ERROR")
        } else {
            null
        }
    }

    override suspend fun enrollUser(
        profile: UserProfile,
        selfieBytes: ByteArray
    ): Result<EnrollmentResponse> {
        simulateNetworkLatencyAndFailures()?.let { return Result.failure(it) }

        if (profile.userId.isBlank()) {
            return Result.failure(HalisiServiceException("AUTHENTICATED_UID_REQUIRED"))
        }
        if (profile.nom.isBlank() || profile.prenom.isBlank() || profile.idNumber.trim().length < 4) {
            return Result.failure(HalisiServiceException("INVALID_IDENTITY_PROFILE"))
        }
        if (selfieBytes.isEmpty()) {
            return Result.failure(HalisiServiceException("SELFIE_BYTES_REQUIRED"))
        }
        return Result.success(
            EnrollmentResponse(
                userId = profile.userId,
                status = "PENDING"
            )
        )
    }

    override suspend fun verifyFace(
        userId: String,
        referenceSelfieBytes: ByteArray?,
        capturedSelfieBytes: ByteArray
    ): Result<FaceVerificationResponse> {
        if (userId.isBlank() || capturedSelfieBytes.isEmpty()) {
            return Result.failure(HalisiServiceException("INVALID_BIOMETRIC_INPUT"))
        }

        simulateNetworkLatencyAndFailures()?.let { return Result.failure(it) }

        if (MockConfig.mode == MockMode.ECHEC_VISAGE) {
            return Result.success(
                FaceVerificationResponse(
                    match = false,
                    score = 42,
                    livenessPassed = false,
                    failureReason = FaceFailureReason.MOCK_FORCED_FAILURE,
                    faceCount = 1
                )
            )
        }

        val analysis = try {
            faceAnalyzer(capturedSelfieBytes)
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit face detection failed", e)
            return Result.failure(HalisiServiceException("MLKIT_FACE_DETECTION_FAILED"))
        }

        if (analysis.faceCount == 0) {
            return Result.success(
                FaceVerificationResponse(
                    match = false,
                    score = 0,
                    livenessPassed = false,
                    failureReason = FaceFailureReason.NO_FACE,
                    faceCount = 0
                )
            )
        }

        if (analysis.faceCount > 1) {
            return Result.success(
                FaceVerificationResponse(
                    match = false,
                    score = 0,
                    livenessPassed = false,
                    failureReason = FaceFailureReason.MULTIPLE_FACES,
                    faceCount = analysis.faceCount
                )
            )
        }

        val leftEye = analysis.leftEyeOpenProbability
        val rightEye = analysis.rightEyeOpenProbability
        if (leftEye == null || rightEye == null) {
            return Result.success(
                FaceVerificationResponse(
                    match = false,
                    score = 52,
                    livenessPassed = false,
                    failureReason = FaceFailureReason.LIVENESS_FAILED,
                    faceCount = 1
                )
            )
        }

        val eyesOpen = leftEye >= 0.45f && rightEye >= 0.45f
        val blinkDetected = max(leftEye, rightEye) >= 0.60f && min(leftEye, rightEye) < 0.35f
        val livenessPassed = eyesOpen || blinkDetected

        if (!livenessPassed) {
            return Result.success(
                FaceVerificationResponse(
                    match = false,
                    score = 55,
                    livenessPassed = false,
                    failureReason = FaceFailureReason.EYES_CLOSED,
                    faceCount = 1
                )
            )
        }

        val eyeConfidence = ((leftEye + rightEye) / 2f).coerceIn(0f, 1f)
        val hasReference = referenceSelfieBytes != null && referenceSelfieBytes.isNotEmpty()
        val refBonus = if (hasReference) 4 else 0
        val simulatedScore = (80 + (eyeConfidence * 14f).roundToInt() + refBonus).coerceIn(80, 98)

        return Result.success(
            FaceVerificationResponse(
                match = true,
                score = simulatedScore,
                livenessPassed = true,
                failureReason = FaceFailureReason.NONE,
                faceCount = 1
            )
        )
    }

    override suspend fun submitCreditRequest(
        userId: String,
        data: CreditIntakeData,
        language: AppLanguage
    ): Result<CreditEvaluationResponse> {
        simulateNetworkLatencyAndFailures()?.let { return Result.failure(it) }

        if (userId.isBlank()) {
            return Result.failure(HalisiServiceException("AUTHENTICATED_UID_REQUIRED"))
        }

        val safeIncome = data.monthlyIncomeUsd.coerceAtLeast(1)
        val totalEstimatedCapacity = safeIncome * data.durationMonths * 0.42f
        val debtRatio = data.requestedAmountUsd / totalEstimatedCapacity.coerceAtLeast(1f)

        var score = 74
        if (debtRatio <= 0.65f) score += 14
        else if (debtRatio <= 1.0f) score += 4
        else if (debtRatio <= 1.35f) score -= 14
        else score -= 28

        if (data.experienceYears >= 5) score += 10
        else if (data.experienceYears >= 2) score += 4
        else score -= 8

        if (data.areaHectares >= 2.0f) score += 6
        else if (data.areaHectares < 0.75f) score -= 6

        when (data.crop) {
            CropType.MAIS, CropType.MANIOC, CropType.HARICOT -> score += 5
            CropType.CAFE, CropType.RIZ -> score += 3
            else -> score += 0
        }

        val finalScore = score.coerceIn(22, 97)
        val riskLevel = when {
            finalScore >= 72 -> RiskLevel.FAIBLE
            finalScore >= 48 -> RiskLevel.MOYEN
            else -> RiskLevel.ELEVE
        }

        val totalWithInterest = (data.requestedAmountUsd * (1.0 + 0.018 * data.durationMonths))
        val avgMonthly = (totalWithInterest / data.durationMonths).roundToInt().coerceAtLeast(1)

        val reasons = buildReasons(data, debtRatio, language)
        val schedule = buildHarvestSchedule(data, totalWithInterest.roundToInt(), language)
        val advice = buildAdvice(riskLevel, language)

        val generatedRequestId = "REQ_${System.currentTimeMillis().toString(36).uppercase(Locale.US)}"

        return Result.success(
            CreditEvaluationResponse(
                requestId = generatedRequestId,
                riskLevel = riskLevel,
                riskScore = finalScore,
                monthlyInstallmentUsd = avgMonthly,
                reasons = reasons,
                schedule = schedule,
                advice = advice
            )
        )
    }

    private fun buildReasons(
        data: CreditIntakeData,
        debtRatio: Float,
        lang: AppLanguage
    ): List<RiskReason> {
        val cropName = AppStrings.cropLabel(data.crop, lang)
        val harvestMonth = AppStrings.months(lang)[data.harvestMonthIndex.coerceIn(0, 11)]
        return if (lang == AppLanguage.FR) {
            listOf(
                RiskReason(
                    title = "Ratio montant / revenu",
                    detail = if (debtRatio <= 0.95f)
                        "Votre revenu mensuel (${data.monthlyIncomeUsd} USD) couvre confortablement les échéances."
                    else
                        "Le montant demandé (${data.requestedAmountUsd} USD) est élevé par rapport au revenu déclaré.",
                    positive = debtRatio <= 0.95f,
                    iconType = RiskIconType.RATIO
                ),
                RiskReason(
                    title = "Culture : $cropName",
                    detail = "Forte demande locale sur les marchés de la RDC pour $cropName (${data.areaHectares} ha).",
                    positive = true,
                    iconType = RiskIconType.CROP
                ),
                RiskReason(
                    title = "Calendrier de récolte ($harvestMonth)",
                    detail = "Les plus grosses échéances sont alignées sur votre période de vente en $harvestMonth.",
                    positive = true,
                    iconType = RiskIconType.SEASON
                ),
                RiskReason(
                    title = "Expérience agricole",
                    detail = if (data.experienceYears >= 2)
                        "${data.experienceYears} ans de pratique confirment la maîtrise de votre parcelle."
                    else
                        "Expérience récente (${data.experienceYears} an) : un suivi agronomique est recommandé.",
                    positive = data.experienceYears >= 2,
                    iconType = RiskIconType.EXPERIENCE
                )
            )
        } else {
            listOf(
                RiskReason(
                    title = "Uwiano wa mkopo na kipato",
                    detail = if (debtRatio <= 0.95f)
                        "Kipato chako cha mwezi (${data.monthlyIncomeUsd} USD) kinatosha kulipa marejesho."
                    else
                        "Kiasi unachoomba (${data.requestedAmountUsd} USD) ni kikubwa ikilinganishwa na kipato.",
                    positive = debtRatio <= 0.95f,
                    iconType = RiskIconType.RATIO
                ),
                RiskReason(
                    title = "Zao: $cropName",
                    detail = "Soko la ndani nchini DRC lina mahitaji makubwa ya $cropName (${data.areaHectares} ha).",
                    positive = true,
                    iconType = RiskIconType.CROP
                ),
                RiskReason(
                    title = "Msimu wa mavuno ($harvestMonth)",
                    detail = "Malipo makubwa yamepangwa kulingana na mwezi wako wa mavuno ($harvestMonth).",
                    positive = true,
                    iconType = RiskIconType.SEASON
                ),
                RiskReason(
                    title = "Uzoefu wa kilimo",
                    detail = if (data.experienceYears >= 2)
                        "Miaka ${data.experienceYears} ya kilimo inaonyesha uthabiti wa uzalishaji."
                    else
                        "Uzoefu wa mwanzo (${data.experienceYears} mwaka): ushauri wa kilimo unapendekezwa.",
                    positive = data.experienceYears >= 2,
                    iconType = RiskIconType.EXPERIENCE
                )
            )
        }
    }

    private fun buildHarvestSchedule(
        data: CreditIntakeData,
        totalUsd: Int,
        lang: AppLanguage
    ): List<RepaymentInstallment> {
        val months = AppStrings.months(lang)
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val count = data.durationMonths.coerceIn(3, 12)

        val weights = (1..count).map { step ->
            val mIdx = (currentMonth + step) % 12
            val isPeak = mIdx == data.harvestMonthIndex || mIdx == (data.harvestMonthIndex + 1) % 12
            if (isPeak) 1.65 else 0.72
        }
        val weightSum = weights.sum()

        var allocated = 0
        return (1..count).map { step ->
            val mIdx = (currentMonth + step) % 12
            val isPeak = mIdx == data.harvestMonthIndex || mIdx == (data.harvestMonthIndex + 1) % 12
            val amount = if (step == count) {
                (totalUsd - allocated).coerceAtLeast(1)
            } else {
                ((totalUsd * (weights[step - 1] / weightSum)).roundToInt()).coerceAtLeast(1)
            }
            allocated += amount
            val note = when {
                isPeak && lang == AppLanguage.FR -> "Échéance récolte (vente)"
                isPeak && lang == AppLanguage.SW -> "Malipo ya mavuno (mauzo)"
                lang == AppLanguage.FR -> "Période de croissance (allégée)"
                else -> "Kipindi cha ukuaji (malipo madogo)"
            }
            RepaymentInstallment(
                monthLabel = months[mIdx],
                amountUsd = amount,
                harvestPeak = isPeak,
                note = note
            )
        }
    }

    private fun buildAdvice(
        riskLevel: RiskLevel,
        lang: AppLanguage
    ): List<String> {
        return if (lang == AppLanguage.FR) {
            buildList {
                add("Mettez de côté 10 % des ventes dès la récolte dans une épargne mobile dédiée au remboursement.")
                add("Diversifiez avec une culture à cycle court (haricot ou maraîchage) pour sécuriser votre trésorerie.")
                add("Utilisez des sacs hermétiques ou un grenier surélevé pour stocker 30 % de la récolte et vendre à meilleur prix.")
                if (riskLevel == RiskLevel.ELEVE) {
                    add("Astuce : réduire le montant de 25 % ou allonger la durée à 9 mois baissera fortement vos mensualités.")
                }
            }
        } else {
            buildList {
                add("Weka akiba ya asilimia 10 ya mauzo mara tu baada ya mavuno kwa ajili ya marejesho.")
                add("Changanya na zao la muda mfupi (kama maharagwe) ili kuimarisha mapato ya kila mwezi.")
                add("Hifadhi sehemu ya mavuno kwenye mifuko bora ili kuuza wakati bei sokoni iko juu.")
                if (riskLevel == RiskLevel.ELEVE) {
                    add("Kidokezo: kupunguza kiasi kwa 25% au kuongeza muda hadi miezi 9 kutapunguza mzigo wa mwezi.")
                }
            }
        }
    }
}

private suspend fun analyzeFaceWithMlKit(imageBytes: ByteArray): FaceAnalysisResult {
    val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, boundsOpts)
    if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) {
        return FaceAnalysisResult(faceCount = 0, leftEyeOpenProbability = null, rightEyeOpenProbability = null)
    }

    var sampleSize = 1
    while (boundsOpts.outWidth / sampleSize > 1024 || boundsOpts.outHeight / sampleSize > 1024) {
        sampleSize *= 2
    }

    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, decodeOpts)
        ?: return FaceAnalysisResult(faceCount = 0, leftEyeOpenProbability = null, rightEyeOpenProbability = null)

    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)
        .build()

    val detector = FaceDetection.getClient(options)
    return try {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val faces = suspendCancellableCoroutine { cont ->
            detector.process(inputImage)
                .addOnSuccessListener { result ->
                    if (cont.isActive) cont.resume(result)
                }
                .addOnFailureListener { error ->
                    if (cont.isActive) cont.resumeWithException(error)
                }
        }
        val firstFace = faces.firstOrNull()
        FaceAnalysisResult(
            faceCount = faces.size,
            leftEyeOpenProbability = firstFace?.leftEyeOpenProbability,
            rightEyeOpenProbability = firstFace?.rightEyeOpenProbability
        )
    } finally {
        runCatching { detector.close() }
        bitmap.recycle()
    }
}
