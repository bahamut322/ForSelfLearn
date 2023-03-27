package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.ViewModel
import com.baidu.tts.client.SpeechSynthesizer
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.baidutts.util.OfflineResource
import com.sendi.deliveredrobot.entity.QuerySql

class SettingViewModel : ViewModel() {
    private fun getParam(): Map<String, String> {
        return HashMap<String, String>().apply {
            this[SpeechSynthesizer.PARAM_SPEAKER] = "4"
            this[SpeechSynthesizer.PARAM_VOLUME] = "15"
            this[SpeechSynthesizer.PARAM_SPEED] = "5"
            this[SpeechSynthesizer.PARAM_PITCH] = "5"
        }
    }

    fun randomVoice(i: Int) {
        val params = getParam()
        if (i == 1) {
            BaiduTTSHelper.getInstance().setParam(params, OfflineResource.VOICE_FEMALE)//女
        } else if (i == 2) {
            BaiduTTSHelper.getInstance().setParam(params, OfflineResource.VOICE_MALE)//男
        } else if (i == 3) {
            BaiduTTSHelper.getInstance().setParam(params, OfflineResource.VOICE_DUYY)//童
        }
    }
     fun timbres() {
        if ("男声" == QuerySql.QueryBasic().robotMode) {
            randomVoice(2)
        } else if ("女声" == QuerySql.QueryBasic().robotMode) {
            randomVoice(1)
        } else if ("童声" == QuerySql.QueryBasic().robotMode) {
            randomVoice(3)
        }
    }
}