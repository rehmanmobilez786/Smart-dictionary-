package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TranslationHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllTranslations(): Flow<List<TranslationHistoryItem>>

    @Query("SELECT * FROM translation_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteTranslations(): Flow<List<TranslationHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(item: TranslationHistoryItem): Long

    @Query("UPDATE translation_history SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Int, isFav: Boolean)

    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteTranslation(id: Int)

    @Query("DELETE FROM translation_history")
    suspend fun clearAllTranslations()
}
