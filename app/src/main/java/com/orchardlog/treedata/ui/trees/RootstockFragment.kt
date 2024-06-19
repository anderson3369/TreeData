package com.orchardlog.treedata.ui.trees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.orchardlog.treedata.R
import com.orchardlog.treedata.data.EnumConverter
import com.orchardlog.treedata.databinding.FragmentRootstockBinding
import com.orchardlog.treedata.entities.Rootstock
import com.orchardlog.treedata.entities.RootstockType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RootstockFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val treeViewModel: TreeViewModel by viewModels()
    private var _binding: FragmentRootstockBinding? = null
    private val binding get() = _binding
    private var rootstockId = -1L
    private var rootstock: Rootstock? = null
    private var rootstockType: RootstockType? = null
    private val rootstocktypes = arrayOf(RootstockType.BAREROOT.toString(), RootstockType.POTTED.toString())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRootstockBinding.inflate(inflater, container, false)
        val vw = binding?.root
        treeViewModel.getAllRootstocks().observe(viewLifecycleOwner) {
            rootstocks ->
            val adapter = ArrayAdapter(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, rootstocks)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.rootstock?.adapter = adapter
            binding?.rootstock?.onItemSelectedListener = this
        }
        val rootstockAdapter = ArrayAdapter(requireContext(),
            R.layout.farm_spinner_layout, R.id.textViewFarmSpinner,rootstocktypes
        )
        rootstockAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)

        binding?.rootstockType?.adapter = rootstockAdapter
        binding?.rootstockType?.onItemSelectedListener = RootStockTypeSelector()

        saveOnClick()
        newOnClick()
        deleteOnClick()

        return vw
    }

    inner class RootStockTypeSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is String) {
                this@RootstockFragment.rootstockType = EnumConverter().toRootstockType(obj)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    private fun saveOnClick() {
        binding?.saveRootstock?.setOnClickListener {
            if(rootstock !=null && rootstock?.id!! > -1L) {
                val stock = this.rootstock?.copy(
                    id = rootstockId,
                    name = binding?.rootstockName?.text.toString(),
                    cultivar = binding?.rootstockCultivar?.text.toString(),
                    rootstockType = this.rootstockType!!
                )
                treeViewModel.update(stock!!)
                Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
            } else {
                this.rootstock = Rootstock(
                    name = binding?.rootstockName?.text.toString(),
                    cultivar = binding?.rootstockCultivar?.text.toString(),
                    rootstockType = this.rootstockType!!
                )
                treeViewModel.add(rootstock!!).observe(viewLifecycleOwner) { _ ->
                    Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun newOnClick() {
        binding?.newRootstock?.setOnClickListener {
            this.rootstock = null
            binding?.rootstockName?.setText("")
            binding?.rootstockCultivar?.setText("")
        }

    }

    private fun deleteOnClick() {
        binding?.deleteRootstock?.setOnClickListener {
            if(rootstock != null) {
                treeViewModel.delete(rootstock!!)
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val stock = parent?.adapter?.getItem(position)
        if(stock is Rootstock) {
            this.rootstock = stock
            binding?.rootstockName?.setText(rootstock!!.name)
            binding?.rootstockCultivar?.setText(rootstock!!.cultivar)
            binding?.rootstockType?.setSelection(rootstocktypes.indexOf(rootstock!!.rootstockType.toString()))
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}