package com.sendi.deliveredrobot.helpers

import android.util.Log
import com.apkfuns.logutils.LogUtils
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.utils.HanziToPinyin

object WakeupWordHelper {
    var wakeupWord: String? = QuerySql.robotConfig().wakeUpWord
        set(value) {
            field = value
            LogUtils.i("wakeupWord: $value")
            val resultList = HanziToPinyin.instance?.get(value?:"")
            val stringBuilder = StringBuilder()
            resultList?.map {
                Log.i("AudioChannel", it.toString())
                stringBuilder.append(it.target)
            }
            wakeupWordPinyin = stringBuilder.toString()
        }

    var wakeupWordPinyin: String? = "null"
}