package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.ApplicationModel

/**
 * @Author Swn
 * @Data 2023/12/6
 * @describe 更多服务列表适配器
 */
class ApplicationAdapter(var context: Context, var data: List<ApplicationModel>) : BaseAdapter() {

    inner class MyHolder {
        lateinit var bgCon: ConstraintLayout
        lateinit var nameTv: TextView
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View?
        val myHolder: MyHolder?

        if (convertView == null) {
            myHolder = MyHolder()
            view = LayoutInflater.from(context).inflate(R.layout.item_business, null)
            myHolder.bgCon = view.findViewById(R.id.bg_con)
            myHolder.nameTv = view.findViewById(R.id.business_name)
            view.tag = myHolder
        } else {
            view = convertView
            myHolder = view.tag as MyHolder
        }

        myHolder.nameTv.text = data[position].name
        return view!!
    }

    override fun getItem(position: Int): Any {
        //获取指定位置(position)上的item对象
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        // 获取指定位置(position)上的item的id
        return position.toLong()
    }

    override fun getCount(): Int {
        //返回一个整数,就是要在listview中现实的数据的条数
        return data.size
    }

}
