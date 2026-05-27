package com.orchardlog.treedata.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.orchardlog.treedata.R
import java.util.Calendar
import java.util.Locale

class DatePickerFragment(private val requestKey: String, private val key: String): DialogFragment(),
    DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cal = Calendar.getInstance()
        val yr = cal.get(Calendar.YEAR)
        val mnth = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireActivity(), this, yr, mnth, day)
    }

    override fun onDateSet(p0: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        // monthOfYear is 0-indexed, convert to MM-DD-YYYY to match expectations in Compose fragments
        val dateStr = String.format(Locale.US, "%02d-%02d-%d", monthOfYear + 1, dayOfMonth, year)
        setFragmentResult(requestKey, bundleOf(key to dateStr))
    }
}
