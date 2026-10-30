package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EssayDao {

    @Query("SELECT * FROM essays ORDER BY createdAtTimestamp DESC")
    fun getAllEssays(): Flow<List<EssayEntity>>

    @Query("SELECT * FROM essays WHERE id = :id LIMIT 1")
    suspend fun getEssayById(id: String): EssayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssay(essay: EssayEntity)

    @Query("UPDATE essays SET isSynced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE essays SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllAsSynced()

    @Query("SELECT COUNT(*) FROM essays WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("DELETE FROM essays WHERE id = :id")
    suspend fun deleteEssay(id: String)
}
