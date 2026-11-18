package com.aistudio.mkulimapay.rdcapp.services

import android.util.Log
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.CreditIntakeData
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout

private const val TAG = "CreditAssistantAi"
const val GEMINI_MODEL_NAME = "gemini-3.8-flash"

data class AiAssistantReply(
    val text: String,
    val isOfflineFallback: Boolean
)

/**
 * Assistant conversationnel pour Mkulima Pay.
 * Utilise Firebase AI Logic (Gemini) avec repli résilient et indication claire si hors ligne.
 */
class CreditAssistantAiService {

    private val model by lazy {
        Firebase.ai.generativeModel(GEMINI_MODEL_NAME)
    }

    suspend fun generateNaturalStepAck(
        stepIndex: Int,
        userReply: String,
        intake: CreditIntakeData,
        nextQuestion: String,
        language: AppLanguage
    ): AiAssistantReply {
        val langName = if (language == AppLanguage.FR) "français" else "kiswahili"
        val cropName = AppStrings.cropLabel(intake.crop, language)
        val prompt = """
            Tu es l'assistant bienveillant de Mkulima Pay pour les agriculteurs de la RDC.
            Langue obligatoire : $langName uniquement. Pas d'emojis. Maximum 2 phrases courtes et simples.
            L'agriculteur vient de répondre : "$userReply" (culture actuelle : $cropName, superficie : ${intake.areaHectares} ha).
            Félicite ou valide brièvement sa réponse avec chaleur, puis pose exactement la question suivante : "$nextQuestion".
        """.trimIndent()

        return try {
            val response = withTimeout(15_000L) {
                model.generateContent(prompt)
            }
            val aiText = response.text?.trim()?.takeIf { it.isNotBlank() }
            if (aiText != null) {
                AiAssistantReply(text = aiText, isOfflineFallback = false)
            } else {
                Log.e(TAG, "Gemini returned blank response for step $stepIndex (model=$GEMINI_MODEL_NAME); using scripted fallback.")
                AiAssistantReply(
                    text = scriptedAck(stepIndex, userReply, nextQuestion, language),
                    isOfflineFallback = true
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini request failed for step $stepIndex (model=$GEMINI_MODEL_NAME): ${e.message}", e)
            AiAssistantReply(
                text = scriptedAck(stepIndex, userReply, nextQuestion, language),
                isOfflineFallback = true
            )
        }
    }

    suspend fun answerFreeQuestion(
        userText: String,
        currentQuestion: String,
        language: AppLanguage
    ): AiAssistantReply {
        val langName = if (language == AppLanguage.FR) "français" else "kiswahili"
        val prompt = """
            Tu es l'assistant de microcrédit agricole Mkulima Pay (RDC).
            Réponds en $langName, sans emojis, en 2 phrases simples maximum à la remarque de l'agriculteur : "$userText",
            puis rappelle doucement la question en cours : "$currentQuestion".
        """.trimIndent()

        return try {
            val response = withTimeout(15_000L) {
                model.generateContent(prompt)
            }
            val aiText = response.text?.trim()?.takeIf { it.isNotBlank() }
            if (aiText != null) {
                AiAssistantReply(text = aiText, isOfflineFallback = false)
            } else {
                Log.e(TAG, "Gemini returned blank response for free question (model=$GEMINI_MODEL_NAME); using scripted fallback.")
                val fallbackText = if (language == AppLanguage.FR) {
                    "Bien noté. $currentQuestion"
                } else {
                    "Imepokelewa. $currentQuestion"
                }
                AiAssistantReply(text = fallbackText, isOfflineFallback = true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini free-question failed (model=$GEMINI_MODEL_NAME): ${e.message}", e)
            val fallbackText = if (language == AppLanguage.FR) {
                "Merci, c'est noté. $currentQuestion"
            } else {
                "Asante, tumeandika. $currentQuestion"
            }
            AiAssistantReply(text = fallbackText, isOfflineFallback = true)
        }
    }

    private fun scriptedAck(
        stepIndex: Int,
        userReply: String,
        nextQuestion: String,
        language: AppLanguage
    ): String {
        return if (language == AppLanguage.FR) {
            when (stepIndex) {
                0 -> "Très bon choix ($userReply), c'est une culture essentielle en RDC. $nextQuestion"
                1 -> "C'est noté pour $userReply. $nextQuestion"
                2 -> "Parfait, nous alignerons votre calendrier sur $userReply. $nextQuestion"
                3 -> "Belle expérience ($userReply) ! Cela renforce votre dossier. $nextQuestion"
                4 -> "Merci, cela aide à calculer une mensualité adaptée. $nextQuestion"
                5 -> "Compris pour $userReply. $nextQuestion"
                6 -> "Entendu, durée retenue : $userReply. $nextQuestion"
                else -> "Merci pour toutes vos réponses. Je calcule votre profil de risque…"
            }
        } else {
            when (stepIndex) {
                0 -> "Chaguo zuri ($userReply), ni zao muhimu nchini DRC. $nextQuestion"
                1 -> "Tumerekodi ukubwa wa $userReply. $nextQuestion"
                2 -> "Sawa kabisa, tutapanga malipo kulingana na $userReply. $nextQuestion"
                3 -> "Uzoefu mzuri ($userReply)! Hili linaimarisha ombi lako. $nextQuestion"
                4 -> "Asante, kipato hiki kinasaidia kupanga marejesho sahihi. $nextQuestion"
                5 -> "Tumepokea kiasi cha $userReply. $nextQuestion"
                6 -> "Sawa, muda uliochaguliwa ni $userReply. $nextQuestion"
                else -> "Asante kwa majibu yako yote. Ninakokotoa wasifu wako wa hatari…"
            }
        }
    }
}
