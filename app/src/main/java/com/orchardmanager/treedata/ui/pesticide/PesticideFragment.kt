package com.orchardmanager.treedata.ui.pesticide

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.databinding.FragmentPesticideBinding
import com.orchardmanager.treedata.entities.Pesticide
import com.orchardmanager.treedata.entities.REIUnit
import com.orchardmanager.treedata.entities.SignalWord
import com.orchardmanager.treedata.ui.SAVE_FAILED
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PesticideFragment : Fragment(), AdapterView.OnItemSelectedListener,
    View.OnClickListener {

    private val pesticideViewModel: PesticideViewModel by viewModels()
    private var _binding: FragmentPesticideBinding? = null
    private val binding get() = _binding
    private var pesticide: Pesticide? = null
    private var signalWord: SignalWord? = null
    private var reiUnit: REIUnit? = null
    val signalWordArray = arrayOf(SignalWord.CAUTION, SignalWord.WARNING, SignalWord.DANGER)

    companion object {
        fun newInstance() = PesticideFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPesticideBinding.inflate(inflater, container, false)
        val vw = binding?.root
        pesticideViewModel.getPesticides().observe(viewLifecycleOwner, Observer {
            pesticides ->
            val adapter = ArrayAdapter<Pesticide>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, pesticides)
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.pesticides?.adapter = adapter
            binding?.pesticides?.onItemSelectedListener = this
        })


        val swAdapter = ArrayAdapter<SignalWord>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, signalWordArray)
        swAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.signalWord?.adapter = swAdapter
        binding?.signalWord?.onItemSelectedListener = signalWordSelector()

        val reiArray = arrayOf(REIUnit.HOUR, REIUnit.DAY)
        val reiAdapter = ArrayAdapter<REIUnit>(requireContext(), R.layout.farm_spinner_layout,
            R.id.textViewFarmSpinner, reiArray)
        reiAdapter.setDropDownViewResource(R.layout.farm_spinner_layout)
        binding?.reiUnit?.adapter = reiAdapter
        binding?.reiUnit?.onItemSelectedListener = reiUnitSelector()

        binding?.savePesticide?.setOnClickListener(this)

        binding?.newPesticide?.setOnClickListener(View.OnClickListener {
            this.pesticide = null
            binding?.pesticideName?.setText("")
            binding?.rei?.setText("")
        })

        binding?.deletePesticide?.setOnClickListener(View.OnClickListener {
            pesticideViewModel?.deletePesticide(pesticide!!)
        })

        return vw
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(PesticideViewModel::class.java)
        // TODO: Use the ViewModel
    }

    inner class reiUnitSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is REIUnit) {
                this@PesticideFragment.reiUnit = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    inner class signalWordSelector: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val obj = parent?.adapter?.getItem(position)
            if(obj is SignalWord) {
                this@PesticideFragment.signalWord = obj
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is Pesticide) {
            this.pesticide = obj
            binding?.pesticideName?.setText(obj.productName)
            binding?.eparegno?.setText(obj.eparegno)
            binding?.signalWord?.setSelection(signalWordArray.indexOf(obj.signalWord))
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onClick(v: View?) {
        val srei = binding?.rei?.text.toString()
        var rei: Int = 0
        if(!srei.isEmpty()) {
            rei = srei.toInt()
        }
        if(this.pesticide != null && pesticide?.id!! > 0) {
            val pst = pesticide?.copy(
                id = pesticide!!.id,
                productName = binding?.pesticideName?.text.toString(),
                eparegno = binding?.eparegno?.text.toString(),
                signalWord = signalWord!!,
                rei = rei,
                reiUnit = reiUnit!!
            )
            try {
                pesticideViewModel.updatePesticide(pst!!)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
            }
        } else {
            val pesticide = Pesticide(
                productName = binding?.pesticideName?.text.toString(),
                eparegno = binding?.eparegno?.text.toString(),
                signalWord = signalWord!!,
                rei = rei,
                reiUnit = reiUnit!!
            )
            pesticideViewModel.addPesticide(pesticide).observe(this, Observer {
                id ->
                if(!id.equals(SAVE_FAILED)) {
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

}