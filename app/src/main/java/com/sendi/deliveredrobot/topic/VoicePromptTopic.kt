package com.sendi.deliveredrobot.topic

import android.os.Environment
import com.iflytek.vtncaetest.utils.CopyAssetsUtils
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.ExplainStatusModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.task.AdvanceGuidingTask
import com.sendi.deliveredrobot.navigationtask.task.GuidingTask
import com.sendi.deliveredrobot.navigationtask.task.OnTheWayExplainTask
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import navigation_base_msgs.VoicePrompt
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.thread

object VoicePromptTopic {
    private val BLOCK_GRADE_1 = MyApplication.instance!!.resources.getString(R.string.please_do_not_crowd_me_1)
    private val BLOCK_GRADE_2 = MyApplication.instance!!.resources.getString(R.string.please_do_not_crowd_me_2)
    private val BLOCK_GRADE_3 = MyApplication.instance!!.resources.getString(R.string.please_do_not_crowd_me_3)
    private val BLOCK_GRADE_4 = MyApplication.instance!!.resources.getString(R.string.please_do_not_crowd_me_4)
    private val TOO_CLOSE_TO_OBSTACLE = MyApplication.instance!!.getString(R.string.too_close_to_obstacle)
    private val THERE_IS_A_OBSTACLE_HALF_METER_FAR_AWAY = MyApplication.instance!!.getString(R.string.there_is_a_obstacle_half_meter_far_away)
    private val mainScope = MainScope()
    private var voiceContent = ""
    private var timer: Timer? = null
    private const val VOICE_DIR = "/sdcard/Android/data/com.sendi.fooddeliveryrobot"
    private const val ASSETS_DIR = "mp3"
    private const val MALE_MANDARIN_DIR = "/male_mandarin"
    private const val FEMALE_MANDARLIN_DIR = "/female_mandarin"
    private const val CHILD_MANDARLIN_DIR = "/child_mandarin"
    private const val FEMALE_CANTONESE_DIR = "/female_cantonese"

    private var voicePathDir: String? = null
        get(){
            val path = when(SpeakHelper.getType()){
                SpeakHelper.TYPE_BAIDU -> {
                    when (RobotStatus.robotConfig?.value?.audioType) {
                        1 -> "${VOICE_DIR}${FEMALE_MANDARLIN_DIR}"
                        2 -> "${VOICE_DIR}${MALE_MANDARIN_DIR}"
                        3 -> "${VOICE_DIR}${CHILD_MANDARLIN_DIR}"
                        4 -> "${VOICE_DIR}${FEMALE_MANDARLIN_DIR}"
                        else -> "${VOICE_DIR}${FEMALE_MANDARLIN_DIR}"
                    }
                }
                SpeakHelper.TYPE_XTTS -> {
                    when (RobotStatus.robotConfig?.value?.audioType) {
                        1 -> "${VOICE_DIR}${FEMALE_MANDARLIN_DIR}"
                        2 -> "${VOICE_DIR}${MALE_MANDARIN_DIR}"
                        3 -> "${VOICE_DIR}${FEMALE_MANDARLIN_DIR}"
                        4 -> "${VOICE_DIR}${FEMALE_CANTONESE_DIR}"
                        else -> "${VOICE_DIR}${FEMALE_MANDARLIN_DIR}"
                    }
                }else -> {
                    null
                }
            }
            return path
        }

    private var voicePath: String? = null


    fun handle(rosResult: RosResult<*>?) {
        CopyAssetsUtils.copyAssetFolder(MyApplication.instance,ASSETS_DIR,VOICE_DIR)
        // 挡路状态
        //                        LogUtil.i("VOICE_PROMPT_TOPIC-->${rosResult.response}")
        if (rosResult == null) return
        val voicePrompt = rosResult.response as VoicePrompt
        // 当前被挡情况
        // 被挡等级
        val grade: Int = voicePrompt.grade
        val task = BillManager.currentBill()?.currentTask()
        when (voicePrompt.type) {
            1 -> {
                AudioMngHelper(MyApplication.context).setVoice100(QuerySql.QueryBasic().voiceVolume)
                // 正常运行
                // 取消等待的语音
                //                                navController.popBackStack()
                if (isDialogShowing()) {
                    DialogHelper.containDialog.dismiss()
                }
                when (task) {
                    is OnTheWayExplainTask -> {
                        // 如果是OnTheWayExplainTask，判断播报类型是文字orMP3，为了不互相影响，选取相反的方式来播报围堵语音
                        when (task.type) {
                            ExplainStatusModel.TYPE_MP3 -> {
                                // 播文字
                                MediaPlayerHelper.getInstance().resume()
                            }

                            ExplainStatusModel.TYPE_TEXT -> {
                                // 播mp3
                                SpeakHelper.resume()
                            }
                        }
                    }

                    is GuidingTask -> {
                        // 如果是GuidingTask，则直接播报mp3
                        SpeakHelper.resume()
                    }
                }
                thread {
                    Thread.sleep(2000)
                    resetTimer()
                }
            }

            2 -> { // 太靠近障碍物，不可行走，进行左右旋转，寻找方向
                voiceContent = when (grade) {
                    2 -> BLOCK_GRADE_1
                    3 -> BLOCK_GRADE_2
                    else -> BLOCK_GRADE_1
                }
                voicePath = when (grade) {
                    2 -> "${voicePathDir}/1.mp3"
                    3 -> "${voicePathDir}/2.mp3"
                    else -> null
                }
                ToastUtil.show(TOO_CLOSE_TO_OBSTACLE)
                LogUtil.i(TOO_CLOSE_TO_OBSTACLE)
                resetTimer()
//                SpeakHelper.stop()
                when (task) {
                    is OnTheWayExplainTask -> {
                        // 如果是OnTheWayExplainTask，判断播报类型是文字orMP3，为了不互相影响，选取相反的方式来播报围堵语音
                        when (task.type) {
                            ExplainStatusModel.TYPE_MP3 -> {
                                // 播文字
                                MediaPlayerHelper.getInstance().pause()
                            }

                            ExplainStatusModel.TYPE_TEXT -> {
                                // 播mp3
                                SpeakHelper.pause()
                            }
                        }
                    }

                    is GuidingTask -> {
                        // 如果是GuidingTask，则直接播报mp3
                        SpeakHelper.pause()
                    }
                }
                playAudio()

            }

            3 -> { // 前方0.5m外有障碍物挡住，直接停下来等待
                voiceContent = when (grade) {
                    1 -> BLOCK_GRADE_3
                    2 -> BLOCK_GRADE_4
                    else -> BLOCK_GRADE_3
                }
                voicePath = when (grade) {
                    1 -> "${voicePathDir}/3.mp3"
                    2 -> "${voicePathDir}/4.mp3"
                    else -> null
                }
                ToastUtil.show(THERE_IS_A_OBSTACLE_HALF_METER_FAR_AWAY)
                LogUtil.i(THERE_IS_A_OBSTACLE_HALF_METER_FAR_AWAY)
//                if (isDialogShowing()) return
                DialogHelper.containDialog.show()
                resetTimer()
                when (task) {
                    is OnTheWayExplainTask -> {
                        // 如果是OnTheWayExplainTask，判断播报类型是文字orMP3，为了不互相影响，选取相反的方式来播报围堵语音
                        when (task.type) {
                            ExplainStatusModel.TYPE_MP3 -> {
                                // 播文字
                                MediaPlayerHelper.getInstance().pause()
                            }

                            ExplainStatusModel.TYPE_TEXT -> {
                                // 播mp3
                                SpeakHelper.pause()
                            }
                        }
                    }

                    is GuidingTask -> {
                        // 如果是GuidingTask，则直接播报mp3
                        SpeakHelper.pause()
                    }
                }
                playAudio()
            }
        }
    }

    private fun isDialogShowing(): Boolean {
        return DialogHelper.containDialog != null && DialogHelper.containDialog.isShowing
    }

    private fun playAudio() {
        timer?.schedule(object : TimerTask() {
            override fun run() {
                if (RobotStatus.stopButtonPressed.value != RobotCommand.STOP_BUTTON_PRESSED) {
                    mainScope.launch(Dispatchers.IO) {
                        if (RobotStatus.currentStatus == TYPE_EXCEPTION) return@launch
                        when (val task = BillManager.currentBill()?.currentTask()) {
                            is OnTheWayExplainTask -> {
                                // 如果是OnTheWayExplainTask，判断播报类型是文字orMP3，为了不互相影响，选取相反的方式来播报围堵语音
                                when (task.type) {
                                    ExplainStatusModel.TYPE_MP3 -> {
                                        // 播文字
//                                        MediaPlayerHelper.getInstance().pause()
                                        SpeakHelper.speak(voiceContent)
                                    }

                                    ExplainStatusModel.TYPE_TEXT -> {
                                        // 播mp3
//                                        SpeakHelper.pause()
                                        voicePath?.let {
                                            MediaPlayerHelper.getInstance().play(it)
                                        }
                                     }

                                     else -> {
                                         voicePath?.let {
                                             MediaPlayerHelper.getInstance().play(it)
                                         }
                                     }
                                }
                            }

                            is GuidingTask -> {
                                // 如果是GuidingTask，则直接播报mp3
//                                SpeakHelper.pause()
                                voicePath?.let {
                                    MediaPlayerHelper.getInstance().play(it)
                                }
                            }

                            is AdvanceGuidingTask -> {
                                voicePath?.let {
                                    MediaPlayerHelper.getInstance().play(it)
                                }
                            }
                        }
//                        SpeakHelper.speakWithoutStop(voiceContent)
                    }
                }
            }
        }, Date(), 6000)
    }

    private fun resetTimer() {
        timer?.cancel()
        timer = Timer()
    }
}