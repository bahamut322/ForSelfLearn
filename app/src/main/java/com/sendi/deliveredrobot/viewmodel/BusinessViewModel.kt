package com.sendi.deliveredrobot.viewmodel

import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.entity.GuideFoundationConfigDB
import com.sendi.deliveredrobot.entity.ShoppingActionDB
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.SecondModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.ros.constant.MyCountDownTimer
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.TaskNext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @Author Swn
 * @Data 2023/11/9
 * @describe 业务办理（导购）ViewModel
 */
class BusinessViewModel : ViewModel() {

    var countDownTimer: MyCountDownTimer? = null
    var hasArrive: Boolean = false //区分到达和未到达的任务


    fun splitTextByPunctuation(text: String?): List<String> {
//        RobotStatus.speakContinue!!.postValue(0)
        Universal.taskNum = 0
        BaiduTTSHelper.getInstance().stop()
        Universal.ExplainSpeak = ArrayList()
        RobotStatus.progress.postValue(0)
        if (Universal.ExplainSpeak != null) {
            Universal.ExplainSpeak.clear()
        }
        if (Universal.taskQueue != null) {
            Universal.taskQueue.clear()
        }
        Universal.ExplainLength = text!!.length
        LogUtil.i("总内容长度: ${text.length}")
        val pattern = "(?<=[，；？！。,.;])".toRegex()
        val splitText = text.split(pattern)
        for (i in splitText.indices) {
            val subText = text.split(pattern)[i]
            if (subText.length > 45) {
                val subTextList = subText.chunked(45)
                for (sub in subTextList) {
                    Universal.ExplainSpeak.add(sub.length)
                    Universal.taskQueue.enqueue(sub)
                }
            } else {
                Universal.ExplainSpeak.add(subText.length)
                Universal.taskQueue.enqueue(subText)
            }
            Universal.taskQueue.resume()
            LogUtil.d("列表长度内容: ${Universal.ExplainSpeak}")
            LogUtil.i("分割内容：${splitText[i]} 内容长度：${splitText[i].length}")
        }
        return splitText
    }

    /**
     * 倒计时
     * @param timer 倒计时所需时间
     * @param type 任务类型
     */
    fun downTimer(timer:Int, type : Int,controller: NavController) {
        RobotStatus.speakNumber.postValue("")
        countDownTimer = MyCountDownTimer(
            millisInFuture = timer * 1000L, // 倒计时总时长，单位为毫秒
            countDownInterval = 1000, // 倒计时间隔，单位为毫秒
            onTick = { millisUntilFinished ->
                // 更新 UI，显示剩余时间
                val seconds = millisUntilFinished / 1000
                LogUtil.i("倒计时器：${seconds}s")

            },
            onFinish = {

                RobotStatus.progress.postValue(0)
                hasArrive = false
                RobotStatus.ready.postValue(0)
                if (type == 1){
                    pageJump(controller)
                }else{
                    TaskNext.setToDo("1")
                    splitTextByPunctuation(QuerySql.ShoppingConfig().completePrompt!!)
//                    BillManager.currentBill()?.executeNextTask()
                }
            }
        )
    }

    fun finishTask() {
        MainScope().launch {
            for (iTaskBill in BillManager.billList()) {
                iTaskBill.earlyFinish()
            }
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)

//            BaiduTTSHelper.getInstance().stop()
            TaskNext.setToDo("0")
            RobotStatus.ArrayPointExplan.postValue(0)
        }
    }

    fun secondBusinessScreenModel( mData: ShoppingActionDB?) {
        var file  = ""
        if (mData!!.bigScreenConfig?.videoFile !=null){
            file = mData.bigScreenConfig?.videoFile.toString()
        }else if (mData.bigScreenConfig?.imageFile!=null){
            file = mData.bigScreenConfig?.imageFile.toString()
        }
        RobotStatus.businessBigModel?.postValue(
            SecondModel(
                picPlayTime = mData.bigScreenConfig?.picPlayTime,
                file = file,
                type = mData.bigScreenConfig?.type,
                textPosition = mData.bigScreenConfig?.textPosition,
                fontLayout = mData.bigScreenConfig?.fontLayout,
                fontContent = mData.bigScreenConfig?.fontContent.toString(),
                fontBackGround = mData.bigScreenConfig?.fontBackGround.toString(),
                fontColor = mData.bigScreenConfig?.fontColor,
                fontSize = mData.bigScreenConfig?.fontSize,
                picType = mData.bigScreenConfig?.picType,
                videolayout = mData.bigScreenConfig?.videolayout,
                videoAudio = mData.bigScreenConfig?.videoAudio
            )
        )
        RobotStatus.sdScreenStatus!!.postValue(4)
        LogUtil.i("图片位置：${mData.bigScreenConfig?.imageFile.toString()}")
    }
    fun secondGuideScreenModel( mData: GuideFoundationConfigDB?) {
        var file  = ""
        if (mData!!.bigScreenConfig?.videoFile !=null){
            file = mData.bigScreenConfig?.videoFile.toString()
        }else if (mData.bigScreenConfig?.imageFile!=null){
            file = mData.bigScreenConfig?.imageFile.toString()
        }
        RobotStatus.businessBigModel?.postValue(
            SecondModel(
                picPlayTime = mData.bigScreenConfig?.picPlayTime,
                file = file,
                type = mData.bigScreenConfig?.type,
                textPosition = mData.bigScreenConfig?.textPosition,
                fontLayout = mData.bigScreenConfig?.fontLayout,
                fontContent = mData.bigScreenConfig?.fontContent.toString(),
                fontBackGround = mData.bigScreenConfig?.fontBackGround.toString(),
                fontColor = mData.bigScreenConfig?.fontColor,
                fontSize = mData.bigScreenConfig?.fontSize,
                picType = mData.bigScreenConfig?.picType,
                videolayout = mData.bigScreenConfig?.videolayout,
                videoAudio = mData.bigScreenConfig?.videoAudio
            )
        )
        RobotStatus.sdScreenStatus!!.postValue(3)
        LogUtil.i("图片位置：${mData.bigScreenConfig?.imageFile.toString()}")
    }


    fun pageJump(controller : NavController) {
        when (FunctionSkip.selectFunction()) {
            0 -> {
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, R.id.GuideFragment)
                })

                LogUtil.i("智能引领")
            }

            1 -> {
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, R.id.ExplanationFragment)
                })
                LogUtil.i("智能讲解")
            }

            2 -> {
                LogUtil.i("智能问答")
            }

            3 -> {
                LogUtil.i("轻应用")
            }

            5 -> {
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, R.id.businessFragment)
                })
                LogUtil.i("导购")
            }

            4 -> {
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, R.id.homeFragment)
                })
                LogUtil.i("主页")
            }

            -1 -> {
                Toast.makeText(MyApplication.context, "请勾选主页面功能模块", Toast.LENGTH_SHORT).show()
            }
        }
    }
}