package com.sendi.deliveredrobot.helpers

import com.google.gson.Gson
import com.sendi.deliveredrobot.entity.Table_Applet_Config
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.model.ApplicationModel
import com.sendi.deliveredrobot.model.ReplyAppletConfigModel
import com.sendi.deliveredrobot.navigationtask.DownloadBill
import com.sendi.deliveredrobot.utils.InstallApkUtils
import com.sendi.deliveredrobot.utils.LogUtil
import org.litepal.LitePal
import org.litepal.extension.findAll
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ReplyAppletConfigHelper {
    val gson = Gson()
    private const val TYPE_APPLET_URL = 1
    private const val TYPE_APPLET_APK = 2
    private const val TYPE_APPLET_RICH_TEXT = 3
    /**
     * @description 更新机器人轻应用配置，比对是否存在这条记录，如果存在则更新，不存在则插入，如果时间戳为-1则删除
     */
    fun replyAppletConfig(json: String) {
        LogUtil.i(json)
        val appletConfig = gson.fromJson(json, ReplyAppletConfigModel::class.java)
        val uninstallList = mutableListOf<String>()
        val installList = mutableListOf<String>()
        appletConfig.applets?.forEach {
            if (LitePal.isExist(Table_Applet_Config::class.java,"appletid = ${it.appletId}")) {
                //如果存在，检查包名是否一致，如果不一致则卸载原包名，如果一致则判断时间戳是否为-1，如果为-1则卸载
                val tableAppletConfig = LitePal.where("appletId = ${it.appletId}")
                    .findFirst(Table_Applet_Config::class.java)
                val needUninstall = if (tableAppletConfig?.packageName != it.packageName) {
                    //卸载原包名
                    true
                } else {
                    var tempVar = false
                    if ((it.timeStamp ?: 0) == -1L) {
                        //卸载原包名
                        tempVar = true
                    }
                    tempVar
                }
                if (needUninstall) {
                    uninstallList.add(tableAppletConfig?.packageName ?: "")
                }
                LitePal.deleteAll(Table_Applet_Config::class.java,"appletid = ${it.appletId}")
            }
            if ((it.timeStamp ?: 0) > -1) {
                val applet = Table_Applet_Config.create(it)
                applet.save()
                applet.bigScreenConfig?.save()
                //如果包名未安装
                if (applet.type == TYPE_APPLET_APK && !InstallApkUtils.isPackageExist(it.packageName ?: "")) {
                    installList.add(it.packageName ?: "")
                }
            }
        }
        val installedList = LitePal.findAll<Table_Applet_Config>()
        uninstallList.filter { packageName ->
            !installList.contains(packageName) && (!installedList.any { it.packageName == packageName})
        }.map {
            val result = InstallApkUtils.uninstallApk(it)
            LogUtil.i("uninstall $it result $result")
        }
        installList.map {
            //下载，安装
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