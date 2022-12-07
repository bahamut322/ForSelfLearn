package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.SettingHomeListHolder
import com.sendi.deliveredrobot.viewmodel.SettingHomeViewModel

class SettingHomeListAdapter(val viewModel: SettingHomeViewModel) :
    Adapter<SettingHomeListHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingHomeListHolder {
        return SettingHomeListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_setting_home, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return viewModel.data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: SettingHomeListHolder, position: Int) {
        with(holder.textView) {
            isClickable = true
            setOnClickListener {
                for (model in viewModel.data) {
                    model.selected = false
                }
                viewModel.data[position].selected = true
                holder.data = viewModel.data[position]
                viewModel.currentSettingPosition.value = position
                notifyDataSetChanged()
            }
            holder.data = viewModel.data[position]
        }
    }
}