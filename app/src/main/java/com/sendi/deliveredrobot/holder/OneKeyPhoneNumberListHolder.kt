package com.sendi.deliveredrobot.holder

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R

/**
 *   @author: heky
 *   @date: 2021/9/3 16:48
 *   @describe: 激光图列表
 */
class OneKeyPhoneNumberListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var data: String? = null
        set(value) {
            field = value
            if (itemView is TextView) {
                (itemView as TextView).apply {
                    text = value
                    textSize = 32f
                    setTextColor(ContextCompat.getColor(MyApplication.context, R.color.color_99FFFFFF))
                    setPadding(0,52,0,0)
                    setOnClickListener {
                        val intent = Intent(Intent.ACTION_CALL)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        val data = Uri.parse("tel:$value")
                        intent.setData(data)
                        ContextCompat.startActivity(MyApplication.context,intent, Bundle())
                    }
                }
            }
        }
}