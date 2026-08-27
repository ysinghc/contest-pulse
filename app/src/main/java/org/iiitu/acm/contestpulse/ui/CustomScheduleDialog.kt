package org.iiitu.acm.contestpulse.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import org.iiitu.acm.contestpulse.data.model.Contest
import org.iiitu.acm.contestpulse.databinding.DialogCustomScheduleBinding
import java.util.Calendar

class CustomScheduleDialog(
    private val context: Context,
    private val onSave: (Contest) -> Unit
) {

    fun show() {
        val binding = DialogCustomScheduleBinding.inflate(LayoutInflater.from(context))

        // Week starts on Monday
        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, days)
        binding.spinnerDay.adapter = adapter

        binding.timePicker.setIs24HourView(false)

        val alertDialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton("Save Reminder", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.setOnShowListener {
            val positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveBtn.setOnClickListener {
                val title = binding.etTitle.text.toString().trim()
                if (title.isEmpty()) {
                    Toast.makeText(context, "Please enter a contest title", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val dayIndex = binding.spinnerDay.selectedItemPosition
                val targetDayOfWeek = when (dayIndex) {
                    0 -> Calendar.MONDAY
                    1 -> Calendar.TUESDAY
                    2 -> Calendar.WEDNESDAY
                    3 -> Calendar.THURSDAY
                    4 -> Calendar.FRIDAY
                    5 -> Calendar.SATURDAY
                    else -> Calendar.SUNDAY
                }

                val hour = binding.timePicker.hour
                val minute = binding.timePicker.minute

                val cal = Calendar.getInstance().apply {
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.DAY_OF_WEEK, targetDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.WEEK_OF_YEAR, 1)
                    }
                }

                val amPmStr = if (hour >= 12) "PM" else "AM"
                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                val timeStr = String.format("%02d:%02d %s", displayHour, minute, amPmStr)

                val customContest = Contest(
                    id = "CUSTOM_" + System.currentTimeMillis(),
                    title = title,
                    platform = "CUSTOM",
                    startTimeMillis = cal.timeInMillis,
                    durationMillis = 120 * 60 * 1000L,
                    url = "",
                    isCustom = true,
                    isAlarmEnabled = true,
                    dayOfWeek = days[dayIndex].uppercase(),
                    timeString = timeStr
                )

                onSave(customContest)
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }
}
