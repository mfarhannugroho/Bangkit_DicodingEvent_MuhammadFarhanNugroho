package com.bangkit_dicodingevent_farhan.data.local.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "event_table")
@Parcelize
data class EventEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageLogo: String,
    val mediaCover: String,
    val ownerName: String,
    val beginTime: String,
    val quota: Int,
    val registrants: Int,
    val description: String,
    val link: String
) : Parcelable