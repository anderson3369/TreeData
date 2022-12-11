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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.databinding.FragmentFarmBinding
import com.orchardmanager.treedata.entities.Farm
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FarmFragment : Fragment(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {

    private var _binding: FragmentFarmBinding? = null
    private val binding get() = _binding!!

    private var farmerId:Long = -1L
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
        val farmSpinner = vw?.findViewById<Spinner>(R.id.farmSpinner)
        farmViewModel.getFarmerId().observe(viewLifecycleOwner,  Observer { frmrId ->
            farmerId = frmrId
            farmViewModel.get(farmerId).observe(viewLifecycleOwner, Observer {
                farms -> sites = farms
                val farmAdapter:ArrayAdapter<Farm> = ArrayAdapter(requireActivity().applicationContext,
                    R.layout.farm_spinner_layout, R.id.textViewFarmSpinner, sites!!)
                farmAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
                farmSpinner?.adapter = farmAdapter
            })
        })
        farmSpinner?.onItemSelectedListener = this

        binding.saveFarm.setOnClickListener(this)

        return vw
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(FarmViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onClick(p0: View?) {
        if(p0?.id == R.id.saveFarm) {
            if(farm?.id != null) {
                val fm = farm!!.copy(
                    name = binding.farmName.text.toString(),
                    siteId = binding.farmName.text.toString()
                )
                farmViewModel.update(farm!!)
            } else {
                Log.i("FarmFragment", "sites size = " + sites?.count())
                val farm = Farm (
                    farmerId = farmerId,
                    name = binding.farmName.text.toString(),
                    siteId = binding.siteId.text.toString()
                )
                farmViewModel.add(farm).observe(this, Observer {
                    id ->
                    Log.i("FarmFragment", "the id..." + id.toString() + "the farmerId..." + farmerId.toString())
                })

            }
        } else if(p0?.id == R.id.newFarm) {
            farm = null
            binding.farmName.setText("")
            binding.siteId.setText("")
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val frm = p0?.getItemAtPosition(p2)
        if(frm != null && frm is Farm) {
            farm = frm
            binding.farmName.setText(frm.name)
            binding.siteId.setText(frm.siteId)
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}