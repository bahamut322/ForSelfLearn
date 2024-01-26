package com.sendi.deliveredrobot.helpers

import com.sendi.deliveredrobot.BaseActivity
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.entity.Table_Advertising
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.model.DefaultModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import org.litepal.LitePal

/**
 * @author heky
 * @date 2024-01-26
 * @description 副屏管理类helper
 */
object SecondScreenManageHelper {
    fun refreshSecondScreen(context: BaseActivity, state: Int?){
        when (state) {
            0 -> {
                val config = LitePal.findFirst(Table_Advertising::class.java)
                if (config != null && config.type != 0) {
                    context.layoutThis(
                        config.picPlayTime,
                        Universal.advertisement,
                        config.type,
                        config.textPosition,
                        config.fontLayout,
                        config.fontContent,
                        config.fontBackGround,
                        config.fontColor,
                        config.fontSize,
                        config.picType,
                        config.videolayout,
                        config.videoAudio,
                        true
                    )
                } else {
                    default(context, Universal.advDefault, true)
                }
            }
            1 -> {
                if (Universal.bigScreenType != 0) {
                    context.layoutThis(
                        Universal.picPlayTime,
                        Universal.Secondary,
                        Universal.bigScreenType,
                        Universal.textPosition,
                        Universal.fontLayout,
                        Universal.fontContent,
                        Universal.fontBackGround,
                        Universal.fontColor,
                        Universal.fontSize,
                        Universal.picTypeNum,
                        Universal.TempVideoLayout,
                        Universal.AllvideoAudio,
                        false
                    )
                } else {
                    default(context, Universal.usherDefault, false)
                }
            }
            2, 3, 4, 5 -> {
                val secondModel = RobotStatus.SecondModel!!.value
                if (secondModel?.type != 0) {
                    context.layoutThis(
                        secondModel?.picPlayTime!!,
                        secondModel.file,
                        secondModel.type!!,
                        secondModel.textPosition!!,
                        secondModel.fontLayout!!,
                        secondModel.fontContent,
                        secondModel.fontBackGround,
                        secondModel.fontColor,
                        secondModel.fontSize!!,
                        secondModel.picType!!,
                        secondModel.videolayout!!,
                        secondModel.videoAudio!!,
                        false
                    )
                } else {
                    val defaultType = when (state) {
                        2 -> Universal.explainDefault
                        3 -> Universal.guideDefault
                        4 -> Universal.businessDefault
                        else -> Universal.advDefault
                    }
                    default(context, defaultType, false)
                }
            }
            else -> {
                default(context, Universal.advDefault, true)
            }
        }
    }

    //默认+下载时大屏幕的样式
    private fun default(context:BaseActivity ,picFile: String,boolean: Boolean){
        val defaultModel = DefaultModel(file = picFile, picPlayTime = 4,type = 1, textPosition = 0, fontLayout = 0, fontContent = "", fontBackGround = (R.color.white).toString(), fontColor = (R.color.white).toString(), fontSize = 1, picType = 1, videolayout = 0, videoAudio = 0)
        context.layoutThis(
            defaultModel.picPlayTime!!,
            defaultModel.file,
            defaultModel.type!!,
            defaultModel.textPosition!!,
            defaultModel.fontLayout!!,
            defaultModel.fontContent,
            defaultModel.fontBackGround,
            defaultModel.fontColor,
            defaultModel.fontSize!!,
            defaultModel.picType!!,
            defaultModel.videolayout!!,
            defaultModel.videoAudio!!,
            boolean
        )
    }

}