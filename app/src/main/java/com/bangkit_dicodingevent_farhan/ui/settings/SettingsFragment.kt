package com.bangkit_dicodingevent_farhan.ui.settings

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bangkit_dicodingevent_farhan.R
import com.bangkit_dicodingevent_farhan.utils.ReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var notificationPreference: SwitchPreferenceCompat
    private lateinit var timePreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themePreference = findPreference<ListPreference>("theme")
        themePreference?.setOnPreferenceChangeListener { _, newValue ->
            applyTheme(newValue as String)
            true
        }

        notificationPreference = findPreference("notifications")!!
        timePreference = findPreference("reminder_time")!!

        timePreference.isVisible = notificationPreference.isChecked
        notificationPreference.setOnPreferenceChangeListener { _, isEnabled ->
            timePreference.isVisible = isEnabled as Boolean
            if (isEnabled) {
                scheduleDailyReminder(requireContext())
            } else {
                cancelDailyReminder(requireContext())
            }
            true
        }

        timePreference.setOnPreferenceClickListener {
            if (notificationPreference.isChecked) {
                showTimePickerDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Mohon aktifkan notifikasi terlebih dahulu untuk mengatur waktu pengingat.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            true
        }
    }

    private fun applyTheme(themeValue: String) {
        when (themeValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val selectedTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            scheduleDailyReminder(requireContext(), selectedTime)
        }, hour, minute, true).show()
    }

    private fun scheduleDailyReminder(context: Context, selectedTime: Calendar = Calendar.getInstance()) {
        val currentTime = Calendar.getInstance()
        if (selectedTime.before(currentTime)) {
            selectedTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = selectedTime.timeInMillis - currentTime.timeInMillis
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("reminder_worker")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_worker")
    }
}