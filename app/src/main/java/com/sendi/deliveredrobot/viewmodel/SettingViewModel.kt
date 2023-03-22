package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.ViewModel
import com.baidu.tts.client.SpeechSynthesizer
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.baidutts.util.OfflineResource

class SettingViewModel : ViewModel() {
    private fun getParam(): Map<String,String>{
        return HashMap<String, String>().apply {
            this[SpeechSynthesizer.PARAM_SPEAKER] = "4"
            this[SpeechSynthesizer.PARAM_VOLUME] = "15"
            this[SpeechSynthesizer.PARAM_SPEED] = "5"
            this[SpeechSynthesizer.PARAM_PITCH] = "5"
        }
    }

    private fun randomVoice(){
        val list = listOf(OfflineResource.VOICE_FEMALE, OfflineResource.VOICE_MALE, OfflineResource.VOICE_DUXY, OfflineResource.VOICE_DUYY)
        val index = (Math.random() * 100).toInt() % 4
        val params = getParam()
        BaiduTTSHelper.getInstance().setParam(params, list[index])
    }
}