package com.sendi.deliveredrobot.adapter

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.holder.BaseRecyclerHolder
import com.sendi.deliveredrobot.room.entity.MyRootMap
import com.sendi.deliveredrobot.view.widget.SwipeMenuLayout

class GeneralViewListAdapter: BaseRecyclerViewAdapter<MyRootMap>{

    constructor(context: Context, data: List<MyRootMap>?): super(context, data) {

    }

    override fun bindView(holder: BaseRecyclerHolder, position: Int) {
        var textViewMapName: TextView  = holder.getView(R.id.textViewMapName)
        val frameLayoutContainer: FrameLayout = holder.getView(R.id.frameLayoutContainer)

        val textViewExport: TextView = holder.getView(R.id.textViewExport)
        val textViewImport: TextView = holder.getView(R.id.textViewImport)
        val textViewDelete: TextView = holder.getView(R.id.textViewDelete)
        val swipeMenuLayout: SwipeMenuLayout = holder.getView(R.id.swipeMenuLayout)
        textViewExport.visibility = View.GONE
        textViewImport.visibility = View.GONE

        var data = datas[position]
        textViewMapName.apply {
            isClickable = false
            val dataName = data?.name ?: ""
            val laserFile = resources.getString(R.string.str_general_view_file)
            val string = laserFile + dataName
            val color = ContextCompat.getColor(context,R.color.white)
            text = CommonHelper.getTipsSpan(laserFile.length, dataName, string, color, 1.07f)
        }
        textViewExport.apply {
            isClickable = true
        }
        textViewImport.apply {
            isClickable = true
        }
        textViewDelete.apply {
            isClickable = true
        }
        swipeMenuLayout.apply {
            isSwipeEnable = true
        }

        frameLayoutContainer.setOnClickListener {
            listener.onItemClick(datas[position], position)
        }
        textViewExport.setOnClickListener {
            listener.onExportButtonClick(datas[position], position)
        }
        textViewImport.setOnClickListener {
            listener.onImportButtonClick(datas[position], position)
        }
        textViewDelete.setOnClickListener {
            listener.onDeleteButtonClick(datas[position], position)
        }

    }

    override fun getLayoutResId(): Int {
        return R.layout.item_laser_list
    }

    private lateinit var listener: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClick(data: MyRootMap, position: Int)
        fun onExportButtonClick(data: MyRootMap, position: Int)
        fun onImportButtonClick(data: MyRootMap, position: Int)
        fun onDeleteButtonClick(data: MyRootMap, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickCallback) {
        this.listener = listener
    }

}