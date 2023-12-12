package com.sendi.deliveredrobot.view.widget

import android.content.Context
import android.os.Bundle
import android.text.style.ClickableSpan
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavGraphNavigator
import com.sendi.deliveredrobot.R


///文本框点击超链接功能
class CustomUrlSpan(context: Context, url: String, navigator: NavController) : ClickableSpan() {
    private val context: Context
    private val url: String
    private val navigator: NavController

    init {
        this.context = context
        this.url = url
        this.navigator = navigator
    }

    override fun onClick(widget: View) {
        val args: Bundle = Bundle().apply {
            // 设置 Bundle 对象参数数据
            this.putString("ManagerUrl", url)
            this.putString("name","")
        }
        navigator.navigate(R.id.appManagerFragment, args)
    }
}