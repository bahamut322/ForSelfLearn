package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.LaserListHolder
import com.sendi.deliveredrobot.holder.LimitSpeedListHolder
import com.sendi.deliveredrobot.room.entity.QuerySubMapEntity

/**
 *   @author: heky
 *   @date: 2022/2/21 16:47
 *   @describe: 限速区列表
 */
class LimitSpeedListAdapter : RecyclerView.Adapter<LimitSpeedListHolder>() {
    var data: List<QuerySubMapEntity> = ArrayList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LimitSpeedListHolder {
        return LimitSpeedListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_limit_speed_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LimitSpeedListHolder, position: Int) {
        holder.data = data[position]
        holder.frameLayoutContainer.setOnClickListener {
            listener.onItemClick(data[position], position)
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
        fun onDeleteButtonClick(data: QuerySubMapEntity, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickCallback) {
        this.listener = listener
    }
}