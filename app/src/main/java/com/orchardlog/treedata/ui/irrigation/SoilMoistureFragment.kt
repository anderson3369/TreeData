package com.orchardlog.treedata.ui.irrigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.orchardlog.treedata.R
import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.data.Validator
import com.orchardlog.treedata.databinding.FragmentSoilMoistureBinding
import com.orchardlog.treedata.entities.Orchard
import com.orchardlog.treedata.entities.SoilMoisture
import com.orchardlog.treedata.ui.SAVE_FAILED
import com.orchardlog.treedata.ui.orchard.OrchardViewModel
import com.orchardlog.treedata.utils.DatePickerFragment
import com.orchardlog.treedata.utils.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime

@AndroidEntryPoint
class SoilMoistureFragment : Fragment(), AdapterView.OnItemSelectedListener,
    View.OnClickListener {

    val irrigationViewModel: IrrigationViewModel by viewModels()
    val orchardViewModel: OrchardViewModel by viewModels()
    private var _binding: FragmentSoilMoistureBinding? = null
    private val binding get() = _binding
    private var soilMoistureArray: MutableList<SoilMoisture>? = null
    private var soilMoisture: SoilMoisture? = null
    private var farmOrchardsMap: Map<Long, String>? = null
    private var orchardId: Long = 0L


    companion object {
        const val soilMoistureDateKey = "soilMoistureDate"
        const val soilMoistureRequestDateKey = "soilMoistureDateKey"
        const val soilMoistureTimeKey = "soilMoistureTime"
        const val soilMoistureRequestTimeKey = "soilMoistureTimeKey"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSoilMoistureBinding.inflate(inflater, container, false)
        val vw = binding?.root
        irrigationViewModel.getSoilMoisture().observe(viewLifecycleOwner) {
            soilMoisture ->
            soilMoistureArray = soilMoisture
            val adapter = ArrayAdapter<SoilMoisture>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, soilMoisture)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.soilMoisture?.adapter = adapter
            binding?.soilMoisture?.onItemSelectedListener = this
        }
        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner) {
                farmWithOrchards ->
            farmOrchardsMap = farmWithOrchards
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.orchardSoilMoisture?.adapter = adapter
            binding?.orchardSoilMoisture?.onItemSelectedListener = orchardSelector()
        }

        binding?.soilMoistureDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.soilMoistureDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), getString(R.string.invalid_date_format_mm_dd_yyyy),
                        Toast.LENGTH_LONG).show()
                }
            }
        })

        binding?.soilMoistureTime?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val time = binding?.soilMoistureTime?.text.toString()
                if(Validator.validateTime(time)) {
                    Toast.makeText(requireContext(), "Invalid time format 00:00", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding?.soilMoistureDateCal?.setOnClickListener {
            DatePickerFragment(soilMoistureRequestDateKey, soilMoistureDateKey)
                .show(childFragmentManager, getString(R.string.date))
        }

        binding?.soilMoistureTimeClock?.setOnClickListener {
            TimePickerFragment(soilMoistureRequestTimeKey, soilMoistureTimeKey)
                .show(childFragmentManager, getString(R.string.time))
        }

        binding?.saveSoilMoisture?.setOnClickListener(this)

        binding?.newSoilMoisture?.setOnClickListener {
            this.soilMoisture = null
            binding?.soilMoistureDate?.setText("")
            binding?.centibar?.setText("")
            binding?.percentSoilMoisture?.setText("")
        }

        binding?.deleteSoilMoisture?.setOnClickListener {
            if(soilMoisture != null) {
                irrigationViewModel.deleteSoilMoisture(this.soilMoisture!!)
            }
        }

        return vw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(soilMoistureRequestDateKey, requireActivity()) {
            dateKey, bundle -> binding?.soilMoistureDate?.setText(bundle.getString(soilMoistureDateKey))
        }
        childFragmentManager.setFragmentResultListener(soilMoistureRequestTimeKey, requireActivity()) {
            dateKey, bundle -> binding?.soilMoistureTime?.setText(bundle.getString(soilMoistureTimeKey))
        }
    }

    inner class orchardSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is Orchard) {
                this@SoilMoistureFragment.orchardId = obj.id
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is SoilMoisture) {
            this.soilMoisture = obj
            binding?.soilMoistureDate?.setText(DateConverter().fromOffsetDateTime(obj.date))
            binding?.centibar?.setText(obj.centibar.toString())
            binding?.percentSoilMoisture?.setText(obj.percent.toString())
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onClick(v: View?) {
        val sdate = binding?.soilMoistureDate?.text.toString()
        var date: LocalDateTime? = null
        if(!sdate.isEmpty()) {
            date = DateConverter().toOffsetDateTime(sdate)
        }
        val scentibar = binding?.centibar?.text.toString()
        var centibar: Int? = null
        if(!scentibar.isEmpty()) {
            centibar = scentibar.toInt()
        }
        val spercent = binding?.percentSoilMoisture?.text.toString()
        var percent: Int? = null
        if(!spercent.isEmpty()) {
            percent = spercent.toInt()
        }
        if(soilMoisture != null && soilMoisture!!.id > 0) {
            val sm = this.soilMoisture!!.copy(
                id = soilMoisture!!.id,
                orchardId = this.orchardId,
                date = date!!,
                centibar = centibar!!,
                percent = percent!!
            )
            try {
                irrigationViewModel.updateSoilMoisture(sm)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
            }
        } else {
            this.soilMoisture = SoilMoisture(
                orchardId = this.orchardId,
                date = date!!,
                centibar = centibar!!,
                percent = percent!!
            )
            irrigationViewModel.addSoilMoisture(this.soilMoisture!!).observe(this) {
                id ->
                if(!id.equals(SAVE_FAILED)) {
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}