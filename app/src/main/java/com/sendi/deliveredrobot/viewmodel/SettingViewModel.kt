package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.ViewModel
import com.baidu.tts.client.SpeechSynthesizer
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.baidutts.util.OfflineResource
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.BasicModel
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
        SpeakHelper.setParam(params, i)
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
        } else if ("粤语" == QuerySql.QueryBasic().robotMode) {
            randomVoice(4, speed)
        }
    }
    fun isNumCharOne(num:Int): Boolean {
        val file = File(Universal.SelfCheck)
        if (!file.exists()) {
            return false
        }
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
    fun settingData() : BasicModel{
        return QuerySql.QueryBasic()
    }
}