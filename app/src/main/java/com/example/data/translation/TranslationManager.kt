package com.example.data.translation

import com.example.data.local.TranslationDao
import com.example.data.local.WordDao
import com.example.data.model.TranslationHistoryItem
import com.example.data.model.TranslationResult
import com.example.data.remote.TranslationApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TranslationManager(
    private val translationDao: TranslationDao,
    wordDao: WordDao,
    private val translationApi: TranslationApi
) {
    private val offlineEngine = OfflineTranslationEngine(wordDao)

    val translationHistory: Flow<List<TranslationHistoryItem>> =
        translationDao.getAllTranslations()

    val favoriteTranslations: Flow<List<TranslationHistoryItem>> =
        translationDao.getFavoriteTranslations()

    suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
        preferOnline: Boolean
    ): TranslationResult = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext TranslationResult(
                sourceText = text,
                translatedText = "",
                sourceLang = sourceLang,
                targetLang = targetLang,
                isOnline = false
            )
        }

        if (preferOnline) {
            try {
                val langPair = "${sourceLang}|${targetLang}"
                val response = translationApi.translate(trimmed, langPair)
                val translated = response.responseData?.translatedText

                if (!translated.isNullOrBlank() && !translated.contains("MYMEMORY WARNING")) {
                    val result = TranslationResult(
                        sourceText = text,
                        translatedText = translated,
                        sourceLang = sourceLang,
                        targetLang = targetLang,
                        isOnline = true,
                        engineNote = "آن لائن لائیو ترجمہ (Live Online Translation)"
                    )
                    // Record in history
                    recordHistory(result)
                    return@withContext result
                }
            } catch (e: Exception) {
                // Online failed, fallback to offline
            }
        }

        // Offline mode or fallback
        val offlineResult = offlineEngine.translate(text, sourceLang, targetLang)
        recordHistory(offlineResult)
        offlineResult
    }

    private suspend fun recordHistory(result: TranslationResult) {
        if (result.translatedText.isNotBlank()) {
            val item = TranslationHistoryItem(
                sourceText = result.sourceText,
                translatedText = result.translatedText,
                sourceLang = result.sourceLang,
                targetLang = result.targetLang,
                isOnline = result.isOnline,
                wordBreakdown = result.wordBreakdown.joinToString("; ") { "${it.original}: ${it.translated}" }
            )
            translationDao.insertTranslation(item)
        }
    }

    suspend fun toggleFavorite(item: TranslationHistoryItem) = withContext(Dispatchers.IO) {
        translationDao.setFavorite(item.id, !item.isFavorite)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        translationDao.clearAllTranslations()
    }
}
