package com.orchardmanager.treedata.ui.trees

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.location.provider.ProviderProperties
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.databinding.FragmentTreeBinding
import com.orchardmanager.treedata.entities.*
import com.orchardmanager.treedata.ui.orchard.OrchardViewModel
import com.orchardmanager.treedata.ui.orchard.PlantedDatePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor
import java.util.function.Consumer

@AndroidEntryPoint
class TreeFragment : Fragment(), AdapterView.OnItemSelectedListener,
    View.OnClickListener, LocationListener {

    private var _binding: FragmentTreeBinding? = null
    private val binding get() = _binding
    private var orchardId: Long = -1L
    internal var location: Location? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var tree: Tree? = null
    private var rootstockId: Long = 0L
    private var varietyId: Long = 0L
    private var farmWithOrchardsMap: Map<Long, String>? = null
    //val locationPermissionRequest: ActivityResultLauncher = null

    companion object {
        fun newInstance() = TreeFragment()
    }

    private val treeViewModel: TreeViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTreeBinding.inflate(inflater, container, false)
        val vw = binding?.root

        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner, Observer {
            farmWithOrchards ->
            this.farmWithOrchardsMap = farmWithOrchards
            //val list = FarmOrchardConverter(farmWithOrchards)
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.farmWithOrchardsSpinner?.adapter = adapter
        })

        treeViewModel?.getAllRootstocks()?.observe(viewLifecycleOwner, Observer {
            rootstocks ->
            val adapter = ArrayAdapter<Rootstock>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, rootstocks)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.rootstockSpinner?.adapter = adapter
            binding?.rootstockSpinner?.onItemSelectedListener = this
        })

        treeViewModel?.getAllVarieties()?.observe(viewLifecycleOwner, Observer {
            varieties ->
            val adapter = ArrayAdapter<Variety>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, varieties)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.varietySpinner?.adapter = adapter
        })

        binding?.showTreePlantedDate?.setOnClickListener(View.OnClickListener {
            PlantedDatePickerFragment().show(childFragmentManager, "Planted Date")
        })
        binding?.saveTree?.setOnClickListener(this)

        markTree()
        addRootstock()
        addVariety()

        return vw
    }

    private fun addRootstock() {
        binding?.addRootstock?.setOnClickListener(View.OnClickListener {
            val action = TreeFragmentDirections.actionNavTreeToNavRootstock()
            view?.findNavController()?.navigate(action)
        })
    }

    private fun addVariety() {
        binding?.addVariety?.setOnClickListener(View.OnClickListener {
            val action = TreeFragmentDirections.actionNavTreeToNavVariety()
            view?.findNavController()?.navigate(action)
        })
    }

    private fun markTree() {
        val locationManager:LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(locationManager?.hasProvider(LocationManager.GPS_PROVIDER) == true) {
            val providerProperties: ProviderProperties = locationManager.getProviderProperties(LocationManager.GPS_PROVIDER)!!
            val locationRequest = LocationRequest.Builder(60000L).build()

            val gnssCapabilities = locationManager?.gnssCapabilities
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val locationPermissionRequest = registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    when {
                        permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                            // Precise location access granted.
                            Log.i("TreeFragment", "Precise location granted")
                            locationManager?.registerGnssStatusCallback(ThreadPerTaskExecutor(), GnssCallback())
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                locationRequest, ThreadPerTaskExecutor(), this)
                        }
                        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                            // Only approximate location access granted.
                            Log.i("TreeFragment", "Coarse location granted")
                        } else -> {

                        }
                    }
                }
                locationPermissionRequest.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION))

            } else {
                locationManager?.registerGnssStatusCallback(ThreadPerTaskExecutor(), GnssCallback())
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    locationRequest, ThreadPerTaskExecutor(), this)
            }

            binding?.markTree?.setOnClickListener(View.OnClickListener {
                if(providerProperties?.accuracy == ProviderProperties.ACCURACY_FINE
                    && gnssCapabilities?.hasMeasurements() == true) {

                    if(location != null) {
                       val accuracy = location?.accuracy
                       Log.i("TreeFragment", "accuracy in meters " + accuracy.toString())
                       latitude = location?.latitude!!
                       longitude = location?.longitude!!
                    }
                }
            })
        }

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
        val obj = parent?.adapter?.getItem(position)
        if(obj is String) {
            val key = farmWithOrchardsMap?.filter { it == obj }?.keys?.first()
            this.orchardId = key!!
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

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

    inner class LocationConsumer: Consumer<Location> {
        override fun accept(t: Location) {
            location = t
        }
    }

    private fun saveTree() {
        if(tree != null && tree?.id!! > 0L) {
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
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            })
        }
    }

    override fun onClick(v: View?) {
        saveTree()
    }

    override fun onLocationChanged(location: Location) {
        this.location = location
    }

}