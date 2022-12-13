package com.orchardmanager.treedata.ui.orchard

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.viewModels
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.databinding.FragmentOrchardBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class OrchardFragment : Fragment(), View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentOrchardBinding? = null
    private val binding get() = _binding

    companion object {
        fun newInstance() = OrchardFragment()
    }

    private val orchardViewModel: OrchardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_orchard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(OrchardViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onClick(p0: View?) {
        if(p0?.id == R.id.saveOrchard) {
            //save
        } else if(p0?.id == R.id.showPlantedDate) {
            //Calendar
            val cal = Calendar.getInstance()
            val yr = cal.get(Calendar.YEAR)
            val mnth = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            val picker = DatePickerDialog(requireActivity(),this, yr, mnth, day)
            picker.show()
        }
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        binding?.plantedDate?.setText(p2.toString() + "-" + p3.toString() + "-" + p1.toString())
    }

}