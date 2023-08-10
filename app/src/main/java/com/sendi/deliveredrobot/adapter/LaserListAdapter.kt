package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.LaserListHolder
import com.sendi.deliveredrobot.room.entity.QuerySubMapEntity

/**
 *   @author: heky
 *   @date: 2021/9/3 16:47
 *   @describe: 激光图列表
 */
class LaserListAdapter : RecyclerView.Adapter<LaserListHolder>() {
    var data: List<QuerySubMapEntity> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    @JvmName("setData1")
    fun setData(data: List<QuerySubMapEntity>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaserListHolder {
        return LaserListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_laser_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LaserListHolder, position: Int) {
        holder.data = data[position]
        holder.frameLayoutContainer.setOnClickListener {
            listener.onItemClick(data[position], position)
        }
        holder.textViewExport.setOnClickListener {
            listener.onExportButtonClick(data[position], position)
        }
        holder.textViewImport.setOnClickListener {
            listener.onImportButtonClick(data[position], position)
        }
        holder.textViewDelete.setOnClickListener {
            listener.onDeleteButtonClick(data[position], position)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private lateinit var listener: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClick(data: QuerySubMapEntity, position: Int)
        fun onExportButtonClick(data: QuerySubMapEntity, position: Int)
        fun onImportButtonClick(data: QuerySubMapEntity, position: Int)
        fun onDeleteButtonClick(data: QuerySubMapEntity, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickCallback) {
        this.listener = listener
    }
}