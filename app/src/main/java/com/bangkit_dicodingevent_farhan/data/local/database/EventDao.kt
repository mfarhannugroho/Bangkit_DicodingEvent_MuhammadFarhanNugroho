package com.bangkit_dicodingevent_farhan.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM event_table")
    fun getAllEvents(): LiveData<List<EventEntity>>

    @Query("SELECT * FROM event_table")
    suspend fun getAllFavorites(): List<EventEntity>

    @Query("SELECT * FROM event_table WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Int): EventEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("DELETE FROM event_table")
    suspend fun deleteAll()
}