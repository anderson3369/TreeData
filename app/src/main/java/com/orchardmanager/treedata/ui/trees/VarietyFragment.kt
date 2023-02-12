package com.orchardmanager.treedata.ui.trees

import androidx.lifecycle.ViewModelProvider
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
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.databinding.FragmentVarietyBinding
import com.orchardmanager.treedata.entities.Variety
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VarietyFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val treeViewModel: TreeViewModel by viewModels()
    private var _binding: FragmentVarietyBinding? = null
    private val binding get() = _binding
    private var variety: Variety? = null
    private var varietyId: Long = -1L

    companion object {
        fun newInstance() = VarietyFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVarietyBinding.inflate(inflater, container, false)
        val vw = binding?.root

        treeViewModel.getAllVarieties().observe(viewLifecycleOwner, Observer {
            varieties ->
            if(varieties != null) {
                val adapter = ArrayAdapter<Variety>(requireContext(),
                    R.layout.farm_spinner_layout, R.id.textViewFarmSpinner, varieties)
                adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
                binding?.varieties?.adapter = adapter
            }
        })
        saveVariety()
        newVariety()
        deleteVariety()

        return vw
        //return inflater.inflate(R.layout.fragment_variety, container, false)
    }

    private fun saveVariety() {
        binding?.saveVariety?.setOnClickListener(View.OnClickListener {
            if(variety != null && variety!!.id > 0L) {
                val vty = variety?.copy(
                    id = varietyId,
                    name = binding?.varietyName?.text.toString(),
                    cultivar = binding?.varietyCultivar?.text.toString()
                )
                treeViewModel?.update(vty!!)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } else {
                this.variety = Variety(
                    name = binding?.varietyName?.text.toString(),
                    cultivar = binding?.varietyCultivar?.text.toString()
                )
                treeViewModel?.add(variety!!)?.observe(viewLifecycleOwner, Observer {
                    id ->
                    Log.i("VarietyFragment", "the variety id is " + id.toString())
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                })
            }
        })
    }

    private fun newVariety() {
        binding?.newVariety?.setOnClickListener(View.OnClickListener {
            this.variety = null
            binding?.varietyName?.setText("")
            binding?.varietyCultivar?.setText("")
        })
    }

    private fun deleteVariety() {
        binding?.deleteVariety?.setOnClickListener(View.OnClickListener {
            if(variety != null) {
                treeViewModel.delete(variety!!)
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(VarietyViewModel::class.java)
        // TODO: Use the ViewModel
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