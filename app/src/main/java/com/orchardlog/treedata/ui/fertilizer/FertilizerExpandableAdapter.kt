package com.orchardlog.treedata.ui.fertilizer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.orchardlog.treedata.R
import com.orchardlog.treedata.entities.FertilizerApplicationWithFertilizers

class FertilizerExpandableAdapter(
    private val fertilizerApplicationWithFertilizers: MutableList<FertilizerApplicationWithFertilizers>,
    private val context:Context
    ):
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return fertilizerApplicationWithFertilizers.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val pesticideAppWPesticide = fertilizerApplicationWithFertilizers.get(groupPosition)
        return pesticideAppWPesticide.fertilizers.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return fertilizerApplicationWithFertilizers.get(groupPosition)
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return fertilizerApplicationWithFertilizers.get(groupPosition).fertilizers.get(childPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return fertilizerApplicationWithFertilizers.get(groupPosition).fertilizerApplication.id
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return fertilizerApplicationWithFertilizers.get(groupPosition).fertilizers.get(childPosition).id
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
                .inflate(R.layout.fertilizer_group_view, parent, false)
            val fertAppView = view.findViewById<TextView>(R.id.fertilizerApplicationView)
            fertAppView.setText(fertilizerApplicationWithFertilizers.get(groupPosition)
                .fertilizerApplication.toString())
            return view
        } else {
            val fertAppView = convertView.findViewById<TextView>(R.id.fertilizerApplicationView)
            fertAppView.setText(fertilizerApplicationWithFertilizers.get(groupPosition)
                .fertilizerApplication.toString())
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
                .inflate(R.layout.fertilizer_child_view, parent, false)
            val fertNameView = view.findViewById<TextView>(R.id.fertilizerNameView)
            fertNameView.setText(fertilizerApplicationWithFertilizers.get(groupPosition).fertilizers
                .get(childPosition).toString())
            return view
        } else {
            val pestNameView = convertView.findViewById<TextView>(R.id.fertilizerNameView)
            pestNameView.setText(fertilizerApplicationWithFertilizers.get(groupPosition).fertilizers
                .get(childPosition).toString())
            return convertView
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

}