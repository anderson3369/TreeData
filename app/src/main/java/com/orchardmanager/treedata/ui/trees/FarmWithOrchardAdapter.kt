package com.orchardmanager.treedata.ui.trees

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.entities.FarmWithOrchards


class FarmWithOrchardAdapter constructor(private val context: Activity,
                                         val farmWithOrchards: MutableList<FarmWithOrchards>):
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        if(!farmWithOrchards.isEmpty()) {
            return farmWithOrchards.size
        }
        return 0
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        if(!farmWithOrchards.isEmpty()) {
            return farmWithOrchards.get(groupPosition).orchards.size
        }
        return 0
    }

    override fun getGroup(groupPosition: Int): Any {
        return farmWithOrchards.get(groupPosition).farm
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return farmWithOrchards.get(groupPosition).orchards.get(childPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return farmWithOrchards.get(groupPosition).farm.id
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return farmWithOrchards.get(groupPosition).orchards.get(childPosition).id
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
        if(convertView != null && convertView is TextView) {
            convertView.setText(farmWithOrchards.get(groupPosition).farm.toString())
            return convertView
        } else {
            var convertView = convertView
            convertView = this.context.layoutInflater.inflate(R.layout.farm_group_textview, parent, false)
            val textView: TextView = convertView.findViewById(R.id.farmGroupView)
            textView.setText(farmWithOrchards.get(groupPosition).farm.toString())
            textView.setTextSize(24F)
            return  convertView
        }
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        if(convertView != null && convertView is TextView) {
            convertView.setText(farmWithOrchards.get(groupPosition).orchards.get(childPosition).toString())
            return convertView
        } else {
            var convertView = convertView
            convertView = this.context.layoutInflater.inflate(R.layout.orchard_child_textview, parent, false)
            val textView = convertView.findViewById<TextView>(R.id.orchardChildView)
            textView.setText(farmWithOrchards.get(groupPosition).orchards.get(childPosition).toString())
            textView.setTextSize(18F)
            return convertView
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}