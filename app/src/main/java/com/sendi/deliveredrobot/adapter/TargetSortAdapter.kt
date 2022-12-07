package com.sendi.deliveredrobot.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.BaseRecyclerHolder
import com.sendi.deliveredrobot.room.entity.PublicArea

class TargetSortAdapter: BaseRecyclerViewAdapter<PublicArea>{

    var isEditIng = false

    constructor(context: Context, data: List<PublicArea>?): super(context, data) {

    }

    override fun bindView(holder: BaseRecyclerHolder, position: Int) {
        val rlContainer: RelativeLayout = holder.getView(R.id.rlContainer)
        var tvName: TextView  = holder.getView(R.id.tvName)
        var ivLock: ImageView  = holder.getView(R.id.ivLock)
        var ivDelete: ImageView  = holder.getView(R.id.ivDelete)



        var data = datas[position]
        tvName.apply {
            text = data.name
        }

        ivLock.apply {
            isClickable = true
            if (data.type == 0){
                visibility = View.VISIBLE
            }else{
                visibility = View.GONE
            }
        }

        ivDelete.apply {
            isClickable = true
            if (data.type == 1){
                visibility = View.VISIBLE
            }else{
                visibility = View.GONE
            }

        }
        rlContainer.setOnClickListener {
            listener.onItemClick(datas[position], position)
        }

        rlContainer.setOnLongClickListener {
            listener.onItemLongClick(datas[position], position)
            return@setOnLongClickListener true
        }
        ivDelete.setOnClickListener {
            listener.onDeleteButtonClick(datas[position], position)
        }

    }

    override fun getLayoutResId(): Int {
        return R.layout.item_target_sort_list
    }

    private lateinit var listener: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClick(data: PublicArea, position: Int)
        fun onItemLongClick(data: PublicArea, position: Int)
        fun onDeleteButtonClick(data: PublicArea, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickCallback) {
        this.listener = listener
    }

}