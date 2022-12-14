package com.orchardmanager.treedata.ui.orchard

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.entities.Farm
import com.orchardmanager.treedata.entities.FarmerWithFarm
import com.orchardmanager.treedata.ui.farm.FarmViewModel
import dagger.hilt.android.AndroidEntryPoint


/**
 * A simple [Fragment] subclass.
 * Use the [FarmDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class FarmDialogFragment constructor(val farms: MutableList<Farm>) : DialogFragment() {

    private val farmViewModel: FarmViewModel by viewModels()
    private var farmId: Long? = 0L

    override fun onCreateDialog(savedInstance: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Select a Farm")
            val farm1 = Farm(
                    farmerId = 10L,
                    name = "Ranch",
                    siteId = "1A"
            )

            val adapter = ArrayAdapter(requireContext(),R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, farms)
            var pos: Int = 0

            builder.setSingleChoiceItems(adapter, pos, DialogInterface.OnClickListener {
                    dialog, id ->
                farmId = farms.get(pos).id
                setFragmentResult("requestKey", bundleOf("farmId" to farmId))
            })
            builder.setPositiveButton(R.string.select_farm, DialogInterface.OnClickListener { dialog, pos ->
                if(farmId == 0L) {
                    builder.setMessage("Please Select a Farm")
                }
            })
            builder.create()
        }!!
    }


}