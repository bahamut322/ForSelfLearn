package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.holder.BaseRecyclerHolder
import java.util.*

abstract class BaseRecyclerViewAdapter<D>: RecyclerView.Adapter<BaseRecyclerHolder> {

    lateinit var context: Context
    var layoutRes = 0
    var datas: List<D> = ArrayList()
    var onRecyclerItemClickListener: OnRecyclerItemClickListener<D>? = null

    constructor(context: Context): super() {
        this.context = context
        layoutRes = layoutRes
        datas = ArrayList()
    }
    constructor(context: Context, data: List<D>?): super() {
        this.context = context
        datas = data ?: ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerHolder {
        return BaseRecyclerHolder(
            LayoutInflater.from(context).inflate(getLayoutResId(), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseRecyclerHolder, position: Int) {
        bindView(holder, position)
    }

    override fun getItemCount(): Int {
        return datas!!.size
    }

//    open fun setOnRecyclerItemClickListener(onRecyclerItemClickListener: OnRecyclerItemClickListener<D>?) {
//        this.onRecyclerItemClickListener = onRecyclerItemClickListener
//    }

    abstract fun bindView(holder: BaseRecyclerHolder, position: Int)
    abstract fun getLayoutResId(): Int

    interface OnRecyclerItemClickListener<D> {
        fun onItemClick(position: Int, tag: Int)
        fun onItemLongClick(position: Int, tag: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(datas: List<D>) {
        this.datas = datas
        notifyDataSetChanged()
    }

}