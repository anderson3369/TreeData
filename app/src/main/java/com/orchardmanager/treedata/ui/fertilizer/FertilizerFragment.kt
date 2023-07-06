package com.orchardmanager.treedata.ui.fertilizer

import androidx.lifecycle.ViewModelProvider
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
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.data.Validator
import com.orchardmanager.treedata.databinding.FragmentFarmBinding
import com.orchardmanager.treedata.databinding.FragmentFertilizerBinding
import com.orchardmanager.treedata.databinding.FragmentOrchardBinding
import com.orchardmanager.treedata.entities.Fertilizer
import com.orchardmanager.treedata.ui.SAVE_FAILED
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

        fertilzerViewModel?.getFertilizers()?.observe(viewLifecycleOwner, Observer {
            fertilizers ->
            val adapter = ArrayAdapter<Fertilizer>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, fertilizers)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.fertilizerSpinner?.adapter = adapter
            binding?.fertilizerSpinner?.onItemSelectedListener = this
        })

        binding?.saveFertilizer?.setOnClickListener(this)

        binding?.newFertilizer?.setOnClickListener(View.OnClickListener {
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
        })

        binding?.deleteFertilzer?.setOnClickListener(View.OnClickListener {
            fertilzerViewModel?.deleteFertilizer(fertilizer!!)
        })

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

        var nitrogen: Double = 0.0
        val snitrogen = binding?.nitrogen?.text.toString()
        if(!snitrogen.isEmpty()) {
            nitrogen = snitrogen.toDouble()
        }
        var phosphorous: Double = 0.0
        val sphosphorous = binding?.phosphorous?.text.toString()
        if(!sphosphorous.isEmpty()) {
            phosphorous = sphosphorous.toDouble()
        }
        var potassium: Double = 0.0
        val spotassium = binding?.potassium?.text.toString()
        if(!spotassium.isEmpty()) {
            potassium = spotassium.toDouble()
        }
        var sulfur: Double = 0.0
        val ssulfur = binding?.sulfur?.text.toString()
        if(!ssulfur.isEmpty()) {
            sulfur = ssulfur.toDouble()
        }
        var calcium: Double = 0.0
        val scalcium = binding?.calcium?.text.toString()
        if(!scalcium.isEmpty()) {
            calcium = scalcium.toDouble()
        }
        var magnesium: Double = 0.0
        val smagnesium = binding?.magnesium?.text.toString()
        if(!smagnesium.isEmpty()) {
            magnesium = smagnesium.toDouble()
        }
        var boron: Double = 0.0
        val sboron = binding?.boron?.text.toString()
        if(!sboron.isEmpty()) {
            boron = sboron.toDouble()
        }
        var zinc: Double = 0.0
        val szinc = binding?.zinc?.text.toString()
        if(!szinc.isEmpty()) {
            zinc = szinc.toDouble()
        }
        var iron: Double = 0.0
        val siron = binding?.iron?.text.toString()
        if(!siron.isEmpty()) {
            iron = siron.toDouble()
        }
        var manganese: Double = 0.0
        val smanganese = binding?.manganese?.text.toString()
        if(!smanganese.isEmpty()) {
            manganese = smanganese.toDouble()
        }
        var molybdenum: Double = 0.0
        val smolybdenum = binding?.molybdenum?.text.toString()
        if(!smolybdenum.isEmpty()) {
            molybdenum = smolybdenum.toDouble()
        }
        var chloride: Double = 0.0
        val schloride = binding?.chloride?.text.toString()
        if(!schloride.isEmpty()) {
            chloride = schloride.toDouble()
        }
        var copper: Double = 0.0
        val scopper = binding?.copper?.text.toString()
        if(!scopper.isEmpty()) {
            copper = scopper.toDouble()
        }
        var nickel: Double = 0.0
        val snickel = binding?.nickel?.text.toString()
        if(!snickel.isEmpty()) {
            nickel = snickel.toDouble()
        }
        var organicMatter: Double = 0.0
        val sorganicMatter = binding?.organicMatter?.text.toString()
        if(!sorganicMatter.isEmpty()) {
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
                fertilzerViewModel?.updateFertilizer(fert)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
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
            fertilzerViewModel.addFertilizer(fertilizer).observe(this, Observer {
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