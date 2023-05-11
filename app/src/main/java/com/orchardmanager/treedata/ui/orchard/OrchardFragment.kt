package com.orchardmanager.treedata.ui.orchard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.databinding.FragmentOrchardBinding
import com.orchardmanager.treedata.entities.Farm
import com.orchardmanager.treedata.entities.LinearUnit
import com.orchardmanager.treedata.entities.Orchard
import com.orchardmanager.treedata.utils.DatePickerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrchardFragment : Fragment(), View.OnClickListener,
    OnItemSelectedListener {

    private var _binding: FragmentOrchardBinding? = null
    private val binding get() = _binding
    private var farmId: Long = -1L
    private var farms: MutableList<Farm>? = null
    private var orchard: Orchard? = null
    private var rowWidthUnit: LinearUnit? = null
    private var distanceBetweenUnit: LinearUnit? = null
    private val linearUnits = arrayOf(LinearUnit.FEET,LinearUnit.METERS, LinearUnit.INCHES)
    private var orchardList: MutableList<Orchard>? = null
    private val platedDateRequestKey = "plantedDateRequestKey"
    private val plantedDateKey = "plantedDateKey"
    val farmOrchardMap = hashMapOf<Long, MutableList<Orchard>>()

    companion object {
        fun newInstance() = OrchardFragment()
    }

    private val orchardViewModel: OrchardViewModel by viewModels()
    //private val farmViewModel: FarmViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrchardBinding.inflate(inflater, container, false)
        val vw = binding?.root

        orchardViewModel.getFarmWithOrchards().observe(viewLifecycleOwner, Observer {
            farmWithOrchards ->
            val farmWithOrchardsIterator = farmWithOrchards.iterator()
            this.farms = mutableListOf<Farm>()

            while (farmWithOrchardsIterator.hasNext()) {
                val farmWithOrchard = farmWithOrchardsIterator.next()
                farms!!.add(farmWithOrchard.farm)
                farmOrchardMap[farmWithOrchard.farm.id] = farmWithOrchard.orchards
            }
            //Set a default value
            this.farmId = farmWithOrchards.get(0).farm.id
            this.orchardList = farmWithOrchards.get(0).orchards

            val farmAdapter = ArrayAdapter<Farm>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farms!!
            )
            farmAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.farms?.adapter = farmAdapter
            binding?.farms?.onItemSelectedListener = this
            binding?.farms?.setSelection(0)
            this.farmId = farmWithOrchards.get(0).farm.id

            val orchardAdapter = ArrayAdapter<Orchard>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, orchardList!!
            )
            orchardAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.orchardSpinner?.adapter = orchardAdapter
            binding?.orchardSpinner?.onItemSelectedListener = this
        })

        binding?.showPlantedDate?.setOnClickListener(View.OnClickListener {
            DatePickerFragment(platedDateRequestKey, plantedDateKey).show(childFragmentManager, "Planted Date")
        })
        binding?.saveOrchard?.setOnClickListener(this)

        rowWidthUnit()
        distanceBetweenUnit()

        binding?.newOrchard?.setOnClickListener(View.OnClickListener {
            this.orchard = null
            binding?.crop?.setText("")
            binding?.plantedDate?.setText("")
        })

        binding?.deleteOrchard?.setOnClickListener(View.OnClickListener {
            if(orchard != null) {
                orchardViewModel.delete(orchard!!)
            }
        })

        return vw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener("requestKey", requireActivity()) {
            key, bundle -> this.farmId = bundle.getLong("farmId")
        }
        childFragmentManager.setFragmentResultListener(platedDateRequestKey, requireActivity()) {
            dateKey, bundle -> binding?.plantedDate?.setText(bundle.getString(plantedDateKey))
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(OrchardViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun linearUnit(): ArrayAdapter<LinearUnit> {
        val adapter = ArrayAdapter<LinearUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, linearUnits)
        adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        return adapter
    }

    private fun rowWidthUnit() {
        val adapter = linearUnit()
        binding?.rowWidthUnit?.adapter = adapter
        binding?.rowWidthUnit?.onItemSelectedListener = rowWidthSelector()
    }

    private fun distanceBetweenUnit() {
        val adapter = linearUnit()
        binding?.distanceBetweenUnit?.adapter = adapter
        binding?.distanceBetweenUnit?.onItemSelectedListener = distanceBetweenSelector()
    }

    inner class rowWidthSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val unit = parent?.adapter?.getItem(position)
            if(unit is LinearUnit) {
                rowWidthUnit = unit
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }

    inner class distanceBetweenSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val unit = parent?.adapter?.getItem(position)
            if(unit is LinearUnit) {
                this@OrchardFragment.distanceBetweenUnit = unit
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    private fun saveOrchard() {
        val ssand = binding?.sand?.text.toString()
        var sand:Double = 0.0
        if(!ssand.equals("")) {
            sand = ssand.toDouble()
        }
        val ssilt = binding?.silt?.text.toString()
        var silt:Double = 0.0
        if(!ssilt.equals("")) {
            silt = ssilt.toDouble()
        }
        val sclay = binding?.clay?.text.toString()
        var clay:Double = 0.0
        if(!sclay.equals("")) {
            clay = sclay.toDouble()
        }
        val som = binding?.organicMatter?.text.toString()
        var om:Double = 0.0
        if(!som.equals("")) {
            om = som.toDouble()
        }
        if(orchard != null && (orchard?.id != null && orchard?.id!! > 0L)) {
            val orchard2 = DateConverter().toOffsetDate(
                binding?.plantedDate?.text.toString())?.let {
                orchard?.copy(
                    farmId = this.farmId,
                    crop = binding?.crop?.text.toString(),
                    plantedDate = it,
                    rowWidth = binding?.rowWidth?.text.toString().toDouble(),
                    rowWidthLinearUnit = rowWidthUnit!!,
                    distanceBetweenTrees = binding?.distanceBetweenTrees?.text.toString().toDouble(),
                    distanceBetweenTreesLinearUnit = distanceBetweenUnit!!,
                    sand = sand,
                    silt = silt,
                    clay = clay,
                    organicMatter = om
                )
            }
            orchardViewModel.update(orchard2!!)
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
        } else {
            orchard = DateConverter().toOffsetDate(binding?.plantedDate?.text.toString())?.let {
                Orchard(
                    farmId = this.farmId,
                    crop = binding?.crop?.text.toString(),
                    plantedDate = it,
                    rowWidth = binding?.rowWidth?.text.toString().toDouble(),
                    rowWidthLinearUnit = rowWidthUnit!!,
                    distanceBetweenTrees = binding?.distanceBetweenTrees?.text.toString().toDouble(),
                    distanceBetweenTreesLinearUnit = distanceBetweenUnit!!,
                    sand = sand,
                    silt = silt,
                    clay = clay,
                    organicMatter = om
                )
            }
            orchardViewModel.add(orchard!!).observe(this, androidx.lifecycle.Observer {
                    id -> Log.i("OrchardFragment", "the orchard id is..." + id.toString())
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            })

        }
    }

    override fun onClick(p0: View?) {
        saveOrchard()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val obj = p0?.adapter?.getItem(p2)
        //val obj = p0?.getItemAtPosition(p2)
        if(obj != null) {
            if(obj is Farm) {
                this.farmId = obj.id
                orchardList = farmOrchardMap.get(obj.id)
            } else if(obj is Orchard) {
                this.orchard = obj
                binding?.crop?.setText(orchard?.crop)
                val sDate = DateConverter().fromOffsetDate(orchard?.plantedDate!!)
                binding?.plantedDate?.setText(sDate)
                this.farmId = orchard?.farmId!!
                binding?.rowWidth?.setText(orchard!!.rowWidth.toString())
                binding?.rowWidthUnit?.setSelection(linearUnits.indexOf(orchard!!.rowWidthLinearUnit))
                binding?.distanceBetweenTrees?.setText(orchard!!.distanceBetweenTrees.toString())
                binding?.distanceBetweenUnit?.setSelection(linearUnits.indexOf(orchard!!.distanceBetweenTreesLinearUnit))
                binding?.sand?.setText(orchard!!.sand.toString())
                binding?.silt?.setText(orchard!!.silt.toString())
                binding?.clay?.setText(orchard!!.clay.toString())
                binding?.organicMatter?.setText(orchard!!.organicMatter.toString())
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}