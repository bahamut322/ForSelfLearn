package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.SelectMapListHolder
import com.sendi.deliveredrobot.room.entity.MyRootMap

class SelectMapListAdapter() : RecyclerView.Adapter<SelectMapListHolder>() {
    lateinit var data: List<MyRootMap>

    constructor(data: List<MyRootMap>?) : this() {
        this.data = data ?: ArrayList(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    @JvmName("setData1")
    fun setData(data: List<MyRootMap>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectMapListHolder {
        return SelectMapListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_select_list, parent, false)
        )
    }

    override fun onBindViewHolder(holderMap: SelectMapListHolder, position: Int) {
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