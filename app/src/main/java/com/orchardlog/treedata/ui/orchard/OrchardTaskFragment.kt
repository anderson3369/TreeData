package com.orchardlog.treedata.ui.orchard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.orchardlog.treedata.R
import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.data.Validator
import com.orchardlog.treedata.databinding.FragmentOrchardTaskBinding
import com.orchardlog.treedata.entities.OrchardActivity
import com.orchardlog.treedata.utils.SAVE_FAILED
import com.orchardlog.treedata.utils.DatePickerFragment
import com.orchardlog.treedata.utils.SortOrchardActivity
import com.orchardlog.treedata.utils.TimePickerFragment
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
    private var farmOrchardsMap: Map<Long, String>? = null
    private var orchardId: Long = 0L

    companion object {
        const val TASKSTARTDATEREQUESTKEY = "requestTaskStartDateKey"
        const val TASKSTOPDATEREQUESTKEY = "requestTaskStopDateKey"
        const val TASKDATEKEY = "taskDate"
        const val TASKSTARTTIMEREQUESTKEY = "requestTaskStartTimeKey"
        const val TASKSTOPTIMEREQUESTKEY = "requestTaskStopTimeKey"
        const val TASKTIMEKEY = "taskTime"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrchardTaskBinding.inflate(inflater, container, false)
        val vw = binding?.root

        activityArray = resources.getStringArray(R.array.orchard_activities)

        orchardViewModel.getOrchardActivity().observe(viewLifecycleOwner) {
            activities ->
            this.activities = activities
            val adapter = ArrayAdapter(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, activities)
            adapter.sort(SortOrchardActivity())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.orchardActivity?.adapter = adapter
            binding?.orchardActivity?.onItemSelectedListener = this

        }

        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner) {
                farmWithOrchards ->
            farmOrchardsMap = farmWithOrchards
            val adapter = ArrayAdapter(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.activityOrchard?.adapter = adapter
            binding?.activityOrchard?.onItemSelectedListener = OrchardSelector()
        }

        val activityAdapter = ArrayAdapter(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, activityArray!!)
        activityAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.activities?.adapter = activityAdapter
        binding?.activities?.onItemSelectedListener = ActivitySelector()

        binding?.saveActivity?.setOnClickListener(this)

        binding?.newActivity?.setOnClickListener {
            this.orchardActivity = null
            binding?.orchardNotes?.setText("")
            binding?.activityStartDate?.setText("")
            binding?.activityStartTime?.setText("")
            binding?.activityStopDate?.setText("")
            binding?.activityStopTime?.setText("")
        }

        binding?.deleteActivity?.setOnClickListener {
            if(this.orchardActivity != null) {
                orchardViewModel.deleteOrchardActivity(this.orchardActivity!!)
            }
        }

        binding?.orchardActivityReport?.setOnClickListener {
            val action = OrchardTaskFragmentDirections.actionNavOrchardTaskToNavOrchardTaskReport()
            view?.findNavController()?.navigate(action)
        }

        binding?.activityStartDate?.onFocusChangeListener =
            View.OnFocusChangeListener { _, _ ->
                val date = binding?.activityStartDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                }
            }

        binding?.activityStartTime?.onFocusChangeListener =
            View.OnFocusChangeListener { _, _ ->
                val time = binding?.activityStartTime?.text.toString()
                if(!Validator.validateTime(time)) {
                    Toast.makeText(requireContext(), "Invalid time format 00:00", Toast.LENGTH_LONG).show()
                }
            }

        binding?.activityStopDate?.onFocusChangeListener =
            View.OnFocusChangeListener { _, _ ->
                val date = binding?.activityStopDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                }
            }

        binding?.activityStopTime?.onFocusChangeListener =
            View.OnFocusChangeListener { _, _ ->
                val time = binding?.activityStopTime?.text.toString()
                if(!Validator.validateTime(time)) {
                    Toast.makeText(requireContext(), "Invalid time format 00:00", Toast.LENGTH_LONG).show()
                }
            }

        binding?.showActivityStartDate?.setOnClickListener {
            DatePickerFragment(TASKSTARTDATEREQUESTKEY, TASKDATEKEY)
                .show(childFragmentManager, R.string.start_date.toString())
        }
        binding?.showActivityStopDate?.setOnClickListener {
            DatePickerFragment(TASKSTOPDATEREQUESTKEY, TASKDATEKEY)
                .show(childFragmentManager, R.string.stop_date.toString())
        }
        binding?.showActivityStartTimeClock?.setOnClickListener {
            TimePickerFragment(TASKSTARTTIMEREQUESTKEY, TASKTIMEKEY)
                .show(childFragmentManager, R.string.start_time.toString())
        }
        binding?.showActivityStopTimeClock?.setOnClickListener {
            TimePickerFragment(TASKSTOPTIMEREQUESTKEY, TASKTIMEKEY)
                .show(childFragmentManager, R.string.stop_time.toString())
        }
        return vw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(TASKSTARTDATEREQUESTKEY, requireActivity()) {
                _, bundle -> binding?.activityStartDate?.setText(bundle.getString(TASKDATEKEY))
        }
        childFragmentManager.setFragmentResultListener(TASKSTOPDATEREQUESTKEY, requireActivity()) {
                _, bundle -> binding?.activityStopDate?.setText(bundle.getString(TASKDATEKEY))
        }
        childFragmentManager.setFragmentResultListener(TASKSTARTTIMEREQUESTKEY, requireActivity()) {
                _, bundle -> binding?.activityStartTime?.setText(bundle.getString(TASKTIMEKEY))
        }
        childFragmentManager.setFragmentResultListener(TASKSTOPTIMEREQUESTKEY, requireActivity()) {
                _, bundle -> binding?.activityStopTime?.setText(bundle.getString(TASKTIMEKEY))
        }
    }

    inner class OrchardSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if((obj is String) && obj.isNotEmpty() && !farmOrchardsMap?.isEmpty()!!) {
                val key = this@OrchardTaskFragment.farmOrchardsMap?.filter { it.value == obj }?.keys?.first()
                this@OrchardTaskFragment.orchardId = key!!
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }

    inner class ActivitySelector: AdapterView.OnItemSelectedListener {
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
            binding?.orchardNotes?.setText(obj.notes)
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

    private fun buildLocalDateTime(isStart: Boolean): LocalDateTime? {
        var dateString: String

        if(isStart) {
            dateString = binding?.activityStartDate?.text.toString()
            dateString = dateString + " " + binding?.activityStartTime?.text.toString()
        } else {
            dateString = binding?.activityStopDate?.text.toString()
            dateString = dateString + " " + binding?.activityStopTime?.text.toString()
        }
        var date: LocalDateTime? = null
        try {
            date = DateConverter().toOffsetDateTime(dateString)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Please enter the proper date format", Toast.LENGTH_LONG).show()
            if(isStart) {
                binding?.activityStopDate?.requestFocus()
            } else {
                binding?.activityStopDate?.requestFocus()
            }
        }
        return date
    }

    override fun onClick(v: View?) {
        val activityStart = buildLocalDateTime(true)
        val activityStop = buildLocalDateTime(false)
        if(activityStart == null || activityStop == null) {
            return
        }
        if(orchardActivity != null && orchardActivity!!.id > 0) {
            val orchActivity = orchardActivity!!.copy(
                id = orchardActivity!!.id,
                orchardId = this.orchardId,
                activity = this.task!!,
                notes = binding?.orchardNotes?.text.toString(),
                activityStart = activityStart,
                activityStop = activityStop
            )
            try {
                orchardViewModel.updateOrchardActivity(orchActivity)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
            }
        } else {
            this.orchardActivity = OrchardActivity(
                activity = this.task!!,
                orchardId = this.orchardId,
                notes = binding?.orchardNotes?.text.toString(),
                activityStart = activityStart,
                activityStop = activityStop
            )
            orchardViewModel.addOrchardActivity(this.orchardActivity!!).observe(this) {
                id ->
                if(!id.equals(SAVE_FAILED)) {
                    Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.update_failed), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}