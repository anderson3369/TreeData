package com.orchardmanager.treedata.ui.irrigation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.data.Validator
import com.orchardmanager.treedata.databinding.FragmentIrrigationBinding
import com.orchardmanager.treedata.entities.Irrigation
import com.orchardmanager.treedata.entities.IrrigationSystem
import com.orchardmanager.treedata.ui.SAVE_FAILED
import com.orchardmanager.treedata.utils.DatePickerFragment
import com.orchardmanager.treedata.utils.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@AndroidEntryPoint
class IrrigationFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener {

    private var _binding: FragmentIrrigationBinding? = null
    private val binding get() = _binding
    private val irrigationViewModel: IrrigationViewModel by viewModels()
    private var irrigationSystemID: Long = 0L
    private var irrigation: Irrigation? = null
    private var irrigationSystems: MutableList<IrrigationSystem>? = null
    private val irrigationStartDateRequestKey = "requestIrrigationStartDateKey"
    private val irrigationStopDateRequestKey = "requestIrrigationStopDateKey"
    private val irrigationDateKey = "irrigationDate"
    private val irrigationStartTimeRequestKey = "requestIrrigationStartTimeKey"
    private val irrigationStopTimeRequestKey = "requestIrrigationStopTimeKey"
    private val irrigationTimeKey = "irrigationTime"

    companion object {
        fun newInstance() = IrrigationFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIrrigationBinding.inflate(inflater, container, false)
        val vw = binding?.root

        val calStart = Calendar.getInstance()
        val yearStart = calStart.get(Calendar.YEAR)
        val startDate = LocalDate.of(yearStart, 1, 1)
        val endDate = LocalDate.of(yearStart, 12, 31)
        irrigationViewModel.getIrrgationsBetween(startDate, endDate).observe(viewLifecycleOwner, Observer {
            irrigations ->
            val adapter = ArrayAdapter<Irrigation>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, irrigations)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.irrigations?.adapter = adapter
            binding?.irrigations?.onItemSelectedListener = irrigationsSelector()
        })

        irrigationViewModel.getIrrigationSystem().observe(viewLifecycleOwner, Observer {
            irrigationSystems ->
            this.irrigationSystems = irrigationSystems
            val adapter = ArrayAdapter<IrrigationSystem>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, irrigationSystems)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.irrigationSystemWithIrrigation?.adapter = adapter
            binding?.irrigationSystemWithIrrigation?.onItemSelectedListener = this
        })

        binding?.irrigationStartDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.irrigationStartDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding?.irrigationStartTime?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val time = binding?.irrigationStartTime?.text.toString()
                if(!Validator.validateTime(time)) {
                    Toast.makeText(requireContext(), "Invalid time format 00:00", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding?.irrigationStopDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.irrigationStopDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding?.irrigationStopTime?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val time = binding?.irrigationStopTime?.text.toString()
                if(!Validator.validateTime(time)) {
                    Toast.makeText(requireContext(), "Invalid time format 00:00", Toast.LENGTH_LONG).show()
                }
            }

        })

        binding?.irrigationStartDateCal?.setOnClickListener(View.OnClickListener {
            DatePickerFragment(irrigationStartDateRequestKey, irrigationDateKey)
                .show(childFragmentManager, getString(R.string.start_date))
        })

        binding?.irrigationStopDateCal?.setOnClickListener(View.OnClickListener {
            DatePickerFragment(irrigationStopDateRequestKey, irrigationDateKey)
                .show(childFragmentManager, getString(R.string.stop_date))
        })

        binding?.irrigationStartTimeClock?.setOnClickListener(View.OnClickListener {
            TimePickerFragment(irrigationStartTimeRequestKey, irrigationTimeKey)
                .show(childFragmentManager, getString(R.string.start_time))
        })

        binding?.irrigationStopTimeClock?.setOnClickListener(View.OnClickListener {
            TimePickerFragment(irrigationStopTimeRequestKey, irrigationTimeKey)
                .show(childFragmentManager, getString(R.string.stop_time))
        })

        binding?.saveIrrigation?.setOnClickListener(this)

        binding?.newIrrigation?.setOnClickListener(View.OnClickListener {
            irrigation = null
            binding?.irrigationStartDate?.setText("")
            binding?.irrigationStartTime?.setText("")
            binding?.irrigationStopDate?.setText("")
            binding?.irrigationStopTime?.setText("")

        })

        binding?.deleteIrrigation?.setOnClickListener(View.OnClickListener {
            if(irrigation != null) {
                irrigationViewModel?.deleteIrrigation(irrigation!!)
            }
        })

        addIrrigationSystem()
        return vw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(irrigationStartDateRequestKey, requireActivity()) {
            dateKey, bundle -> binding?.irrigationStartDate?.setText(bundle.getString(irrigationDateKey))
        }
        childFragmentManager.setFragmentResultListener(irrigationStopDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.irrigationStopDate?.setText(bundle.getString(irrigationDateKey))
        }
        childFragmentManager.setFragmentResultListener(irrigationStartTimeRequestKey, requireActivity()) {
            dateKey, bundle -> binding?.irrigationStartTime?.setText(bundle.getString(irrigationTimeKey))
        }
        childFragmentManager.setFragmentResultListener(irrigationStopTimeRequestKey, requireActivity()) {
            dateKey, bundle -> binding?.irrigationStopTime?.setText(bundle.getString(irrigationTimeKey))
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(IrrigationViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun addIrrigationSystem() {
        binding?.addIrrigationSystem?.setOnClickListener(View.OnClickListener {
            val action = IrrigationFragmentDirections.actionNavIrrigationToNavIrrigationSystem()
            view?.findNavController()?.navigate(action)
        })
    }

    inner class irrigationsSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is Irrigation) {
                this@IrrigationFragment.irrigation = obj
                this@IrrigationFragment.irrigationSystemID = obj.irrigationSystemId
                val irrsys = this@IrrigationFragment.irrigationSystems?.filter {
                    it.id == this@IrrigationFragment.irrigationSystemID }?.get(0)
                binding?.irrigationSystemWithIrrigation?.setSelection(
                    this@IrrigationFragment.irrigationSystems!!.indexOf(irrsys!!))
                parseLocalDateTime(isStart = true, obj.startTime)
                parseLocalDateTime(isStart = false, obj.stopTime)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is IrrigationSystem) {
            irrigationSystemID = obj.id
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun parseLocalDateTime(isStart: Boolean, dateTime: LocalDateTime) {
        if(isStart) {
            val date = dateTime.toLocalDate()
            binding?.irrigationStartDate?.setText(DateConverter().fromOffsetDate(date))
            val time = dateTime.toLocalTime()
            binding?.irrigationStartTime?.setText(DateConverter().fromTime(time))
        } else {
            val date = dateTime.toLocalDate()
            binding?.irrigationStopDate?.setText(DateConverter().fromOffsetDate(date))
            val time = dateTime.toLocalTime()
            binding?.irrigationStopTime?.setText(DateConverter().fromTime(time))
        }
    }

    private fun buildLocalDateTime(isStart: Boolean): LocalDateTime {
        var dateString = ""
        if(isStart) {
            dateString = binding?.irrigationStartDate?.text.toString()
            dateString = dateString + " " + binding?.irrigationStartTime?.text.toString()
        } else {
            dateString = binding?.irrigationStopDate?.text.toString()
            dateString = dateString + " " + binding?.irrigationStopTime?.text.toString()
        }
        return DateConverter().toOffsetDateTime(dateString)!!
    }

    override fun onClick(v: View?) {
        val startTime = buildLocalDateTime(true)
        val stopTime = buildLocalDateTime(false)
        if(irrigation != null && irrigation!!.id > 0L) {
            val irr = irrigation!!.copy(
                id = irrigation!!.id,
                irrigationSystemId = irrigationSystemID,
                startTime = startTime,
                stopTime = stopTime
            )
            try {
                irrigationViewModel.updateIrrigation(irr)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
            }

        } else {
            irrigation = Irrigation(
                irrigationSystemId = irrigationSystemID,
                startTime = startTime,
                stopTime = stopTime
            )
            irrigationViewModel.addIrrigation(irrigation!!).observe(this, Observer {
                id ->
                Log.i("IrrigationFragment", "the irrigaton id is " + id.toString())
                if(!id.equals(SAVE_FAILED)) {
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
                }
            })
        }
    }


}