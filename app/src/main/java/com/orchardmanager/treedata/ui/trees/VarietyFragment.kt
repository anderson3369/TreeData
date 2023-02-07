package com.orchardmanager.treedata.ui.trees

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.databinding.FragmentVarietyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VarietyFragment : Fragment() {

    private val treeViewModel: TreeViewModel by viewModels()
    private var _binding: FragmentVarietyBinding? = null
    private val binding get() = _binding

    companion object {
        fun newInstance() = VarietyFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVarietyBinding.inflate(inflater, container, false)
        val vw = binding?.root

        return vw
        //return inflater.inflate(R.layout.fragment_variety, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(VarietyViewModel::class.java)
        // TODO: Use the ViewModel
    }

}