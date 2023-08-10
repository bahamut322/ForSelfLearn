package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.SelectReadyPointListHolder
import com.sendi.deliveredrobot.room.entity.QueryPointEntity

/**
 * @Author Swn
 * @describe 选择待命点
 * @Data 2023-04-24 16:59
 */
class SelectReadyPointListAdapter() : RecyclerView.Adapter<SelectReadyPointListHolder>() {
    lateinit var data:ArrayList<QueryPointEntity>
    constructor(data: ArrayList<QueryPointEntity>?) : this() {
        this.data = data?:ArrayList(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    @JvmName("setData1")
    fun setData(data: ArrayList<QueryPointEntity>?) {
        this.data = data?: ArrayList(0)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(index: Int, item: QueryPointEntity){
        this.data.add(index, item)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeData(index: Int){
        this.data.removeAt(index)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectReadyPointListHolder {
        return SelectReadyPointListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_select_list, parent, false)
        )
    }

    override fun onBindViewHolder(holderMap: SelectReadyPointListHolder, position: Int) {
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