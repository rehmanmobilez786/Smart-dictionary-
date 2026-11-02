package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object OcrHelper {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Extracts text from an in-memory Bitmap (e.g. from Camera preview/capture).
     */
    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): Result<String> = withContext(Dispatchers.Default) {
        suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text.trim()
                        continuation.resume(Result.success(text))
                    }
                    .addOnFailureListener { error ->
                        continuation.resume(Result.failure(error))
                    }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    /**
     * Extracts text from a device Uri (e.g. picked from Gallery/Photos).
     */
    suspend fun recognizeTextFromUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text.trim()
                        continuation.resume(Result.success(text))
                    }
                    .addOnFailureListener { error ->
                        continuation.resume(Result.failure(error))
                    }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    /**
     * Extracts distinct cleaned words suitable for dictionary searches from scanned text.
     */
    fun extractSearchableWords(text: String): List<String> {
        val wordRegex = Regex("[a-zA-Z\\u0600-\\u06FF]+")
        return wordRegex.findAll(text)
            .map { it.value.trim() }
            .filter { it.length >= 2 }
            .distinct()
            .toList()
    }
}
