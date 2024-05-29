package com.sendi.deliveredrobot.adapter.base.i

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.room.entity.QueryPointEntity


/**
 * 智能引领列表适配器
 * @author swn
 */
class GuidePointAdapter (var context: Context, var datas:List<QueryPointEntity>) : BaseAdapter(){

    inner class MyHolder {
        lateinit var imageId : ImageView
        lateinit var text : TextView
        lateinit var textViewPointName: TextView
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
            myHolder.textViewPointName = view.findViewById(R.id.tv_point_name)
            view.tag = myHolder
        }else{
            view = convertView
            myHolder = view.tag as MyHolder
        }
        myHolder.text.text = datas[position].pointName
        val guideConfigList = QuerySql.selectGuideConfig(QuerySql.robotConfig().mapName, datas[position].pointName)
        val imageUrl: String? = guideConfigList.firstOrNull()?.guidePicUrl
        if (imageUrl.isNullOrEmpty()) {
            myHolder.imageId.visibility = View.GONE
            myHolder.textViewPointName.apply{
                visibility = View.VISIBLE
                val endSubStringIndex = if((datas[position].pointName?.length ?: 0) > 4){
                    4
                }else{
                    datas[position].pointName?.length ?: 0
                }
                text = datas[position].pointName?.substring(0, endSubStringIndex)
                textSize = when(endSubStringIndex){
                    1 -> 64f
                    2 -> 56f
                    3 -> 56f
                    4 -> 48f
                    else -> 48f
                }
            }
        } else {
            myHolder.imageId.visibility = View.VISIBLE
            myHolder.textViewPointName.visibility = View.GONE
            Glide.with(context)
                .load(imageUrl)
//                .apply(
//                    RequestOptions()
                .placeholder(R.drawable.img_strat_explation) // 设置占位图
//                        .transform(GranularRoundedCorners(24f,24f,0f,0f))
//                        .priority((Priority.HIGH))//优先级设置最高
//                )
                .transform(GranularRoundedCorners(12f,12f,0f,0f))
//                .skipMemoryCache(true) // 跳过内存缓存
//                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
//                .transition(DrawableTransitionOptions.withCrossFade()) // 添加淡入动画
                .into(myHolder.imageId)
        }
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
