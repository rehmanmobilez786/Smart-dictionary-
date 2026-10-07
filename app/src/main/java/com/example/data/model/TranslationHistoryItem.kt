package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationHistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String, // "en" or "ur"
    val targetLang: String, // "ur" or "en"
    val isOnline: Boolean,
    val wordBreakdown: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

data class TranslationResult(
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val isOnline: Boolean,
    val wordBreakdown: List<WordBreakdown> = emptyList(),
    val engineNote: String = ""
)

data class WordBreakdown(
    val original: String,
    val translated: String,
    val partOfSpeech: String = ""
)
