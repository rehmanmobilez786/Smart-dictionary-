package com.example.data.translation

import com.example.data.local.CommonPhrases
import com.example.data.local.WordDao
import com.example.data.model.TranslationResult
import com.example.data.model.WordBreakdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class OfflineTranslationEngine(private val wordDao: WordDao) {

    suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): TranslationResult = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext TranslationResult(
                sourceText = text,
                translatedText = "",
                sourceLang = sourceLang,
                targetLang = targetLang,
                isOnline = false,
                engineNote = "خالی متن"
            )
        }

        val isEnToUr = sourceLang == "en"

        // 1. Check exact phrase match in curated common phrases
        val exactPhrase = CommonPhrases.findExactPhrase(trimmed, isEnToUr)
        if (exactPhrase != null) {
            return@withContext TranslationResult(
                sourceText = text,
                translatedText = exactPhrase,
                sourceLang = sourceLang,
                targetLang = targetLang,
                isOnline = false,
                engineNote = "آف لائن لغت کا براہِ راست ترجمہ (Exact match)"
            )
        }

        // 2. Check if the entire query matches a word in the dictionary
        val dictResults = wordDao.searchWords(trimmed).firstOrNull() ?: emptyList()
        val exactWordMatch = dictResults.firstOrNull {
            if (isEnToUr) it.english.equals(trimmed, ignoreCase = true)
            else it.urdu.contains(trimmed) || it.romanUrdu.equals(trimmed, ignoreCase = true)
        }

        if (exactWordMatch != null) {
            val translated = if (isEnToUr) exactWordMatch.urdu else exactWordMatch.english
            return@withContext TranslationResult(
                sourceText = text,
                translatedText = translated,
                sourceLang = sourceLang,
                targetLang = targetLang,
                isOnline = false,
                wordBreakdown = listOf(
                    WordBreakdown(
                        original = trimmed,
                        translated = translated,
                        partOfSpeech = exactWordMatch.partOfSpeech
                    )
                ),
                engineNote = "آف لائن لغت کا لفظی ترجمہ (Dictionary match)"
            )
        }

        // 3. Sentence tokenization & word-by-word synthesis
        val tokens = trimmed.split(Regex("\\s+"))
        val breakdownList = mutableListOf<WordBreakdown>()
        val translatedTokens = mutableListOf<String>()

        for (token in tokens) {
            val cleanToken = token.replace(Regex("[^\\p{L}\\p{Nd}]"), "")
            if (cleanToken.isEmpty()) {
                translatedTokens.add(token)
                continue
            }

            if (isEnToUr) {
                val lowerToken = cleanToken.lowercase()
                // Check grammar word
                val grammarMeaning = CommonPhrases.commonGrammarEnToUr[lowerToken]
                if (grammarMeaning != null) {
                    val cleanMeaning = grammarMeaning.split("/").first().trim()
                    translatedTokens.add(cleanMeaning)
                    breakdownList.add(WordBreakdown(cleanToken, grammarMeaning, "Grammar"))
                } else {
                    // Check local dictionary
                    val tokenMatches = wordDao.searchWords(cleanToken).firstOrNull() ?: emptyList()
                    val match = tokenMatches.firstOrNull { it.english.equals(cleanToken, ignoreCase = true) }
                        ?: tokenMatches.firstOrNull()

                    if (match != null) {
                        val simpleMeaning = match.urdu.split("/").first().trim()
                        translatedTokens.add(simpleMeaning)
                        breakdownList.add(WordBreakdown(cleanToken, match.urdu, match.partOfSpeech))
                    } else {
                        translatedTokens.add(token) // Keep original if unknown
                        breakdownList.add(WordBreakdown(cleanToken, "(نامعلوم / Unknown)"))
                    }
                }
            } else {
                // Urdu to English
                val tokenMatches = wordDao.searchWords(cleanToken).firstOrNull() ?: emptyList()
                val match = tokenMatches.firstOrNull { it.urdu.contains(cleanToken) }
                    ?: tokenMatches.firstOrNull()

                if (match != null) {
                    translatedTokens.add(match.english)
                    breakdownList.add(WordBreakdown(cleanToken, match.english, match.partOfSpeech))
                } else {
                    translatedTokens.add(token)
                    breakdownList.add(WordBreakdown(cleanToken, "(Unknown)"))
                }
            }
        }

        val assembledSentence = translatedTokens.joinToString(" ")
        TranslationResult(
            sourceText = text,
            translatedText = assembledSentence,
            sourceLang = sourceLang,
            targetLang = targetLang,
            isOnline = false,
            wordBreakdown = breakdownList,
            engineNote = "آف لائن تجزیاتی ترجمہ (Offline Synthesized Translation)"
        )
    }
}
