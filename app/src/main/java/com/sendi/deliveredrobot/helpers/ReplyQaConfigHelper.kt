package com.sendi.deliveredrobot.helpers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.sendi.deliveredrobot.entity.QaConfigDB
import com.sendi.deliveredrobot.entity.ShoppingActionDB
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.model.ReplyQaConfigModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.litepal.LitePal

/**
 * @author heky
 * @date 2023/11/14
 * @description 机器人回复配置
 */
object ReplyQaConfigHelper {
    private val gson = Gson()
    private const val ITEM_COUNTS = 10
    fun replyQaConfig(json: String) {
        Log.d("TAG", "replyQaConfig: $json")
        LitePal.deleteAll(QaConfigDB::class.java)
        val qaConfigDB = QaConfigDB()
        qaConfigDB.qaJson = json
        qaConfigDB.save()
    }

    /**
     * @description 获取机器人回复配置
     */
    suspend fun queryReplyConfig(): List<String> = coroutineScope {
        withContext(Dispatchers.IO){
            val json = QuerySql.selectQaConfig()
            return@withContext getReplyQaConfig(json)
        }
    }


    private fun getReplyQaConfig(json: String): List<String> {
        val replyQaConfigModel = gson.fromJson(json, ReplyQaConfigModel::class.java)
        return if(replyQaConfigModel != null) {
            val guideTexts = replyQaConfigModel.guideTexts
            val finalTexts = if ((guideTexts?.size ?: 0) < ITEM_COUNTS) {
                val size = ITEM_COUNTS - (guideTexts?.size ?: 0)
                val addList = replyQaConfigModel.standardQuestions?.shuffled()?.slice(0..size)
                guideTexts?.plus(addList ?: arrayListOf())
            } else {
                guideTexts
            }
            finalTexts?.shuffled()?.slice(0..ITEM_COUNTS) ?: arrayListOf()
        }else{
            arrayListOf()
        }
    }
}