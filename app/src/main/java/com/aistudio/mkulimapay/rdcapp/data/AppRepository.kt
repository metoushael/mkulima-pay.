package com.aistudio.mkulimapay.rdcapp.data

import android.content.Context
import com.aistudio.mkulimapay.rdcapp.R
import com.aistudio.mkulimapay.rdcapp.model.AgriculturalSector
import com.aistudio.mkulimapay.rdcapp.model.CreditIntakeData
import com.aistudio.mkulimapay.rdcapp.model.CreditPurpose
import com.aistudio.mkulimapay.rdcapp.model.CreditRequest
import com.aistudio.mkulimapay.rdcapp.model.CreditStatus
import com.aistudio.mkulimapay.rdcapp.model.CropType
import com.aistudio.mkulimapay.rdcapp.model.FarmerProfile
import com.aistudio.mkulimapay.rdcapp.model.FinancialTransaction
import com.aistudio.mkulimapay.rdcapp.model.RepaymentInstallment
import com.aistudio.mkulimapay.rdcapp.model.RiskIconType
import com.aistudio.mkulimapay.rdcapp.model.RiskLevel
import com.aistudio.mkulimapay.rdcapp.model.RiskReason
import com.aistudio.mkulimapay.rdcapp.model.TransactionStatus
import com.aistudio.mkulimapay.rdcapp.model.TransactionType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

class AppRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
    private val farmerProfileRepository: FarmerProfileRepository = FarmerProfileRepository(db, auth)
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<FarmerProfile?> =
        farmerProfileRepository.observeFarmerProfile(userId)

    fun observeCreditRequests(userId: String): Flow<List<CreditRequest>> = flow {
        val uid = userId.takeIf { it.isNotBlank() } ?: requireUserId()
        val path = "users/$uid/creditRequests"
        emitAll(
            db.collection("users").document(uid).collection("creditRequests")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { parseCreditRequest(it) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeTransactions(userId: String): Flow<List<FinancialTransaction>> = flow {
        val uid = userId.takeIf { it.isNotBlank() } ?: requireUserId()
        val path = "users/$uid/transactions"
        emitAll(
            db.collection("users").document(uid).collection("transactions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { parseTransaction(it) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveOrUpdateProfile(profile: FarmerProfile): Result<Unit> =
        farmerProfileRepository.saveOrUpdateFarmerProfile(profile)

    suspend fun updateFarmerSectorAndLocation(
        agriculturalSector: AgriculturalSector,
        province: String,
        location: String
    ): Result<Unit> =
        farmerProfileRepository.updateFarmerSectorAndLocation(agriculturalSector, province, location)

    suspend fun markIdentityVerified(score: Int): Result<Unit> =
        farmerProfileRepository.markIdentityVerified(score)

    suspend fun acceptCreditRequestWithDisbursement(
        request: CreditRequest,
        channel: String,
        disbursementDescription: String
    ): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/creditRequests"
        return try {
            val uid = requireUserId()
            val requestId = sanitizeId(request.requestId.ifBlank {
                "REQ_${UUID.randomUUID().toString().replace("-", "").take(10).uppercase(Locale.US)}"
            })
            val txId = "TX_${UUID.randomUUID().toString().replace("-", "").take(10).uppercase(Locale.US)}"

            val reqRef = db.collection("users").document(uid).collection("creditRequests").document(requestId)
            val txRef = db.collection("users").document(uid).collection("transactions").document(txId)
            docPath = reqRef.path

            val safeAmount = request.data.requestedAmountUsd.coerceIn(10, 100_000)
            val safeDuration = request.data.durationMonths.coerceIn(1, 60)
            val safeIncome = request.data.monthlyIncomeUsd.coerceIn(1, 1_000_000)
            val safeArea = request.data.areaHectares.toDouble().coerceIn(0.1, 500.0)
            val safeHarvest = request.data.harvestMonthIndex.coerceIn(0, 11)
            val safeExp = request.data.experienceYears.coerceIn(0, 90)
            val safeScore = request.riskScore.coerceIn(0, 100)
            val safeMonthly = request.monthlyInstallmentUsd.coerceIn(1, 100_000)

            val reqPayload = mapOf(
                "userId" to uid,
                "crop" to request.data.crop.name,
                "areaHectares" to safeArea,
                "harvestMonthIndex" to safeHarvest,
                "experienceYears" to safeExp,
                "monthlyIncomeUsd" to safeIncome,
                "requestedAmountUsd" to safeAmount,
                "durationMonths" to safeDuration,
                "purpose" to request.data.purpose.name,
                "riskLevel" to request.riskLevel.name,
                "riskScore" to safeScore,
                "monthlyInstallmentUsd" to safeMonthly,
                "status" to CreditStatus.ACCEPTED.name,
                "reasons" to request.reasons.take(10).map { r ->
                    mapOf(
                        "title" to r.title.take(120),
                        "detail" to r.detail.take(300),
                        "positive" to r.positive,
                        "iconType" to r.iconType.name
                    )
                },
                "schedule" to request.schedule.take(24).map { s ->
                    mapOf(
                        "monthLabel" to s.monthLabel.take(32),
                        "amountUsd" to s.amountUsd.coerceAtLeast(1),
                        "harvestPeak" to s.harvestPeak,
                        "note" to s.note.take(120)
                    )
                },
                "advice" to request.advice.take(10).map { it.take(300) },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val txPayload = mapOf(
                "userId" to uid,
                "requestId" to requestId,
                "type" to TransactionType.DISBURSEMENT.name,
                "amountUsd" to safeAmount,
                "channel" to channel.trim().ifBlank { "M-Pesa" }.take(40),
                "reference" to "MKP-${requestId.takeLast(6)}-${txId.takeLast(4)}",
                "description" to disbursementDescription.trim().ifBlank { "Credit disbursement" }.take(200),
                "status" to TransactionStatus.COMPLETED.name,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val existingSnap = reqRef.get().await()
            val batch = db.batch()
            if (existingSnap.exists()) {
                batch.update(
                    reqRef,
                    mapOf(
                        "status" to CreditStatus.ACCEPTED.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.set(reqRef, reqPayload)
            }
            batch.set(txRef, txPayload)
            batch.commit().await()

            Result.success(requestId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun recordRepaymentTransaction(
        requestId: String,
        amountUsd: Int,
        channel: String,
        reference: String,
        description: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/transactions"
        return try {
            val uid = requireUserId()
            val cleanReqId = sanitizeId(requestId)
            require(cleanReqId.isNotBlank()) { "Credit request ID required." }
            require(amountUsd in 1..100_000) { "Amount must be between 1 and 100000 USD." }
            val cleanChannel = channel.trim().take(40)
            val cleanRef = reference.trim().take(64)
            val cleanDesc = description.trim().take(200)
            require(cleanChannel.length >= 2 && cleanRef.length >= 2 && cleanDesc.length >= 2) {
                "Channel, reference, and description required."
            }

            val txId = "TX_${UUID.randomUUID().toString().replace("-", "").take(10).uppercase(Locale.US)}"
            val txRef = db.collection("users").document(uid).collection("transactions").document(txId)
            docPath = txRef.path

            val payload = mapOf(
                "userId" to uid,
                "requestId" to cleanReqId,
                "type" to TransactionType.REPAYMENT.name,
                "amountUsd" to amountUsd,
                "channel" to cleanChannel,
                "reference" to cleanRef,
                "description" to cleanDesc,
                "status" to TransactionStatus.COMPLETED.name,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            txRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserData(): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val userDoc = db.collection("users").document(uid)
            docPath = userDoc.path

            val txSnaps = userDoc.collection("transactions").get().await()
            val reqSnaps = userDoc.collection("creditRequests").get().await()

            val batch = db.batch()
            txSnaps.documents.forEach { batch.delete(it.reference) }
            reqSnaps.documents.forEach { batch.delete(it.reference) }
            batch.delete(userDoc)
            batch.commit().await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseCreditRequest(doc: DocumentSnapshot): CreditRequest? {
        return runCatching {
            val cropRaw = doc.getString("crop") ?: CropType.MAIS.name
            val purposeRaw = doc.getString("purpose") ?: CreditPurpose.SEMENCES.name
            val riskRaw = doc.getString("riskLevel") ?: RiskLevel.MOYEN.name
            val statusRaw = doc.getString("status") ?: CreditStatus.IN_PROGRESS.name

            val reasonsRaw = doc.get("reasons") as? List<Map<String, Any?>> ?: emptyList()
            val reasons = reasonsRaw.map { m ->
                val iconRaw = m["iconType"] as? String ?: RiskIconType.RATIO.name
                RiskReason(
                    title = m["title"] as? String ?: "",
                    detail = m["detail"] as? String ?: "",
                    positive = m["positive"] as? Boolean ?: true,
                    iconType = RiskIconType.entries.firstOrNull { it.name == iconRaw } ?: RiskIconType.RATIO
                )
            }

            val scheduleRaw = doc.get("schedule") as? List<Map<String, Any?>> ?: emptyList()
            val schedule = scheduleRaw.map { m ->
                RepaymentInstallment(
                    monthLabel = m["monthLabel"] as? String ?: "",
                    amountUsd = (m["amountUsd"] as? Number)?.toInt() ?: 0,
                    harvestPeak = m["harvestPeak"] as? Boolean ?: false,
                    note = m["note"] as? String ?: ""
                )
            }

            val advice = (doc.get("advice") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val createdTs = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val updatedTs = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)

            CreditRequest(
                requestId = doc.id,
                userId = doc.getString("userId").orEmpty(),
                data = CreditIntakeData(
                    crop = CropType.entries.firstOrNull { it.name == cropRaw } ?: CropType.MAIS,
                    areaHectares = (doc.get("areaHectares") as? Number)?.toFloat() ?: 1.0f,
                    harvestMonthIndex = (doc.get("harvestMonthIndex") as? Number)?.toInt() ?: 5,
                    experienceYears = (doc.get("experienceYears") as? Number)?.toInt() ?: 1,
                    monthlyIncomeUsd = (doc.get("monthlyIncomeUsd") as? Number)?.toInt() ?: 100,
                    requestedAmountUsd = (doc.get("requestedAmountUsd") as? Number)?.toInt() ?: 100,
                    durationMonths = (doc.get("durationMonths") as? Number)?.toInt() ?: 6,
                    purpose = CreditPurpose.entries.firstOrNull { it.name == purposeRaw } ?: CreditPurpose.SEMENCES
                ),
                riskLevel = RiskLevel.entries.firstOrNull { it.name == riskRaw } ?: RiskLevel.MOYEN,
                riskScore = (doc.get("riskScore") as? Number)?.toInt() ?: 50,
                monthlyInstallmentUsd = (doc.get("monthlyInstallmentUsd") as? Number)?.toInt() ?: 20,
                reasons = reasons,
                schedule = schedule,
                advice = advice,
                status = CreditStatus.entries.firstOrNull { it.name == statusRaw } ?: CreditStatus.IN_PROGRESS,
                createdAt = createdTs,
                updatedAt = updatedTs
            )
        }.getOrNull()
    }

    private fun parseTransaction(doc: DocumentSnapshot): FinancialTransaction? {
        return runCatching {
            val typeRaw = doc.getString("type") ?: TransactionType.DISBURSEMENT.name
            val statusRaw = doc.getString("status") ?: TransactionStatus.COMPLETED.name
            val createdTs = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val updatedTs = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            FinancialTransaction(
                txId = doc.id,
                userId = doc.getString("userId").orEmpty(),
                requestId = doc.getString("requestId").orEmpty(),
                type = TransactionType.entries.firstOrNull { it.name == typeRaw } ?: TransactionType.DISBURSEMENT,
                amountUsd = (doc.get("amountUsd") as? Number)?.toInt() ?: 0,
                channel = doc.getString("channel").orEmpty(),
                reference = doc.getString("reference").orEmpty(),
                description = doc.getString("description").orEmpty(),
                status = TransactionStatus.entries.firstOrNull { it.name == statusRaw } ?: TransactionStatus.COMPLETED,
                createdAt = createdTs,
                updatedAt = updatedTs
            )
        }.getOrNull()
    }

    private fun sanitizeId(raw: String): String {
        return raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128)
    }
}
