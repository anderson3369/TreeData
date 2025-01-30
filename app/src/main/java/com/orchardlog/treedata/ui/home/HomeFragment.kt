package com.orchardlog.treedata.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.orchardlog.treedata.R
import com.orchardlog.treedata.databinding.FragmentHomeBinding
import com.orchardlog.treedata.ui.data.model.UserPreferencesViewModel
import com.orchardlog.treedata.ui.farm.FarmViewModel
import com.orchardlog.treedata.ui.farmer.FarmerViewModel
import com.orchardlog.treedata.ui.orchard.OrchardViewModel
import com.orchardlog.treedata.utils.RoomBackUp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val farmerViewModel: FarmerViewModel by viewModels()
    private val farmViewModel: FarmViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()
    private val userPreferencesViewModel: UserPreferencesViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private var isLoggedIn = false

    private lateinit var auth: FirebaseAuth

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var canBackup:Boolean? = null
    private var uid: String? = null

    companion object {
        const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.textHome.text = getString(R.string.let_s_get_to_work)

        lifecycleScope.launch {
            userPreferencesViewModel.getCanBackup().observe(viewLifecycleOwner) {
                canBackup = it
            }
        }

        auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener {
            if (it.currentUser != null && !it.currentUser!!.isAnonymous) {
                isLoggedIn = true
                uid = it.currentUser!!.uid

                if(canBackup == null || canBackup == false) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        userPreferencesViewModel.setBackup(true)
                    }

                    Log.i(TAG, "User is logged in, but backup is null/false")
                }
            } else {
                isLoggedIn = false
            }
        }

        farmerViewModel.get().observe(viewLifecycleOwner) { farmers ->
            if (farmers.isEmpty()) {
                //Might be first time

                if(isLoggedIn) {
                    lifecycleScope.launch {
                        val isFirstTime = RoomBackUp.isFirstTime(uid)
                        if(!isFirstTime) {
                            RoomBackUp.restoreDatabase(requireContext(), uid)
                        } else {
                            val action = HomeFragmentDirections.actionNavHomeToNavFirstFarmer()
                            view?.findNavController()?.navigate(action)
                        }
                    }
                } else {
                    if (canBackup == null || canBackup == false) {
                        AlertDialog.Builder(requireContext()).setTitle("Data Backup")
                            .setMessage("Login to backup your data")
                            .setPositiveButton("Yes") { _, _ ->
                                try {
                                    lifecycleScope.launch {
                                        userPreferencesViewModel.setBackup(true)
                                        if(!isLoggedIn || isLoggedIn == null) {
                                            val action = HomeFragmentDirections.actionNavHomeToNavLogin()
                                            view?.findNavController()?.navigate(action)
                                        }
                                    }
                                } catch(e: Exception) {
                                    Log.i(TAG, e.toString())
                                }
                            }.setNegativeButton("No") { _, _ ->
                                Log.i(TAG, "No backup")
                            }.show()
                    }
                }
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

    override fun onStart() {
        super.onStart()

        if(canBackup == true) {
            auth = FirebaseAuth.getInstance()

            if (auth.currentUser != null && !auth.currentUser!!.isAnonymous) {
                isLoggedIn = true
            } else {
                isLoggedIn = false
                val action = HomeFragmentDirections.actionNavHomeToNavLogin()
                view?.findNavController()?.navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}