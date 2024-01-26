package com.sendi.deliveredrobot.helpers

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.sendi.deliveredrobot.model.ReplyIntentModel
import com.sendi.deliveredrobot.utils.LogUtil

/**
 * @author heky
 * @date 2023/11/14
 * @description
 */
object ReplyIntentHelper {
    private val gson = Gson()
    val replyIntentLiveData = MutableLiveData<ReplyIntentModel>()
    fun replyIntent(json: String){
        val replyIntentModel = gson.fromJson(json, ReplyIntentModel::class.java)
        if (replyIntentModel != null) {
            replyIntentLiveData.value = replyIntentModel
        }
    }

    fun clearCache(){
        replyIntentLiveData.postValue(null)
    }
}