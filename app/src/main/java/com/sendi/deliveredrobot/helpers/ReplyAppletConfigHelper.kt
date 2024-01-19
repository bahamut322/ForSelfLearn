package com.sendi.deliveredrobot.helpers

import android.util.Log
import com.google.gson.Gson
import com.sendi.deliveredrobot.entity.Table_Applet_Config
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.model.ApplicationModel
import com.sendi.deliveredrobot.model.ReplyAppletConfigModel
import com.sendi.deliveredrobot.utils.LogUtil
import org.litepal.LitePal
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ReplyAppletConfigHelper {
    val gson = Gson()
    /**
     * @description 更新机器人轻应用配置，比对是否存在这条记录，如果存在则更新，不存在则插入，如果时间戳为-1则删除
     */
    fun replyAppletConfig(json: String) {
        LogUtil.i(json)
        val appletConfig = gson.fromJson(json, ReplyAppletConfigModel::class.java)
        appletConfig.applets?.forEach {
            if (LitePal.isExist(Table_Applet_Config::class.java,"appletId = ${it.appletId}")) {
                //如果存在，update
                LitePal.deleteAll(Table_Applet_Config::class.java,"appletId = ${it.appletId}")
            }
            if ((it.timeStamp ?: 0) > -1) {
                val applet = Table_Applet_Config.create(it)
                applet.save()
                applet.bigScreenConfig?.save()
            }
        }
    }

    /**
     * @description 获取机器人回复配置
     */
    suspend fun queryAppletConfig(): List<String> = suspendCoroutine{
        it.resume(listOf())
    }

    /**
     * @description 获取时间戳String
     */
    suspend fun queryAppletConfigTimeStamp(): String = suspendCoroutine {
        it.resume(QuerySql.queryAppletIdList().toString() )
    }

    suspend fun queryApplicationModelList(): List<ApplicationModel> = suspendCoroutine {
        it.resume(QuerySql.queryApplicationModelList())
    }
}