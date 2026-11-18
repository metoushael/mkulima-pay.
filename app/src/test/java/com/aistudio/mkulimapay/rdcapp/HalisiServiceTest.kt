package com.aistudio.mkulimapay.rdcapp

import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.CreditIntakeData
import com.aistudio.mkulimapay.rdcapp.model.CreditPurpose
import com.aistudio.mkulimapay.rdcapp.model.CropType
import com.aistudio.mkulimapay.rdcapp.model.RiskLevel
import com.aistudio.mkulimapay.rdcapp.services.FaceAnalysisResult
import com.aistudio.mkulimapay.rdcapp.services.FaceFailureReason
import com.aistudio.mkulimapay.rdcapp.services.MockConfig
import com.aistudio.mkulimapay.rdcapp.services.MockHalisiService
import com.aistudio.mkulimapay.rdcapp.services.MockMode
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HalisiServiceTest {

    @After
    fun tearDown() {
        MockConfig.mode = MockMode.NORMAL
    }

    @Test
    fun `verifyFace rejects when no face detected`() = runTest {
        val service = MockHalisiService(
            faceAnalyzer = {
                FaceAnalysisResult(faceCount = 0, leftEyeOpenProbability = null, rightEyeOpenProbability = null)
            }
        )

        val result = service.verifyFace("user_1", null, byteArrayOf(1, 2, 3))
        assertTrue(result.isSuccess)
        val res = result.getOrThrow()
        assertFalse(res.match)
        assertFalse(res.livenessPassed)
        assertEquals(FaceFailureReason.NO_FACE, res.failureReason)
    }

    @Test
    fun `verifyFace rejects when multiple faces detected`() = runTest {
        val service = MockHalisiService(
            faceAnalyzer = {
                FaceAnalysisResult(faceCount = 2, leftEyeOpenProbability = 0.9f, rightEyeOpenProbability = 0.9f)
            }
        )

        val result = service.verifyFace("user_1", null, byteArrayOf(1, 2, 3))
        assertTrue(result.isSuccess)
        val res = result.getOrThrow()
        assertFalse(res.match)
        assertEquals(FaceFailureReason.MULTIPLE_FACES, res.failureReason)
    }

    @Test
    fun `verifyFace rejects when eyes are closed`() = runTest {
        val service = MockHalisiService(
            faceAnalyzer = {
                FaceAnalysisResult(faceCount = 1, leftEyeOpenProbability = 0.12f, rightEyeOpenProbability = 0.15f)
            }
        )

        val result = service.verifyFace("user_1", null, byteArrayOf(1, 2, 3))
        assertTrue(result.isSuccess)
        val res = result.getOrThrow()
        assertFalse(res.match)
        assertFalse(res.livenessPassed)
        assertEquals(FaceFailureReason.EYES_CLOSED, res.failureReason)
    }

    @Test
    fun `verifyFace succeeds with simulated score in 80 to 98 range when one open face detected`() = runTest {
        val service = MockHalisiService(
            faceAnalyzer = {
                FaceAnalysisResult(faceCount = 1, leftEyeOpenProbability = 0.92f, rightEyeOpenProbability = 0.88f)
            }
        )

        val result = service.verifyFace("user_1", byteArrayOf(9, 8), byteArrayOf(1, 2, 3))
        assertTrue(result.isSuccess)
        val res = result.getOrThrow()
        assertTrue(res.match)
        assertTrue(res.livenessPassed)
        assertEquals(FaceFailureReason.NONE, res.failureReason)
        assertTrue(res.score in 80..98)
    }

    @Test
    fun `verifyFace respects MockMode ECHEC_VISAGE and ECHEC_RESEAU`() = runTest {
        val service = MockHalisiService(
            faceAnalyzer = {
                FaceAnalysisResult(faceCount = 1, leftEyeOpenProbability = 0.95f, rightEyeOpenProbability = 0.95f)
            }
        )

        MockConfig.mode = MockMode.ECHEC_VISAGE
        val faceFail = service.verifyFace("user_1", null, byteArrayOf(1, 2, 3)).getOrThrow()
        assertFalse(faceFail.match)
        assertEquals(FaceFailureReason.MOCK_FORCED_FAILURE, faceFail.failureReason)

        MockConfig.mode = MockMode.ECHEC_RESEAU
        val networkFail = service.verifyFace("user_1", null, byteArrayOf(1, 2, 3))
        assertTrue(networkFail.isFailure)
    }

    @Test
    fun `submitCreditRequest builds harvest aligned schedule and localized strings`() = runTest {
        val service = MockHalisiService()
        val intake = CreditIntakeData(
            crop = CropType.MAIS,
            areaHectares = 2.5f,
            harvestMonthIndex = 5,
            experienceYears = 6,
            monthlyIncomeUsd = 280,
            requestedAmountUsd = 350,
            durationMonths = 6,
            purpose = CreditPurpose.SEMENCES
        )

        val result = service.submitCreditRequest("user_1", intake, AppLanguage.FR)
        assertTrue(result.isSuccess)
        val evaluation = result.getOrThrow()
        assertEquals(RiskLevel.FAIBLE, evaluation.riskLevel)
        assertEquals(6, evaluation.schedule.size)
        assertTrue(evaluation.advice.isNotEmpty())

        val swStrings = AppStrings.get(AppLanguage.SW)
        assertTrue(swStrings.faceVerifyDemoNotice.isNotBlank())
        assertTrue(swStrings.aiOfflineFallbackNotice.isNotBlank())
    }
}
