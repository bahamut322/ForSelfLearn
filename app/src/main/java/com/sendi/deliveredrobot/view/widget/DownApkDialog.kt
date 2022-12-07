package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R

/**
 *   @author: heky
 *   @date: 2022-04-12
 *   @describe: 下载apkDialog
 */
@SuppressLint("InflateParams")
class DownApkDialog(context: Context, themeResId: Int = R.style.simpleDialogStyle) :
    HideNavigationBarDialog(context, themeResId, needBlur = false) {
    var progressBar:ProgressBar
        private set

    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialogView: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_download_apk, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        progressBar = dialogView.findViewById(R.id.progressBar)
        mWindowWidth = displayMetrics.widthPixels
        mWindowHeight = displayMetrics.heightPixels
        setContentView(
            dialogView, ViewGroup.MarginLayoutParams(
                mWindowWidth,
                mWindowHeight
            )
        )
    }

}