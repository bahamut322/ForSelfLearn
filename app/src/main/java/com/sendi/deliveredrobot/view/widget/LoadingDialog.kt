package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 *   @author: heky
 *   @date: 2021/9/11 15:52
 *   @describe: 加载中Dialog
 */
@SuppressLint("InflateParams")
class LoadingDialog(context: Context, themeResId: Int) :
    HideNavigationBarDialog(context, themeResId, needBlur = false) {
    val mainScope = MainScope()
    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialogView: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_loading, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        mWindowWidth = displayMetrics.widthPixels
        mWindowHeight = displayMetrics.heightPixels
        setContentView(
            dialogView, ViewGroup.MarginLayoutParams(
                mWindowWidth,
                mWindowHeight
            )
        )
    }

    override fun show() {
        mainScope.launch(Dispatchers.Main) {
            super.show()
        }
    }

    override fun dismiss() {
        mainScope.launch(Dispatchers.Main) {
            super.dismiss()
        }
    }
}