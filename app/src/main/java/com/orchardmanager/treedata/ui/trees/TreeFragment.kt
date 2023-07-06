package com.orchardmanager.treedata.ui.trees

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.location.provider.ProviderProperties
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.databinding.FragmentTreeBinding
import com.orchardmanager.treedata.entities.Rootstock
import com.orchardmanager.treedata.entities.Tree
import com.orchardmanager.treedata.entities.Variety
import com.orchardmanager.treedata.ui.orchard.OrchardViewModel
import com.orchardmanager.treedata.utils.DatePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

@AndroidEntryPoint
class TreeFragment : Fragment(), AdapterView.OnItemSelectedListener,
    View.OnClickListener, LocationListener {

    private var _binding: FragmentTreeBinding? = null
    private val binding get() = _binding
    private var orchardId: Long = 0L
    internal var location: Location? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var tree: Tree? = null
    private var rootstockId: Long = 0L
    private var varietyId: Long = 0L
    private var farmWithOrchardsMap: Map<Long, String>? = null
    private val plantedDateRequestKey = "plantedDateRequestKey"
    private val plantedDateKey = "plantedDateKey"


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
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.farmWithOrchardsSpinner?.adapter = adapter
            binding?.farmWithOrchardsSpinner?.onItemSelectedListener = this
        })

        treeViewModel?.getAllRootstocks()?.observe(viewLifecycleOwner, Observer {
            rootstocks ->
            val adapter = ArrayAdapter<Rootstock>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, rootstocks)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.rootstockSpinner?.adapter = adapter
            binding?.rootstockSpinner?.onItemSelectedListener = rootstockSelector()
        })

        treeViewModel?.getAllVarieties()?.observe(viewLifecycleOwner, Observer {
            varieties ->
            val adapter = ArrayAdapter<Variety>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, varieties)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.varietySpinner?.adapter = adapter
            binding?.varietySpinner?.onItemSelectedListener = varietySelector()
        })

        binding?.showTreePlantedDate?.setOnClickListener(View.OnClickListener {
            DatePickerFragment(plantedDateRequestKey, plantedDateKey).show(childFragmentManager, "Planted Date")
        })
        binding?.saveTree?.setOnClickListener(this)

        binding?.newTree?.setOnClickListener(View.OnClickListener {
            this.tree = null
            binding?.treePlantedDate?.setText("")
            binding?.treeNotes?.setText("")
        })

        binding?.deleteTree?.setOnClickListener(View.OnClickListener {
            if(this.tree != null)  {
                treeViewModel.delete(this.tree!!)
            } else {
                Toast.makeText(requireContext(), "Select a tree first", Toast.LENGTH_LONG)
            }
        })

        markTree()
        addRootstock()
        addVariety()

        return vw
    }

    inner class rootstockSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is Rootstock) {
                rootstockId = obj.id
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }

    inner class varietySelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is Variety) {
                varietyId = obj.id
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

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
        childFragmentManager.setFragmentResultListener(plantedDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.showTreePlantedDate?.setText(bundle.getString(plantedDateKey))
        }
        setFragmentResultListener(getString(R.string.treerequestkey)) {
            treeKey, bundle ->
            treeViewModel.getTree(bundle.getLong(getString(R.string.treekey))).observe(viewLifecycleOwner, Observer {
                tree ->
                this.tree = tree
                populateTree()
            })
        }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is String && !obj.isEmpty() && !farmWithOrchardsMap?.isEmpty()!!) {
            val keys = farmWithOrchardsMap?.filter { it == obj }?.keys
            if(keys != null && !keys.isEmpty()) {
                this.orchardId = keys.first()
            }

        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun populateTree() {
        if(this.tree != null) {
            this.orchardId = this.tree?.orchardId!!
            this.rootstockId = this.tree?.rootstockId!!
            this.varietyId = this.tree?.varietyId!!
            binding?.treePlantedDate?.setText(DateConverter().fromOffsetDate(this.tree?.plantedDate!!))
            binding?.treeNotes?.setText(this.tree?.notes)
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


    private fun saveTree() {
        if(this.orchardId == 0L) {
            Toast.makeText(requireContext(), "Please select an Orchard", Toast.LENGTH_LONG).show()
            return
        }
        if(tree != null && tree?.id!! > 0L) {
            //update
            val plantDate = binding?.treePlantedDate?.text.toString()
            val date = DateConverter().toOffsetDate(plantDate)
            val tree2 = tree?.copy(
                orchardId = orchardId,
                rootstockId = rootstockId,
                varietyId = varietyId,
                plantedDate = date!!,
                notes = binding?.treeNotes?.text.toString(),
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
                notes = binding?.treeNotes?.text.toString(),
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