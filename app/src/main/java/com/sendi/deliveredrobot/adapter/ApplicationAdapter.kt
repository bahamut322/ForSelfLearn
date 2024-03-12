package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.ApplicationModel
import com.sendi.deliveredrobot.utils.LogUtil

/**
 * @Author Swn
 * @Data 2023/12/6
 * @describe 更多服务列表适配器
 */
class ApplicationAdapter(var context: Context, var data: List<ApplicationModel>) : BaseAdapter() {

    inner class MyHolder {
        lateinit var imageViewApplet: ImageView
        lateinit var textViewApplet: TextView
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View?
        val myHolder: MyHolder?

        if (convertView == null) {
            myHolder = MyHolder()
            view = LayoutInflater.from(context).inflate(R.layout.item_app_content, null)
            myHolder.imageViewApplet = view.findViewById(R.id.image_view_applet)
            myHolder.textViewApplet = view.findViewById(R.id.text_view_applet)
            view.tag = myHolder
        } else {
            view = convertView
            myHolder = view.tag as MyHolder
        }
        myHolder.textViewApplet.text = data[position].name
        Glide.with(myHolder.imageViewApplet)
            .load("${BuildConfig.HTTP_HOST}${data[position].icon}")
            .placeholder(R.color.white)
            .into(myHolder.imageViewApplet)
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
