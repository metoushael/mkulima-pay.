package com.aistudio.mkulimapay.rdcapp.data

import android.content.Context
import com.aistudio.mkulimapay.rdcapp.R
import com.aistudio.mkulimapay.rdcapp.model.AgriculturalSector
import com.aistudio.mkulimapay.rdcapp.model.FarmerProfile
import com.aistudio.mkulimapay.rdcapp.model.IdDocumentType
import com.aistudio.mkulimapay.rdcapp.model.IdentityStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Dedicated Cloud Firestore repository for reading, storing, and updating
 * a Farmer's profile ([FarmerProfile]), including agricultural sector and location.
 */
class FarmerProfileRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
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

    fun observeFarmerProfile(userId: String): Flow<FarmerProfile?> = flow {
        val uid = userId.takeIf { it.isNotBlank() } ?: requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null else parseFarmerProfile(snapshot)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveOrUpdateFarmerProfile(profile: FarmerProfile): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path

            val nomClean = profile.nom.trim().take(80)
            val postnomClean = profile.postnom.trim().take(80)
            val prenomClean = profile.prenom.trim().take(80)
            val dobClean = profile.dateOfBirth.trim().take(20)
            val phoneClean = profile.phone.trim().take(20)
            val provinceClean = profile.province.trim().take(80)
            val locationClean = profile.location.trim().take(120)
            val idNumberClean = profile.idNumber.trim().take(50)

            require(nomClean.isNotEmpty() && postnomClean.isNotEmpty() && prenomClean.isNotEmpty()) {
                "Nom, postnom et prénom requis."
            }
            require(dobClean.length >= 6 && phoneClean.length >= 9 && idNumberClean.length >= 4) {
                "Informations d'identité incomplètes."
            }
            require(provinceClean.length >= 2) { "Province ou territoire requis." }
            require(profile.hasSelfie) { "Selfie biométrique requis." }

            val exists = docRef.get().await().exists()
            val emailClean = (profile.email ?: auth.currentUser?.email)
                ?.trim()?.takeIf { it.isNotEmpty() }?.take(128)

            if (!exists) {
                val createPayload = mapOf(
                    "userId" to uid,
                    "email" to emailClean,
                    "nom" to nomClean,
                    "postnom" to postnomClean,
                    "prenom" to prenomClean,
                    "dateOfBirth" to dobClean,
                    "sex" to if (profile.sex == "F") "F" else "M",
                    "phone" to phoneClean,
                    "province" to provinceClean,
                    "location" to locationClean,
                    "agriculturalSector" to profile.agriculturalSector.name,
                    "idType" to profile.idType.name,
                    "idNumber" to idNumberClean,
                    "hasIdPhoto" to profile.hasIdPhoto,
                    "hasSelfie" to true,
                    "identityStatus" to IdentityStatus.PENDING.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(createPayload).await()
            } else {
                val updatePayload = mutableMapOf<String, Any>(
                    "nom" to nomClean,
                    "postnom" to postnomClean,
                    "prenom" to prenomClean,
                    "dateOfBirth" to dobClean,
                    "sex" to if (profile.sex == "F") "F" else "M",
                    "phone" to phoneClean,
                    "province" to provinceClean,
                    "location" to locationClean,
                    "agriculturalSector" to profile.agriculturalSector.name,
                    "idType" to profile.idType.name,
                    "idNumber" to idNumberClean,
                    "hasIdPhoto" to profile.hasIdPhoto,
                    "hasSelfie" to true,
                    "identityStatus" to IdentityStatus.PENDING.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                emailClean?.let { updatePayload["email"] = it }
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateFarmerSectorAndLocation(
        agriculturalSector: AgriculturalSector,
        province: String,
        location: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val cleanProvince = province.trim().take(80)
            val cleanLocation = location.trim().take(120)
            require(cleanProvince.length >= 2) { "Province requise." }
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "province" to cleanProvince,
                    "location" to cleanLocation,
                    "agriculturalSector" to agriculturalSector.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun markIdentityVerified(score: Int): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val boundedScore = score.coerceIn(70, 100)
            docRef.update(
                mapOf(
                    "identityStatus" to IdentityStatus.VERIFIED.name,
                    "lastVerificationScore" to boundedScore,
                    "verifiedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    internal fun parseFarmerProfile(doc: DocumentSnapshot): FarmerProfile {
        val statusRaw = doc.getString("identityStatus") ?: IdentityStatus.UNVERIFIED.name
        val idTypeRaw = doc.getString("idType") ?: IdDocumentType.VOTER_CARD.name
        val sectorRaw = doc.getString("agriculturalSector") ?: AgriculturalSector.VIVRIER.name
        val createdTs = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        val updatedTs = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        val verifiedTs = doc.getTimestamp("verifiedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        val scoreVal = doc.getLong("lastVerificationScore")?.toInt()

        return FarmerProfile(
            userId = doc.getString("userId") ?: doc.id,
            email = doc.getString("email"),
            nom = doc.getString("nom").orEmpty(),
            postnom = doc.getString("postnom").orEmpty(),
            prenom = doc.getString("prenom").orEmpty(),
            dateOfBirth = doc.getString("dateOfBirth").orEmpty(),
            sex = doc.getString("sex") ?: "M",
            phone = doc.getString("phone").orEmpty(),
            province = doc.getString("province").orEmpty(),
            location = doc.getString("location").orEmpty(),
            agriculturalSector = AgriculturalSector.entries.firstOrNull { it.name == sectorRaw }
                ?: AgriculturalSector.VIVRIER,
            idType = IdDocumentType.entries.firstOrNull { it.name == idTypeRaw } ?: IdDocumentType.VOTER_CARD,
            idNumber = doc.getString("idNumber").orEmpty(),
            localIdPhotoUri = null,
            localSelfieUri = null,
            hasIdPhoto = doc.getBoolean("hasIdPhoto") ?: false,
            hasSelfie = doc.getBoolean("hasSelfie") ?: false,
            identityStatus = IdentityStatus.entries.firstOrNull { it.name == statusRaw } ?: IdentityStatus.UNVERIFIED,
            lastVerificationScore = scoreVal,
            verifiedAt = verifiedTs,
            createdAt = createdTs,
            updatedAt = updatedTs
        )
    }
}
