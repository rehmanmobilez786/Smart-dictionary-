package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.WordDao
import com.example.data.model.WordItem
import com.example.data.remote.OnlineDictionaryApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DictionaryRepository(
    private val wordDao: WordDao,
    private val onlineApi: OnlineDictionaryApi
) {
    val favorites: Flow<List<WordItem>> = wordDao.getFavorites()
    val recentHistory: Flow<List<WordItem>> = wordDao.getRecentHistory()
    val categories: Flow<List<String>> = wordDao.getAllCategories()

    fun searchWords(query: String): Flow<List<WordItem>> {
        return wordDao.searchWords(query.trim())
    }

    fun searchWordsByCategory(query: String, category: String): Flow<List<WordItem>> {
        return if (category == "All" || category == "سب" || category == "All / تمام") {
            wordDao.searchWords(query.trim())
        } else {
            wordDao.searchWordsByCategory(query.trim(), category)
        }
    }

    suspend fun getWordOfDay(): WordItem? = withContext(Dispatchers.IO) {
        wordDao.getRandomWord()
    }

    suspend fun getRandomQuizWords(count: Int = 4): List<WordItem> = withContext(Dispatchers.IO) {
        wordDao.getRandomWords(count)
    }

    suspend fun toggleFavorite(word: WordItem) = withContext(Dispatchers.IO) {
        wordDao.setFavorite(word.id, !word.isFavorite)
    }

    suspend fun recordSearch(word: WordItem) = withContext(Dispatchers.IO) {
        wordDao.recordSearch(word.id, System.currentTimeMillis())
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        wordDao.clearHistory()
    }

    suspend fun addCustomWord(word: WordItem): Long = withContext(Dispatchers.IO) {
        wordDao.insertWord(word)
    }

    suspend fun deleteWord(id: Int) = withContext(Dispatchers.IO) {
        wordDao.deleteWord(id)
    }

    suspend fun ensureDatabasePopulated() = withContext(Dispatchers.IO) {
        AppDatabase.populateDatabase(wordDao)
    }

    suspend fun searchOnlineWord(query: String): Result<WordItem> = withContext(Dispatchers.IO) {
        try {
            val trimmed = query.trim().lowercase()
            val entries = onlineApi.lookupWord(trimmed)
            if (entries.isNotEmpty()) {
                val entry = entries.first()
                val wordText = entry.word?.replaceFirstChar { it.uppercase() } ?: query
                val phonetic = entry.phonetic ?: entry.phonetics?.firstOrNull { !it.text.isNullOrBlank() }?.text ?: ""
                val firstMeaning = entry.meanings?.firstOrNull()
                val partOfSpeech = firstMeaning?.partOfSpeech?.replaceFirstChar { it.uppercase() } ?: "Noun"
                val definition = firstMeaning?.definitions?.firstOrNull()?.definition ?: "No definition found"
                val example = firstMeaning?.definitions?.firstOrNull()?.example ?: ""
                val synonyms = firstMeaning?.synonyms?.joinToString(", ") ?: ""

                // Derive a helpful Roman Urdu / Urdu placeholder note for newly fetched online words
                val urduTranslit = "آن لائن تلاش شدہ لفظ ($wordText)"
                val urduDef = "آن لائن ڈکشنری سے حاصل کردہ تعریف: $definition"

                val item = WordItem(
                    english = wordText,
                    urdu = urduTranslit,
                    romanUrdu = if (phonetic.isNotBlank()) "Phonetic: $phonetic" else wordText,
                    partOfSpeech = "$partOfSpeech ($phonetic)",
                    definition = definition,
                    urduDefinition = urduDef,
                    exampleEn = example,
                    exampleUr = if (example.isNotBlank()) "مثال: $example" else "",
                    synonyms = synonyms,
                    category = "آن لائن (Online)",
                    isCustom = true
                )
                Result.success(item)
            } else {
                Result.failure(Exception("لفظ نہیں مل سکا / Word not found in online dictionary"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
