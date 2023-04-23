package com.orchardmanager.treedata.ui.irrigation

import android.os.Bundle
import android.util.Log
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
import com.orchardmanager.treedata.databinding.FragmentIrrigationBinding
import com.orchardmanager.treedata.databinding.FragmentIrrigationSystemBinding
import com.orchardmanager.treedata.entities.*
import com.orchardmanager.treedata.ui.SAVE_FAILED
import com.orchardmanager.treedata.ui.orchard.OrchardViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.LinkedHashMap


@AndroidEntryPoint
class IrrigationSystemFragment : Fragment(), View.OnClickListener,
    AdapterView.OnItemSelectedListener{

    private var _binding: FragmentIrrigationSystemBinding? = null
    private val binding get() = _binding
    private val irrigationViewModel: IrrigationViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()
    private val pumpViewModel: PumpViewModel by viewModels()
    private var irrigationSystem: IrrigationSystem? = null
    private var orchardId: Long = 0L
    private var pumpId: Long = 0L
    private var irrigationMethod: IrrigationMethod? = null
    private val irrigationMethods = arrayOf(IrrigationMethod.MICROSPRINKLER, IrrigationMethod.SPRINKLER,
        IrrigationMethod.DRIP, IrrigationMethod.FLOOD)
    private var emitterFlowUnit: FlowRateUnit? = null
    private val flowRateUnits = arrayOf(FlowRateUnit.GALLONSPERMINUTE, FlowRateUnit.GALLONSPERHOUR)
    private var emitterRadiusUnit: LinearUnit? = null
    private val linearUnits = arrayOf(LinearUnit.FEET,LinearUnit.INCHES,LinearUnit.METERS)
    private var emitterSpacingUnit: LinearUnit? = null
    private var farmOrchardsMap: kotlin.collections.Map<Long, String>? = null
    private var pumpMap: Map<Long, Pump>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIrrigationSystemBinding.inflate(inflater, container, false)
        val vw = binding?.root
        irrigationViewModel.getIrrigationSystem().observe(viewLifecycleOwner, Observer {
            irrigationSystems ->
            val adapter = ArrayAdapter<IrrigationSystem>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, irrigationSystems)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.irrigationSystems?.adapter = adapter
            binding?.irrigationSystems?.onItemSelectedListener = this
        })

        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner, Observer {
            farmWithOrchards ->
            farmOrchardsMap = farmWithOrchards
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.irrigationSystemOrchard?.adapter = adapter
            binding?.irrigationSystemOrchard?.onItemSelectedListener = orchardSelector()
        })

        pumpViewModel.getPumpsMap().observe(viewLifecycleOwner, Observer {
            pumps ->
            val adapter = ArrayAdapter<Pump>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, pumps.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.pumps?.adapter = adapter
            binding?.pumps?.onItemSelectedListener = pumpSelector()
        })

        val irrigationMethodAdapter = ArrayAdapter<IrrigationMethod>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, irrigationMethods)
        irrigationMethodAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.irrigationMethod?.adapter = irrigationMethodAdapter
        binding?.irrigationMethod?.onItemSelectedListener = irrigationMethodSelector()

        val emitterFlowRateAdapter = ArrayAdapter<FlowRateUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, flowRateUnits)
        emitterFlowRateAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.emitterFlowRateUnit?.adapter = emitterFlowRateAdapter
        binding?.emitterFlowRateUnit?.onItemSelectedListener = emitterFlowUnitSelector()

        val emitterRadiusUnitAdapter = ArrayAdapter<LinearUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, linearUnits)
        emitterRadiusUnitAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.emitterRadiusUnit?.adapter = emitterRadiusUnitAdapter
        binding?.emitterRadiusUnit?.onItemSelectedListener = emitterRadiusUnitSelector()

        val emitterSpacingUnitAdapter = ArrayAdapter<LinearUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, linearUnits)
        emitterSpacingUnitAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.emitterSpacingUnit?.adapter = emitterSpacingUnitAdapter
        binding?.emitterSpacingUnit?.onItemSelectedListener = emitterSpacingUnitSelector()

        binding?.saveIrrigationSystem?.setOnClickListener(this)

        binding?.newIrrigationSystem?.setOnClickListener(View.OnClickListener {
            irrigationSystem = null
            orchardId = 0L
            pumpId = 0L
            binding?.irrigationSystemName?.setText("")
            binding?.emitterFlowRate?.setText("")
            binding?.emitterRadius?.setText("")
            binding?.emitterSpacing?.setText("")
        })

        binding?.deleteIrrigationSystem?.setOnClickListener(View.OnClickListener {
            irrigationViewModel?.deleteIrrigationSystem(irrigationSystem!!)
        })
        newPump()
        return vw
    }

    inner class emitterSpacingUnitSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is LinearUnit) {
                this@IrrigationSystemFragment.emitterSpacingUnit = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class emitterRadiusUnitSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is LinearUnit) {
                this@IrrigationSystemFragment.emitterRadiusUnit = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class emitterFlowUnitSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if( obj is FlowRateUnit) {
                this@IrrigationSystemFragment.emitterFlowUnit = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class irrigationMethodSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is IrrigationMethod) {
                this@IrrigationSystemFragment.irrigationMethod = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class pumpSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is Pump) {
                this@IrrigationSystemFragment.pumpId = obj.id
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class orchardSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is String) {
                val key = this@IrrigationSystemFragment.farmOrchardsMap?.filter { it.value == obj }?.keys?.first()
                this@IrrigationSystemFragment.orchardId = key!!
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    companion object {
        fun newInstance() = IrrigationSystemFragment()
    }

    private fun newPump() {
        binding?.addPump?.setOnClickListener(View.OnClickListener {
            val action = IrrigationSystemFragmentDirections.actionNavIrrigationSystemToNavPump()
            view?.findNavController()?.navigate(action)
        })
    }

    override fun onClick(v: View?) {
        var emitterFlowRate: Double = 0.0
        val semitterFlowRate = binding?.emitterFlowRate?.text.toString()
        if(!semitterFlowRate.isEmpty()) {
            emitterFlowRate = semitterFlowRate.toDouble()
        }
        var emitterRadius: Double = 0.0
        val semitterRadius = binding?.emitterRadius?.text.toString()
        if(!semitterRadius.isEmpty()) {
            emitterRadius = semitterRadius.toDouble()
        }
        var emitterSpacing: Double = 0.0
        val semitterSpacing = binding?.emitterSpacing?.text.toString()
        if(!semitterSpacing.isEmpty()) {
            emitterSpacing = semitterRadius.toDouble()
        }
        if(irrigationSystem != null && irrigationSystem!!.id > 0L) {
            val is2 = irrigationSystem!!.copy(
                id = irrigationSystem!!.id,
                orchardId = orchardId,
                pumpId = pumpId,
                name = binding?.irrigationSystemName?.text.toString(),
                irrigationMethod = irrigationMethod!!,
                emitterFlowRate = emitterFlowRate,
                emitterFlowUnit = emitterFlowUnit!!,
                emitterRadius = emitterRadius,
                emitterRadiusLinearUnit = emitterRadiusUnit!!,
                emitterSpacing = emitterSpacing,
                emitterSpacingLinearUnit = emitterSpacingUnit!!
            )
            try {
                irrigationViewModel.updateIrrigationSystem(is2)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
            }
        } else {
            val irrigationSystem = IrrigationSystem(
                orchardId = orchardId,
                pumpId = pumpId,
                name = binding?.irrigationSystemName?.text.toString(),
                irrigationMethod = irrigationMethod!!,
                emitterFlowRate = emitterFlowRate,
                emitterFlowUnit = emitterFlowUnit!!,
                emitterRadius = emitterRadius,
                emitterRadiusLinearUnit = emitterRadiusUnit!!,
                emitterSpacing = emitterSpacing,
                emitterSpacingLinearUnit = emitterSpacingUnit!!
            )
            irrigationViewModel.addIrrigationSystem(irrigationSystem).observe(this, Observer {
                id ->
                Log.i("IrrigationSystemFragment", "the irrigaiton system id is " + id.toString())
                if(!id.equals(SAVE_FAILED)) {
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Save Failed", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is IrrigationSystem) {
            this.irrigationSystem = obj
            this.orchardId = obj.orchardId
            //Adjust the orchard spinner
            val sfo = farmOrchardsMap?.get(orchardId)
            val orchardPosition = farmOrchardsMap?.values!!.indexOf(sfo)
            //val orchard = farmOrchards?.filter { id == orchardId }
            binding?.irrigationSystemOrchard?.setSelection(orchardPosition)
            this.pumpId = obj.pumpId
            val pmp = pumpMap?.get(pumpId)
            val pumpPosition = pumpMap?.values!!.indexOf(pmp)
            binding?.pumps?.setSelection(pumpPosition)
            binding?.irrigationSystemName?.setText(obj.name)
            binding?.irrigationMethod?.setSelection(irrigationMethods.indexOf(obj.irrigationMethod))
            binding?.emitterFlowRate?.setText(obj.emitterFlowRate.toString())
            binding?.emitterFlowRateUnit?.setSelection(flowRateUnits.indexOf(obj.emitterFlowUnit))
            binding?.emitterRadius?.setText(obj.emitterRadius.toString())
            binding?.emitterRadiusUnit?.setSelection(linearUnits.indexOf(obj.emitterRadiusLinearUnit))
            binding?.emitterSpacing?.setText(obj.emitterSpacing.toString())
            binding?.emitterSpacingUnit?.setSelection(linearUnits.indexOf(obj.emitterSpacingLinearUnit))
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}