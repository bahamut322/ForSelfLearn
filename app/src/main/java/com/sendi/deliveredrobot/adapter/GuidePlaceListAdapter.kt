package com.sendi.deliveredrobot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.PlaceListHolder
import com.sendi.deliveredrobot.viewmodel.GuidePlaceViewModel

class GuidePlaceListAdapter(
    private val viewModel: GuidePlaceViewModel,
    private val fragmentIndex: Int
) :
    Adapter<PlaceListHolder>() {
    private val localData = viewModel.data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceListHolder {
        return PlaceListHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_place_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlaceListHolder, position: Int) {
        holder.data = localData[fragmentIndex]
        with(holder.textView) {
            isClickable = true
            setOnClickListener {
                viewModel.setCurrentSelected(fragmentIndex, position)
                holder.data = localData[fragmentIndex]
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return localData[fragmentIndex].size
    }
}