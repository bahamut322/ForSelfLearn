package com.sendi.deliveredrobot.baidutts.listener

import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.LogUtil

/**
 * @Author Swn
 * @Data 2024/1/10
 * @describe 字体分割方法
 */
class PunctuationTextSplitter : TextSplitter {
    override fun splitTextByPunctuation(text: String?): List<String> {
        Universal.taskNum = 0
        BaiduTTSHelper.getInstance().stop()
        Universal.ExplainSpeak = ArrayList()
        RobotStatus.progress.postValue(0)
        if (Universal.ExplainSpeak != null) {
            Universal.ExplainSpeak.clear()
        }
//        if (Universal.taskQueue != null) {
//            Universal.taskQueue.clear()
//        }
        Universal.ExplainLength = text!!.length
        LogUtil.i("总内容长度: ${text.length}")
        val pattern = "(?<=[，；？！。,.;])".toRegex()
        val splitText = text.split(pattern).filter { it.isNotEmpty() } // 过滤掉空字符串
        for (i in splitText.indices) {
            val subText = text.split(pattern)[i]
            if (subText.length > 45) {
                val subTextList = subText.chunked(45)
                for (sub in subTextList) {
                    Universal.ExplainSpeak.add(sub.length)
//                    Universal.taskQueue.enqueue(sub)
                }
            } else {
                Universal.ExplainSpeak.add(subText.length)
//                Universal.taskQueue.enqueue(subText)
            }
//            Universal.taskQueue.resume()
            LogUtil.d("列表长度内容: ${Universal.ExplainSpeak}")
            LogUtil.i("分割内容：${splitText[i]} 内容长度：${splitText[i].length}")
        }
        return splitText
    }
}