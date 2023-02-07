package com.orchardmanager.treedata.ui.trees

import android.Manifest
import android.app.ProgressDialog.show
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssCapabilities
import android.location.GnssStatus
import android.location.GnssStatus.Callback
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion
import androidx.activity.result.registerForActivityResult
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.databinding.FragmentOrchardBinding
import com.orchardmanager.treedata.databinding.FragmentTreeBinding
import com.orchardmanager.treedata.entities.Farm
import com.orchardmanager.treedata.entities.Orchard
import com.orchardmanager.treedata.entities.Tree
import com.orchardmanager.treedata.ui.orchard.DatePickerFragment
import com.orchardmanager.treedata.ui.orchard.OrchardViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

private const val REQUEST_CODE = 100

@AndroidEntryPoint
class TreeFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener {

    private var _binding: FragmentTreeBinding? = null
    private val binding get() = _binding
    private var orchardId: Long = -1L
    private var locationManager: LocationManager? = null
    private var providerProperties: ProviderProperties? = null
    private var gnssCapabilities: GnssCapabilities? = null
    private var location: Location? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var tree: Tree? = null
    private var rootstockId: Long = 0L
    private var varietyId: Long = 0L

    companion object {
        fun newInstance() = TreeFragment()
    }

    private val treeViewModel: TreeViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        providerProperties = locationManager?.getProviderProperties(LocationManager.GPS_PROVIDER)
        gnssCapabilities = locationManager?.gnssCapabilities

        _binding = FragmentTreeBinding.inflate(inflater, container, false)
        val vw = binding?.root
        orchardViewModel.getFarmWithOrchards().observe(viewLifecycleOwner, Observer {
            farmWithOrchards ->
            val adapter = FarmWithOrchardAdapter(requireActivity(), farmWithOrchards)
            binding?.farmWithOrchardListView?.setAdapter(adapter)
        })

        binding?.showTreePlantedDate?.setOnClickListener(View.OnClickListener {
            DatePickerFragment().show(childFragmentManager, "Planted Date")
        })
        binding?.saveTree?.setOnClickListener(this)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            val locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        // Precise location access granted.
                        Log.i("TreeFragment", "Precise location granted")
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        // Only approximate location access granted.
                        Log.i("TreeFragment", "Coarse location granted")
                    } else -> {
                    // No location access granted.

                    }
                }
            }
            locationPermissionRequest.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
            //locationManager?.registerGnssStatusCallback(ThreadPerTaskExecutor(), GnssCallback())
        }

        binding?.addRootstock?.setOnClickListener(View.OnClickListener {
           val action = TreeFragmentDirections.actionNavTreeToNavRootstock()
            view?.findNavController()?.navigate(action)
        })

        binding?.markTree?.setOnClickListener(View.OnClickListener {
            if(providerProperties?.accuracy == ProviderProperties.ACCURACY_FINE
                && gnssCapabilities?.hasMeasurements() == true) {
                location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(location != null) {
                    val accuracy = location?.accuracy
                    Log.i("TreeFragment", "accuracy in meters " + accuracy.toString())
                    latitude = location?.latitude!!
                    longitude = location?.longitude!!
                }
            }
        })
        return vw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener("requestDateKey", requireActivity()) {
                dateKey, bundle -> binding?.showTreePlantedDate?.setText(bundle.getString("plantedDate"))
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(TreeViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.getItemAtPosition(position)
        if(obj != null) {
            if(obj is Farm) {
                //alert dialog please select a orchard
            } else if(obj is Orchard) {
                orchardId = obj.id
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        if(parent?.getItemAtPosition(0) != null) {
            binding?.farmWithOrchardListView?.expandGroup(0)
        }
    }

    inner class ThreadPerTaskExecutor: Executor {
        override fun execute(command: Runnable?) {
            Thread(command).start()
        }
    }

    inner class GnssCallback: GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            super.onSatelliteStatusChanged(status)
            Log.i("TreeFrgament", "number of satellites" + status.satelliteCount)
        }
    }

    override fun onClick(v: View?) {
        if(tree != null && (tree?.id != null && tree?.id!! > 0L)) {
            //update
            val plantDate = binding?.treePlantedDate?.text.toString()
            val date = DateConverter().toOffsetDate(plantDate)
            val tree2 = tree?.copy(
                orchardId = orchardId,
                rootstockId = rootstockId,
                varietyId = varietyId,
                plantedDate = date!!,
                latitude = latitude,
                longitude = longitude
            )
            treeViewModel.update(tree2!!)
        } else {
            val plantDate = binding?.treePlantedDate?.text.toString()
            val date = DateConverter().toOffsetDate(plantDate)
            tree = Tree(
                orchardId = orchardId,
                rootstockId = rootstockId,
                varietyId = varietyId,
                plantedDate = date!!,
                latitude = latitude,
                longitude = longitude
            )
            treeViewModel.add(tree!!).observe(this, Observer {
                id ->
                Log.i("TreeFragment", "the tree saved " + id.toString())
            })
        }
    }

}