package com.orchardmanager.treedata.ui.trees

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.orchardmanager.treedata.R

class FarmWithOrchardsFragment : Fragment() {

    private val treeViewModel: TreeViewModel by viewModels()

    companion object {
        fun newInstance() = FarmWithOrchardsFragment()
    }

    //private lateinit var viewModel: FarmWithOrchardsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_farm_with_orchards, container, false)
    }


}