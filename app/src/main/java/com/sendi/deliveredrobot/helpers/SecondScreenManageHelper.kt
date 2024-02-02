package com.sendi.deliveredrobot.helpers

import android.annotation.SuppressLint
import com.sendi.deliveredrobot.BaseActivity
import com.sendi.deliveredrobot.entity.Table_Advertising
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.model.DefaultModel
import com.sendi.deliveredrobot.model.SecondModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import org.litepal.LitePal
import kotlin.properties.Delegates

/**
 * @author heky
 * @date 2024-01-26
 * @description 副屏管理类helper
 */
object SecondScreenManageHelper {
    // 0:空闲 1:测温 2:讲解 3:引领 4:导购 5:迎宾 6:轻应用
    const val STATE_IDLE = 0
    const val STATE_TEMPERATURE = 1
    const val STATE_EXPLAIN = 2
    const val STATE_GUIDE = 3
    const val STATE_BUSINESS = 4
    const val STATE_GREET = 5
    const val STATE_APPLET = 6

    @SuppressLint("StaticFieldLeak")
    var context: BaseActivity? = null

    fun init(context: BaseActivity) {
        this.context = context
    }

    private var secondModel: SecondModel? by Delegates.observable(null) { _, _, newValue ->
        if (context?.mPresentation != null) {
            context?.mPresentation?.layoutThis(
                newValue?.picPlayTime ?: 30,
                newValue?.file ?: "",
                newValue?.type ?: 0,
                newValue?.textPosition ?: 0,
                newValue?.fontLayout ?: 0,
                newValue?.fontContent?:"",
                newValue?.fontBackGround?:"",
                newValue?.fontColor?:"",
                newValue?.fontSize ?: 0,
                newValue?.picType ?: 0,
                newValue?.videolayout ?: 0,
                newValue?.videoAudio ?: 0,
                false
            )
        }
    }

    fun refreshSecondScreen(state: Int?, tempSecondModel: SecondModel? = null) {
        RobotStatus.sdScreenStatus = state
        when (state) {
            STATE_IDLE -> {
                val config = LitePal.findFirst(Table_Advertising::class.java)
                if (config != null && config.type != 0) {
                    secondModel = SecondModel(
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
                    default(Universal.advDefault, true)
                }
            }

            STATE_TEMPERATURE -> {
                if (Universal.bigScreenType != 0) {
                    secondModel = SecondModel(
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
                    default(Universal.usherDefault, false)
                }
            }

            STATE_EXPLAIN,
            STATE_GUIDE,
            STATE_BUSINESS,
            STATE_GREET,
            STATE_APPLET -> {
                val finalSecondModel = tempSecondModel ?: secondModel
                if (finalSecondModel?.type != 0) {
                    secondModel = finalSecondModel
                } else {
                    val defaultType = when (state) {
                        STATE_EXPLAIN -> Universal.explainDefault
                        STATE_GUIDE -> Universal.guideDefault
                        STATE_BUSINESS -> Universal.businessDefault
                        STATE_GREET -> Universal.greetDefault
                        else -> Universal.advDefault
                    }
                    default(defaultType, false)
                }
            }

            else -> {
                default(Universal.advDefault, true)
            }
        }
    }

    //默认+下载时大屏幕的样式
    private fun default(picFile: String, adv: Boolean) {
        val defaultModel = DefaultModel(
            file = picFile
        )
        secondModel = SecondModel(
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
            adv
        )
    }
}