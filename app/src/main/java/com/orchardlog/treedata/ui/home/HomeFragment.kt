package com.orchardlog.treedata.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.orchardlog.treedata.R
import com.orchardlog.treedata.databinding.FragmentHomeBinding
import com.orchardlog.treedata.ui.farm.FarmViewModel
import com.orchardlog.treedata.ui.farmer.FarmerViewModel
import com.orchardlog.treedata.ui.orchard.OrchardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val farmerViewModel: FarmerViewModel by viewModels()
    private val farmViewModel: FarmViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val homeViewModel =
            //ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.textHome.setText(getString(R.string.let_s_get_to_work))

        farmerViewModel.get().observe(viewLifecycleOwner) {
            farmers ->
            if(farmers.isEmpty()) {
                val action = HomeFragmentDirections.actionNavHomeToNavFirstFarmer()
                view?.findNavController()?.navigate(action)
            } else {
                farmViewModel.getFarms().observe(viewLifecycleOwner) {
                        farms ->
                    if(farms.isEmpty()) {
                        val action = HomeFragmentDirections.actionNavHomeToNavFirstFarm()
                        view?.findNavController()?.navigate(action)
                    } else {
                        orchardViewModel.getAllOrchards().observe(viewLifecycleOwner) {
                            orchards ->
                            if(orchards.isEmpty()) {
                                val action = HomeFragmentDirections.actionNavHomeToNavFirstOrchard()
                                view?.findNavController()?.navigate(action)
                            }
                        }
                    }
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}