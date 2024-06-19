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
import com.orchardlog.treedata.R
import com.orchardlog.treedata.databinding.FragmentFertilizerBinding
import com.orchardlog.treedata.entities.Fertilizer
import com.orchardlog.treedata.utils.SAVE_FAILED
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FertilizerFragment : Fragment(), AdapterView.OnItemSelectedListener,
    View.OnClickListener{

    private var _binding: FragmentFertilizerBinding? = null
    private val binding get() = _binding
    private val fertilzerViewModel: FertilizerViewModel by viewModels()
    private var fertilizer: Fertilizer? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFertilizerBinding.inflate(inflater, container, false)
        val vw = binding?.root

        fertilzerViewModel.getFertilizers().observe(viewLifecycleOwner) {
            fertilizers ->
            val adapter = ArrayAdapter(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, fertilizers)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.fertilizerSpinner?.adapter = adapter
            binding?.fertilizerSpinner?.onItemSelectedListener = this
        }

        binding?.saveFertilizer?.setOnClickListener(this)

        binding?.newFertilizer?.setOnClickListener {
            this.fertilizer = null
            binding?.fertilizerName?.setText("")
            binding?.nitrogen?.setText("")
            binding?.phosphorous?.setText("")
            binding?.potassium?.setText("")
            binding?.sulfur?.setText("")
            binding?.calcium?.setText("")
            binding?.magnesium?.setText("")
            binding?.boron?.setText("")
            binding?.zinc?.setText("")
            binding?.iron?.setText("")
            binding?.manganese?.setText("")
            binding?.molybdenum?.setText("")
            binding?.chloride?.setText("")
            binding?.copper?.setText("")
            binding?.nickel?.setText("")
            binding?.organicMatter?.setText("")
        }

        binding?.deleteFertilizer?.setOnClickListener {
            fertilzerViewModel.deleteFertilizer(fertilizer!!)
        }

        return vw
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is Fertilizer) {
            this.fertilizer = obj
            binding?.fertilizerName?.setText(obj.name)
            binding?.nitrogen?.setText(obj.nitrogen.toString())
            binding?.phosphorous?.setText(obj.phosphorous.toString())
            binding?.potassium?.setText(obj.potassium.toString())
            binding?.sulfur?.setText(obj.sulfur.toString())
            binding?.calcium?.setText(obj.calcium.toString())
            binding?.magnesium?.setText(obj.magnesium.toString())
            binding?.boron?.setText(obj.boron.toString())
            binding?.zinc?.setText(obj.zinc.toString())
            binding?.iron?.setText(obj.iron.toString())
            binding?.manganese?.setText(obj.manganese.toString())
            binding?.molybdenum?.setText(obj.molybdenum.toString())
            binding?.chloride?.setText(obj.chloride.toString())
            binding?.copper?.setText(obj.copper.toString())
            binding?.nickel?.setText(obj.nickel.toString())
            binding?.organicMatter?.setText(obj.organicMatter.toString())
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onClick(v: View?) {

        var nitrogen = 0.0
        val snitrogen = binding?.nitrogen?.text.toString()
        if(snitrogen.isNotEmpty()) {
            nitrogen = snitrogen.toDouble()
        }
        var phosphorous = 0.0
        val sphosphorous = binding?.phosphorous?.text.toString()
        if(sphosphorous.isNotEmpty()) {
            phosphorous = sphosphorous.toDouble()
        }
        var potassium = 0.0
        val spotassium = binding?.potassium?.text.toString()
        if(spotassium.isNotEmpty()) {
            potassium = spotassium.toDouble()
        }
        var sulfur = 0.0
        val ssulfur = binding?.sulfur?.text.toString()
        if(ssulfur.isNotEmpty()) {
            sulfur = ssulfur.toDouble()
        }
        var calcium = 0.0
        val scalcium = binding?.calcium?.text.toString()
        if(scalcium.isNotEmpty()) {
            calcium = scalcium.toDouble()
        }
        var magnesium = 0.0
        val smagnesium = binding?.magnesium?.text.toString()
        if(smagnesium.isNotEmpty()) {
            magnesium = smagnesium.toDouble()
        }
        var boron = 0.0
        val sboron = binding?.boron?.text.toString()
        if(sboron.isNotEmpty()) {
            boron = sboron.toDouble()
        }
        var zinc = 0.0
        val szinc = binding?.zinc?.text.toString()
        if(szinc.isNotEmpty()) {
            zinc = szinc.toDouble()
        }
        var iron = 0.0
        val siron = binding?.iron?.text.toString()
        if(siron.isNotEmpty()) {
            iron = siron.toDouble()
        }
        var manganese = 0.0
        val smanganese = binding?.manganese?.text.toString()
        if(smanganese.isNotEmpty()) {
            manganese = smanganese.toDouble()
        }
        var molybdenum = 0.0
        val smolybdenum = binding?.molybdenum?.text.toString()
        if(smolybdenum.isNotEmpty()) {
            molybdenum = smolybdenum.toDouble()
        }
        var chloride = 0.0
        val schloride = binding?.chloride?.text.toString()
        if(schloride.isNotEmpty()) {
            chloride = schloride.toDouble()
        }
        var copper = 0.0
        val scopper = binding?.copper?.text.toString()
        if(scopper.isNotEmpty()) {
            copper = scopper.toDouble()
        }
        var nickel = 0.0
        val snickel = binding?.nickel?.text.toString()
        if(snickel.isNotEmpty()) {
            nickel = snickel.toDouble()
        }
        var organicMatter = 0.0
        val sorganicMatter = binding?.organicMatter?.text.toString()
        if(sorganicMatter.isNotEmpty()) {
            organicMatter = sorganicMatter.toDouble()
        }

        if(fertilizer != null && fertilizer!!.id > 0) {
            val fert = fertilizer!!.copy(
                id = fertilizer!!.id,
                name = binding?.fertilizerName?.text.toString(),
                nitrogen = nitrogen,
                phosphorous = phosphorous,
                potassium = potassium,
                sulfur = sulfur,
                calcium = calcium,
                magnesium = magnesium,
                boron = boron,
                zinc = zinc,
                iron = iron,
                manganese = manganese,
                molybdenum = molybdenum,
                chloride = chloride,
                copper = copper,
                nickel = nickel,
                organicMatter = organicMatter
            )
            try {
                fertilzerViewModel.updateFertilizer(fert)
                Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), getString(R.string.update_failed), Toast.LENGTH_LONG).show()
            }
        } else {
            val fertilizer = Fertilizer(
                name = binding?.fertilizerName?.text.toString(),
                nitrogen = nitrogen,
                phosphorous = phosphorous,
                potassium = potassium,
                sulfur = sulfur,
                calcium = calcium,
                magnesium = magnesium,
                boron = boron,
                zinc = zinc,
                iron = iron,
                manganese = manganese,
                molybdenum = molybdenum,
                chloride = chloride,
                copper = copper,
                nickel = nickel,
                organicMatter = organicMatter
            )
            fertilzerViewModel.addFertilizer(fertilizer).observe(this) {
                id ->
                if(!id.equals(SAVE_FAILED)) {
                    Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.save_failed), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}