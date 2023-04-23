package com.orchardmanager.treedata.ui.fertilizer

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateFormat.is24HourFormat
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalTime
import java.util.*

class FertilizerTimePickerFragment constructor(private val isStartTime: Boolean): DialogFragment(),
    TimePickerDialog.OnTimeSetListener {

    private var requestKey = "requestFertilizerStartTimeKey"

    init {
        if (!isStartTime) {
            requestKey = "requestFertilizerStopTimeKey"
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val time = LocalTime.of(hourOfDay, minute)
        setFragmentResult(requestKey, bundleOf("fertilizerTime" to DateConverter().fromTime(time)))
    }
}