package com.sendi.deliveredrobot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.SelectChargePointListHolder
import com.sendi.deliveredrobot.room.entity.QueryPointEntity

/**
 * @describe 选择充电点
 */
class SelectChargePointListAdapter() : RecyclerView.Adapter<SelectChargePointListHolder>() {
    lateinit var data:List<QueryPointEntity>
    constructor(data: List<QueryPointEntity>?) : this() {
        this.data = data?:ArrayList(0)
    }

    @JvmName("setData1")
    fun setData(data: List<QueryPointEntity>?) {
        this.data = data?: ArrayList(0)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectChargePointListHolder {
        return SelectChargePointListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_select_list, parent, false)
        )
    }

    override fun onBindViewHolder(holderMap: SelectChargePointListHolder, position: Int) {
        holderMap.data = data[position]
        holderMap.frameLayoutContainer.setOnClickListener {
            listener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private lateinit var listener: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickCallback) {
        this.listener = listener
    }
}