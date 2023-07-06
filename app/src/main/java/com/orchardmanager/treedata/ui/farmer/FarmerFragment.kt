package com.orchardmanager.treedata.ui.farmer

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.orchardmanager.treedata.databinding.FragmentFarmerBinding
import com.orchardmanager.treedata.entities.Farmer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FarmerFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentFarmerBinding? = null
    private val binding get() = _binding!!

    private val farmerViewModel: FarmerViewModel by viewModels()
    private var farmer: Farmer? = null

    companion object {
        fun newInstance() = FarmerFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //val vw = inflater.inflate(R.layout.fragment_farmer, container, false)
        _binding = FragmentFarmerBinding.inflate(inflater, container, false)
        val vw = binding.root
        binding.saveFarmer.setOnClickListener(this)

        farmerViewModel.get().observe(viewLifecycleOwner, Observer {
            farmers ->
            if(farmers != null && !farmers.isEmpty()) {
                farmer = farmers.get(0)
                if(farmer != null) {
                    binding.name.setText(farmer!!.name)
                    binding.address.setText(farmer!!.address)
                    binding.city.setText(farmer!!.city)
                    binding.state.setText(farmer!!.state)
                    binding.zip.setText((farmer!!.zip))
                    binding.phone.setText(farmer!!.phone)
                    binding.email.setText(farmer!!.email)
                }
            }
        })
        //binding?.phone?.inputType = InputType.TYPE_CLASS_PHONE
        return vw
    }

    override fun onClick(p0: View?) {
        if(farmer != null && farmer!!.id > 0L) {
            val updFarmer = farmer!!.copy(
                name = binding.name?.text.toString(),
                address = binding.address?.text.toString(),
                city = binding.city?.text.toString(),
                state = binding.state?.text.toString(),
                zip = binding.zip?.text.toString(),
                phone = binding.phone?.text.toString(),
                email = binding.email?.text.toString()
            )
            farmerViewModel.update(updFarmer!!)
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
        } else {
            this.farmer = Farmer(
                name = binding.name?.text.toString(),
                address = binding.address?.text.toString(),
                city = binding.city?.text.toString(),
                state = binding.state?.text.toString(),
                zip = binding.zip?.text.toString(),
                phone = binding.phone?.text.toString(),
                email = binding.email?.text.toString()
            )
            farmerViewModel.add(farmer!!).observe(this, Observer { farmerId ->
                Log.i("FarmerFragment", "the farmer ID...$farmerId")
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

