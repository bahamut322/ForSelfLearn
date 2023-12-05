package com.sendi.fooddeliveryrobot

class AiXiaoYueVoiceRecorder:BaseVoiceRecorder() {
    override fun initAudioChannel(): BaseAudioChannel? {
        return SendiAudioChannel(audioRecorder!!)
    }
}