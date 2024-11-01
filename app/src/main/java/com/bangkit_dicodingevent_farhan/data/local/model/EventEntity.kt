package com.bangkit_dicodingevent_farhan.data.local.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(tableName = "event_table")
@Parcelize
data class EventEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "image_logo")
    val imageLogo: String,

    @ColumnInfo(name = "media_cover")
    val mediaCover: String,

    @ColumnInfo(name = "owner_name")
    val ownerName: String,

    @ColumnInfo(name = "begin_time")
    val beginTime: String,

    @ColumnInfo(name = "quota")
    val quota: Int,

    @ColumnInfo(name = "registrants")
    val registrants: Int,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "link")
    val link: String
) : Parcelable {

    fun getFormattedBeginTime(): String {
        return try {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val targetFormat = SimpleDateFormat("dd/MM/yyyy \n 'Pukul:' HH:mm", Locale.getDefault())
            val date = originalFormat.parse(beginTime)
            "Tanggal: ${targetFormat.format(date!!)}"
        } catch (e: Exception) {
            beginTime
        }
    }
}