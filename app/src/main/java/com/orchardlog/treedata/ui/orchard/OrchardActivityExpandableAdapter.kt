package com.orchardlog.treedata.ui.orchard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.orchardlog.treedata.R
import com.orchardlog.treedata.entities.OrchardWithOrchardActivity

class OrchardActivityExpandableAdapter(
    private val orchardWithOrchardActivity: MutableList<OrchardWithOrchardActivity>,
    private val context:Context
    ):
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return orchardWithOrchardActivity.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val orchardWActivity = orchardWithOrchardActivity.get(groupPosition)
        return orchardWActivity.orchardActivities.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return orchardWithOrchardActivity.get(groupPosition)
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return orchardWithOrchardActivity.get(groupPosition).orchardActivities.get(childPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return orchardWithOrchardActivity.get(groupPosition).orchard.id
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return orchardWithOrchardActivity.get(groupPosition).orchardActivities.get(childPosition).id
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
                .inflate(R.layout.orchard_group_view, parent, false)
            val orchardView = view.findViewById<TextView>(R.id.orchardView)
            orchardView.setText(orchardWithOrchardActivity.get(groupPosition)
                .orchardActivities.toString())
            return view
        } else {
            val orchardView = convertView.findViewById<TextView>(R.id.orchardView)
            orchardView.setText(orchardWithOrchardActivity.get(groupPosition)
                .orchard.toString())
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
                .inflate(R.layout.orchard_child_view, parent, false)
            val orchardView = view.findViewById<TextView>(R.id.orchardActivityView)
            orchardView.setText(orchardWithOrchardActivity.get(groupPosition).orchardActivities
                .get(childPosition).toString())
            return view
        } else {
            val orchardView = convertView.findViewById<TextView>(R.id.orchardActivityView)
            orchardView.setText(orchardWithOrchardActivity.get(groupPosition).orchardActivities
                .get(childPosition).toString())
            return convertView
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

}