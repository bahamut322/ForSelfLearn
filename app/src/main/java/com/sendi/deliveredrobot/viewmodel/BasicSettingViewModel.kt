package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.room.entity.BasicConfig

/**
 * @describe 基础设置
 */
class BasicSettingViewModel : ViewModel() {
    var basicConfig: BasicConfig
    = BasicConfig(
        appVersion = "",
        brightness = 123,
        sendSpeed = 0.7f,
        sendVolume = 0,
        sendPutObjectTime = 30,
        sendWaitTakeObjectTime = 30,
        sendTakeObjectTime = 30,
        needTakeObjectPassword = 0,
        guideSpeed = 0.7f,
        guideVolume = 50,
        guideWalkPauseTime = 30,
        robotUseDeadLine = "",
        verifyPassword = "00000",
        wifiOpen = 0,
        sendVolumeLift = 60,
        sendVolumeLobby = 60,
        guideVolumeLift = 60,
        guideVolumeLobby = 60,
        sendModeOpen = 1,
        sendModeVerifyPassword = 1,
        guideModeOpen = 1,
        guideModeVerifyPassword = 1
    )
}