package com.orchardmanager.treedata.ui.orchard

import android.app.DatePickerDialog
import android.app.ProgressDialog.show
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.fragment.app.viewModels
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.databinding.FragmentOrchardBinding
import com.orchardmanager.treedata.entities.Farm
import com.orchardmanager.treedata.entities.FarmWithOrchards
import com.orchardmanager.treedata.entities.Orchard
import com.orchardmanager.treedata.ui.farm.FarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class OrchardFragment : Fragment(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {

    private var _binding: FragmentOrchardBinding? = null
    private val binding get() = _binding
    private var farmId: Long = 0L
    private var orchards: MutableList<Orchard>? = null
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

        farmViewModel.getFarmerWithFarm().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            farmerWithFarm ->
            this.farms = farmerWithFarm.get(0).farms
        })
        orchardViewModel.getAllOrchards().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            orchards -> this.orchards = orchards
            val orchardAdapter: ArrayAdapter<Orchard> = ArrayAdapter(requireActivity().applicationContext,
            R.layout.farm_spinner_layout, R.id.textViewFarmSpinner, this.orchards!!)
            orchardAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.orchardSpinner?.adapter = orchardAdapter
        })
        binding?.showPlantedDate?.setOnClickListener(View.OnClickListener {
            DatePickerFragment().show(childFragmentManager, "Planted Date")
        })
        binding?.saveOrchard?.setOnClickListener(this)

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

    override fun onClick(p0: View?) {
        if(this.farmId == 0L) {
            FarmDialogFragment(this.farms!!).show(childFragmentManager, "Set")
            return
        }
        if(p0?.id == R.id.saveOrchard) {
            //save
            //val dc: DateConverter? = DateConverter()
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
            } else {
                orchard = DateConverter().toOffsetDate(binding?.plantedDate?.text.toString())?.let {
                    Orchard(
                        farmId = this.farmId,
                        crop = binding?.crop?.text.toString(),
                        plantedDate = it
                    )
                }
                val id = orchardViewModel.add(orchard!!)
                Log.i("OrchardFragment", "the orchard id is..." + id.toString())
            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val orch = p0?.getItemAtPosition(p2)
        if(orch != null && orch is Orchard) {
            this.orchard = orch
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}