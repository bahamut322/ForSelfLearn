package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.interfaces.DownLoadListener
import com.sendi.deliveredrobot.navigationtask.DownloadBill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @Author Swn
 * @Data 2023/11/29
 * @describe 配置更新Dialog
 */
@SuppressLint("MissingInflatedId", "InflateParams")
class RobotUpDataDialog(
    context: Context,
    themeResId: Int = R.style.simpleDialogStyle,
    needBlur: Boolean = true
) : HideNavigationBarDialog(context = context, themeResId = themeResId, needBlur = needBlur) {
    private var progress : ProgressBar
    private var taskNum : TextView
    val mainScope = MainScope()
    init {
        val dialogView: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_robot_updata, null)

        // 设置对话框内容视图
        setContentView(dialogView)
        progress = dialogView.findViewById(R.id.progressBar)
        taskNum = dialogView.findViewById(R.id.task_tv)
        // 获取对话框窗口
        val window = window
        window?.apply {
            // 设置窗口布局参数以匹配父窗口大小
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            // 设置窗口背景为透明（如果需要）
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun show() {
        mainScope.launch(Dispatchers.Main) {
            setDate()
            super.show()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun setDate(){
        DownLoadListener.setOnChangeListener {
            progress.progress = DownLoadListener.getProgress()
            taskNum.text = "当前任务进度：${DownLoadListener.getProgress()}% /剩余任务：${DownloadBill.getInstance().taskCount}"
        }
    }

    override fun dismiss() {
        mainScope.launch(Dispatchers.Main) {
            super.dismiss()
        }
    }
}