package com.sendi.deliveredrobot.topic

import android.content.Intent
import androidx.navigation.NavController
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.POP_BACK_STACK
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import navigation_base_msgs.VoicePrompt
import java.util.*

object VoicePromptTopic {
    private val BLOCK_GRADE_1 = MyApplication.instance!!.resources.getString(R.string.please_do_not_crowd_me_1)
    private val BLOCK_GRADE_2 = MyApplication.instance!!.resources.getString(R.string.please_do_not_crowd_me_2)
    private val BLOCK_GRADE_3 = MyApplication.instance!!.resources.getString(R.string.please_do_not_crowd_me_3)
    private val BLOCK_GRADE_4 = MyApplication.instance!!.resources.getString(R.string.please_do_not_crowd_me_4)
    private val TOO_CLOSE_TO_OBSTACLE = MyApplication.instance!!.getString(R.string.too_close_to_obstacle)
    private val THERE_IS_A_OBSTACLE_HALF_METER_FAR_AWAY = MyApplication.instance!!.getString(R.string.there_is_a_obstacle_half_meter_far_away)
    private val mainScope = MainScope()
    private var voiceContent = ""
    private  var timer: Timer? = null



    fun handle(
        rosResult: RosResult<*>?,
        navController: NavController
    ) {
        // 挡路状态
        //                        LogUtil.i("VOICE_PROMPT_TOPIC-->${rosResult.response}")
        val voicePrompt = rosResult?.response as VoicePrompt
        // 当前被挡情况
        // 被挡等级
        val grade: Int = voicePrompt.grade
        when (voicePrompt.type) {
            1 -> {
                // 正常运行
                // 取消等待的语音
                //                                navController.popBackStack()
                //此处硬编码判断当前页面是否是在ContainFragment中，暂未找到更优雅的方式
                if ("ContainFragment" == navController.currentDestination?.label) {
                    MyApplication.instance!!.sendBroadcast(Intent().apply {
                        action = ACTION_NAVIGATE
                        putExtra(NAVIGATE_ID, POP_BACK_STACK)
                    })
                }
                resetTimer()
            }
            2 -> { // 太靠近障碍物，不可行走，进行左右旋转，寻找方向
                voiceContent = when (grade) {
                    2 -> BLOCK_GRADE_1
                    3 -> BLOCK_GRADE_2
                    else -> BLOCK_GRADE_1
                }
                ToastUtil.show(TOO_CLOSE_TO_OBSTACLE)
                LogUtil.i(TOO_CLOSE_TO_OBSTACLE)
                resetTimer()
                SpeakHelper.stop()
                playAudio()

            }
            3 -> { // 前方0.5m外有障碍物挡住，直接停下来等待
                voiceContent = when (grade) {
                    1 -> BLOCK_GRADE_3
                    2 -> BLOCK_GRADE_4
                    else -> BLOCK_GRADE_3
                }
                ToastUtil.show(THERE_IS_A_OBSTACLE_HALF_METER_FAR_AWAY)
                LogUtil.i(THERE_IS_A_OBSTACLE_HALF_METER_FAR_AWAY)
                //此处硬编码判断当前页面是否是不在ContainFragment中，暂未找到更优雅的方式
                if ("ContainFragment" == navController.currentDestination?.label) return
                navigateToContainFragment()
                resetTimer()
                SpeakHelper.stop()
                playAudio()
            }
        }
    }

    private fun navigateToContainFragment(){
        MyApplication.instance!!.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, R.id.containFragment)
        })
    }

    private fun playAudio(){
        timer?.schedule(object : TimerTask(){
            override fun run() {
                if(RobotStatus.stopButtonPressed.value != RobotCommand.STOP_BUTTON_PRESSED){
                    mainScope.launch(Dispatchers.IO) {
                        if(RobotStatus.currentStatus == TYPE_EXCEPTION) return@launch
                        SpeakHelper.speakWithoutStop(voiceContent)
                    }
                }
            }
        }, Date(),6000)
    }

    private fun resetTimer(){
        timer?.cancel()
        timer = Timer()
    }
}