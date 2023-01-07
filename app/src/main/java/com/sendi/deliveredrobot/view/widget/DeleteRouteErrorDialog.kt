package com.sendi.deliveredrobot.view.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentGeneralViewListBinding
import com.sendi.deliveredrobot.MyApplication

class DeleteRouteErrorDialog : HideNavigationBarDialog {
    lateinit var binding: FragmentGeneralViewListBinding
    var strContent: String = ""

    constructor(context: Context,strContent: String) : super(
        context,R.style.simpleDialogStyle,needBlur = false
    ) {
        this.strContent = strContent
        initView()
    }

    private fun initView() {
        val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
            .inflate(R.layout.dialog_route_unbind, null)
        setContentView(dialogView)

        val window : Window? = this.getWindow()
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.transparency)))
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
        }


        val string = "该"+strContent+"已被总图名称绑定，请先解绑后在执行操作"
        val fstart = string.indexOf("总图名称")
        val fend = fstart+"总图名称".length
        val style= SpannableStringBuilder(string)
        style.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.col_common_btn_bg_dark_end)),fstart,fend, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        findViewById<TextView>(R.id.tvRoutetip).setText(style)

        findViewById<TextView>(R.id.tvRouteunbindconfirm).setOnClickListener {
            cancel()
        }
    }
}