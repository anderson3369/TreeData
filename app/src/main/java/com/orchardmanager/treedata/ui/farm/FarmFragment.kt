package com.orchardmanager.treedata.ui.farm

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.databinding.FragmentFarmBinding
import com.orchardmanager.treedata.entities.Farm
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FarmFragment : Fragment(),
    AdapterView.OnItemSelectedListener {

    private var _binding: FragmentFarmBinding? = null
    private val binding get() = _binding!!

    private var farmerId:Long = 0L
    private var farm:Farm? = null
    private var sites:MutableList<Farm>? = null

    companion object {
        fun newInstance() = FarmFragment()
    }

    private val farmViewModel: FarmViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //val vw = inflater.inflate(R.layout.fragment_farm, container, false)
        _binding = FragmentFarmBinding.inflate(inflater, container, false)
        val vw = binding.root
        
        farmViewModel.getFarmerId().observe(viewLifecycleOwner,  Observer { frmrId ->
            if(frmrId != null) {
                farmerId = frmrId
                farmViewModel.get(farmerId).observe(viewLifecycleOwner, Observer {
                        farms -> sites = farms
                    val farmAdapter: ArrayAdapter<Farm> = ArrayAdapter(requireContext(),
                        R.layout.farm_spinner_layout, R.id.textViewFarmSpinner, sites!!)
                    farmAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
                    //farmSpinner?.adapter = farmAdapter
                    binding.farmSpinner.adapter = farmAdapter
                })
            }

        })
        //farmSpinner?.onItemSelectedListener = this
        binding.farmSpinner.onItemSelectedListener = this

        saveOnClick()
        newOnClick()
        deleteOnClick()

        return vw
    }


    private fun saveOnClick() {
        binding.saveFarm?.setOnClickListener(View.OnClickListener {
            if(farm != null && farm?.id!! > 0L) {
                val fm = farm!!.copy(
                    name = binding.farmName.text.toString(),
                    siteId = binding.farmName.text.toString()
                )
                farmViewModel.update(fm!!)
                setFragmentResult("requestKey", bundleOf("farmId" to farm?.id))
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } else {
                Log.i("FarmFragment", "sites size = " + sites?.count())
                val farm = Farm (
                    farmerId = farmerId,
                    name = binding.farmName.text.toString(),
                    siteId = binding.siteId.text.toString()
                )
                farmViewModel.add(farm).observe(viewLifecycleOwner, Observer {
                        id ->
                    Log.i("FarmFragment",
                        "the id..." + id.toString() + "the farmerId..." + farmerId.toString())
                    setFragmentResult("requestKey", bundleOf("farmId" to farm?.id))
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                })
            }
        })
    }

    private fun newOnClick() {
        binding?.newFarm?.setOnClickListener(View.OnClickListener {
            farm = null
            binding.farmName.setText("")
            binding.siteId.setText("")
        })
    }

    private fun deleteOnClick() {
        binding?.deleteFarm?.setOnClickListener(View.OnClickListener {
            if(farm != null && farm?.id!! > 0L) {
                farmViewModel.delete(farm!!)
            }
        })
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val frm = p0?.adapter?.getItem(p2)
        if(frm != null && frm is Farm) {
            farm = frm
            binding.farmName.setText(frm.name)
            binding.siteId.setText(frm.siteId)
            setFragmentResult("requestKey", bundleOf("farmId" to farm?.id))
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}