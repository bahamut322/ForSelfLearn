package com.sendi.deliveredrobot.adapter.base.i

import android.annotation.SuppressLint
import androidx.constraintlayout.widget.ConstraintLayout
import com.sendi.deliveredrobot.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.sendi.deliveredrobot.room.entity.QueryPointEntity

/**
 * 智能引领列表适配器
 * @author swn
 */
class GuidePointAdapter (var context: Context, var datas:List<QueryPointEntity>) : BaseAdapter(){

    inner class MyHolder {
        lateinit var imageId : ImageView
        lateinit var text : TextView
        lateinit var mRelative : ConstraintLayout
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View?
        val myHolder: MyHolder?

        if(convertView == null){
            myHolder = MyHolder()
            view = LayoutInflater.from(context).inflate(R.layout.guide_select_item,null)
            myHolder.imageId = view.findViewById(R.id.ivGuideRound)
            myHolder.text = view.findViewById(R.id.tv_name)
            myHolder.mRelative = view.findViewById(R.id.CLayout)
            view.tag = myHolder
        }else{
            view = convertView
            myHolder = view.tag as MyHolder
        }

        myHolder.text.text = datas[position].pointName
//        myHolder.imageId.setImageResource(datas.get(position).imageId)
        myHolder.imageId.setImageResource(R.drawable.img_strat_explation)

        return view!!
    }

    override fun getItem(position: Int): Any {
        //获取指定位置(position)上的item对象
        return datas[position]
    }

    override fun getItemId(position: Int): Long {
        // 获取指定位置(position)上的item的id
        return position.toLong()
    }

    override fun getCount(): Int {
        //返回一个整数,就是要在listview中现实的数据的条数
        return datas.size
    }

}
