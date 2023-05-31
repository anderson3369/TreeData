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
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.databinding.FragmentPumpBinding
import com.orchardmanager.treedata.entities.FlowRateUnit
import com.orchardmanager.treedata.entities.Pump
import com.orchardmanager.treedata.ui.SAVE_FAILED
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PumpFragment : Fragment(),
    AdapterView.OnItemSelectedListener,
    View.OnClickListener{

    private var _bining: FragmentPumpBinding? = null
    private val binding get() = _bining
    //private var pumpId: Long = 0L
    private var pump: Pump? = null
    private var flowRateUnit:FlowRateUnit? = null
    private val pumpViewModel: PumpViewModel by viewModels()
    private val flowRateUnits = arrayOf(FlowRateUnit.GALLONSPERMINUTE, FlowRateUnit.GALLONSPERHOUR)

    companion object {
        fun newInstance() = PumpFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bining = FragmentPumpBinding.inflate(inflater, container, false)
        val vw = binding?.root
        pumpViewModel.getPumps().observe(viewLifecycleOwner, Observer {
            pumps ->
            val adapter = ArrayAdapter<Pump>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, pumps)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.pumps?.adapter = adapter
        })
        binding?.pumps?.onItemSelectedListener = this
        val flowRateAdapter = ArrayAdapter<FlowRateUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, flowRateUnits)
        flowRateAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.pumpFlowRateUnit?.adapter = flowRateAdapter
        binding?.pumpFlowRateUnit?.onItemSelectedListener = flowRateUnitSelector()

        //binding?.horsepower?.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        binding?.savePump?.setOnClickListener(this)

        binding?.newPump?.setOnClickListener(View.OnClickListener {
            this.pump = null
            binding?.pumpType?.setText("")
            binding?.horsepower?.setText("")
            binding?.phase?.setText("")
            binding?.pumpFlowRate?.setText("")
        })

        binding?.deletePump?.setOnClickListener(View.OnClickListener {
            pumpViewModel.deletePump(pump!!)
        })
        return vw
    }

    inner class flowRateUnitSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is FlowRateUnit) {
                this@PumpFragment.flowRateUnit = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is Pump) {
            this.pump = obj
            binding?.pumpType?.setText(pump!!.type)
            binding?.horsepower?.setText(pump!!.horsepower.toString())
            binding?.phase?.setText(pump!!.phase.toString())
            binding?.pumpFlowRate?.setText(pump!!.flowRate.toString())
            binding?.pumpFlowRateUnit?.setSelection(flowRateUnits.indexOf(pump!!.flowRateUnit))
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onClick(v: View?) {
        var hp: Double = 0.0
        val shp = binding?.horsepower?.text.toString()
        if(!shp.isEmpty()) {
            hp = shp.toDouble()
        }
        var phase: Int = 0
        var sphase = binding?.phase?.text.toString()
        if(!sphase.isEmpty()) {
            phase = sphase.toInt()
        }
        var flowRate: Double = 0.0
        var sflowRate = binding?.pumpFlowRate?.text.toString()
        if(!sflowRate.isEmpty()) {
            flowRate = sflowRate.toDouble()
        }
        if(pump != null && pump!!.id > 0L) {
            val pmp = pump?.copy(
                id = this.pump!!.id,
                type = binding?.pumpType?.text.toString(),
                horsepower = hp,
                phase = phase,
                flowRate = flowRate,
                flowRateUnit = this.flowRateUnit!!
            )
            try {
                pumpViewModel.updatePump(pmp!!)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
            }

        } else {
            pump = Pump(
                type = binding?.pumpType?.text.toString(),
                horsepower = hp,
                phase = phase,
                flowRate = flowRate,
                flowRateUnit = this.flowRateUnit!!
            )
            pumpViewModel.addPump(pump!!).observe(this, Observer {
                id ->
                Log.i("PumpFragment", "the pump ID is " + id.toString())
                if(!id.equals(SAVE_FAILED)) {
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

}