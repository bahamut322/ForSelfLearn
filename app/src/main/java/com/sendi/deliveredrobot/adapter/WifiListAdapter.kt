package com.sendi.deliveredrobot.adapter

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.WifiListHolder

class WifiListAdapter() : RecyclerView.Adapter<WifiListHolder>() {
    private lateinit var data: List<ScanResult>
    fun setData(data: List<ScanResult>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiListHolder {
        return WifiListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_wifi_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: WifiListHolder, position: Int) {
        holder.data = data[position]
        holder.frameLayoutContainer.setOnClickListener {
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