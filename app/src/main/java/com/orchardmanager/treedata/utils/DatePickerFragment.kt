package com.orchardmanager.treedata.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDate
import java.util.Calendar

class DatePickerFragment constructor(private val requestKey: String, private val key: String): DialogFragment(),
    DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Calendar
        val cal = Calendar.getInstance()
        val yr = cal.get(Calendar.YEAR)
        val mnth = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireActivity(),this, yr, mnth, day)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val date = LocalDate.of(p1, p2, p3)
        setFragmentResult(requestKey, bundleOf(key to DateConverter().fromOffsetDate(date)))
    }
}