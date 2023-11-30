package com.sendi.deliveredrobot.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.topic.BatteryStateTopic
import com.sendi.deliveredrobot.view.widget.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *   @author: heky
 *   @date: 2021/7/8 15:11
 *   @describe:四种异常dialog的helper
 */
class DialogHelper {

    interface DialogListener {
        fun cancel()
        fun confirm()
    }


    @SuppressLint("StaticFieldLeak")
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var activity: Activity
        lateinit var troubleDialog: Dialog
        lateinit var distractedDialog: Dialog
        lateinit var rebootDialog: Dialog
        lateinit var stopDialog: Dialog
        lateinit var dockFailDialog: Dialog
        lateinit var pullOutAdapterDialog: Dialog
        lateinit var exceptionWaitForHelpDialog: Dialog
        lateinit var chargingDialog: Dialog
        lateinit var loadingDialog: Dialog
        lateinit var robotUpDataDialog : Dialog
        lateinit var lowPowerGoBack : Dialog
        lateinit var briefingDialog : Dialog
        lateinit var containDialog : Dialog
        var myCustomPopupWin: MyCustomPopupWin? = null

        /**
         * 初始化4种类型的dialog
         */
        fun initDialog(_activity: Activity) {
            activity = _activity
            troubleDialog = TroubleDialog(activity)
            briefingDialog = BrieFingDialog(activity)
            distractedDialog = DistractedDialog(activity)
            rebootDialog = TryRebootDialog(activity)
            stopDialog = ResetStopButtonDialog(activity, needBlur = false)
            dockFailDialog = DockFailDialog(activity)
            pullOutAdapterDialog = PullOutAdapterDialog(activity)
            exceptionWaitForHelpDialog = ExceptionWaitForHelpDialog(activity, needBlur = false)
            loadingDialog = LoadingDialog(activity, R.style.simpleDialogStyle)
            robotUpDataDialog = RobotUpDataDialog(activity,R.style.simpleDialogStyle)
            containDialog = ContainDialog(activity,R.style.simpleDialogStyle)
            lowPowerGoBack = LowPowerDialog(activity,R.style.simpleDialogStyle)
        }

        /**
         * 初始化-自检提示Dialog
         */
        @SuppressLint("InflateParams")
        fun selfCheckDialog(
            title: String,
            content: String,
            errorCode: String,
            enableCloseBtn: Boolean,
            enableConfirmBtn: Boolean,
            dialogListener: DialogListener?
        ): Dialog {
            val mWindowWidth: Int
            val mWindowHeight: Int
            val dialog =
                HideNavigationBarDialog(activity, R.style.simpleDialogStyle)
            val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
                .inflate(R.layout.dialog_robot_self_check_hint, null)
            val displayMetrics = MyApplication.instance!!.resources.displayMetrics
            dialogView.findViewById<ImageView>(R.id.imageViewClose).apply {
                if (enableCloseBtn) {
                    visibility = View.VISIBLE
                    isClickable = true
                } else {
                    visibility = View.GONE
                }
                setOnClickListener {
                    dialogListener?.cancel()
                    dialog.dismiss()
                }
            }

            dialogView.findViewById<Button>(R.id.confirm_btn).apply {
                if (enableConfirmBtn) {
                    visibility = View.VISIBLE
                    isClickable = true
                } else {
                    visibility = View.GONE
                }
                setOnClickListener {
                    dialog.dismiss()
                    dialogListener?.confirm()
                }
            }

            dialogView.findViewById<TextView>(R.id.title_tv).apply {
                text = title
            }
            dialogView.findViewById<TextView>(R.id.error_code_tv).apply {
                text = errorCode
            }

            dialogView.findViewById<TextView>(R.id.error_code_tv).apply {
                if (errorCode.equals("")) {
                    visibility = View.GONE
                } else {
                    text = String.format("错误编码：%1\$s", errorCode)
                }
            }


            dialogView.findViewById<TextView>(R.id.content_tv).apply {
                text = content
            }
            mWindowWidth = displayMetrics.widthPixels
            mWindowHeight = displayMetrics.heightPixels
            dialog.setContentView(
                dialogView, ViewGroup.MarginLayoutParams(
                    mWindowWidth,
                    mWindowHeight
                )
            )
            return dialog
        }

        @SuppressLint("InflateParams")
        fun getRemindDialog(message: String): Dialog {
            return RemindDialog(message, activity)
        }

        /**
         * @describe 充电中
         */
        fun initChargingDialog(owner: LifecycleOwner): Dialog {
            val mWindowWidth: Int
            val mWindowHeight: Int
            val dialog =
                HideNavigationBarDialog(activity, R.style.simpleDialogStyle)
            dialog.setCancelable(false)
            val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
                .inflate(R.layout.dialog_robot_charging, null)
            val displayMetrics = MyApplication.instance!!.resources.displayMetrics
            val imageViewCharge = dialogView.findViewById<ImageView>(R.id.imageViewCharge)
            val textViewProgress = dialogView.findViewById<TextView>(R.id.textViewProgress)

            Glide.with(MyApplication.instance!!).asGif().load(R.raw.charge_bg).into(imageViewCharge)
            RobotStatus.batteryPower.observe(owner) {
                MainScope().launch {
                    withContext(Dispatchers.Main) {
                        textViewProgress.text =
                            CommonHelper.getPresentSpan((it * 100).toInt(), 2.5f)
                    }
                }
            }
            mWindowWidth = displayMetrics.widthPixels
//            mWindowHeight = displayMetrics.heightPixels
            mWindowHeight = 800
            dialog.setContentView(
                dialogView, ViewGroup.MarginLayoutParams(
                    mWindowWidth,
                    mWindowHeight
                )
            )
            return dialog
        }

        fun setMyCommonPopupWinAndShow(builder: MyCustomPopupWin.Builder) {
            if (myCustomPopupWin != null) {
                myCustomPopupWin!!.dismiss()
                myCustomPopupWin = null
            }
            myCustomPopupWin = builder.build()
            myCustomPopupWin?.show()
        }

        fun hideCommonPopupWinAndDestroy() {
            if (myCustomPopupWin != null) {
                myCustomPopupWin!!.dismiss()
                myCustomPopupWin = null
            }
        }

        fun getSendFailDialog(
            message: String,
            activity: Activity,
            sendFailListener: SendFailDialog.SendFailListener
        ): Dialog {
            return SendFailDialog(message, activity, sendFailListener = sendFailListener)
        }

//        fun lowPowerGoBack() :Dialog{
//            return LowPowerDialog(MainActivity.instance, lowPowerDialogListener = object :
//                LowPowerDialog.LowPowerDialogListener {
//                override fun timeUp(dialog: LowPowerDialog) {
//                    dialog.dismiss()
//                    MainScope().launch {
//                        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
//                    }
//
//                }
//                override fun buttonPress(dialog: LowPowerDialog) {
//                    dialog.dismiss()
//                    MainScope().launch {
//                        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
//                    }
//                }
//            })
//        }

    }
}