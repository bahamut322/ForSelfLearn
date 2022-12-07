package com.sendi.deliveredrobot.adapter

import android.content.Context
import android.widget.RelativeLayout
import android.widget.TextView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.BaseRecyclerHolder
import com.sendi.deliveredrobot.room.entity.Point

class TargetPointAdapter: BaseRecyclerViewAdapter<Point>{

    var isEditIng = false

    constructor(context: Context, data: List<Point>?): super(context, data) {

    }

    override fun bindView(holder: BaseRecyclerHolder, position: Int) {
        val rlContainer: RelativeLayout = holder.getView(R.id.rlContainer)
        var tvName: TextView  = holder.getView(R.id.tvName)


        var data = datas[position]
        tvName.apply {
            text = data.name
        }


        rlContainer.setOnClickListener {
            listener.onItemClick(datas[position], position)
        }

        rlContainer.setOnLongClickListener {
            listener.onItemLongClick(datas[position], position)
            return@setOnLongClickListener true
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.item_target_point_list
    }

    private lateinit var listener: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClick(data: Point, position: Int)
        fun onItemLongClick(data: Point, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickCallback) {
        this.listener = listener
    }

}