package com.sendi.deliveredrobot.view.widget

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentGeneralViewListBinding

class DeleteRouteConfirmDialog : HideNavigationBarDialog {
    var deleteRouteConfirmDialogListener: DeleteRouteDialogListener? = null
    lateinit var binding: FragmentGeneralViewListBinding
    var strContent: String = ""

    constructor(context: Context, deleteRouteConfirmDialogListener: DeleteRouteDialogListener, strContent: String) : super(
        context,R.style.simpleDialogStyle,needBlur = false) {
        this.deleteRouteConfirmDialogListener = deleteRouteConfirmDialogListener
        this.strContent = strContent
        initView()
    }

    private fun initView() {
        val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
            .inflate(R.layout.dialog_route_map_delete, null)

        setContentView(dialogView)

        val window : Window? = this.getWindow()
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context,R.color.transparency)))
        }

        val string = "确认要删除"+strContent+"吗？"
        val fstart = string.indexOf(strContent)
        val fend = fstart+strContent.length
        val style= SpannableStringBuilder(string)
        style.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.col_common_btn_bg_dark_end)),fstart,fend,Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        findViewById<TextView>(R.id.tvRoutetip1).setText(style)
        findViewById<TextView>(R.id.tvRouteconfirm).setOnClickListener {
            deleteRouteConfirmDialogListener?.confirm(this)
            cancel()
        }

        findViewById<TextView>(R.id.tvRoutecancel).setOnClickListener {
            dismiss()
        }
    }

    interface DeleteRouteDialogListener {
        fun cancel(dialog: DeleteRouteConfirmDialog)
        fun confirm(dialog: DeleteRouteConfirmDialog)
    }

}