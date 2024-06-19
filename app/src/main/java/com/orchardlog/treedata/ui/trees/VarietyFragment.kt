package com.orchardlog.treedata.ui.trees

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
import com.orchardlog.treedata.databinding.FragmentVarietyBinding
import com.orchardlog.treedata.entities.Variety
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VarietyFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val treeViewModel: TreeViewModel by viewModels()
    private var _binding: FragmentVarietyBinding? = null
    private val binding get() = _binding
    private var variety: Variety? = null
    private var varietyId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVarietyBinding.inflate(inflater, container, false)
        val vw = binding?.root

        treeViewModel.getAllVarieties().observe(viewLifecycleOwner) {
            varieties ->
            if(varieties != null) {
                val adapter = ArrayAdapter(requireContext(),
                    R.layout.farm_spinner_layout, R.id.textViewFarmSpinner, varieties)
                adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
                binding?.varieties?.adapter = adapter
            }
        }
        saveVariety()
        newVariety()
        deleteVariety()

        return vw
    }

    private fun saveVariety() {
        binding?.saveVariety?.setOnClickListener {
            if(variety != null && variety!!.id > 0L) {
                val vty = variety?.copy(
                    id = varietyId,
                    name = binding?.varietyName?.text.toString(),
                    cultivar = binding?.varietyCultivar?.text.toString()
                )
                treeViewModel.update(vty!!)
                Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
            } else {
                this.variety = Variety(
                    name = binding?.varietyName?.text.toString(),
                    cultivar = binding?.varietyCultivar?.text.toString()
                )
                treeViewModel.add(variety!!).observe(viewLifecycleOwner) { _ ->
                    Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun newVariety() {
        binding?.newVariety?.setOnClickListener {
            this.variety = null
            binding?.varietyName?.setText("")
            binding?.varietyCultivar?.setText("")
        }
    }

    private fun deleteVariety() {
        binding?.deleteVariety?.setOnClickListener {
            if(variety != null) {
                treeViewModel.delete(variety!!)
            }
        }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val vty = parent?.adapter?.getItem(position)
        if(vty is Variety) {
            this.variety = vty
            varietyId = vty.id
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}