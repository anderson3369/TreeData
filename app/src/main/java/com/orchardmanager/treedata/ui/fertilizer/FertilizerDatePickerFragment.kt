package com.orchardmanager.treedata.ui.fertilizer

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDate
import java.util.*

class FertilizerDatePickerFragment constructor(private val isStartDate: Boolean): DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var requestKey = "requestFertilizerStartDateKey"

    init {
        if (!isStartDate) {
            requestKey = "requestFertilizerStopDateKey"
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Calendar
        val cal = Calendar.getInstance()
        val yr = cal.get(Calendar.YEAR)
        val mnth = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
Log.i("FertilizerDatePickerFragfment", "the month is " + mnth.toString())
        return DatePickerDialog(requireActivity(),this, yr, mnth, day)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val date = LocalDate.of(p1, p2, p3)
        setFragmentResult(requestKey, bundleOf("fertilizerDate" to DateConverter().fromOffsetDate(date)))
    }
}