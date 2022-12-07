package com.sendi.deliveredrobot.utils

import android.os.Environment
import java.io.File

/**
 * @description 文件处理类
 * @author heky
 * @date 2021-12-15
 */
object FileUtil {

    /**
     * @description 遍历Log文件夹，大于30天则删除
     */
    fun checkAndDeleteLogFilesCache(){
        val time = System.currentTimeMillis()
        val directory = File("${Environment.getExternalStorageDirectory()}/logger")
        if (!directory.exists()) return
        directory.listFiles()?.map {
            if((time - it.lastModified()) / 1000 / 60 / 60 / 24 > 30){
                //如果相差大于30天
                it.delete()
            }
        }
    }
}