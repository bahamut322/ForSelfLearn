package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.LaserListHolder
import com.sendi.deliveredrobot.holder.OneKeyPhoneNumberListHolder
import com.sendi.deliveredrobot.model.OneKeyCallPhoneModel
import com.sendi.deliveredrobot.room.entity.QuerySubMapEntity

/**
 *   @author: heky
 *   @date: 2024/3/18
 *   @describe: 电话列表adapter
 */
class OneKeyPhoneNumberListAdapter : RecyclerView.Adapter<OneKeyPhoneNumberListHolder>() {
    var data: List<OneKeyCallPhoneModel> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    @JvmName("setData1")
    fun setData(data: List<OneKeyCallPhoneModel>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneKeyPhoneNumberListHolder {
        return OneKeyPhoneNumberListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_one_key_call_phone_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: OneKeyPhoneNumberListHolder, position: Int) {
        holder.data = data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }
}