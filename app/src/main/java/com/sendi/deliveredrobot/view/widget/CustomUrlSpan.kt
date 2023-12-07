package com.sendi.deliveredrobot.view.widget

import android.content.Context
import android.text.style.ClickableSpan
import android.view.View


///文本框点击超链接功能
class CustomUrlSpan(context: Context, url: String) : ClickableSpan() {
    private val context: Context
    private val url: String

    init {
        this.context = context
        this.url = url
    }

    override fun onClick(widget: View) {
        // 在这里可以做任何自己想要的处理
//        BrowserActivity.start(context, url)
        println("URL: $url")
    }
}