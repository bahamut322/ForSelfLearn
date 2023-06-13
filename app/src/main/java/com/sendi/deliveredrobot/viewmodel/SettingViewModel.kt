package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.ViewModel
import com.baidu.tts.client.SpeechSynthesizer
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.baidutts.util.OfflineResource
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.entity.Universal
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class SettingViewModel : ViewModel() {
    private fun getParam(speak:String): Map<String, String> {
        return HashMap<String, String>().apply {
            this[SpeechSynthesizer.PARAM_SPEAKER] = "4"
            this[SpeechSynthesizer.PARAM_VOLUME] = "15"
            this[SpeechSynthesizer.PARAM_SPEED] = speak
            this[SpeechSynthesizer.PARAM_PITCH] = "5"
        }
    }

    /**
     * 音色
     */
    fun randomVoice(i: Int,speak: String) {
        val params = getParam(speak)
        if (i == 1) {
            BaiduTTSHelper.getInstance().setParam(params, OfflineResource.VOICE_FEMALE)//女
        } else if (i == 2) {
            BaiduTTSHelper.getInstance().setParam(params, OfflineResource.VOICE_MALE)//男
        } else if (i == 3) {
            BaiduTTSHelper.getInstance().setParam(params, OfflineResource.VOICE_DUYY)//童
        }
    }

    /**
     * 语速
     */

    fun timbres(speed: String) {
        if ("男声" == QuerySql.QueryBasic().robotMode) {
            randomVoice(2, speed)
        } else if ("女声" == QuerySql.QueryBasic().robotMode) {
            randomVoice(1, speed)
        } else if ("童声" == QuerySql.QueryBasic().robotMode) {
            randomVoice(3, speed)
        }
    }
    fun isNumCharOne(num:Int): Boolean {
        val file = File(Universal.SelfCheck)
        val inputStream = FileInputStream(file)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String? = bufferedReader.readLine()
        while (line != null) {
            stringBuilder.append(line)
            line = bufferedReader.readLine()
        }
        return stringBuilder.toString().length >= 8 && stringBuilder.toString()[num] == '1'
    }
}