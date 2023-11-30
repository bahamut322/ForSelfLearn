package com.sendi.deliveredrobot.view.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendi.deliveredrobot.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @Author Swn
 * @Data 2023/11/30
 * @describe 被围堵dialog
 */
class ContainDialog(context: Context, themeResId: Int) :
    HideNavigationBarDialog(context, themeResId, needBlur = false) {
    val mainScope = MainScope()

    init {
        val dialogView: View = LayoutInflater.from(context)
            .inflate(R.layout.fragment_contain, null)

        // 设置对话框内容视图
        setContentView(dialogView)

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
            super.show()
        }
    }

    override fun dismiss() {
        mainScope.launch(Dispatchers.Main) {
            super.dismiss()
        }
    }
}