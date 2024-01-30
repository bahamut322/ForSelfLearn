package com.sendi.deliveredrobot.viewmodel

import androidx.lifecycle.ViewModel
import com.sendi.deliveredrobot.entity.Table_Big_Screen
import com.sendi.deliveredrobot.helpers.SecondScreenManageHelper
import com.sendi.deliveredrobot.model.SecondModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.LogUtil

/**
 * @Author Swn
 * @Data 2024/1/16
 * @describe 迎宾ViewModel
 */
class GreetViewModel : ViewModel() {

    fun greetBigScreenModel(mData: Table_Big_Screen?) {
        var file = ""
        if (mData?.videoFile != null) {
            file = mData.videoFile.toString()
        } else if (mData?.imageFile != null) {
            file = mData.imageFile.toString()
        }
        SecondScreenManageHelper.refreshSecondScreen(SecondScreenManageHelper.STATE_GREET,SecondModel(
            picPlayTime = mData?.picPlayTime,
            file = file,
            type = mData?.type ?: 0,
            textPosition = mData?.textPosition,
            fontLayout = mData?.fontLayout,
            fontContent = mData?.fontContent.toString(),
            fontBackGround = mData?.fontBackGround.toString(),
            fontColor = mData?.fontColor,
            fontSize = mData?.fontSize,
            picType = mData?.picType,
            videolayout = mData?.videolayout,
            videoAudio = mData?.videoAudio,
            false
        ))
        LogUtil.i("图片位置：${mData?.imageFile.toString()}")
    }
}