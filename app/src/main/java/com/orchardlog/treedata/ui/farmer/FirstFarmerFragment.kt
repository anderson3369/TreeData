package com.orchardlog.treedata.ui.farmer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.orchardlog.treedata.R
import com.orchardlog.treedata.databinding.FragmentFirstFarmerBinding
import com.orchardlog.treedata.entities.Farmer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FirstFarmerFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentFirstFarmerBinding? = null
    private val binding get() = _binding!!

    private val farmerViewModel: FarmerViewModel by viewModels()
    private var farmer: Farmer? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstFarmerBinding.inflate(inflater, container, false)
        val vw = binding.root
        binding.createSaveFarmer.setOnClickListener(this)

        farmerViewModel.get().observe(viewLifecycleOwner) {
            farmers ->
            if(farmers != null && !farmers.isEmpty()) {
                farmer = farmers.get(0)
                binding.createName.setText(farmer!!.name)
                binding.createAddress.setText(farmer!!.address)
                binding.createCity.setText(farmer!!.city)
                binding.createState.setText(farmer!!.state)
                binding.createZip.setText((farmer!!.zip))
                binding.createPhone.setText(farmer!!.phone)
                binding.createEmail.setText(farmer!!.email)
            }
        }

        binding.nextFarm.setOnClickListener {
            val action = FirstFarmerFragmentDirections.actionNavFirstFarmerToNavFirstFarm()
            view?.findNavController()?.navigate(action)
        }

        return vw
    }

    override fun onClick(p0: View?) {
        if(farmer != null && farmer!!.id > 0L) {
            val updFarmer = farmer!!.copy(
                name = binding.createName.text.toString(),
                address = binding.createAddress.text.toString(),
                city = binding.createCity.text.toString(),
                state = binding.createState.text.toString(),
                zip = binding.createZip.text.toString(),
                phone = binding.createPhone.text.toString(),
                email = binding.createEmail.text.toString()
            )
            farmerViewModel.update(updFarmer)
            Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
        } else {
            this.farmer = Farmer(
                name = binding.createName.text.toString(),
                address = binding.createAddress.text.toString(),
                city = binding.createCity.text.toString(),
                state = binding.createState.text.toString(),
                zip = binding.createZip.text.toString(),
                phone = binding.createPhone.text.toString(),
                email = binding.createEmail.text.toString()
            )
            farmerViewModel.add(farmer!!).observe(this) { farmerId ->
                Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

