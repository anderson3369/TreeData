package com.orchardmanager.treedata.ui.orchard

import android.os.Bundle
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
import com.orchardmanager.treedata.databinding.FragmentOrchardTaskBinding
import com.orchardmanager.treedata.entities.OrchardActivity
import com.orchardmanager.treedata.ui.SAVE_FAILED
import com.orchardmanager.treedata.utils.DatePickerFragment
import com.orchardmanager.treedata.utils.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime

@AndroidEntryPoint
class OrchardTaskFragment : Fragment(), AdapterView.OnItemSelectedListener,
    View.OnClickListener {

    private val orchardViewModel: OrchardViewModel by viewModels()
    private var _binding: FragmentOrchardTaskBinding? = null
    private val binding get() = _binding
    private var activities: MutableList<OrchardActivity>? = null
    private var activityArray: Array<String>? = null
    private var task: String? = null
    private var orchardActivity: OrchardActivity? = null
    private val taskStartDateRequestKey = "requestTaskStartDateKey"
    private val taskStopDateRequestKey = "requestTaskStopDateKey"
    private val taskDateKey = "taskDate"
    private val taskStartTimeRequestKey = "requestTaskStartTimeKey"
    private val taskStopTimeRequestKey = "requestTaskStopTimeKey"
    private val taskTimeKey = "taskTime"

    companion object {
        fun newInstance() = OrchardTaskFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrchardTaskBinding.inflate(inflater, container, false)
        val vw = binding?.root

        activityArray = resources.getStringArray(R.array.orchard_activities)

        orchardViewModel?.getOrchardActivity()?.observe(viewLifecycleOwner, Observer {
            activities ->
            this.activities = activities
            val adapter = ArrayAdapter<OrchardActivity>(requireContext(), R.layout.farm_spinner_layout,
             R.id.textViewFarmSpinner, activities)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.orchardActivity?.adapter = adapter
            binding?.orchardActivity?.onItemSelectedListener = this
        })

        val activityAdapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, activityArray!!)
        activityAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.activities?.adapter = activityAdapter
        binding?.activities?.onItemSelectedListener = activitySelector()

        binding?.saveActivity?.setOnClickListener(this)

        binding?.newActivity?.setOnClickListener(View.OnClickListener {
            this.orchardActivity = null
            binding?.notes?.setText("")
            binding?.activityStartDate?.setText("")
            binding?.activityStartTime?.setText("")
            binding?.activityStopDate?.setText("")
            binding?.activityStopTime?.setText("")
        })

        binding?.deleteActivity?.setOnClickListener(View.OnClickListener {
            orchardViewModel?.deleteOrchardActivity(this.orchardActivity!!)
        })

        binding?.showActivityStartDate?.setOnClickListener(View.OnClickListener {
            DatePickerFragment(taskStartDateRequestKey, taskDateKey)
                .show(childFragmentManager, R.string.start_date.toString())
        })
        binding?.showActivityStopDate?.setOnClickListener(View.OnClickListener {
            DatePickerFragment(taskStopDateRequestKey, taskDateKey)
                .show(childFragmentManager, R.string.stop_date.toString())
        })
        binding?.showActivityStartTimeClock?.setOnClickListener(View.OnClickListener {
            TimePickerFragment(taskStartTimeRequestKey, taskTimeKey)
                .show(childFragmentManager, R.string.start_time.toString())
        })
        binding?.showActivityStopTimeClock?.setOnClickListener(View.OnClickListener {
            TimePickerFragment(taskStopTimeRequestKey, taskTimeKey)
                .show(childFragmentManager, R.string.stop_time.toString())
        })
        return vw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(taskStartDateRequestKey, requireActivity()) {
            dateKey, bundle -> binding?.activityStartDate?.setText(bundle?.getString(taskDateKey))
        }
        childFragmentManager.setFragmentResultListener(taskStopDateRequestKey, requireActivity()) {
            datekey, bundle -> binding?.activityStopDate?.setText(bundle?.getString(taskDateKey))
        }
        childFragmentManager.setFragmentResultListener(taskStartTimeRequestKey, requireActivity()) {
            dateKey, bundle -> binding?.activityStartTime?.setText(bundle?.getString(taskTimeKey))
        }
        childFragmentManager.setFragmentResultListener(taskStopTimeRequestKey, requireActivity()) {
            dateKey, bundle -> binding?.activityStopTime?.setText(bundle?.getString(taskTimeKey))
        }
    }

    inner class activitySelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is String) {
                this@OrchardTaskFragment.task = obj
                if(obj == getString(R.string.soil_moisture)) {
                    val action = OrchardTaskFragmentDirections.actionNavOrchardTaskToNavSoilMoisture()
                    view?.findNavController()?.navigate(action)
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is OrchardActivity) {
            this.orchardActivity = obj
            binding?.activities?.setSelection(activityArray!!.indexOf(obj.activity))
            binding?.notes?.setText(obj.notes)
            parseLocalDateTime(true, obj.activityStart)
            parseLocalDateTime(false, obj.activityStop)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun parseLocalDateTime(isStart: Boolean, dateTime: LocalDateTime) {
        if(isStart) {
            val date = dateTime.toLocalDate()
            binding?.activityStartDate?.setText(DateConverter().fromOffsetDate(date))
            val time = dateTime.toLocalTime()
            binding?.activityStartTime?.setText(DateConverter().fromTime(time))
        } else {
            val date = dateTime.toLocalDate()
            binding?.activityStopDate?.setText(DateConverter().fromOffsetDate(date))
            val time = dateTime.toLocalTime()
            binding?.activityStopTime?.setText(DateConverter().fromTime(time))
        }
    }

    private fun buildLocalDateTime(isStart: Boolean): LocalDateTime {
        var dateString: String
        if(isStart) {
            dateString = binding?.activityStartDate?.text.toString()
            dateString = dateString + " " + binding?.activityStartTime?.text.toString()
        } else {
            dateString = binding?.activityStopDate?.text.toString()
            dateString = dateString + " " + binding?.activityStopTime?.text.toString()
        }
        return DateConverter().toOffsetDateTime(dateString)!!
    }

    override fun onClick(v: View?) {
        if(orchardActivity != null && orchardActivity!!.id > 0) {
            val orchActivity = orchardActivity!!.copy(
                id = orchardActivity!!.id,
                activity = this.task!!,
                notes = binding?.notes?.text.toString(),
                activityStart = buildLocalDateTime(true),
                activityStop = buildLocalDateTime(false)
            )
            try {
                orchardViewModel?.updateOrchardActivity(orchActivity!!)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
            }
        } else {
            this.orchardActivity = OrchardActivity(
                activity = this.task!!,
                notes = binding?.notes?.text.toString(),
                activityStart = buildLocalDateTime(true),
                activityStop = buildLocalDateTime(false)
            )
            orchardViewModel?.addOrchardActivity(this.orchardActivity!!)?.observe(this, Observer {
                id ->
                if(!id.equals(SAVE_FAILED)) {
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

}