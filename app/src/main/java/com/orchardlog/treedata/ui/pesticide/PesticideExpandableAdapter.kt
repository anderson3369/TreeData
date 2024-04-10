package com.orchardlog.treedata.ui.pesticide

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.orchardlog.treedata.R
import com.orchardlog.treedata.entities.PesticideApplicationWithPesticides

class PesticideExpandableAdapter(
    private val pesticideApplicationWithPesticides: MutableList<PesticideApplicationWithPesticides>,
    private val context:Context
    ):
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return pesticideApplicationWithPesticides.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val pesticideAppWPesticide = pesticideApplicationWithPesticides.get(groupPosition)
        return pesticideAppWPesticide.pesticides.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return pesticideApplicationWithPesticides.get(groupPosition)
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return pesticideApplicationWithPesticides.get(groupPosition).pesticides.get(childPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return pesticideApplicationWithPesticides.get(groupPosition).pesticideApplication.id
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return pesticideApplicationWithPesticides.get(groupPosition).pesticides.get(childPosition).id
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        if(convertView == null) {
            val view = LayoutInflater.from(this.context)
                .inflate(R.layout.pesticide_group_view, parent, false)
            val pestAppView = view.findViewById<TextView>(R.id.pesticideApplicationView)
            pestAppView.setText(pesticideApplicationWithPesticides.get(groupPosition)
                .pesticideApplication.toString())
            return view
        } else {
            val pestAppView = convertView.findViewById<TextView>(R.id.pesticideApplicationView)
            pestAppView.setText(pesticideApplicationWithPesticides.get(groupPosition)
                .pesticideApplication.toString())
            return convertView
        }
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        if(convertView == null) {
            val view = LayoutInflater.from(this.context)
                .inflate(R.layout.pesticide_child_view, parent, false)
            val pestNameView = view.findViewById<TextView>(R.id.pesticideNameView)
            pestNameView.setText(pesticideApplicationWithPesticides.get(groupPosition).pesticides
                .get(childPosition).toString())
            return view
        } else {
            val pestNameView = convertView.findViewById<TextView>(R.id.pesticideNameView)
            pestNameView.setText(pesticideApplicationWithPesticides.get(groupPosition).pesticides
                .get(childPosition).toString())
            return convertView
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

}