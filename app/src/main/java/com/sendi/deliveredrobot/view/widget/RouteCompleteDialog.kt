package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RouteCompleteDialog : Dialog {
    var routeCompleteDialogListener: RouteCompleteDialogListener? = null
    private lateinit var dao: DeliveredRobotDao
    constructor(context: Context, mRouteCompleteDialogListener: RouteCompleteDialogListener) : super(
        context) {
        this.routeCompleteDialogListener = mRouteCompleteDialogListener
        dao = DataBaseDeliveredRobotMap.getDatabase(context).getDao()
        initView()
    }

    private fun initView() {
        val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
            .inflate(R.layout.dialog_route_map_complete, null)

        setContentView(dialogView)

        var edtRouteName = findViewById<TextView>(R.id.edtRouteName)
        val window : Window? = this.getWindow()
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context,R.color.transparency)))
        }

        findViewById<TextView>(R.id.tvRouteConfirm).setOnClickListener {
            MainScope().launch {
                val mapName = edtRouteName.text.toString().trim()
                var routeNameExist: Boolean
                withContext(Dispatchers.Default){
                    routeNameExist = dao.queryRouteMapExist(mapName) > 0
                }
                if (routeNameExist) {
                    ToastUtil.show("路径图名已存在")
                    return@launch
                }
                if (mapName != ""){
                    routeCompleteDialogListener?.confirm(mapName)
                    dismiss()
                }else{
                    ToastUtil.show(context.getString(R.string.please_input_map_name))
                }
            }
        }

        findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            routeCompleteDialogListener?.cancel()
            dismiss()
        }

    }

    interface RouteCompleteDialogListener {
        fun cancel()
        fun confirm(mapName:String)
    }

}