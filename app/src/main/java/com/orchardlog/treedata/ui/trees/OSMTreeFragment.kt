package com.orchardlog.treedata.ui.trees

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.orchardlog.treedata.R
import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.databinding.FragmentOSMTreeBinding
import com.orchardlog.treedata.entities.Tree
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.wms.BuildConfig
import org.osmdroid.wms.WMSEndpoint
import org.osmdroid.wms.WMSParser
import org.osmdroid.wms.WMSTileSource
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import kotlin.concurrent.thread


@AndroidEntryPoint
class OSMTreeFragment : Fragment(), LocationListener {

    //private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    //private lateinit var mapView : MapView
    private val treeViewModel: TreeViewModel by viewModels()
    private var _binding: FragmentOSMTreeBinding? = null
    private val binding get() = _binding
    private var rootstocks: Map<Long, String>? = null
    private var varieties: Map<Long, String>? = null
    private var endPoint: WMSEndpoint? = null
    private var location: Location? = null
    private var trees: MutableList<Tree>? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //private val wms = "https://basemap.nationalmap.gov:443/arcgis/services/USGSImageryOnly/MapServer/WmsServer?version=1.3.0&request=GetCapabilities&service=WMS"

    companion object {
        fun newInstance() = OSMTreeFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOSMTreeBinding.inflate(inflater, container, false)
        val vw = binding?.root

        treeViewModel.getRootstocksMap().observe(viewLifecycleOwner) {
            rootstocks ->
            this.rootstocks = rootstocks
        }

        treeViewModel.getVarietiesMap().observe(viewLifecycleOwner) {
            varieties ->
            this.varieties = varieties
        }

        //treeViewModel.getAllOrchardWithTrees().observe(viewLifecycleOwner) {
        //    orchardWithTrees ->
        //    val orchTreesIter = orchardWithTrees.iterator()
        //    while (orchTreesIter.hasNext()) {
        //        val orchWTree = orchTreesIter.next()
        //        val listTrees = orchWTree.trees
        //    }
        //}

        treeViewModel.getAllTrees().observe(viewLifecycleOwner) {
            trees ->
            this.trees = trees
            if(trees.isNotEmpty()) {
                val treeIter = trees.iterator()
                while(treeIter.hasNext()) {
                    val tree = treeIter.next()
                    val marker = Marker(binding?.map)
                    marker.position = GeoPoint(tree.latitude, tree.longitude)
                    marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.baseline_edit_location_24, null)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = tree.id.toString()
                    var variety = ""
                    if(varieties != null  && !varieties?.isEmpty()!!) {
                        variety = varieties!![tree.varietyId]!!
                    }
                    var rootstock = ""
                    if(rootstocks != null && !rootstocks?.isEmpty()!!) {
                        rootstock = rootstocks!![tree.rootstockId]!!
                    }
                    marker.snippet = "$variety on $rootstock"
                    marker.subDescription = "Planted on " + DateConverter().fromOffsetDate(tree.plantedDate)

                    marker.setOnMarkerClickListener(Marker.OnMarkerClickListener {
                       marker, mapView ->
                        val title = marker.title
                        if(title != null) {
                            setFragmentResult(getString(R.string.treerequestkey), bundleOf(getString(
                                                            R.string.treekey) to title.toLong()))
                        }
                        val action = OSMTreeFragmentDirections.actionNavMapToNavTree()
                        mapView?.findNavController()?.navigate(action)
                        return@OnMarkerClickListener true
                    })

                    binding?.map?.overlayManager?.add(marker)
                }
            }
        }

        Configuration.getInstance().userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME

        initLocation()
        parseWMS()

        binding?.map?.controller?.setZoom(18.5)
        //binding?.map?.zoomController?.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        binding?.map?.zoomController?.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)

        copyright()

        binding?.newTree?.setOnClickListener {
            val action = OSMTreeFragmentDirections.actionNavMapToNavTree()
            view?.findNavController()?.navigate(action)
        }

        return vw
    }

    private fun copyright() {
        val overlay = CopyrightOverlay(parentFragment?.requireContext())
        //val width = 300
        //val height = 300
        //val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        //val canvas = Canvas(bitmap)
        //overlay.setCopyrightNotice("© OpenStreetMap")
        overlay.setAlignRight(true)
        overlay.setAlignBottom(true)
        //overlay.draw(canvas, binding?.map, false)
        binding?.map?.overlayManager?.add(overlay)
    }

    private fun initLocation() {
        val locationManager: LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE)
                as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if(locationManager.hasProvider(LocationManager.GPS_PROVIDER)) {
            //val providerProperties: ProviderProperties = locationManager.getProviderProperties(
            //    LocationManager.GPS_PROVIDER)!!
            //val locationRequest = LocationRequest.Builder(60000L).build()

            //val gnssCapabilities = locationManager?.gnssCapabilities
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
                            //Log.i("TreeFragment", "Precise location granted")
                            locationManager.registerGnssStatusCallback(ThreadPerTaskExecutor(), GnssCallback())
                            //locationManager.requestLocationUpdates(
                               // LocationManager.GPS_PROVIDER,
                               // locationRequest, ThreadPerTaskExecutor(), this)
                        }
                        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                            // Only approximate location access granted.
                            //Log.i("TreeFragment", "Coarse location granted")
                        } else -> {

                    }
                    }
                }
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION))
                zoomToLocation()

            } else {
                locationManager.registerGnssStatusCallback(ThreadPerTaskExecutor(), GnssCallback())
                zoomToLocation()
                //locationManager.requestLocationUpdates(
                    //LocationManager.GPS_PROVIDER,
                    //locationRequest, ThreadPerTaskExecutor(), this)
            }

        }

    }

    @SuppressLint("MissingPermission")
    private fun zoomToLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener {
                location : Location? ->
            if(location != null) {
                this.location = location
                val center: IGeoPoint = GeoPoint(location)
                binding?.map?.controller?.animateTo(center)
            }
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


    private fun parseWMS() {
        val url = "https://basemap.nationalmap.gov:443/arcgis/services/USGSImageryOnly/MapServer/WmsServer?version=1.3.0&request=GetCapabilities&service=WMS"
        thread(start = true) {
            try {
                var conn: HttpURLConnection? = null
                var inputStream: InputStream? = null
                try {
                    conn = URL(url).openConnection() as HttpURLConnection?
                    inputStream = conn?.inputStream
                    endPoint = WMSParser.parse(inputStream)
                    if(endPoint != null) {
                        val wmsLayer = endPoint!!.layers.first()
                        val bbox = wmsLayer.bbox
                        binding?.map?.zoomToBoundingBox(bbox, false)

                        val source = WMSTileSource.createFrom(endPoint, wmsLayer)
                        binding?.map?.setTileSource(source)
                    }

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    inputStream?.close()
                    conn?.disconnect()
                } finally {
                    inputStream?.close()
                    conn?.disconnect()
                }
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        this.location = location
        val center: IGeoPoint = GeoPoint(location.latitude, location.longitude)
        binding?.map?.controller?.animateTo(center)
    }

}