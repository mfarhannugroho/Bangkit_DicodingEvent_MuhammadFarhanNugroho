package com.bangkit_dicodingevent_farhan.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bangkit_dicodingevent_farhan.R
import com.bangkit_dicodingevent_farhan.data.remote.retrofit.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getEvents(1)
                if (response.isSuccessful) {
                    val events = response.body()?.listEvents
                    val event = events?.firstOrNull()
                    event?.let {
                        showNotification(
                            eventId = it.id,
                            content = "${it.name} pada ${it.beginTime}"
                        )
                    }
                } else {
                    Log.e("ReminderWorker", "Gagal mengambil data event: ${response.message()}")
                }
                Result.success()
            } catch (e: Exception) {
                Log.e("ReminderWorker", "Kesalahan saat mengambil data event", e)
                Result.failure()
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun showNotification(eventId: Int, content: String) {
        val channelId = "event_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("ReminderWorker", "Izin notifikasi tidak diberikan")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Reminder"
            val descriptionText = "Pengingat untuk acara yang akan datang"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Gunakan NavDeepLinkBuilder untuk membuat PendingIntent yang mengarah ke detail event dengan deeplink
        val pendingIntent = NavDeepLinkBuilder(applicationContext)
            .setGraph(R.navigation.nav_graph) // Gantikan dengan ID grafik navigasi Anda
            .setDestination(R.id.eventDetailFragment)
            .setArguments(Bundle().apply { putInt("eventId", eventId) })
            .createPendingIntent()

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Pengingat Event Mendatang")
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(eventId, notification)
    }
}