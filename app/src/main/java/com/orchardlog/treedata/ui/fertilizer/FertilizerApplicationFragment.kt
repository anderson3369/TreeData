package com.orchardlog.treedata.ui.fertilizer

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
import com.orchardlog.treedata.databinding.FragmentFertilizerApplicationBinding
import com.orchardlog.treedata.entities.Fertilizer
import com.orchardlog.treedata.entities.FertilizerApplication
import com.orchardlog.treedata.entities.OrchardUnit
import com.orchardlog.treedata.entities.WeightOrMeasureUnit
import com.orchardlog.treedata.ui.SAVE_FAILED
import com.orchardlog.treedata.ui.orchard.OrchardViewModel
import com.orchardlog.treedata.utils.DatePickerFragment
import com.orchardlog.treedata.utils.SortFertilizerApplications
import com.orchardlog.treedata.utils.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime

@AndroidEntryPoint
class FertilizerApplicationFragment : Fragment(),
    AdapterView.OnItemSelectedListener,
    View.OnClickListener {

    private val fertilizerViewModel: FertilizerViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()
    private var _binding: FragmentFertilizerApplicationBinding? = null
    private val binding get() = _binding
    private var fertilizerApplication: FertilizerApplication? = null
    private var orchardId: Long = 0L
    private var farmOrchardsMap: Map<Long, String>? = null
    private var fertilizerId: Long = 0L
    private var weightOrMeasureUnit: WeightOrMeasureUnit? = null
    private var orchardUnit: OrchardUnit? = null
    private var fertilizers: MutableList<Fertilizer>? = null
    private val wmUnitArray = arrayOf(WeightOrMeasureUnit.POUNDS, WeightOrMeasureUnit.TONS,WeightOrMeasureUnit.GALLONS,
        WeightOrMeasureUnit.QUARTS, WeightOrMeasureUnit.PINTS, WeightOrMeasureUnit.FLUIDOUNCES, WeightOrMeasureUnit.OUNCES,
        WeightOrMeasureUnit.GRAMS
    )
    private val areaUnitArray = arrayOf(OrchardUnit.ACRE, OrchardUnit.HECTARE)

    companion object {
        const val fertilizerStartDateRequestKey = "requestFertilizerStartDateKey"
        const val fertilizerStopDateRequestKey = "requestFertilizerStopDateKey"
        const val fertilizerDateKey = "fertilizerDate"
        const val fertilizerStartTimeRequestKey = "requestFertilizerStartTimeKey"
        const val fertilizerStopTimeRequestKey = "requestFertilizerStopTimeKey"
        const val fertilizerTimeKey = "fertilizerTime"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFertilizerApplicationBinding.inflate(inflater, container, false)
        val vw = binding?.root

        fertilizerViewModel.getFertilizerApplications().observe(viewLifecycleOwner) {
            fertilizerApplications ->
            val adapter = ArrayAdapter<FertilizerApplication>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, fertilizerApplications)
            adapter.sort(SortFertilizerApplications())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.fertilizerApplications?.adapter = adapter
            binding?.fertilizerApplications?.onItemSelectedListener = this
        }

        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner) {
            farmWithOrchards ->
            this.farmOrchardsMap = farmWithOrchards
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.fertilizerOrchard?.adapter = adapter
            binding?.fertilizerOrchard?.onItemSelectedListener = orchardSelector()
        }

        val wmUnitAdapter = ArrayAdapter<WeightOrMeasureUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, wmUnitArray)
        wmUnitAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.fertilizerAppliedUnit?.adapter = wmUnitAdapter
        binding?.fertilizerAppliedUnit?.onItemSelectedListener = wmUnitSelector()

        val orchardUnitAdapter = ArrayAdapter<OrchardUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, areaUnitArray)
        orchardUnitAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.fertilizerAreaTreatedUnit?.adapter = orchardUnitAdapter
        binding?.fertilizerAreaTreatedUnit?.onItemSelectedListener = orchardUnitSelector()

        fertilizerViewModel.getFertilizers().observe(viewLifecycleOwner) {
            fertilizers ->
            this.fertilizers = fertilizers
            val adapter = ArrayAdapter<Fertilizer>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, fertilizers)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.fertilizers?.adapter = adapter
            binding?.fertilizers?.onItemSelectedListener = fertilizerSelector()
        }

        binding?.fertilizerReport?.setOnClickListener {
            val action = FertilizerApplicationFragmentDirections.actionNavFertilizerApplicationToNavFertilizerReport()
            view?.findNavController()?.navigate(action)
        }

        binding?.addFertilizer?.setOnClickListener {
            val action = FertilizerApplicationFragmentDirections.actionNavFertilizerApplicationToNavFertilizer()
            view?.findNavController()?.navigate(action)
        }

        binding?.fertilizerApplicationStartDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.fertilizerApplicationStartDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), getString(R.string.invalid_date_format_mm_dd_yyyy),
                        Toast.LENGTH_SHORT).show()
                    binding?.fertilizerApplicationStartDate?.requestFocus()
                }
            }
        })

        binding?.fertilizerApplicationStartTime?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val time = binding?.fertilizerApplicationStartTime?.text.toString()
                if(!Validator.validateTime(time)) {
                    Toast.makeText(requireContext(), getString(R.string.invalid_time_format_00_00),
                        Toast.LENGTH_SHORT).show()
                    binding?.fertilizerApplicationStartTime?.requestFocus()
                }
            }
        })

        binding?.fertilizerApplicationStopDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.fertilizerApplicationStopDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), getString(R.string.invalid_date_format_mm_dd_yyyy),
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding?.fertilizerApplicationStopTime?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val time = binding?.fertilizerApplicationStopTime?.text.toString()
                if(!Validator.validateTime(time)) {
                    Toast.makeText(requireContext(), getString(R.string.invalid_time_format_00_00),
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding?.saveFertilizerApplication?.setOnClickListener(this)

        binding?.newFertilizerApplication?.setOnClickListener {
            this.fertilizerApplication = null
            binding?.fertilizerApplicationStartDate?.setText("")
            binding?.fertilizerApplicationStartTime?.setText("")
            binding?.fertilizerApplicationStopDate?.setText("")
            binding?.fertilizerApplicationStopTime?.setText("")
            binding?.applied?.setText("")
            binding?.fertilizerAreaTreated?.setText("")
        }

        binding?.deleteFertilzerApplication?.setOnClickListener {
            if(this.fertilizerApplication != null) {
                fertilizerViewModel.deleteFertilizerApplication(fertilizerApplication!!)
            }
        }

        binding?.showFertilizerApplicationStartDate?.setOnClickListener {
            DatePickerFragment(fertilizerStartDateRequestKey, fertilizerDateKey)
                .show(childFragmentManager, "Start Date")
        }

        binding?.showFertilizerApplicationStopDate?.setOnClickListener {
            DatePickerFragment(fertilizerStopDateRequestKey, fertilizerDateKey)
                .show(childFragmentManager, "Stop Date")
        }

        binding?.fertilizerApplicationStartTimeClock?.setOnClickListener {
            TimePickerFragment(fertilizerStartTimeRequestKey, fertilizerTimeKey)
                .show(childFragmentManager, "Start Time")
        }

        binding?.fertilizerApplicationStopTimeClock?.setOnClickListener {
            TimePickerFragment(fertilizerStopTimeRequestKey, fertilizerTimeKey)
                .show(childFragmentManager, "Stop Time")
        }

        return vw!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(fertilizerStartDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.fertilizerApplicationStartDate?.setText(bundle.getString(fertilizerDateKey))
        }
        childFragmentManager.setFragmentResultListener(fertilizerStopDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.fertilizerApplicationStopDate?.setText(bundle.getString(fertilizerDateKey))
        }
        childFragmentManager.setFragmentResultListener(fertilizerStartTimeRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.fertilizerApplicationStartTime?.setText(bundle.getString(fertilizerTimeKey))
        }
        childFragmentManager.setFragmentResultListener(fertilizerStopTimeRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.fertilizerApplicationStopTime?.setText(bundle.getString(fertilizerTimeKey))
        }
    }

    inner class orchardUnitSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is OrchardUnit) {
                this@FertilizerApplicationFragment.orchardUnit = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }

    inner class wmUnitSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is WeightOrMeasureUnit) {
                this@FertilizerApplicationFragment.weightOrMeasureUnit = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class orchardSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is String && !obj.isEmpty() && !farmOrchardsMap?.isEmpty()!!) {
                this@FertilizerApplicationFragment.orchardId = farmOrchardsMap?.filter { it.value == obj }?.keys!!.first()
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }

    inner class fertilizerSelector: AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if (obj is Fertilizer) {
                this@FertilizerApplicationFragment.fertilizerId = obj.id
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is FertilizerApplication) {
            this.fertilizerApplication = obj
            if(farmOrchardsMap != null && !farmOrchardsMap!!.isEmpty()) {
                binding?.fertilizerOrchard?.setSelection(farmOrchardsMap?.values!!.indexOf(farmOrchardsMap?.get(obj.orchardId)))
            }
            if(fertilizers != null && !fertilizers!!.isEmpty()) {
                val fert: Fertilizer = this.fertilizers?.filter { it.id == obj.fertilizerId }!!.first()
                val index: Int = this.fertilizers?.indexOf(fert)!!
                binding?.fertilizers?.setSelection(index)
            }
            parseLocalDateTime(true, obj.applicationStart)
            parseLocalDateTime(false, obj.applicationStop)
            binding?.applied?.setText(obj.applied.toString())
            binding?.fertilizerAppliedUnit?.setSelection(wmUnitArray.indexOf(obj.weightOrMeasureUnit))
            binding?.fertilizerAreaTreated?.setText(obj.areaTreated.toString())
            binding?.fertilizerAreaTreatedUnit?.setSelection(areaUnitArray.indexOf(obj.orchardUnit))
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun parseLocalDateTime(isStart: Boolean, dateTime: LocalDateTime) {
        if(isStart) {
            val date = dateTime.toLocalDate()
            binding?.fertilizerApplicationStartDate?.setText(DateConverter().fromOffsetDate(date))
            val time = dateTime.toLocalTime()
            binding?.fertilizerApplicationStartTime?.setText(DateConverter().fromTime(time))
        } else {
            val date = dateTime.toLocalDate()
            binding?.fertilizerApplicationStopDate?.setText(DateConverter().fromOffsetDate(date))
            val time = dateTime.toLocalTime()
            binding?.fertilizerApplicationStopTime?.setText(DateConverter().fromTime(time))
        }
    }

    private fun buildLocalDateTime(isStart: Boolean): LocalDateTime? {
        var dateString: String
        if(isStart) {
            dateString = binding?.fertilizerApplicationStartDate?.text.toString()
            dateString = dateString + " " + binding?.fertilizerApplicationStartTime?.text.toString()
        } else {
            dateString = binding?.fertilizerApplicationStopDate?.text.toString()
            dateString = dateString + " " + binding?.fertilizerApplicationStopTime?.text.toString()
        }
        var date: LocalDateTime? = null
        try {
            date = DateConverter().toOffsetDateTime(dateString)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Enter a proper date format", Toast.LENGTH_LONG).show()
            if(isStart) {
                binding?.fertilizerApplicationStartDate?.requestFocus()
            } else {
                binding?.fertilizerApplicationStopDate?.requestFocus()
            }
        }
        return date
    }

    override fun onClick(v: View?) {
        if(this.orchardId == 0L || this.fertilizerId == 0L) {
            Toast.makeText(requireContext(), getString(R.string.please_select_an_orchard_or_fertilizer),
                Toast.LENGTH_LONG).show()
            return
        }
        var applied = 0.0
        val sapplied = binding?.applied?.text.toString()
        if(!sapplied.isEmpty()) {
            applied = sapplied.toDouble()
        }
        var areaTreated = 0.0
        val sareaTreated = binding?.fertilizerAreaTreated?.text.toString()
        if(!sareaTreated.isEmpty()) {
            areaTreated = sareaTreated.toDouble()
        }
        val applicationStart = buildLocalDateTime(true)
        val applicationStop = buildLocalDateTime(false)
        if(applicationStart == null || applicationStop == null ) {
            return
        }
        if(fertilizerApplication != null && fertilizerApplication!!.id > 0L) {
            val fa = fertilizerApplication!!.copy(
                id = fertilizerApplication!!.id,
                orchardId = orchardId,
                fertilizerId = fertilizerId,
                applicationStart = applicationStart,
                applicationStop = applicationStop,
                applied = applied,
                weightOrMeasureUnit = weightOrMeasureUnit!!,
                areaTreated = areaTreated,
                orchardUnit = orchardUnit!!
            )
            try {
                fertilizerViewModel.updateFertilizerApplication(fa)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
            }

        } else {
            this.fertilizerApplication = FertilizerApplication(
                orchardId = orchardId,
                fertilizerId = fertilizerId,
                applicationStart = applicationStart,
                applicationStop = applicationStop,
                applied = applied,
                weightOrMeasureUnit = weightOrMeasureUnit!!,
                areaTreated = areaTreated,
                orchardUnit = orchardUnit!!
            )
            fertilizerViewModel.addFertilizerApplication(fertilizerApplication!!).observe(this) {
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