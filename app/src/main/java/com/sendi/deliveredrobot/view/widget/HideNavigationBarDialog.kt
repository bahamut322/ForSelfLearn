package com.sendi.deliveredrobot.view.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.utils.FastBlurUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * @describe 弹出时防止NavigationBar和状态栏弹出
 */
open class HideNavigationBarDialog(context: Context, themeResId: Int, private val needBlur: Boolean = true) : Dialog(context, themeResId) {
    private val mutex = Mutex()
    private val mainScope = MainScope()
    override fun show() {
        if (isShowing) {
            return
        }
//        mainScope.launch(Dispatchers.Main) {
//            mutex.withLock {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
            super.show()
            if (window?.decorView != null) {
                fullScreenImmersive(window?.decorView!!)
            }
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            if (needBlur) {
                var blurBackgroundDrawer: Bitmap? = null
//                    withContext(Dispatchers.Default) {
                try {
                    blurBackgroundDrawer =
                        FastBlurUtil.getBlurBackgroundDrawer(DialogHelper.activity)!!
                } catch (e: Exception) {

                }
//                    }
                if (blurBackgroundDrawer != null)
                    window?.setBackgroundDrawable(
                        BitmapDrawable(
                            DialogHelper.activity.resources,
                            blurBackgroundDrawer
                        )
                    )
            }
            window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
//            }
//        }
    }

    override fun dismiss() {
        if (!isShowing) return
//        mainScope.launch(Dispatchers.Main) {
//            mutex.withLock {
        super.dismiss()
//            }
//        }
    }

    private fun fullScreenImmersive(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            view.systemUiVisibility = uiOptions
        }
    }
}
