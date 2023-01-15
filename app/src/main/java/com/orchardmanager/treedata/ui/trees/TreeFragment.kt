package com.orchardmanager.treedata.ui.trees

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.orchardmanager.treedata.R

class TreeFragment : Fragment() {

    companion object {
        fun newInstance() = TreeFragment()
    }

    private lateinit var viewModel: TreeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tree, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TreeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}