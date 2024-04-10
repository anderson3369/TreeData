package com.orchardlog.treedata.ui.farmer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.orchardlog.treedata.R
import com.orchardlog.treedata.databinding.FragmentFarmerBinding
import com.orchardlog.treedata.entities.Farmer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FarmerFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentFarmerBinding? = null
    private val binding get() = _binding!!

    private val farmerViewModel: FarmerViewModel by viewModels()
    private var farmer: Farmer? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFarmerBinding.inflate(inflater, container, false)
        val vw = binding.root
        binding.saveFarmer.setOnClickListener(this)

        farmerViewModel.get().observe(viewLifecycleOwner) {
            farmers ->
            if(farmers != null && !farmers.isEmpty()) {
                farmer = farmers.get(0)
                binding.name.setText(farmer!!.name)
                binding.address.setText(farmer!!.address)
                binding.city.setText(farmer!!.city)
                binding.state.setText(farmer!!.state)
                binding.zip.setText((farmer!!.zip))
                binding.phone.setText(farmer!!.phone)
                binding.email.setText(farmer!!.email)

            }
        }
        return vw
    }

    override fun onClick(p0: View?) {
        if(farmer != null && farmer!!.id > 0L) {
            val updFarmer = farmer!!.copy(
                name = binding.name.text.toString(),
                address = binding.address.text.toString(),
                city = binding.city.text.toString(),
                state = binding.state.text.toString(),
                zip = binding.zip.text.toString(),
                phone = binding.phone.text.toString(),
                email = binding.email.text.toString()
            )
            farmerViewModel.update(updFarmer)
            Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
        } else {
            this.farmer = Farmer(
                name = binding.name.text.toString(),
                address = binding.address.text.toString(),
                city = binding.city.text.toString(),
                state = binding.state.text.toString(),
                zip = binding.zip.text.toString(),
                phone = binding.phone.text.toString(),
                email = binding.email.text.toString()
            )
            farmerViewModel.add(farmer!!).observe(this) { farmerId ->
                Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

