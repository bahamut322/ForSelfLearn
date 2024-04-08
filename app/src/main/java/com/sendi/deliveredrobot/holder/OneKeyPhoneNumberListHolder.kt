package com.sendi.deliveredrobot.holder

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.OneKeyCallPhoneModel
import com.sendi.deliveredrobot.model.PhoneConfigModel

/**
 *   @author: heky
 *   @date: 2021/9/3 16:48
 *   @describe: 激光图列表
 */
class OneKeyPhoneNumberListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textViewName: TextView = itemView.findViewById(R.id.tv_name)
    private val textViewNumber: TextView = itemView.findViewById(R.id.tv_number)
    var data: PhoneConfigModel? = null
        set(value) {
            field = value
            textViewName.text = value?.remarks?:""
            textViewNumber.text = value?.phone?:""
            itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_CALL)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val data = Uri.parse("tel:${value?.phone?:""}")
                intent.setData(data)
                ContextCompat.startActivity(MyApplication.context, intent, Bundle())
            }
        }
}