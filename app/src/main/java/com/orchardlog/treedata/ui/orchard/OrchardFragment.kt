package com.orchardlog.treedata.ui.orchard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.orchardlog.treedata.R
import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.data.Validator
import com.orchardlog.treedata.databinding.FragmentOrchardBinding
import com.orchardlog.treedata.entities.Farm
import com.orchardlog.treedata.entities.LinearUnit
import com.orchardlog.treedata.entities.Orchard
import com.orchardlog.treedata.utils.DatePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class OrchardFragment : Fragment(), View.OnClickListener,
    OnItemSelectedListener {

    private var _binding: FragmentOrchardBinding? = null
    private val binding get() = _binding
    private var farmId: Long = 0L
    private var farms: MutableList<Farm>? = null
    private var orchard: Orchard? = null
    private var rowWidthUnit: LinearUnit? = null
    private var distanceBetweenUnit: LinearUnit? = null
    private val linearUnits = arrayOf(LinearUnit.FEET,LinearUnit.METERS, LinearUnit.INCHES)
    private var orchardList: MutableList<Orchard>? = null

    private val farmOrchardMap = hashMapOf<Long, MutableList<Orchard>>()

    private val orchardViewModel: OrchardViewModel by viewModels()

    companion object {
        const val PLANTEDDATEREQUESTKEY = "plantedDateRequestKey"
        const val PLANTEDDATEKEY = "plantedDateKey"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrchardBinding.inflate(inflater, container, false)
        val vw = binding?.root

        orchardViewModel.getFarmWithOrchards().observe(viewLifecycleOwner) {
            farmWithOrchards ->
            val farmWithOrchardsIterator = farmWithOrchards.iterator()
            this.farms = mutableListOf()

            while (farmWithOrchardsIterator.hasNext()) {
                val farmWithOrchard = farmWithOrchardsIterator.next()
                farms!!.add(farmWithOrchard.farm)
                farmOrchardMap[farmWithOrchard.farm.id] = farmWithOrchard.orchards
            }
            //Set a default value
            if(farmWithOrchards.isNotEmpty()) {
                this.farmId = farmWithOrchards[0].farm.id
                this.orchardList = farmWithOrchards[0].orchards

                val farmAdapter = ArrayAdapter(requireContext(), R.layout.farm_spinner_layout,
                    R.id.textViewFarmSpinner, farms!!
                )
                farmAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
                binding?.farms?.adapter = farmAdapter
                binding?.farms?.onItemSelectedListener = this
                //binding?.farms?.setSelection(0)

                val orchardAdapter = ArrayAdapter(requireContext(), R.layout.farm_spinner_layout,
                    R.id.textViewFarmSpinner, orchardList!!
                )
                orchardAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
                binding?.orchardSpinner?.adapter = orchardAdapter
                binding?.orchardSpinner?.onItemSelectedListener = this
            }
        }

        binding?.plantedDate?.onFocusChangeListener =
            View.OnFocusChangeListener { _, _ ->
                val date = binding?.plantedDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                }
            }

        binding?.showPlantedDate?.setOnClickListener {
            DatePickerFragment(PLANTEDDATEREQUESTKEY, PLANTEDDATEKEY).show(childFragmentManager, "Planted Date")
        }
        binding?.saveOrchard?.setOnClickListener(this)

        rowWidthUnit()
        distanceBetweenUnit()

        binding?.newOrchard?.setOnClickListener {
            this.orchard = null
            binding?.crop?.setText("")
            binding?.plantedDate?.setText("")
        }

        binding?.deleteOrchard?.setOnClickListener {
            if(orchard != null) {
                orchardViewModel.delete(orchard!!)
            }
        }

        return vw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener("requestKey", requireActivity()) {
                _, bundle -> this.farmId = bundle.getLong("farmId")
        }
        childFragmentManager.setFragmentResultListener(PLANTEDDATEREQUESTKEY, requireActivity()) {
                _, bundle -> binding?.plantedDate?.setText(bundle.getString(PLANTEDDATEKEY))
        }
    }


    private fun linearUnit(): ArrayAdapter<LinearUnit> {
        val adapter = ArrayAdapter(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, linearUnits)
        adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        return adapter
    }

    private fun rowWidthUnit() {
        val adapter = linearUnit()
        binding?.rowWidthUnit?.adapter = adapter
        binding?.rowWidthUnit?.onItemSelectedListener = CropRowWidthSelector()
    }

    private fun distanceBetweenUnit() {
        val adapter = linearUnit()
        binding?.distanceBetweenUnit?.adapter = adapter
        binding?.distanceBetweenUnit?.onItemSelectedListener = DistanceBetweenSelector()
    }

    inner class CropRowWidthSelector: OnItemSelectedListener {
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

    inner class DistanceBetweenSelector: OnItemSelectedListener {
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
        if(this.farmId == 0L) {
            Toast.makeText(requireContext(), "Please select a farm", Toast.LENGTH_LONG).show()
            return
        }

        val pDate: LocalDate?
        try {
            pDate = DateConverter().toOffsetDate(binding?.plantedDate?.text.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Check the date format", Toast.LENGTH_LONG).show()
            return
        }

        val sRowWidth = binding?.rowWidth?.text.toString()
        var rowWidth = 0.0
        if(sRowWidth.isNotBlank()) {
            rowWidth = sRowWidth.toDouble()
        }

        val ssand = binding?.sand?.text.toString()
        var sand = 0.0

        if(ssand.isNotBlank()) {
            sand = ssand.toDouble()
        }

        val ssilt = binding?.silt?.text.toString()
        var silt = 0.0

        if(ssilt.isNotBlank()) {
            silt = ssilt.toDouble()
        }

        val sclay = binding?.clay?.text.toString()
        var clay = 0.0

        if(sclay.isNotBlank()) {
            clay = sclay.toDouble()
        }

        val som = binding?.organicMatter?.text.toString()
        var om = 0.0
        if(som.isNotBlank()) {
            om = som.toDouble()
        }
        if(orchard != null && orchard?.id!! > 0L) {
            val orchard2 = orchard?.copy(
                    farmId = this.farmId,
                    crop = binding?.crop?.text.toString(),
                    plantedDate = pDate!!,
                    rowWidth = rowWidth,
                    rowWidthLinearUnit = rowWidthUnit!!,
                    distanceBetweenTrees = binding?.distanceBetweenTrees?.text.toString().toDouble(),
                    distanceBetweenTreesLinearUnit = distanceBetweenUnit!!,
                    sand = sand,
                    silt = silt,
                    clay = clay,
                    organicMatter = om
                )
            orchardViewModel.update(orchard2!!)
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
        } else {
            orchard = Orchard(
                    farmId = this.farmId,
                    crop = binding?.crop?.text.toString(),
                    plantedDate = pDate!!,
                    rowWidth = rowWidth,
                    rowWidthLinearUnit = rowWidthUnit!!,
                    distanceBetweenTrees = binding?.distanceBetweenTrees?.text.toString().toDouble(),
                    distanceBetweenTreesLinearUnit = distanceBetweenUnit!!,
                    sand = sand,
                    silt = silt,
                    clay = clay,
                    organicMatter = om
                )
            orchardViewModel.add(orchard!!).observe(this) { _ ->
                Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
            }
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
                orchardList = farmOrchardMap[obj.id]
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