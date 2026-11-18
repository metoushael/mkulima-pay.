package com.aistudio.mkulimapay.rdcapp

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.aistudio.mkulimapay.rdcapp.data.OfflineCacheRepository
import com.aistudio.mkulimapay.rdcapp.data.UiState
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.model.AgriculturalSector
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.CreditIntakeData
import com.aistudio.mkulimapay.rdcapp.model.CreditNotificationItem
import com.aistudio.mkulimapay.rdcapp.model.CreditNotificationType
import com.aistudio.mkulimapay.rdcapp.model.CreditRequest
import com.aistudio.mkulimapay.rdcapp.model.CreditStatus
import com.aistudio.mkulimapay.rdcapp.model.CropType
import com.aistudio.mkulimapay.rdcapp.model.FarmerProfile
import com.aistudio.mkulimapay.rdcapp.model.FinancialTransaction
import com.aistudio.mkulimapay.rdcapp.model.IdentityStatus
import com.aistudio.mkulimapay.rdcapp.model.TransactionStatus
import com.aistudio.mkulimapay.rdcapp.model.TransactionType
import com.aistudio.mkulimapay.rdcapp.ui.screens.HomeScreen
import com.aistudio.mkulimapay.rdcapp.ui.theme.MkulimaPayTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineCacheTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var cacheRepository: OfflineCacheRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        cacheRepository = OfflineCacheRepository(context)
        runBlocking { cacheRepository.clearCache() }
    }

    @After
    fun tearDown() {
        runBlocking { cacheRepository.clearCache() }
    }

    @Test
    fun `cacheFarmerIdentity persists identity status and isolates by userId`() = runBlocking {
        val verifiedMillis = 1_730_000_000_000L
        val profile = FarmerProfile(
            userId = "farmer_uid_1",
            nom = "Kabongo",
            postnom = "Mutombo",
            prenom = "Aline",
            province = "Haut-Katanga",
            location = "Kipushi",
            agriculturalSector = AgriculturalSector.VIVRIER,
            hasSelfie = true,
            identityStatus = IdentityStatus.VERIFIED,
            lastVerificationScore = 94,
            verifiedAt = Timestamp(Date(verifiedMillis))
        )

        cacheRepository.cacheFarmerIdentity(profile, nowMillis = 1_730_000_123_000L)

        val snapshot = cacheRepository.observeCache("farmer_uid_1").first()
        assertNotNull(snapshot)
        assertEquals(IdentityStatus.VERIFIED, snapshot?.identityStatus)
        assertEquals(94, snapshot?.lastVerificationScore)
        assertEquals(verifiedMillis, snapshot?.verifiedAtMillis)
        assertEquals("Aline", snapshot?.prenom)
        assertEquals("Kipushi, Haut-Katanga", snapshot?.formattedLocation)
        assertTrue(snapshot?.hasEnrolledProfile == true)

        val otherUserSnapshot = cacheRepository.observeCache("farmer_uid_other").first()
        assertNull(otherUserSnapshot)
    }

    @Test
    fun `cacheCreditNotifications stores latest sorted credit notifications in DataStore`() = runBlocking {
        val req = CreditRequest(
            requestId = "REQ_101",
            userId = "farmer_uid_1",
            data = CreditIntakeData(
                crop = CropType.MAIS,
                requestedAmountUsd = 400,
                durationMonths = 6
            ),
            monthlyInstallmentUsd = 74,
            status = CreditStatus.ACCEPTED,
            createdAt = Timestamp(Date(1_730_000_000_000L)),
            updatedAt = Timestamp(Date(1_730_000_100_000L))
        )
        val tx = FinancialTransaction(
            txId = "TX_900",
            userId = "farmer_uid_1",
            requestId = "REQ_101",
            type = TransactionType.DISBURSEMENT,
            amountUsd = 400,
            channel = "M-Pesa",
            reference = "MKP-101-900",
            status = TransactionStatus.COMPLETED,
            createdAt = Timestamp(Date(1_730_000_200_000L))
        )

        cacheRepository.cacheFarmerIdentity(
            FarmerProfile(
                userId = "farmer_uid_1",
                nom = "Kabongo",
                prenom = "Aline",
                hasSelfie = true,
                identityStatus = IdentityStatus.VERIFIED
            ),
            nowMillis = 1_730_000_200_000L
        )
        cacheRepository.cacheCreditNotifications(
            userId = "farmer_uid_1",
            requests = listOf(req),
            transactions = listOf(tx),
            nowMillis = 1_730_000_300_000L
        )

        val snapshot = cacheRepository.observeCache("farmer_uid_1").first()
        assertNotNull(snapshot)
        assertEquals(2, snapshot?.notifications?.size)
        assertEquals(CreditNotificationType.DISBURSEMENT, snapshot?.notifications?.first()?.type)
        assertEquals(CreditNotificationType.CREDIT_ACCEPTED, snapshot?.notifications?.get(1)?.type)

        cacheRepository.clearCache()
        assertNull(cacheRepository.observeCache("farmer_uid_1").first())
    }

    @Test
    fun `homeScreen renders offline banner identity status and cached credit notifications when offline`() {
        val strings = AppStrings.get(AppLanguage.FR)
        val cachedProfile = FarmerProfile(
            userId = "farmer_uid_1",
            nom = "Kabongo",
            prenom = "Aline",
            province = "Haut-Katanga",
            hasSelfie = true,
            identityStatus = IdentityStatus.VERIFIED,
            lastVerificationScore = 92
        )
        val cachedNotifs = listOf(
            CreditNotificationItem(
                id = "notif_1",
                requestId = "REQ_101",
                type = CreditNotificationType.CREDIT_ACCEPTED,
                titleFr = "Microcrédit approuvé (Maïs)",
                titleSw = "Mkopo umekubaliwa (Mahindi)",
                messageFr = "400 USD sur 6 mois • Mensualité 74 USD.",
                messageSw = "400 USD kwa miezi 6 • Malipo 74 USD/mwezi.",
                timestampMillis = 1_730_000_100_000L
            )
        )

        composeTestRule.setContent {
            MkulimaPayTheme {
                HomeScreen(
                    strings = strings,
                    language = AppLanguage.FR,
                    currentUserEmail = "aline@example.com",
                    profileState = UiState.Success(cachedProfile),
                    creditRequestsState = UiState.Error("Offline"),
                    transactionsState = UiState.Error("Offline"),
                    isOnline = false,
                    creditNotifications = cachedNotifs,
                    lastSyncedAtMillis = 1_730_000_200_000L,
                    onToggleLanguage = {},
                    onNavigateEnroll = {},
                    onNavigateFaceVerify = {},
                    onNavigateCreditAssistant = {},
                    onOpenCreditResult = {},
                    onNavigateHistory = {},
                    onNavigateProfile = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("offline_mode_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("credit_notifications_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vérifiée").assertIsDisplayed()
        composeTestRule.onNodeWithText("Microcrédit approuvé (Maïs)").assertIsDisplayed()
    }
}
