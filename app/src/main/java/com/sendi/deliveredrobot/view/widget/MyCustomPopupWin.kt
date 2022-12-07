package com.sendi.deliveredrobot.view.widget

import android.app.Service
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow

class MyCustomPopupWin : PopupWindow {
    val DIALOG_HIDE_LOADING = 0
    private var context: Context? = null
    private var dropDownView: View? = null
    private val focusable = true
    private val outsideTouchable = true
    private var xOff = 0
    private var yOff = 0

//    private val dialogHandler: WeakHandler? = WeakHandler { msg ->
//        when (msg.what) {
//            DIALOG_HIDE_LOADING -> DialogUtil.hideShowLoadingDialogAndDestroy()
//        }
//        true
//    }

    constructor(builder: Builder):super(builder.context) {
        context = builder.context
        dropDownView = builder.dropDownView
        xOff = builder.xOff
        yOff = builder.yOff
        builder.contentView!!.systemUiVisibility = getFullscreenOption(false, false)
        setContentView(builder.contentView)
        setHeight(builder.height)
        setWidth(builder.width)
        setBackgroundDrawable(BitmapDrawable())
        setOutsideTouchable(true)
        getContentView().setFocusable(true)
        getContentView().setFocusableInTouchMode(true)
    }

    fun show() {
        setFocusable(false)
        showAsDropDown(dropDownView, xOff, yOff)
        setFullscreen(false, false)
        val params = dropDownView!!.rootView.layoutParams
        val windowManager = context!!.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        (params as WindowManager.LayoutParams).flags =
            params.flags and (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM).inv()
        windowManager.updateViewLayout(dropDownView!!.rootView, params)
        setFocusable(true)
        getContentView().setOnKeyListener(View.OnKeyListener { v: View?, keyCode: Int, event: KeyEvent? -> true })
        getContentView().setOnSystemUiVisibilityChangeListener(View.OnSystemUiVisibilityChangeListener { visibility: Int ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                setFullscreen(false, false)
            }
        })
    }

//    fun hideDelay(what: Int, delay: Int) {
//        if (dialogHandler != null) {
//            dialogHandler.sendEmptyMessageDelayed(what, delay)
//        }
//    }

    override fun dismiss() {
        super.dismiss()
//        if (dialogHandler != null) {
//            dialogHandler.removeCallbacksAndMessages(null)
//        }
    }

    fun setFullscreen(isShowStatusBar: Boolean, isShowNavigationBar: Boolean) {
        var uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE)
        if (!isShowStatusBar) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        if (!isShowNavigationBar) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        getContentView().setSystemUiVisibility(uiOptions)
    }

    fun getFullscreenOption(isShowStatusBar: Boolean, isShowNavigationBar: Boolean): Int {
        var uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE)
        if (!isShowStatusBar) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        if (!isShowNavigationBar) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        return uiOptions
    }

    interface OnHandlerBuilderDelegate {
        fun handlerBuilder(view: View?)
    }

    class Builder {
        lateinit var context : Context
        var contentView // 布局view
                : View? = null
        var dropDownView // 参考view
                : View? = null
        private var focusable = true
        private var outsideTouchable = true
        var height: Int
        var width: Int
        var xOff = 0 // 偏移量
        var yOff = 0 // 偏移量

        constructor(context: Context) {
            this.context = context
            val displayMetrics = context.resources.displayMetrics
            height = displayMetrics.heightPixels
            width = displayMetrics.widthPixels
        }

        fun contentView(resView: Int): Builder {
            contentView = LayoutInflater.from(context).inflate(resView, null)
            return this
        }

        fun contentView(resView: View?): Builder {
            contentView = resView
            return this
        }

        fun focusable(`val`: Boolean): Builder {
            focusable = `val`
            return this
        }

        fun outsideTouchable(`val`: Boolean): Builder {
            outsideTouchable = `val`
            return this
        }

        fun xOff(xVal: Int): Builder {
            xOff = xVal
            return this
        }

        fun yOff(yVal: Int): Builder {
            yOff = yVal
            return this
        }

        fun dropDownView(resView: View?): Builder {
            dropDownView = resView
            return this
        }

        fun handlerBuilder(onHandlerBuilderDelegate: OnHandlerBuilderDelegate): Builder {
            if (contentView != null) {
                onHandlerBuilderDelegate.handlerBuilder(contentView)
            }
            return this
        }

        fun height(`val`: Int): Builder {
            height = `val`
            return this
        }

        fun width(`val`: Int): Builder {
            width = `val`
            return this
        }

        fun setViewOnclick(viewRes: Int, listener: View.OnClickListener?): Builder {
            contentView!!.findViewById<View>(viewRes).setOnClickListener(listener)
            return this
        }

        fun build(): MyCustomPopupWin {
            return MyCustomPopupWin(this)
        }

    }
}