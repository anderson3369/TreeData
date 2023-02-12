package com.orchardmanager.treedata.ui.orchard

import android.app.ProgressDialog.show
import android.content.DialogInterface
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
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.databinding.FragmentOrchardBinding
import com.orchardmanager.treedata.entities.Farm
import com.orchardmanager.treedata.entities.FarmWithOrchards
import com.orchardmanager.treedata.entities.Orchard
import com.orchardmanager.treedata.ui.farm.FarmViewModel
import com.orchardmanager.treedata.ui.trees.FarmWithOrchardAdapter
import com.orchardmanager.treedata.ui.trees.FarmWithOrchardsID
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrchardFragment : Fragment(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {

    private var _binding: FragmentOrchardBinding? = null
    private val binding get() = _binding
    private var farmId: Long = -1L
    //private var orchards: MutableList<Orchard>? = null
    private var farms: MutableList<Farm>? = null
    private var orchard: Orchard? = null

    companion object {
        fun newInstance() = OrchardFragment()
    }

    private val orchardViewModel: OrchardViewModel by viewModels()
    private val farmViewModel: FarmViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrchardBinding.inflate(inflater, container, false)
        val vw = binding?.root
/**
        farmViewModel.getFarmerWithFarm().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            farmerWithFarm ->
            if(farmerWithFarm != null) {
                if(farmerWithFarm != null && !farmerWithFarm.isEmpty()) {
                    this.farms = farmerWithFarm.get(0).farms
                    val adapter = ArrayAdapter<Farm>(requireContext(), R.layout.farm_spinner_layout,
                    R.id.textViewFarmSpinner, farms!!)
                    adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
                    binding?.farmOrchardSpinner?.adapter = adapter
                    binding?.farmOrchardSpinner?.onItemSelectedListener
                }
            }
        })
*/
        orchardViewModel.getFarmWithOrchards().observe(viewLifecycleOwner, Observer {
            farmWithOrchards ->
            val list = createFarmWithOrchardsIO(farmWithOrchards)
            val adapter = ArrayAdapter<FarmWithOrchardsID>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, list)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.farmOrchardSpinner?.adapter = adapter
            binding?.farmOrchardSpinner?.onItemSelectedListener = this
        })

        //val headerView = layoutInflater.inflate(R.layout.orchard_child_textview, container, false)

        binding?.showPlantedDate?.setOnClickListener(View.OnClickListener {
            DatePickerFragment().show(childFragmentManager, "Planted Date")
        })
        binding?.saveOrchard?.setOnClickListener(this)

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
        childFragmentManager.setFragmentResultListener("requestDateKey", requireActivity()) {
            dateKey, bundle -> binding?.plantedDate?.setText(bundle.getString("plantedDate"))
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(OrchardViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun saveOrchard() {
        if(orchard != null && (orchard?.id != null && orchard?.id!! > 0L)) {
            val orchard2 = DateConverter().toOffsetDate(
                binding?.plantedDate?.text.toString())?.let {
                orchard?.copy(
                    farmId = this.farmId,
                    crop = binding?.crop?.text.toString(),
                    plantedDate = it
                )
            }
            orchardViewModel.update(orchard2!!)
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
        } else {
            orchard = DateConverter().toOffsetDate(binding?.plantedDate?.text.toString())?.let {
                Orchard(
                    farmId = this.farmId,
                    crop = binding?.crop?.text.toString(),
                    plantedDate = it
                )
            }
            orchardViewModel.add(orchard!!).observe(this, androidx.lifecycle.Observer {
                    id -> Log.i("OrchardFragment", "the orchard id is..." + id.toString())
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            })

        }
    }

    private fun createFarmWithOrchardsIO(farmWithOrchards: MutableList<FarmWithOrchards>): MutableList<FarmWithOrchardsID> {
        val list = mutableListOf<FarmWithOrchardsID>()
        val farmWithOrchardsIterator = farmWithOrchards.iterator()
        while (farmWithOrchardsIterator.hasNext()) {
            val farmWithOrchard = farmWithOrchardsIterator.next()
            this.farmId = farmWithOrchard.farm.id
            val orchards = farmWithOrchard.orchards
            if(!orchards.isEmpty()) {
                val orchardsIterator = orchards.iterator()
                while (orchardsIterator.hasNext()) {
                    val orchard = orchardsIterator.next()
                    val fwo = FarmWithOrchardsID(
                        id = farmWithOrchard.farm.id.toString()+":"+orchard.id.toString(),
                        name = farmWithOrchard.farm.siteId + " - " + orchard.crop
                    )
                    list.add(fwo)
                }
            } else {
                val fwo = FarmWithOrchardsID(
                    id = farmWithOrchard.farm.id.toString(),
                    name = farmWithOrchard.farm.siteId
                )
                list.add(fwo)
            }

        }
        return list
    }

    override fun onClick(p0: View?) {
        //if(this.farmId == -1L) {
        //     val fdf = FarmDialogFragment(this.farms!!)
        //    fdf.show(childFragmentManager, "Set")
        //} else {
            saveOrchard()
        //}
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val obj = p0?.adapter?.getItem(p2)
        //val obj = p0?.getItemAtPosition(p2)
        if(obj != null) {
            if(obj is Farm) {
                this.farmId = obj.id
            } else if(obj is Orchard) {
                this.orchard = obj
                binding?.crop?.setText(orchard?.crop)
                val sDate = DateConverter().fromOffsetDate(orchard?.plantedDate!!)
                binding?.plantedDate?.setText(sDate)
                this.farmId = orchard?.farmId!!
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}