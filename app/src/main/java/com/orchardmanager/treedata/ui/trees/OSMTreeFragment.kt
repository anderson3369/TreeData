package com.orchardmanager.treedata.ui.trees

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.orchardmanager.treedata.R

class OSMTreeFragment : Fragment() {

    companion object {
        fun newInstance() = OSMTreeFragment()
    }

    //private lateinit var viewModel: OSMTreeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_o_s_m_tree, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(OSMTreeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}