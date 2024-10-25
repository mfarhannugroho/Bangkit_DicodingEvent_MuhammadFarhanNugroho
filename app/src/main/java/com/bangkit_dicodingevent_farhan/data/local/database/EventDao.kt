package com.bangkit_dicodingevent_farhan.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity

@Dao
interface EventDao {
    // Mengambil semua data event dari tabel
    @Query("SELECT * FROM event_table")
    fun getAllEvents(): LiveData<List<EventEntity>>

    // Menyisipkan data event baru
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: EventEntity)

    // Menghapus semua data dari tabel event
    @Query("DELETE FROM event_table")
    suspend fun deleteAll()
}