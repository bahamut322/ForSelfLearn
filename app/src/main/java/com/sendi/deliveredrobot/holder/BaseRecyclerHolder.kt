package com.sendi.deliveredrobot.holder

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

/**
 * @author lsz
 * @desc RecyclerView.Adapter 调试地图列表数据基类
 * @date 2021/9/13
 **/

class BaseRecyclerHolder: RecyclerView.ViewHolder {

    private lateinit var mViews: SparseArrayCompat<View>

    constructor(itemView: View): super(itemView) {
        mViews = SparseArrayCompat()
    }

    fun <V : View> getView(@IdRes res: Int): V {
        var v:View? = mViews!![res]
        if (v == null) {
            v = itemView.findViewById(res)
            mViews!!.put(res, v)
        }
        return v as V
    }

    /**
     * 直接赋值textview
     * @param TvRes
     * @param text
     */
    fun setText(@IdRes TvRes: Int, text: CharSequence?): BaseRecyclerHolder? {
//        LogUtil.d(text.toString());
        val textView = getView<TextView>(TvRes)!!
        textView.text = text
        return this
    }

    /**
     * 点击事件
     * @param viewId
     * @param listener
     * @return
     */
    fun setOnClickListener(
        @IdRes viewId: Int,
        listener: View.OnClickListener?
    ): BaseRecyclerHolder? {
        val view = getView<View>(viewId)!!
        view.setOnClickListener(listener)
        return this
    }
}