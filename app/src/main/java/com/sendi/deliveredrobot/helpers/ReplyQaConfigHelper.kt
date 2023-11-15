package com.sendi.deliveredrobot.helpers

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.sendi.deliveredrobot.model.ReplyQaConfigModel

/**
 * @author heky
 * @date 2023/11/14
 * @description 机器人回复配置
 */
object ReplyQaConfigHelper {
    private val gson = Gson()
    private const val ITEM_COUNTS = 10
    val replyQaConfigLiveData = MutableLiveData<List<String>>()
    fun replyQaConfig(json: String){
        val replyQaConfigModel = gson.fromJson(json, ReplyQaConfigModel::class.java)
        if (replyQaConfigModel != null) {
            val guideTexts = replyQaConfigModel.guideTexts
            val finalTexts = if((guideTexts?.size ?: 0) < ITEM_COUNTS){
                val size = ITEM_COUNTS - (guideTexts?.size ?: 0)
                val addList = replyQaConfigModel.standardQuestions?.shuffled()?.slice(0..size)
                guideTexts?.plus(addList?: arrayListOf())
            }else{
                guideTexts
            }
            replyQaConfigLiveData.value = finalTexts?.shuffled()?.slice(0..ITEM_COUNTS)?: arrayListOf()
        }
    }
}