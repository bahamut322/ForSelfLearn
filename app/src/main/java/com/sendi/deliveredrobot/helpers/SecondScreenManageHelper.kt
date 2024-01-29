package com.sendi.deliveredrobot.helpers

import android.annotation.SuppressLint
import com.sendi.deliveredrobot.BaseActivity
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.entity.Table_Advertising
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.model.DefaultModel
import com.sendi.deliveredrobot.model.SecondModel
import org.litepal.LitePal
import kotlin.properties.Delegates

/**
 * @author heky
 * @date 2024-01-26
 * @description 副屏管理类helper
 */
object SecondScreenManageHelper {
    @SuppressLint("StaticFieldLeak")
    var context: BaseActivity? = null

    fun init(context: BaseActivity) {
        this.context = context
    }

    private var secondModel: SecondModel? by Delegates.observable(null) { _, _, newValue ->
        if (context?.mPresentation != null) {
            context?.layoutThis(
                newValue?.picPlayTime ?: 30,
                newValue?.file ?: "",
                newValue?.type ?: 0,
                newValue?.textPosition ?: 0,
                newValue?.fontLayout ?: 0,
                newValue?.fontContent,
                newValue?.fontBackGround,
                newValue?.fontColor,
                newValue?.fontSize ?: 0,
                newValue?.picType ?: 0,
                newValue?.videolayout ?: 0,
                newValue?.videoAudio ?: 0,
                false
            )
        }
    }

    fun refreshSecondScreen(state: Int?, tempSecondModel: SecondModel? = null) {
        when (state) {
            0 -> {
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

            1 -> {
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

            2, 3, 4, 5 -> {
                val finalSecondModel = tempSecondModel ?: secondModel
                if (finalSecondModel?.type != 0) {
                    secondModel = finalSecondModel
                } else {
                    val defaultType = when (state) {
                        2 -> Universal.explainDefault
                        3 -> Universal.guideDefault
                        4 -> Universal.businessDefault
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
    private fun default(picFile: String, boolean: Boolean) {
        val defaultModel = DefaultModel(
            file = picFile,
            picPlayTime = 4,
            type = 1,
            textPosition = 0,
            fontLayout = 0,
            fontContent = "",
            fontBackGround = (R.color.white).toString(),
            fontColor = (R.color.white).toString(),
            fontSize = 1,
            picType = 1,
            videolayout = 0,
            videoAudio = 0
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
            boolean
        )
    }
}