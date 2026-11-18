package com.aistudio.mkulimapay.rdcapp.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.model.AgriculturalSector
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.CreditNotificationItem
import com.aistudio.mkulimapay.rdcapp.model.CreditNotificationType
import com.aistudio.mkulimapay.rdcapp.model.CreditRequest
import com.aistudio.mkulimapay.rdcapp.model.CreditStatus
import com.aistudio.mkulimapay.rdcapp.model.FarmerProfile
import com.aistudio.mkulimapay.rdcapp.model.FinancialTransaction
import com.aistudio.mkulimapay.rdcapp.model.IdentityStatus
import com.aistudio.mkulimapay.rdcapp.model.OfflineCacheSnapshot
import com.aistudio.mkulimapay.rdcapp.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "OfflineCacheRepo"
private const val MAX_CACHED_NOTIFICATIONS = 10

private val Context.mkulimaOfflineCacheDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mkulima_pay_offline_cache"
)

/**
 * Cache local léger basé sur Preferences DataStore permettant de consulter
 * le statut d'identité et les dernières notifications de crédit sans connexion Internet.
 * Aucune photo biométrique ni donnée sensible brute n'est stockée ici.
 */
class OfflineCacheRepository(private val context: Context) {

    private object Keys {
        val USER_ID = stringPreferencesKey("cached_user_id")
        val FULL_NAME = stringPreferencesKey("cached_full_name")
        val PRENOM = stringPreferencesKey("cached_prenom")
        val FORMATTED_LOCATION = stringPreferencesKey("cached_formatted_location")
        val SECTOR = stringPreferencesKey("cached_agricultural_sector")
        val IDENTITY_STATUS = stringPreferencesKey("cached_identity_status")
        val VERIFICATION_SCORE = intPreferencesKey("cached_verification_score")
        val VERIFIED_AT_MILLIS = longPreferencesKey("cached_verified_at_millis")
        val HAS_ENROLLED_PROFILE = booleanPreferencesKey("cached_has_enrolled_profile")
        val NOTIFICATIONS_JSON = stringPreferencesKey("cached_credit_notifications_json")
        val LAST_SYNCED_AT_MILLIS = longPreferencesKey("cached_last_synced_at_millis")
    }

    fun observeCache(userId: String): Flow<OfflineCacheSnapshot?> {
        return context.mkulimaOfflineCacheDataStore.data
            .catch { error ->
                Log.w(TAG, "Failed reading offline cache DataStore", error)
                emit(androidx.datastore.preferences.core.emptyPreferences())
            }
            .map { prefs ->
                val cachedUid = prefs[Keys.USER_ID].orEmpty()
                if (userId.isBlank() || cachedUid != userId) {
                    return@map null
                }
                val sectorRaw = prefs[Keys.SECTOR] ?: AgriculturalSector.VIVRIER.name
                val statusRaw = prefs[Keys.IDENTITY_STATUS] ?: IdentityStatus.UNVERIFIED.name
                OfflineCacheSnapshot(
                    userId = cachedUid,
                    fullName = prefs[Keys.FULL_NAME].orEmpty(),
                    prenom = prefs[Keys.PRENOM].orEmpty(),
                    formattedLocation = prefs[Keys.FORMATTED_LOCATION].orEmpty(),
                    agriculturalSector = AgriculturalSector.entries.firstOrNull { it.name == sectorRaw }
                        ?: AgriculturalSector.VIVRIER,
                    identityStatus = IdentityStatus.entries.firstOrNull { it.name == statusRaw }
                        ?: IdentityStatus.UNVERIFIED,
                    lastVerificationScore = prefs[Keys.VERIFICATION_SCORE],
                    verifiedAtMillis = prefs[Keys.VERIFIED_AT_MILLIS],
                    hasEnrolledProfile = prefs[Keys.HAS_ENROLLED_PROFILE] ?: false,
                    notifications = decodeNotifications(prefs[Keys.NOTIFICATIONS_JSON].orEmpty()),
                    lastSyncedAtMillis = prefs[Keys.LAST_SYNCED_AT_MILLIS] ?: 0L
                )
            }
    }

    suspend fun cacheFarmerIdentity(
        profile: FarmerProfile,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        if (profile.userId.isBlank()) return
        context.mkulimaOfflineCacheDataStore.edit { prefs ->
            clearIfDifferentUser(prefs, profile.userId)
            prefs[Keys.USER_ID] = profile.userId
            prefs[Keys.FULL_NAME] = profile.fullName
            prefs[Keys.PRENOM] = profile.prenom
            prefs[Keys.FORMATTED_LOCATION] = profile.formattedLocation
            prefs[Keys.SECTOR] = profile.agriculturalSector.name
            prefs[Keys.IDENTITY_STATUS] = profile.identityStatus.name
            prefs[Keys.HAS_ENROLLED_PROFILE] = profile.hasEnrolledProfile
            prefs[Keys.LAST_SYNCED_AT_MILLIS] = nowMillis

            val score = profile.lastVerificationScore
            if (score != null) {
                prefs[Keys.VERIFICATION_SCORE] = score
            } else {
                prefs.remove(Keys.VERIFICATION_SCORE)
            }

            val verifiedMs = profile.verifiedAt?.toDate()?.time
            if (verifiedMs != null) {
                prefs[Keys.VERIFIED_AT_MILLIS] = verifiedMs
            } else {
                prefs.remove(Keys.VERIFIED_AT_MILLIS)
            }
        }
    }

    suspend fun cacheCreditNotifications(
        userId: String,
        requests: List<CreditRequest>,
        transactions: List<FinancialTransaction>,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        if (userId.isBlank()) return
        val notifications = buildCreditNotifications(requests, transactions, nowMillis)
        val encoded = encodeNotifications(notifications)
        context.mkulimaOfflineCacheDataStore.edit { prefs ->
            clearIfDifferentUser(prefs, userId)
            prefs[Keys.USER_ID] = userId
            prefs[Keys.NOTIFICATIONS_JSON] = encoded
            prefs[Keys.LAST_SYNCED_AT_MILLIS] = nowMillis
        }
    }

    suspend fun clearCache() {
        context.mkulimaOfflineCacheDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    private fun clearIfDifferentUser(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        userId: String
    ) {
        val existingUid = prefs[Keys.USER_ID]
        if (!existingUid.isNullOrBlank() && existingUid != userId) {
            prefs.clear()
        }
    }

    companion object {
        fun buildCreditNotifications(
            requests: List<CreditRequest>,
            transactions: List<FinancialTransaction>,
            fallbackTimestampMillis: Long = System.currentTimeMillis()
        ): List<CreditNotificationItem> {
            val requestItems = requests.map { req ->
                val cropFr = AppStrings.cropLabel(req.data.crop, AppLanguage.FR)
                val cropSw = AppStrings.cropLabel(req.data.crop, AppLanguage.SW)
                val ts = req.updatedAt?.toDate()?.time
                    ?: req.createdAt?.toDate()?.time
                    ?: fallbackTimestampMillis
                when (req.status) {
                    CreditStatus.ACCEPTED -> CreditNotificationItem(
                        id = "req_${req.requestId}_${req.status.name}",
                        requestId = req.requestId,
                        type = CreditNotificationType.CREDIT_ACCEPTED,
                        titleFr = "Microcrédit approuvé ($cropFr)",
                        titleSw = "Mkopo umekubaliwa ($cropSw)",
                        messageFr = "${req.data.requestedAmountUsd} USD sur ${req.data.durationMonths} mois • Mensualité ${req.monthlyInstallmentUsd} USD.",
                        messageSw = "${req.data.requestedAmountUsd} USD kwa miezi ${req.data.durationMonths} • Malipo ${req.monthlyInstallmentUsd} USD/mwezi.",
                        timestampMillis = ts
                    )
                    CreditStatus.IN_PROGRESS -> CreditNotificationItem(
                        id = "req_${req.requestId}_${req.status.name}",
                        requestId = req.requestId,
                        type = CreditNotificationType.CREDIT_IN_PROGRESS,
                        titleFr = "Demande en cours ($cropFr)",
                        titleSw = "Ombi linaendelea ($cropSw)",
                        messageFr = "Dossier de ${req.data.requestedAmountUsd} USD en attente de confirmation.",
                        messageSw = "Ombi la ${req.data.requestedAmountUsd} USD linasubiri uthibitisho.",
                        timestampMillis = ts
                    )
                    CreditStatus.REJECTED -> CreditNotificationItem(
                        id = "req_${req.requestId}_${req.status.name}",
                        requestId = req.requestId,
                        type = CreditNotificationType.CREDIT_REJECTED,
                        titleFr = "Demande non retenue ($cropFr)",
                        titleSw = "Ombi halikukubaliwa ($cropSw)",
                        messageFr = "La demande de ${req.data.requestedAmountUsd} USD n'a pas été retenue.",
                        messageSw = "Ombi la ${req.data.requestedAmountUsd} USD halikuidhinishwa.",
                        timestampMillis = ts
                    )
                }
            }

            val txItems = transactions.map { tx ->
                val ts = tx.createdAt?.toDate()?.time ?: fallbackTimestampMillis
                if (tx.type == TransactionType.DISBURSEMENT) {
                    CreditNotificationItem(
                        id = "tx_${tx.txId}",
                        requestId = tx.requestId,
                        type = CreditNotificationType.DISBURSEMENT,
                        titleFr = "Décaissement reçu : +${tx.amountUsd} USD",
                        titleSw = "Fedha zimetolewa: +${tx.amountUsd} USD",
                        messageFr = "${tx.channel} • Réf ${tx.reference}",
                        messageSw = "${tx.channel} • Kumb ${tx.reference}",
                        timestampMillis = ts
                    )
                } else {
                    CreditNotificationItem(
                        id = "tx_${tx.txId}",
                        requestId = tx.requestId,
                        type = CreditNotificationType.REPAYMENT,
                        titleFr = "Remboursement enregistré : -${tx.amountUsd} USD",
                        titleSw = "Marejesho yamehifadhiwa: -${tx.amountUsd} USD",
                        messageFr = "${tx.channel} • Réf ${tx.reference}",
                        messageSw = "${tx.channel} • Kumb ${tx.reference}",
                        timestampMillis = ts
                    )
                }
            }

            return (txItems + requestItems)
                .sortedByDescending { it.timestampMillis }
                .take(MAX_CACHED_NOTIFICATIONS)
        }

        internal fun encodeNotifications(items: List<CreditNotificationItem>): String {
            val array = JSONArray()
            items.take(MAX_CACHED_NOTIFICATIONS).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("requestId", item.requestId)
                    put("type", item.type.name)
                    put("titleFr", item.titleFr)
                    put("titleSw", item.titleSw)
                    put("messageFr", item.messageFr)
                    put("messageSw", item.messageSw)
                    put("timestampMillis", item.timestampMillis)
                }
                array.put(obj)
            }
            return array.toString()
        }

        internal fun decodeNotifications(raw: String): List<CreditNotificationItem> {
            if (raw.isBlank()) return emptyList()
            return runCatching {
                val array = JSONArray(raw)
                buildList {
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i) ?: continue
                        val typeRaw = obj.optString("type", CreditNotificationType.CREDIT_IN_PROGRESS.name)
                        val type = CreditNotificationType.entries.firstOrNull { it.name == typeRaw }
                            ?: CreditNotificationType.CREDIT_IN_PROGRESS
                        add(
                            CreditNotificationItem(
                                id = obj.optString("id"),
                                requestId = obj.optString("requestId"),
                                type = type,
                                titleFr = obj.optString("titleFr"),
                                titleSw = obj.optString("titleSw"),
                                messageFr = obj.optString("messageFr"),
                                messageSw = obj.optString("messageSw"),
                                timestampMillis = obj.optLong("timestampMillis", 0L)
                            )
                        )
                    }
                }
            }.getOrElse { error ->
                Log.w(TAG, "Failed decoding cached credit notifications", error)
                emptyList()
            }
        }
    }
}
