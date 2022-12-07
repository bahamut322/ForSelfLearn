package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.LimitSpeedLineListHolder
import com.sendi.deliveredrobot.model.LineInfoModel

/**
 *   @author: heky
 *   @date: 2022/2/22 16:47
 *   @describe: 限速区Line列表
 */
class LimitSpeedLineListAdapter : RecyclerView.Adapter<LimitSpeedLineListHolder>() {
    var data: List<LineInfoModel> = ArrayList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LimitSpeedLineListHolder {
        return LimitSpeedLineListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_limit_speed_line_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LimitSpeedLineListHolder, position: Int) {
        holder.data = data[position]
        holder.textViewEdit.setOnClickListener {
            listener.onEditButtonClick(data[position], position)
        }
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
        fun onEditButtonClick(data: LineInfoModel, position: Int)
        fun onDeleteButtonClick(data: LineInfoModel, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickCallback) {
        this.listener = listener
    }
}