package com.sendi.deliveredrobot.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.MyResultModel


class ChangePointGridViewAdapter(private val context: Context, private val items: List<MyResultModel?>?,private val nameString: String) : BaseAdapter() {

    override fun getCount(): Int {
        return items!!.size
    }

    override fun getItem(position: Int): Any {
        return items!![position]!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_changing_point, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        holder.itemTextView.text = items?.get(position)!!.name
        if (holder.itemTextView.text == nameString){
            holder.itemTextView.isEnabled = false;
            holder.itemTextView.isClickable = false
            holder.itemTextView.setBackgroundResource(R.drawable.bg_button_1)
            holder.itemCon.isEnabled = false
            holder.itemCon.isClickable = false
        }
        return view!!
    }

    private class ViewHolder(view: View) {
        val itemTextView: TextView = view.findViewById(R.id.pointName)
        val itemCon : ConstraintLayout = view.findViewById(R.id.pointCatalogue)
    }

}//按钮禁用失败，怎么解决