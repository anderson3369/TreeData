package com.orchardlog.treedata.ui.trees

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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.orchardlog.treedata.R
import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.databinding.FragmentTreeBinding
import com.orchardlog.treedata.entities.Rootstock
import com.orchardlog.treedata.entities.Tree
import com.orchardlog.treedata.entities.TreeRanking
import com.orchardlog.treedata.entities.Variety
import com.orchardlog.treedata.ui.orchard.OrchardViewModel
import com.orchardlog.treedata.utils.DatePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
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
    private var treeRanking: TreeRanking? = null
    private var farmWithOrchardsMap: Map<Long, String>? = null
    private val treeRankingArray = arrayOf(TreeRanking.EXCELLENT, TreeRanking.GOOD,
        TreeRanking.MODERATE, TreeRanking.POOR, TreeRanking.DYING)
    private var treeRankingDescriptionArray: Array<String>? = null

    companion object {
        const val plantedDateRequestKey = "plantedDateRequestKey"
        const val plantedDateKey = "plantedDateKey"
        const val TAG = "TreeFragment"
    }

    private val treeViewModel: TreeViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTreeBinding.inflate(inflater, container, false)
        val vw = binding?.root

        treeRankingDescriptionArray = resources.getStringArray(R.array.tree_ranking_descriptions)

        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner) {
            farmWithOrchards ->
            this.farmWithOrchardsMap = farmWithOrchards
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.farmWithOrchardsSpinner?.adapter = adapter
            binding?.farmWithOrchardsSpinner?.onItemSelectedListener = this
            val keys = farmWithOrchards.keys
            if(keys.size == 1) {
                this.orchardId = keys.first()
            }
        }

        treeViewModel.getAllRootstocks().observe(viewLifecycleOwner) {
            rootstocks ->
            val adapter = ArrayAdapter<Rootstock>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, rootstocks)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.rootstockSpinner?.adapter = adapter
            binding?.rootstockSpinner?.onItemSelectedListener = rootstockSelector()
        }

        treeViewModel.getAllVarieties().observe(viewLifecycleOwner) {
            varieties ->
            val adapter = ArrayAdapter<Variety>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, varieties)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.varietySpinner?.adapter = adapter
            binding?.varietySpinner?.onItemSelectedListener = varietySelector()
        }

        val treeRankingAdapter = ArrayAdapter<TreeRanking>(requireContext(), R.layout.farm_spinner_layout,
        R.id.textViewFarmSpinner, treeRankingArray)
        treeRankingAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.treeRanking?.adapter = treeRankingAdapter
        binding?.treeRanking?.onItemSelectedListener = treeRankingSelector()

        binding?.treeRankingInfo?.setOnClickListener {
            val window = ListPopupWindow(requireContext())
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, treeRankingDescriptionArray!!)
            window.anchorView = binding?.treeRankingInfo
            window.height = ListPopupWindow.WRAP_CONTENT
            window.width = view?.measuredWidth!!
            window.setDropDownGravity(Gravity.TOP)
            window.setAdapter(adapter)
            window.show()
        }

        binding?.showTreePlantedDate?.setOnClickListener {
            DatePickerFragment(plantedDateRequestKey, plantedDateKey).show(childFragmentManager,
                getString(R.string.planted_date))
        }
        binding?.saveTree?.setOnClickListener(this)

        binding?.newTree?.setOnClickListener {
            this.tree = null
            binding?.treePlantedDate?.setText("")
            binding?.treeNotes?.setText("")
        }

        binding?.deleteTree?.setOnClickListener {
            if(this.tree != null)  {
                treeViewModel.delete(this.tree!!)
            } else {
                Toast.makeText(requireContext(), getString(R.string.select_a_tree_first),
                    Toast.LENGTH_LONG).show()
            }
        }

        markTree()
        addRootstock()
        addVariety()

        return vw
    }

    inner class treeRankingSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is TreeRanking) {
                this@TreeFragment.treeRanking = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

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
        binding?.addRootstock?.setOnClickListener {
            val action = TreeFragmentDirections.actionNavTreeToNavRootstock()
            view?.findNavController()?.navigate(action)
        }
    }

    private fun addVariety() {
        binding?.addVariety?.setOnClickListener {
            val action = TreeFragmentDirections.actionNavTreeToNavVariety()
            view?.findNavController()?.navigate(action)
        }
    }

    private fun markTree() {
        val locationManager:LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(locationManager.hasProvider(LocationManager.GPS_PROVIDER) == true) {
            val providerProperties: ProviderProperties = locationManager.getProviderProperties(LocationManager.GPS_PROVIDER)!!
            val locationRequest = LocationRequest.Builder(60000L).build()

            val gnssCapabilities = locationManager.gnssCapabilities
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
                            locationManager.registerGnssStatusCallback(ThreadPerTaskExecutor(), GnssCallback())
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                locationRequest, ThreadPerTaskExecutor(), this)
                        }
                        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                            // Only approximate location access granted.
                        } else -> {

                        }
                    }
                }
                locationPermissionRequest.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION))

            } else {
                locationManager.registerGnssStatusCallback(ThreadPerTaskExecutor(), GnssCallback())
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    locationRequest, ThreadPerTaskExecutor(), this)
            }

            binding?.markTree?.setOnClickListener {
                if(providerProperties.accuracy == ProviderProperties.ACCURACY_FINE
                    && gnssCapabilities.hasMeasurements()) {

                    if(location != null) {
                       //val accuracy = location?.accuracy
                       latitude = location?.latitude!!
                       longitude = location?.longitude!!
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(plantedDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.showTreePlantedDate?.setText(bundle.getString(plantedDateKey))
        }
        setFragmentResultListener(getString(R.string.treerequestkey)) {
            treeKey, bundle ->
            treeViewModel.getTree(bundle.getLong(getString(R.string.treekey))).observe(viewLifecycleOwner) {
                tree ->
                this.tree = tree
                populateTree()
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is String && !obj.isEmpty() && !farmWithOrchardsMap?.isEmpty()!!) {
            val keys = farmWithOrchardsMap?.filter { it.value == obj }?.keys
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
            Log.i(TAG, "number of satellites" + status.satelliteCount)
        }
    }


    private fun saveTree() {
        if(this.orchardId == 0L) {
            Toast.makeText(requireContext(), getString(R.string.please_select_an_orchard), Toast.LENGTH_LONG).show()
            return
        }
        //val plantDate = binding?.treePlantedDate?.text.toString()
        val date: LocalDate?
        try {
            date = DateConverter().toOffsetDate(binding?.treePlantedDate?.text.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Check the date format", Toast.LENGTH_LONG).show()
            return
        }
        if(tree != null && tree?.id!! > 0L) {
            val tree2 = tree?.copy(
                orchardId = orchardId,
                rootstockId = rootstockId,
                varietyId = varietyId,
                plantedDate = date!!,
                treeRanking = this.treeRanking!!,
                notes = binding?.treeNotes?.text.toString(),
                latitude = latitude,
                longitude = longitude
            )
            treeViewModel.update(tree2!!)
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
        } else {
            tree = Tree(
                orchardId = orchardId,
                rootstockId = rootstockId,
                varietyId = varietyId,
                plantedDate = date!!,
                treeRanking = this.treeRanking!!,
                notes = binding?.treeNotes?.text.toString(),
                latitude = latitude,
                longitude = longitude
            )
            treeViewModel.add(tree!!).observe(this) {
                    id ->
                Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(v: View?) {
        saveTree()
    }

    override fun onLocationChanged(location: Location) {
        this.location = location
    }

}