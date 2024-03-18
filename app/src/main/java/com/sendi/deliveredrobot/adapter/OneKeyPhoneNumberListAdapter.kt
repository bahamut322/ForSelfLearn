package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.LaserListHolder
import com.sendi.deliveredrobot.holder.OneKeyPhoneNumberListHolder
import com.sendi.deliveredrobot.room.entity.QuerySubMapEntity

/**
 *   @author: heky
 *   @date: 2024/3/18
 *   @describe: 电话列表adapter
 */
class OneKeyPhoneNumberListAdapter : RecyclerView.Adapter<OneKeyPhoneNumberListHolder>() {
    var data: List<String> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    @JvmName("setData1")
    fun setData(data: List<String>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneKeyPhoneNumberListHolder {
        return OneKeyPhoneNumberListHolder(
            TextView(parent.context)
        )
    }

    override fun onBindViewHolder(holder: OneKeyPhoneNumberListHolder, position: Int) {
        holder.data = data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }
}