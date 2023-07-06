package com.orchardmanager.treedata.ui.irrigation

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.databinding.GridLayoutBinding
import com.orchardmanager.treedata.entities.Irrigation

class IrrigationListAdapter(private val context: Context, private val irrigations: MutableList<Irrigation>):
    BaseAdapter() {

    override fun getCount(): Int {
        return irrigations.size
    }

    override fun getItem(position: Int): Any {
        return irrigations.get(position)
    }

    override fun getItemId(position: Int): Long {
        return irrigations.get(position).id
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = null
        Log.i("IrrigationListAdapter", "the item is " + irrigations.get(position).toString())
        if(convertView == null) {
            val binding = GridLayoutBinding.inflate(LayoutInflater.from(parent?.context),parent, false)
            view = binding.root
            val gridId = view?.findViewById<TextView>(R.id.irrigationId)
            gridId?.text = irrigations.get(position).toString()
            val gridSystemId = view?.findViewById<TextView>(R.id.irrigationSystem)
            gridSystemId?.text = irrigations.get(position).toString()
            val gridStart = view?.findViewById<TextView>(R.id.irrigationStart)
            gridStart?.text = irrigations.get(position).toString()
            return view
        } else {
            val gridId = convertView?.findViewById<TextView>(R.id.irrigationId)
            gridId?.text = irrigations.get(position).toString()
            val gridSystemId = convertView?.findViewById<TextView>(R.id.irrigationId)
            gridSystemId?.text = irrigations.get(position).toString()
            val gridStart = convertView?.findViewById<TextView>(R.id.irrigationStart)
            gridStart?.text = irrigations.get(position).toString()
        }
        return convertView!!
    }


}