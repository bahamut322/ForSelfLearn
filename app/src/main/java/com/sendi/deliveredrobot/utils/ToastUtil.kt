package com.sendi.deliveredrobot.utils

import android.annotation.SuppressLint
import android.view.Gravity
import android.widget.Toast
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MainActivity
import java.lang.Exception

/**
 *   @author: heky
 *   @date: 2021/7/14 9:06
 *   @describe:Toast工具
 */
object ToastUtil {
    private var toast:Toast? = null
    private var activity: MainActivity? = null

    fun initial(activity: MainActivity) {
        this.activity = activity
    }

    /**
     * @describe debug弹toast
     */
    @SuppressLint("ShowToast")
    fun show(msg: String) {
        if (BuildConfig.IS_TOAST) {
            if (activity == null) {
                throw Exception("didn't init ToastUtil")
            }
            activity!!.runOnUiThread {
                toast?.cancel()
                toast = Toast.makeText(activity,msg,Toast.LENGTH_LONG).apply {
                    setGravity(Gravity.CENTER, 0, -300)
                    show()
                }
            }
        }
    }
}
