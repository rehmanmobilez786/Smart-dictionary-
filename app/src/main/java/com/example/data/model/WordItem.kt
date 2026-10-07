package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val english: String,
    val urdu: String,
    val romanUrdu: String,
    val partOfSpeech: String, // e.g. "Noun (اسم)", "Verb (فعل)", "Adjective (صفت)"
    val definition: String,
    val urduDefinition: String,
    val exampleEn: String = "",
    val exampleUr: String = "",
    val synonyms: String = "",
    val antonyms: String = "",
    val category: String = "عام (General)", // Academic, Science & Tech, Medical, Law, Business, Daily, Literature
    val isFavorite: Boolean = false,
    val searchCount: Int = 0,
    val lastSearchedTimestamp: Long = 0L,
    val isCustom: Boolean = false
)
