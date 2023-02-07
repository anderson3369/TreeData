package com.orchardmanager.treedata.ui.orchard

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.orchardmanager.treedata.R
import java.time.LocalDate
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DatePickerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DatePickerFragment: DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var date: LocalDate = LocalDate.now()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Calendar
        val cal = Calendar.getInstance()
        val yr = cal.get(Calendar.YEAR)
        val mnth = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireActivity(),this, yr, mnth, day)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        date = LocalDate.of(p1, p2, p3)
        setFragmentResult("requestDateKey", bundleOf("plantedDate" to date.toString()))
    }
}