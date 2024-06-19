package com.orchardlog.treedata.ui.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.orchardlog.treedata.databinding.FragmentVersionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VersionFragment : Fragment() {

    private var fbinding: FragmentVersionBinding? = null
    val binding get() = fbinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentVersionBinding.inflate(inflater, container, false)
        val vw = binding?.root
        binding?.checkUpdates?.setOnClickListener {
            val appUpdateManager = AppUpdateManagerFactory.create(requireContext())

// Returns an intent object that you use to check for an update.
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            val appUpdateOptions = AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)

// Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    Log.i("VersionFragment", "Update Available")

                    appUpdateManager.startUpdateFlow(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        requireActivity(),
                        appUpdateOptions)
                } else {
                    Log.i("VersionFragment", "No update available")
                }
            }
        }

        binding?.license?.setOnClickListener{
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
        }

        return vw
    }



}