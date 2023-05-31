package com.orchardmanager.treedata.ui.pesticide

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.data.Validator
import com.orchardmanager.treedata.databinding.FragmentPesticideApplicationBinding
import com.orchardmanager.treedata.entities.*
import com.orchardmanager.treedata.ui.SAVE_FAILED
import com.orchardmanager.treedata.ui.orchard.OrchardViewModel
import com.orchardmanager.treedata.utils.DatePickerFragment
import com.orchardmanager.treedata.utils.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime

@AndroidEntryPoint
class PesticideApplicationFragment : Fragment(), AdapterView.OnItemSelectedListener,
    View.OnClickListener {

    private val pesticideViewModel: PesticideViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()
    private var _binding: FragmentPesticideApplicationBinding? = null
    private val binding get() = _binding
    private var pesticideApplication: PesticideApplication? = null
    private var farmOrchardsMap: Map<Long, String>? = null
    private var orchardId: Long = 0L
    private var pesticideId: Long = 0L
    private var appliedWMUnit: WeightOrMeasureUnit? = null
    private var dilutionWMUnit: WeightOrMeasureUnit? = null
    private var orchardUnit: OrchardUnit? = null
    private var applicationMethod: ApplicationMethod? = null
    private var pesticides: MutableList<Pesticide>? = null
    private val areaUnitArray = arrayOf(OrchardUnit.ACRE, OrchardUnit.HECTARE)
    private val wmUnitArray = arrayOf(
        WeightOrMeasureUnit.POUNDS, WeightOrMeasureUnit.GALLONS, WeightOrMeasureUnit.QUARTS,
        WeightOrMeasureUnit.PINTS, WeightOrMeasureUnit.FLUIDOUNCES, WeightOrMeasureUnit.OUNCES, WeightOrMeasureUnit.GRAMS)
    private val applicationMethodArray = arrayOf(ApplicationMethod.AIRBLAST, ApplicationMethod.AIR, ApplicationMethod.GROUNDBOOM,
        ApplicationMethod.CHEMIGATION, ApplicationMethod.FOGGER, ApplicationMethod.HAND)
    //private val areaUnitArray = arrayOf(OrchardUnit.ACRE, OrchardUnit.HECTARE)
    private val pesticideStartDateRequestKey = "requestPesticideStartDateKey"
    private val pesticideStopDateRequestKey = "requestPesticideStopDateKey"
    private val pesticideDateKey = "pesticideDate"
    private val pesticideStartTimeRequestKey = "requestPesticideStartTimeKey"
    private val pesticideStopTimeRequestKey = "requestPesticideStopTimeKey"
    private val pesticideTimeKey = "pesticideTime"

    companion object {
        fun newInstance() = PesticideApplicationFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPesticideApplicationBinding.inflate(inflater, container, false)
        val vw = binding?.root

        pesticideViewModel.getPesticideApplications().observe(viewLifecycleOwner, Observer {
            applications ->
            val adapter = ArrayAdapter<PesticideApplication>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, applications)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.pesticideApplications?.adapter = adapter
            binding?.pesticideApplications?.onItemSelectedListener = this
        })

        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner, Observer {
                farmWithOrchards ->
            this.farmOrchardsMap = farmWithOrchards
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.pesticideOrchard?.adapter = adapter
            binding?.pesticideOrchard?.onItemSelectedListener = orchardSelector()
        })

        pesticideViewModel.getPesticides().observe(viewLifecycleOwner, Observer {
            pesticides ->
            this.pesticides = pesticides
            val adapater = ArrayAdapter<Pesticide>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, pesticides)
            adapater.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.pesticidesSpinner?.adapter = adapater
            binding?.pesticidesSpinner?.onItemSelectedListener = pesticideSelector()
        })

        val appliedAdapter = ArrayAdapter<WeightOrMeasureUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, wmUnitArray)
        appliedAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.pesticideAppliedUnit?.adapter = appliedAdapter
        binding?.pesticideAppliedUnit?.onItemSelectedListener = appliedUnitSelector()

        val dilutionAdapter = ArrayAdapter<WeightOrMeasureUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, wmUnitArray)
        dilutionAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.dilutionUnit?.adapter = dilutionAdapter
        binding?.dilutionUnit?.onItemSelectedListener = dilutionUnitSelector()

        val orchardUnitAdapter = ArrayAdapter<OrchardUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, areaUnitArray)
        orchardUnitAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.treatedAreaUnit?.adapter = orchardUnitAdapter
        binding?.treatedAreaUnit?.onItemSelectedListener = orchardUnitSelector()

        val appMethodAdapter = ArrayAdapter<ApplicationMethod>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, applicationMethodArray)
        appMethodAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.applicationMethod?.adapter = appMethodAdapter
        binding?.applicationMethod?.onItemSelectedListener = applicationMethodSelector()

        binding?.addPesticide?.setOnClickListener(View.OnClickListener {
            val action = PesticideApplicationFragmentDirections.actionNavPesticideApplicationToNavPesticide()
            view?.findNavController()?.navigate(action)
        })

        binding?.savePesticideApplication?.setOnClickListener(this)

        binding?.pesticideApplicationStartDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.pesticideApplicationStartDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding?.pesticideApplicationStartTime?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val time = binding?.pesticideApplicationStartTime?.text.toString()
                if(!Validator.validateTime(time)) {
                    Toast.makeText(requireContext(), "Invalid time format 00:00", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding?.pesticideApplicationStopDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.pesticideApplicationStopDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding?.pesticideApplicationStopTime?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val time = binding?.pesticideApplicationStopTime?.text.toString()
                if(!Validator.validateTime(time)) {
                    Toast.makeText(requireContext(), "Invalid time format 00:00", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding?.newPesticideApplication?.setOnClickListener(View.OnClickListener {
            this.pesticideApplication = null
            binding?.pesticideApplicationStartDate?.setText("")
            binding?.pesticideApplicationStartTime?.setText("")
            binding?.pesticideApplicationStopDate?.setText("")
            binding?.pesticideApplicationStopTime?.setText("")
            binding?.pesticideApplied?.setText("")
            binding?.dilution?.setText("")
            binding?.areaTreated?.setText("")
        })

        binding?.deletePesticideApplication?.setOnClickListener(View.OnClickListener {
            if(this.pesticideApplication != null) {
                pesticideViewModel?.deletePesticideApplication(this.pesticideApplication!!)
            }
        })

        binding?.showPesticideApplicationStartDate?.setOnClickListener(View.OnClickListener {
            DatePickerFragment(pesticideStartDateRequestKey, pesticideDateKey).show(childFragmentManager, "Start Date")
        })

        binding?.showPesticideApplicationStopDate?.setOnClickListener(View.OnClickListener {
            DatePickerFragment(pesticideStopDateRequestKey, pesticideDateKey).show(childFragmentManager, "Stop Date")
        })

        binding?.pesticideApplicationStartTimeClock?.setOnClickListener(View.OnClickListener {
            TimePickerFragment(pesticideStartTimeRequestKey, pesticideTimeKey).show(childFragmentManager, "Start Time")
        })

        binding?.pesticideApplicationStopTimeClock?.setOnClickListener(View.OnClickListener {
            TimePickerFragment(pesticideStopTimeRequestKey, pesticideTimeKey).show(childFragmentManager, "Stop Time")
        })

        return vw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(pesticideStartDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.pesticideApplicationStartDate?.setText(bundle.getString(pesticideDateKey))
        }
        childFragmentManager.setFragmentResultListener(pesticideStopDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.pesticideApplicationStopDate?.setText(bundle.getString(pesticideDateKey))
        }
        childFragmentManager.setFragmentResultListener(pesticideStartTimeRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.pesticideApplicationStartTime?.setText(bundle.getString(pesticideTimeKey))
        }
        childFragmentManager.setFragmentResultListener(pesticideStopTimeRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.pesticideApplicationStopTime?.setText(bundle.getString(pesticideTimeKey))
        }
    }

    inner class pesticideSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is Pesticide) {
                this@PesticideApplicationFragment.pesticideId = obj.id
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class applicationMethodSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is ApplicationMethod) {
                applicationMethod = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class orchardUnitSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is OrchardUnit) {
                this@PesticideApplicationFragment.orchardUnit = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class dilutionUnitSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is WeightOrMeasureUnit) {
                this@PesticideApplicationFragment.dilutionWMUnit = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class appliedUnitSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is WeightOrMeasureUnit) {
                this@PesticideApplicationFragment.appliedWMUnit = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
        ;
    }

    inner class orchardSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is String && !obj.isEmpty()  && !farmOrchardsMap?.isEmpty()!!) {
                this@PesticideApplicationFragment.orchardId = farmOrchardsMap?.filter { it.value == obj }?.keys!!.first()
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }


    private fun parseLocalDateTime(isStart: Boolean, dateTime: LocalDateTime) {
        if(isStart) {
            val date = dateTime.toLocalDate()
            binding?.pesticideApplicationStartDate?.setText(DateConverter().fromOffsetDate(date))
            val time = dateTime.toLocalTime()
            binding?.pesticideApplicationStartTime?.setText(DateConverter().fromTime(time))
        } else {
            val date = dateTime.toLocalDate()
            binding?.pesticideApplicationStopDate?.setText(DateConverter().fromOffsetDate(date))
            val time = dateTime.toLocalTime()
            binding?.pesticideApplicationStopTime?.setText(DateConverter().fromTime(time))
        }
    }

    private fun buildLocalDateTime(isStart: Boolean): LocalDateTime {
        var dateString = ""
        if(isStart) {
            dateString = binding?.pesticideApplicationStartDate?.text.toString()
            dateString = dateString + " " + binding?.pesticideApplicationStartTime?.text.toString()
        } else {
            dateString = binding?.pesticideApplicationStopDate?.text.toString()
            dateString = dateString + " " + binding?.pesticideApplicationStopTime?.text.toString()
        }
        return DateConverter().toOffsetDateTime(dateString)!!
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is PesticideApplication) {
            this.pesticideApplication = obj
            binding?.pesticideOrchard?.setSelection(farmOrchardsMap?.values!!.indexOf(farmOrchardsMap?.get(obj.orchardId)))
            val pestIndex = this.pesticides?.indexOf(this.pesticides?.filter { it.id == obj.pesticideId }!!.first())
            binding?.pesticidesSpinner?.setSelection(pestIndex!!)
            parseLocalDateTime(true, obj.applicationStart)
            parseLocalDateTime(false, obj.applicationStop)
            binding?.pesticideApplied?.setText(obj.applied.toString())
            binding?.pesticideAppliedUnit?.setSelection(wmUnitArray.indexOf(obj.appliedUnit))
            binding?.dilution?.setText(obj.dilution.toString())
            binding?.dilutionUnit?.setSelection(wmUnitArray.indexOf(obj.dilutionUnit))
            binding?.areaTreated?.setText(obj.areaTreated.toString())
            binding?.treatedAreaUnit?.setSelection(areaUnitArray.indexOf(obj.areaTreatedUnit))
            binding?.applicationMethod?.setSelection(applicationMethodArray.indexOf(obj.applicationMethod))
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onClick(v: View?) {
        if(this.orchardId == 0L || this.pesticideId == 0L) {
            Toast.makeText(requireContext(), "Please select an Orchard or Pesticide", Toast.LENGTH_LONG).show()
            return
        }
        val sapplied = binding?.pesticideApplied?.text.toString()
        var applied = 0.0
        if(!sapplied.isEmpty()) {
            applied = sapplied.toDouble()
        }
        val sdilution = binding?.dilution?.text.toString()
        var dilution = 0
        if(!sdilution.isEmpty()) {
            dilution = sdilution.toInt()
        }
        val sareaTreated = binding?.areaTreated?.text.toString()
        var areaTreated = 0.0
        if(!sareaTreated.isEmpty()) {
            areaTreated = sareaTreated.toDouble()
        }
        if(this.pesticideApplication != null && pesticideApplication!!.id > 0) {
            val pa = this.pesticideApplication!!.copy(
                id = pesticideApplication!!.id,
                orchardId = this.orchardId,
                pesticideId = this.pesticideId,
                applicationStart = buildLocalDateTime(true),
                applicationStop = buildLocalDateTime(false),
                applied = applied,
                appliedUnit = this.appliedWMUnit!!,
                dilution = dilution,
                dilutionUnit = this.dilutionWMUnit!!,
                areaTreated = areaTreated,
                areaTreatedUnit = this.orchardUnit!!,
                applicationMethod = this.applicationMethod!!
            )
            try {
                pesticideViewModel.updatePesticideApplication(pa)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
            }
        } else {
            this.pesticideApplication = PesticideApplication(
                orchardId = this.orchardId,
                pesticideId = this.pesticideId,
                applicationStart = buildLocalDateTime(true),
                applicationStop = buildLocalDateTime(false),
                applied = applied,
                appliedUnit = this.appliedWMUnit!!,
                dilution = dilution,
                dilutionUnit = this.dilutionWMUnit!!,
                areaTreated = areaTreated,
                areaTreatedUnit = this.orchardUnit!!,
                applicationMethod = this.applicationMethod!!
            )
            pesticideViewModel.addPesticideApplication(pesticideApplication!!).observe(viewLifecycleOwner, Observer {
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