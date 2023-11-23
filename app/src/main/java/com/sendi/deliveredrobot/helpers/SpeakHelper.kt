package com.sendi.deliveredrobot.helpers

import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import kotlinx.coroutines.MainScope

/**
 *   @author: heky
 *   @date: 2021/8/12 14:52
 *   @describe: 语音播报包装类
 */
object SpeakHelper {
    val mainScope = MainScope()

    fun speak(msg: String) {
        if(RobotStatus.currentStatus == TYPE_EXCEPTION) return
        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
        if (BuildConfig.IS_SPEAK) {
            stop()
            AudioMngHelper(MyApplication.context).setVoice100(QuerySql.QueryBasic().voiceVolume)
//            mainScope.launch(Dispatchers.IO) {
                BaiduTTSHelper.getInstance().speak(msg)
//            }
        }
    }

    fun speakWithoutStop(msg: String) {
        if(RobotStatus.currentStatus == TYPE_EXCEPTION) return
        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
        if (BuildConfig.IS_SPEAK) {
//            mainScope.launch(Dispatchers.IO) {
            var result = msg
            val list = ArrayList<String>()
            if(result.length > 60){
                while(result.length > 60){
                    val result1 = result.substring(0,60)
                    result = result.substring(60)
                    list.add(result1)
                }
                if(result.isNotEmpty()){
                   list.add(result)
                }
            }else{
                list.add(result)
            }
            list.map {
                BaiduTTSHelper.getInstance().speaks(it, "speakWithoutStop")
            }
//            }
        }
    }

    fun stop() {
//        mainScope.launch(Dispatchers.IO) {
            BaiduTTSHelper.getInstance().stop()
//        }
    }
}