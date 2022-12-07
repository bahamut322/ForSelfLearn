package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.utils.ToastUtil

class AddAreaDialog : Dialog {
    var addAreaDialogListener: AddAreaDialogListener? = null
    lateinit var tvSortTips:TextView
    constructor(context: Context, addAreaDialogListener: AddAreaDialogListener) : super(
        context) {
        this.addAreaDialogListener = addAreaDialogListener

        initView()
    }

    private fun initView() {
        val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
            .inflate(R.layout.dialog_add_area, null)

        setContentView(dialogView)
        setCanceledOnTouchOutside(false)
        tvSortTips = findViewById<TextView>(R.id.tvSortTips)
        var edtRouteName = findViewById<TextView>(R.id.edtSortName)
        val window : Window? = this.window
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context,R.color.transparency)))
        }

        findViewById<TextView>(R.id.tvSortConfirm).setOnClickListener {
            var mapName = edtRouteName.getText().toString().trim()
            if (mapName != ""){
                addAreaDialogListener?.confirm(mapName)
            }else{
                ToastUtil.show(context.getString(R.string.please_input_area_name))
            }
        }

        findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            addAreaDialogListener?.cancel()
            dismiss()
        }

    }

    fun setTips(content:String) {
        tvSortTips.text = content
    }

    interface AddAreaDialogListener {
        fun cancel()
        fun confirm(mapName:String)
    }

}