package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WordItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("""
        SELECT * FROM words 
        WHERE english LIKE '%' || :query || '%' 
           OR urdu LIKE '%' || :query || '%' 
           OR romanUrdu LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN english LIKE :query || '%' THEN 1 
                WHEN urdu LIKE :query || '%' THEN 2
                WHEN romanUrdu LIKE :query || '%' THEN 3
                ELSE 4 
            END,
            english ASC
        LIMIT 100
    """)
    fun searchWords(query: String): Flow<List<WordItem>>

    @Query("""
        SELECT * FROM words 
        WHERE category = :category AND (
            english LIKE '%' || :query || '%' 
            OR urdu LIKE '%' || :query || '%' 
            OR romanUrdu LIKE '%' || :query || '%'
        )
        ORDER BY english ASC
        LIMIT 100
    """)
    fun searchWordsByCategory(query: String, category: String): Flow<List<WordItem>>

    @Query("SELECT * FROM words WHERE isFavorite = 1 ORDER BY english ASC")
    fun getFavorites(): Flow<List<WordItem>>

    @Query("SELECT * FROM words WHERE lastSearchedTimestamp > 0 ORDER BY lastSearchedTimestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<WordItem>>

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: Int): WordItem?

    @Query("SELECT * FROM words WHERE category = :category ORDER BY english ASC")
    fun getWordsByCategory(category: String): Flow<List<WordItem>>

    @Query("SELECT DISTINCT category FROM words ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(): WordItem?

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWords(limit: Int): List<WordItem>

    @Query("UPDATE words SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Int, isFav: Boolean)

    @Query("UPDATE words SET lastSearchedTimestamp = :timestamp, searchCount = searchCount + 1 WHERE id = :id")
    suspend fun recordSearch(id: Int, timestamp: Long)

    @Query("UPDATE words SET lastSearchedTimestamp = 0")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordItem): Long

    @Update
    suspend fun updateWord(word: WordItem)

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun deleteWord(id: Int)
}
