package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.VirtualWallLineListHolder
import com.sendi.deliveredrobot.model.LineInfoModel
import com.sendi.deliveredrobot.model.VirtualWallLineInfoModel

/**
 *   @author: heky
 *   @date: 2022/2/22 16:47
 *   @describe: 虚拟墙Line列表
 */
class VirtualWallLineListAdapter : RecyclerView.Adapter<VirtualWallLineListHolder>() {
    var data: List<LineInfoModel> = ArrayList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VirtualWallLineListHolder {
        return VirtualWallLineListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_virtual_wall_line_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VirtualWallLineListHolder, position: Int) {
        holder.data = data[position]
        holder.textViewDelete.setOnClickListener {
            listener.onDeleteButtonClick(data[position], position)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * @describe add
     */
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(element: LineInfoModel){
        (data as ArrayList).add(element)
        notifyDataSetChanged()
    }

    /**
     * @describe remove
     */
    @SuppressLint("NotifyDataSetChanged")
    fun removeItem(element:LineInfoModel){
        (data as ArrayList).remove(element)
        notifyDataSetChanged()
    }

    fun setItem(element: LineInfoModel){
        val list = (data as ArrayList)
        list[list.indexOf(element)] = element
    }

    private lateinit var listener: OnItemClickCallback

    interface OnItemClickCallback {
        fun onDeleteButtonClick(data: LineInfoModel, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickCallback) {
        this.listener = listener
    }
}