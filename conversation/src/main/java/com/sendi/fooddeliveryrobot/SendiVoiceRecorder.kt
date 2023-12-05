package com.sendi.fooddeliveryrobot

class SendiVoiceRecorder: BaseVoiceRecorder() {
    override fun initAudioChannel(): BaseAudioChannel? {
        return SendiAudioChannel(audioRecorder!!)
    }
}