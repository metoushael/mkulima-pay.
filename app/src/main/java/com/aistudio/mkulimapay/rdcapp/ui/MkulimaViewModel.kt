package com.aistudio.mkulimapay.rdcapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.mkulimapay.rdcapp.data.AppRepository
import com.aistudio.mkulimapay.rdcapp.data.LocalBiometricStorage
import com.aistudio.mkulimapay.rdcapp.data.OfflineCacheRepository
import com.aistudio.mkulimapay.rdcapp.data.UiState
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.model.AgriculturalSector
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.ChatMessage
import com.aistudio.mkulimapay.rdcapp.model.ChatWidgetType
import com.aistudio.mkulimapay.rdcapp.model.CreditIntakeData
import com.aistudio.mkulimapay.rdcapp.model.CreditNotificationItem
import com.aistudio.mkulimapay.rdcapp.model.CreditPurpose
import com.aistudio.mkulimapay.rdcapp.model.CreditRequest
import com.aistudio.mkulimapay.rdcapp.model.CreditStatus
import com.aistudio.mkulimapay.rdcapp.model.CropType
import com.aistudio.mkulimapay.rdcapp.model.FinancialTransaction
import com.aistudio.mkulimapay.rdcapp.model.IdDocumentType
import com.aistudio.mkulimapay.rdcapp.model.IdentityStatus
import com.aistudio.mkulimapay.rdcapp.model.OfflineCacheSnapshot
import com.aistudio.mkulimapay.rdcapp.model.UserProfile
import com.aistudio.mkulimapay.rdcapp.services.CreditAssistantAiService
import com.aistudio.mkulimapay.rdcapp.services.FaceFailureReason
import com.aistudio.mkulimapay.rdcapp.services.HalisiService
import com.aistudio.mkulimapay.rdcapp.services.provideHalisiService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class EnrollmentUiState(
    val step: Int = 0,
    val nom: String = "",
    val postnom: String = "",
    val prenom: String = "",
    val dateOfBirth: String = "",
    val sex: String = "M",
    val phoneDigits: String = "",
    val province: String = AppStrings.drcProvinces.first(),
    val location: String = "",
    val agriculturalSector: AgriculturalSector = AgriculturalSector.VIVRIER,
    val idType: IdDocumentType = IdDocumentType.VOTER_CARD,
    val idNumber: String = "",
    val idPhotoUri: String = "",
    val selfieUri: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

enum class FaceVerifyOutcome {
    IDLE,
    SUCCESS,
    FAILURE,
    MAX_ATTEMPTS
}

data class FaceVerifyUiState(
    val isScanning: Boolean = false,
    val attemptsRemaining: Int = 3,
    val lastScore: Int? = null,
    val livenessPassed: Boolean = false,
    val outcome: FaceVerifyOutcome = FaceVerifyOutcome.IDLE,
    val failureReason: FaceFailureReason = FaceFailureReason.NONE,
    val capturedUri: String = "",
    val errorMessage: String? = null
)

data class CreditAssistantUiState(
    val questionIndex: Int = 0,
    val intake: CreditIntakeData = CreditIntakeData(),
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val isCalculating: Boolean = false,
    val errorMessage: String? = null
)

class MkulimaViewModel(
    private val repository: AppRepository,
    private val currentUserId: String,
    private val currentUserEmail: String?,
    private val readUriBytes: suspend (String) -> ByteArray,
    private val biometricStorage: LocalBiometricStorage? = null,
    private val offlineCacheRepository: OfflineCacheRepository? = null,
    isOnlineFlow: Flow<Boolean> = flowOf(true),
    private val halisiService: HalisiService = provideHalisiService(),
    private val aiService: CreditAssistantAiService = CreditAssistantAiService()
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    val offlineCacheState: StateFlow<OfflineCacheSnapshot?> = (
        offlineCacheRepository?.observeCache(currentUserId) ?: flowOf(null)
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private val liveUserProfileFlow: Flow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { profile ->
            val enriched = profile?.copy(
                localSelfieUri = biometricStorage?.getSelfieUriIfExists() ?: profile.localSelfieUri,
                localIdPhotoUri = biometricStorage?.getIdDocUriIfExists() ?: profile.localIdPhotoUri
            )
            if (enriched != null) {
                offlineCacheRepository?.cacheFarmerIdentity(enriched)
            }
            UiState.Success(enriched)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user profile", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load profile"))
        }

    val userProfileState: StateFlow<UiState<UserProfile?>> = combine(
        liveUserProfileFlow.onStart { emit(UiState.Loading) },
        offlineCacheState,
        isOnline
    ) { liveState, cache, online ->
        when {
            liveState is UiState.Success && (liveState.data != null || online) -> liveState
            cache != null && (liveState is UiState.Loading || liveState is UiState.Error || !online) -> {
                UiState.Success(
                    cache.toFallbackProfile(
                        email = currentUserEmail,
                        localSelfieUri = biometricStorage?.getSelfieUriIfExists(),
                        localIdPhotoUri = biometricStorage?.getIdDocUriIfExists()
                    )
                )
            }
            else -> liveState
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val creditRequestsState: StateFlow<UiState<List<CreditRequest>>> = repository.observeCreditRequests(currentUserId)
        .map<List<CreditRequest>, UiState<List<CreditRequest>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing credit requests", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load credit requests"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val transactionsState: StateFlow<UiState<List<FinancialTransaction>>> = repository.observeTransactions(currentUserId)
        .map<List<FinancialTransaction>, UiState<List<FinancialTransaction>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing transactions", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load transactions"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val creditNotifications: StateFlow<List<CreditNotificationItem>> = combine(
        creditRequestsState,
        transactionsState,
        offlineCacheState
    ) { reqState, txState, cache ->
        val liveReqs = (reqState as? UiState.Success)?.data
        val liveTxs = (txState as? UiState.Success)?.data
        when {
            liveReqs != null && liveTxs != null -> {
                val liveItems = OfflineCacheRepository.buildCreditNotifications(liveReqs, liveTxs)
                if (liveItems.isNotEmpty() || cache == null) liveItems else cache.notifications
            }
            cache != null -> cache.notifications
            liveReqs != null || liveTxs != null ->
                OfflineCacheRepository.buildCreditNotifications(liveReqs.orEmpty(), liveTxs.orEmpty())
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    init {
        viewModelScope.launch {
            combine(creditRequestsState, transactionsState) { reqState, txState ->
                val reqs = (reqState as? UiState.Success)?.data
                val txs = (txState as? UiState.Success)?.data
                if (reqs != null && txs != null) reqs to txs else null
            }.collect { pair ->
                if (pair != null) {
                    offlineCacheRepository?.cacheCreditNotifications(
                        userId = currentUserId,
                        requests = pair.first,
                        transactions = pair.second
                    )
                }
            }
        }
    }

    private val _enrollment = MutableStateFlow(EnrollmentUiState())
    val enrollment: StateFlow<EnrollmentUiState> = _enrollment.asStateFlow()

    private val _faceVerify = MutableStateFlow(FaceVerifyUiState())
    val faceVerify: StateFlow<FaceVerifyUiState> = _faceVerify.asStateFlow()

    private val _assistant = MutableStateFlow(CreditAssistantUiState())
    val assistant: StateFlow<CreditAssistantUiState> = _assistant.asStateFlow()

    private val _activeCreditResult = MutableStateFlow<CreditRequest?>(null)
    val activeCreditResult: StateFlow<CreditRequest?> = _activeCreditResult.asStateFlow()

    fun currentProfileOrNull(): UserProfile? =
        (userProfileState.value as? UiState.Success)?.data

    // --- Enrollment ---
    fun initEnrollmentFromProfile() {
        val p = currentProfileOrNull()
        _enrollment.value = EnrollmentUiState(
            step = 0,
            nom = p?.nom.orEmpty(),
            postnom = p?.postnom.orEmpty(),
            prenom = p?.prenom.orEmpty(),
            dateOfBirth = p?.dateOfBirth.orEmpty(),
            sex = p?.sex?.ifBlank { "M" } ?: "M",
            phoneDigits = p?.phone?.removePrefix("+243")?.trim().orEmpty(),
            province = p?.province?.ifBlank { AppStrings.drcProvinces.first() } ?: AppStrings.drcProvinces.first(),
            location = p?.location.orEmpty(),
            agriculturalSector = p?.agriculturalSector ?: AgriculturalSector.VIVRIER,
            idType = p?.idType ?: IdDocumentType.VOTER_CARD,
            idNumber = p?.idNumber.orEmpty(),
            idPhotoUri = p?.localIdPhotoUri.orEmpty(),
            selfieUri = p?.localSelfieUri.orEmpty()
        )
    }

    fun updateEnrollment(transform: (EnrollmentUiState) -> EnrollmentUiState) {
        _enrollment.update { transform(it).copy(errorMessage = null) }
    }

    fun isEnrollmentStep1Valid(): Boolean {
        val e = _enrollment.value
        val digits = e.phoneDigits.filter { it.isDigit() }
        return e.nom.isNotBlank() &&
            e.postnom.isNotBlank() &&
            e.prenom.isNotBlank() &&
            e.dateOfBirth.isNotBlank() &&
            digits.length == 9 &&
            e.province.isNotBlank() &&
            e.location.trim().length >= 2
    }

    fun isEnrollmentStep2Valid(): Boolean {
        return _enrollment.value.idNumber.trim().length >= 4
    }

    fun submitEnrollment(language: AppLanguage) {
        val e = _enrollment.value
        val strings = AppStrings.get(language)

        if (e.selfieUri.isBlank()) {
            _enrollment.update { it.copy(errorMessage = strings.errSelfieRequired) }
            return
        }

        viewModelScope.launch {
            _enrollment.update { it.copy(isSubmitting = true, errorMessage = null) }
            val privateSelfieUri = biometricStorage?.saveSelfieFromUri(e.selfieUri) ?: e.selfieUri
            val privateIdPhotoUri = e.idPhotoUri.takeIf { it.isNotBlank() }?.let { uri ->
                biometricStorage?.saveIdDocFromUri(uri) ?: uri
            }
            val selfieBytes = readUriBytes(privateSelfieUri)
            if (selfieBytes.isEmpty()) {
                _enrollment.update {
                    it.copy(isSubmitting = false, errorMessage = strings.errSelfieRequired)
                }
                return@launch
            }

            if (privateSelfieUri != e.selfieUri) {
                biometricStorage?.deleteTemporaryCapture(e.selfieUri)
            }
            if (!privateIdPhotoUri.isNullOrBlank() && privateIdPhotoUri != e.idPhotoUri) {
                biometricStorage?.deleteTemporaryCapture(e.idPhotoUri)
            }

            val profile = UserProfile(
                userId = currentUserId,
                email = currentUserEmail,
                nom = e.nom.trim(),
                postnom = e.postnom.trim(),
                prenom = e.prenom.trim(),
                dateOfBirth = e.dateOfBirth,
                sex = e.sex,
                phone = "+243${e.phoneDigits.filter { it.isDigit() }}",
                province = e.province,
                location = e.location.trim(),
                agriculturalSector = e.agriculturalSector,
                idType = e.idType,
                idNumber = e.idNumber.trim(),
                localIdPhotoUri = privateIdPhotoUri,
                localSelfieUri = privateSelfieUri,
                hasIdPhoto = !privateIdPhotoUri.isNullOrBlank(),
                hasSelfie = true,
                identityStatus = IdentityStatus.PENDING
            )

            val enrollResult = halisiService.enrollUser(profile, selfieBytes)
            if (enrollResult.isFailure) {
                _enrollment.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = enrollResult.exceptionOrNull()?.localizedMessage ?: strings.halisiErrorNetwork
                    )
                }
                return@launch
            }

            val saveResult = repository.saveOrUpdateProfile(profile)
            saveResult.onSuccess {
                _enrollment.update { it.copy(isSubmitting = false, isSuccess = true) }
            }.onFailure { err ->
                _enrollment.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = err.localizedMessage ?: strings.halisiErrorNetwork
                    )
                }
            }
        }
    }

    // --- Face Verification ---
    fun resetFaceVerify() {
        _faceVerify.value = FaceVerifyUiState()
    }

    fun verifyFaceFromCapture(uriString: String, language: AppLanguage) {
        val current = _faceVerify.value
        if (current.attemptsRemaining <= 0) {
            _faceVerify.update { it.copy(outcome = FaceVerifyOutcome.MAX_ATTEMPTS) }
            return
        }

        val profile = currentProfileOrNull()
        val strings = AppStrings.get(language)
        if (profile == null || !profile.hasEnrolledProfile) {
            _faceVerify.update {
                it.copy(errorMessage = strings.faceVerifyNoProfile)
            }
            return
        }

        viewModelScope.launch {
            _faceVerify.update {
                it.copy(
                    isScanning = true,
                    capturedUri = uriString,
                    errorMessage = null,
                    outcome = FaceVerifyOutcome.IDLE
                )
            }
            val capturedBytes = readUriBytes(uriString)
            if (capturedBytes.isEmpty()) {
                _faceVerify.update {
                    it.copy(isScanning = false, errorMessage = strings.errSelfieRequired)
                }
                return@launch
            }

            val referenceBytes = biometricStorage?.readSelfieBytes()?.takeIf { it.isNotEmpty() }
                ?: profile.localSelfieUri?.let { readUriBytes(it) }?.takeIf { it.isNotEmpty() }
            val result = try {
                halisiService.verifyFace(profile.userId, referenceBytes, capturedBytes)
            } finally {
                biometricStorage?.deleteTemporaryCapture(uriString)
            }

            result.onSuccess { res ->
                if (res.match && res.livenessPassed) {
                    val persistResult = repository.markIdentityVerified(res.score)
                    persistResult.onSuccess {
                        _faceVerify.update {
                            it.copy(
                                isScanning = false,
                                lastScore = res.score,
                                livenessPassed = res.livenessPassed,
                                outcome = FaceVerifyOutcome.SUCCESS,
                                failureReason = FaceFailureReason.NONE
                            )
                        }
                    }.onFailure { err ->
                        _faceVerify.update {
                            it.copy(
                                isScanning = false,
                                errorMessage = err.localizedMessage ?: strings.halisiErrorNetwork
                            )
                        }
                    }
                } else {
                    val remaining = (_faceVerify.value.attemptsRemaining - 1).coerceAtLeast(0)
                    _faceVerify.update {
                        it.copy(
                            isScanning = false,
                            attemptsRemaining = remaining,
                            lastScore = res.score,
                            livenessPassed = res.livenessPassed,
                            failureReason = res.failureReason,
                            outcome = if (remaining == 0) {
                                FaceVerifyOutcome.MAX_ATTEMPTS
                            } else {
                                FaceVerifyOutcome.FAILURE
                            }
                        )
                    }
                }
            }.onFailure { err ->
                _faceVerify.update {
                    it.copy(
                        isScanning = false,
                        errorMessage = err.localizedMessage ?: strings.halisiErrorNetwork
                    )
                }
            }
        }
    }

    // --- Credit Assistant ---
    fun startCreditSession(language: AppLanguage, prefill: CreditIntakeData? = null) {
        val strings = AppStrings.get(language)
        val initialIntake = prefill ?: CreditIntakeData()
        val nowTime = currentTimestamp()
        val firstMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromAssistant = true,
            text = strings.q1Crop,
            timestamp = nowTime,
            widgetType = ChatWidgetType.CROP_SELECTOR
        )
        _assistant.value = CreditAssistantUiState(
            questionIndex = 0,
            intake = initialIntake,
            messages = listOf(firstMessage),
            isTyping = false,
            isCalculating = false,
            errorMessage = null
        )
    }

    fun submitCropAnswer(crop: CropType, language: AppLanguage) {
        val label = AppStrings.cropLabel(crop, language)
        advanceAssistantStep(
            expectedStep = 0,
            userDisplayText = label,
            language = language,
            transformIntake = { it.copy(crop = crop) }
        )
    }

    fun submitAreaAnswer(hectares: Float, language: AppLanguage) {
        val unit = AppStrings.get(language).hectaresUnit
        val rounded = ((hectares * 4f).toInt() / 4f).coerceAtLeast(0.25f)
        advanceAssistantStep(
            expectedStep = 1,
            userDisplayText = "${String.format(Locale.US, "%.2f", rounded)} $unit",
            language = language,
            transformIntake = { it.copy(areaHectares = rounded) }
        )
    }

    fun submitHarvestMonthAnswer(monthIndex: Int, language: AppLanguage) {
        val monthName = AppStrings.months(language)[monthIndex.coerceIn(0, 11)]
        advanceAssistantStep(
            expectedStep = 2,
            userDisplayText = monthName,
            language = language,
            transformIntake = { it.copy(harvestMonthIndex = monthIndex.coerceIn(0, 11)) }
        )
    }

    fun submitExperienceAnswer(years: Int, language: AppLanguage) {
        val unit = AppStrings.get(language).yearsUnit
        advanceAssistantStep(
            expectedStep = 3,
            userDisplayText = "$years $unit",
            language = language,
            transformIntake = { it.copy(experienceYears = years.coerceIn(0, 90)) }
        )
    }

    fun submitIncomeAnswer(incomeUsd: Int, language: AppLanguage) {
        val clean = incomeUsd.coerceIn(20, 100_000)
        advanceAssistantStep(
            expectedStep = 4,
            userDisplayText = "$clean USD",
            language = language,
            transformIntake = { it.copy(monthlyIncomeUsd = clean) }
        )
    }

    fun submitAmountAnswer(amountUsd: Int, language: AppLanguage) {
        val clean = amountUsd.coerceIn(50, 50_000)
        advanceAssistantStep(
            expectedStep = 5,
            userDisplayText = "$clean USD",
            language = language,
            transformIntake = { it.copy(requestedAmountUsd = clean) }
        )
    }

    fun submitDurationAnswer(months: Int, language: AppLanguage) {
        val unit = AppStrings.get(language).monthsUnit
        val cleanMonths = months.coerceIn(3, 24)
        advanceAssistantStep(
            expectedStep = 6,
            userDisplayText = "$cleanMonths $unit",
            language = language,
            transformIntake = { it.copy(durationMonths = cleanMonths) }
        )
    }

    fun submitPurposeAnswer(purpose: CreditPurpose, language: AppLanguage) {
        val label = AppStrings.purposeLabel(purpose, language)
        advanceAssistantStep(
            expectedStep = 7,
            userDisplayText = label,
            language = language,
            transformIntake = { it.copy(purpose = purpose) }
        )
    }

    fun sendFreeTextOrVoice(text: String, language: AppLanguage) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val state = _assistant.value
        if (state.isTyping || state.isCalculating) return

        val step = state.questionIndex
        val number = Regex("\\d+(?:[.,]\\d+)?").find(trimmed)?.value?.replace(',', '.')?.toFloatOrNull()
        when (step) {
            1 -> if (number != null && number in 0.25f..50f) {
                submitAreaAnswer(number, language)
                return
            }
            3 -> if (number != null && number in 0f..60f) {
                submitExperienceAnswer(number.toInt(), language)
                return
            }
            4 -> if (number != null && number >= 10f) {
                submitIncomeAnswer(number.toInt(), language)
                return
            }
            5 -> if (number != null && number >= 20f) {
                submitAmountAnswer(number.toInt(), language)
                return
            }
            6 -> if (number != null && number in 3f..24f) {
                submitDurationAnswer(number.toInt(), language)
                return
            }
        }

        val strings = AppStrings.get(language)
        val currentQ = questionTextForStep(step, strings)
        val currentWidget = widgetForStep(step)

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromAssistant = false,
            text = trimmed,
            timestamp = currentTimestamp()
        )
        _assistant.update {
            it.copy(messages = it.messages + userMsg, isTyping = true, errorMessage = null)
        }

        viewModelScope.launch {
            val reply = aiService.answerFreeQuestion(trimmed, currentQ, language)
            val botMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                isFromAssistant = true,
                text = reply.text,
                timestamp = currentTimestamp(),
                widgetType = currentWidget,
                isOfflineFallback = reply.isOfflineFallback
            )
            _assistant.update {
                it.copy(messages = it.messages + botMsg, isTyping = false)
            }
        }
    }

    private fun advanceAssistantStep(
        expectedStep: Int,
        userDisplayText: String,
        language: AppLanguage,
        transformIntake: (CreditIntakeData) -> CreditIntakeData
    ) {
        val currentState = _assistant.value
        if (currentState.questionIndex != expectedStep || currentState.isTyping || currentState.isCalculating) {
            return
        }

        val strings = AppStrings.get(language)
        val updatedIntake = transformIntake(currentState.intake)

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromAssistant = false,
            text = userDisplayText,
            timestamp = currentTimestamp()
        )

        val nextStep = expectedStep + 1
        _assistant.update {
            it.copy(
                intake = updatedIntake,
                messages = it.messages + userMsg,
                isTyping = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            if (nextStep < 8) {
                val nextQ = questionTextForStep(nextStep, strings)
                val nextReply = aiService.generateNaturalStepAck(
                    stepIndex = expectedStep,
                    userReply = userDisplayText,
                    intake = updatedIntake,
                    nextQuestion = nextQ,
                    language = language
                )
                val botMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isFromAssistant = true,
                    text = nextReply.text,
                    timestamp = currentTimestamp(),
                    widgetType = widgetForStep(nextStep),
                    isOfflineFallback = nextReply.isOfflineFallback
                )
                _assistant.update {
                    it.copy(
                        questionIndex = nextStep,
                        messages = it.messages + botMsg,
                        isTyping = false
                    )
                }
            } else {
                val calcMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isFromAssistant = true,
                    text = strings.assistantCalculating,
                    timestamp = currentTimestamp(),
                    widgetType = ChatWidgetType.NONE
                )
                _assistant.update {
                    it.copy(
                        questionIndex = 8,
                        messages = it.messages + calcMsg,
                        isTyping = false,
                        isCalculating = true
                    )
                }
            }
        }
    }

    fun evaluateCreditRequest(language: AppLanguage, onSuccess: () -> Unit) {
        val intake = _assistant.value.intake
        val strings = AppStrings.get(language)

        viewModelScope.launch {
            _assistant.update { it.copy(isCalculating = true, errorMessage = null) }
            val result = halisiService.submitCreditRequest(currentUserId, intake, language)
            result.onSuccess { res ->
                val req = CreditRequest(
                    requestId = res.requestId,
                    userId = currentUserId,
                    data = intake,
                    riskLevel = res.riskLevel,
                    riskScore = res.riskScore,
                    monthlyInstallmentUsd = res.monthlyInstallmentUsd,
                    reasons = res.reasons,
                    schedule = res.schedule,
                    advice = res.advice,
                    status = CreditStatus.IN_PROGRESS
                )
                _activeCreditResult.value = req
                _assistant.update { it.copy(isCalculating = false) }
                onSuccess()
            }.onFailure { err ->
                _assistant.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = err.localizedMessage ?: strings.halisiErrorNetwork
                    )
                }
            }
        }
    }

    fun setActiveCreditResult(request: CreditRequest) {
        _activeCreditResult.value = request
    }

    fun acceptActiveCreditRequest(
        language: AppLanguage,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val active = _activeCreditResult.value ?: return
        val strings = AppStrings.get(language)
        val cropName = AppStrings.cropLabel(active.data.crop, language)
        val desc = if (language == AppLanguage.FR) {
            "Décaissement microcrédit $cropName (${active.data.durationMonths} mois)"
        } else {
            "Utoaji wa mkopo $cropName (miezi ${active.data.durationMonths})"
        }
        viewModelScope.launch {
            val result = repository.acceptCreditRequestWithDisbursement(
                request = active,
                channel = "M-Pesa",
                disbursementDescription = desc
            )
            result.onSuccess { savedId ->
                _activeCreditResult.value = active.copy(
                    requestId = savedId,
                    status = CreditStatus.ACCEPTED
                )
                onDone()
            }.onFailure { err ->
                onError(err.localizedMessage ?: strings.halisiErrorNetwork)
            }
        }
    }

    fun recordRepayment(
        requestId: String,
        amountUsd: Int,
        channel: String,
        reference: String,
        language: AppLanguage,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val strings = AppStrings.get(language)
        val desc = if (language == AppLanguage.FR) {
            "Remboursement microcrédit $requestId"
        } else {
            "Marejesho ya mkopo $requestId"
        }
        viewModelScope.launch {
            val result = repository.recordRepaymentTransaction(
                requestId = requestId,
                amountUsd = amountUsd,
                channel = channel,
                reference = reference,
                description = desc
            )
            result.onSuccess {
                onDone()
            }.onFailure { err ->
                onError(err.localizedMessage ?: strings.halisiErrorNetwork)
            }
        }
    }

    fun deleteAllUserData(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteAllUserData()
            result.onSuccess {
                biometricStorage?.deleteAllBiometrics()
                offlineCacheRepository?.clearCache()
                _enrollment.value = EnrollmentUiState()
                _faceVerify.value = FaceVerifyUiState()
                _activeCreditResult.value = null
                onComplete(true)
            }.onFailure {
                onComplete(false)
            }
        }
    }

    private fun OfflineCacheSnapshot.toFallbackProfile(
        email: String?,
        localSelfieUri: String?,
        localIdPhotoUri: String?
    ): UserProfile {
        val fallbackNom = fullName.removePrefix(prenom).trim()
            .ifBlank { if (hasEnrolledProfile) prenom else "" }
        return UserProfile(
            userId = userId,
            email = email,
            nom = fallbackNom,
            postnom = "",
            prenom = prenom,
            province = formattedLocation,
            location = "",
            agriculturalSector = agriculturalSector,
            localIdPhotoUri = localIdPhotoUri,
            localSelfieUri = localSelfieUri,
            hasIdPhoto = !localIdPhotoUri.isNullOrBlank(),
            hasSelfie = hasEnrolledProfile,
            identityStatus = identityStatus,
            lastVerificationScore = lastVerificationScore,
            verifiedAt = verifiedAtMillis?.let { Timestamp(Date(it)) }
        )
    }

    private fun questionTextForStep(step: Int, strings: com.aistudio.mkulimapay.rdcapp.i18n.LocalizedStrings): String {
        return when (step) {
            0 -> strings.q1Crop
            1 -> strings.q2Area
            2 -> strings.q3Harvest
            3 -> strings.q4Experience
            4 -> strings.q5Income
            5 -> strings.q6Amount
            6 -> strings.q7Duration
            else -> strings.q8Purpose
        }
    }

    private fun widgetForStep(step: Int): ChatWidgetType {
        return when (step) {
            0 -> ChatWidgetType.CROP_SELECTOR
            1 -> ChatWidgetType.AREA_SLIDER
            2 -> ChatWidgetType.HARVEST_MONTH_SELECTOR
            3 -> ChatWidgetType.EXPERIENCE_SELECTOR
            4 -> ChatWidgetType.INCOME_INPUT
            5 -> ChatWidgetType.AMOUNT_SLIDER
            6 -> ChatWidgetType.DURATION_SELECTOR
            7 -> ChatWidgetType.PURPOSE_SELECTOR
            else -> ChatWidgetType.NONE
        }
    }

    private fun currentTimestamp(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private companion object {
        const val TAG = "MkulimaVM"
        const val STOP_TIMEOUT_MS = 5000L
    }
}
