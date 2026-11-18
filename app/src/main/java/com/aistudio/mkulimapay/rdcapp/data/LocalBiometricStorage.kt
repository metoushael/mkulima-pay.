package com.aistudio.mkulimapay.rdcapp.data

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File

private const val TAG = "LocalBiometricStorage"
private const val BIOMETRICS_DIR = "private_biometrics"
private const val CACHE_CAPTURES_DIR = "captures"
private const val SELFIE_FILE_NAME = "selfie.jpg"
private const val ID_DOC_FILE_NAME = "id_doc.jpg"

/**
 * Stockage local strictement privé dans le répertoire interne de l'application (`context.filesDir`).
 * Les selfies et documents biométriques ne sont jamais envoyés ni stockés sur Firestore.
 */
class LocalBiometricStorage(context: Context) {

    private val appContext = context.applicationContext
    private val biometricDir: File by lazy {
        File(appContext.filesDir, BIOMETRICS_DIR).apply { mkdirs() }
    }
    private val cacheCapturesDir: File by lazy {
        File(appContext.cacheDir, CACHE_CAPTURES_DIR)
    }

    fun getSelfieUriIfExists(): String? {
        val file = File(biometricDir, SELFIE_FILE_NAME)
        return if (file.exists() && file.length() > 0L) Uri.fromFile(file).toString() else null
    }

    fun getIdDocUriIfExists(): String? {
        val file = File(biometricDir, ID_DOC_FILE_NAME)
        return if (file.exists() && file.length() > 0L) Uri.fromFile(file).toString() else null
    }

    fun readSelfieBytes(): ByteArray {
        val file = File(biometricDir, SELFIE_FILE_NAME)
        return if (file.exists() && file.length() > 0L) {
            runCatching { file.readBytes() }.getOrElse { ByteArray(0) }
        } else {
            ByteArray(0)
        }
    }

    fun saveSelfieBytes(bytes: ByteArray): String? {
        if (bytes.isEmpty()) return null
        return runCatching {
            val dest = File(biometricDir, SELFIE_FILE_NAME)
            dest.writeBytes(bytes)
            Uri.fromFile(dest).toString()
        }.onFailure { err ->
            Log.e(TAG, "Failed to save selfie bytes to private internal storage", err)
        }.getOrNull()
    }

    fun saveIdDocFromUri(sourceUriString: String): String? {
        return copyUriToPrivateFile(sourceUriString, ID_DOC_FILE_NAME)
    }

    fun saveSelfieFromUri(sourceUriString: String): String? {
        return copyUriToPrivateFile(sourceUriString, SELFIE_FILE_NAME)
    }

    fun deleteTemporaryCapture(uriString: String) {
        if (uriString.isBlank()) return
        runCatching {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val file = uri.path?.let { File(it) } ?: return@runCatching
                val cacheRoot = cacheCapturesDir.canonicalPath
                if (file.canonicalPath.startsWith(cacheRoot) && file.exists()) {
                    file.delete()
                }
            }
        }.onFailure { err ->
            Log.w(TAG, "Failed to delete temporary capture file", err)
        }
    }

    fun deleteAllBiometrics() {
        runCatching {
            biometricDir.listFiles()?.forEach { it.delete() }
            if (cacheCapturesDir.exists()) {
                cacheCapturesDir.listFiles()?.forEach { it.delete() }
            }
        }.onFailure { err ->
            Log.e(TAG, "Failed to purge local biometric files", err)
        }
    }

    private fun copyUriToPrivateFile(sourceUriString: String, targetFileName: String): String? {
        if (sourceUriString.isBlank()) return null
        return runCatching {
            val sourceUri = Uri.parse(sourceUriString)
            val dest = File(biometricDir, targetFileName)
            if (sourceUri.scheme == "file" && sourceUri.path == dest.absolutePath && dest.exists()) {
                return@runCatching Uri.fromFile(dest).toString()
            }
            val bytes = appContext.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
                ?: return@runCatching null
            if (bytes.isEmpty()) return@runCatching null
            dest.writeBytes(bytes)
            deleteTemporaryCapture(sourceUriString)
            Uri.fromFile(dest).toString()
        }.onFailure { err ->
            Log.e(TAG, "Failed to persist URI into private biometric storage", err)
        }.getOrNull()
    }
}
