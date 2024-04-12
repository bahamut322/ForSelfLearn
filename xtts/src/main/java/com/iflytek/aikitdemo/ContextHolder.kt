package com.iflytek.aikitdemo

import android.content.Context
import kotlin.properties.Delegates

/**
 * @Desc:
 * @Author leon
 * @Date 2023/2/24-11:01
 * Copyright 2023 iFLYTEK Inc. All Rights Reserved.
 */
class ContextHolder{

    companion object {
        var CONTEXT: Context by Delegates.notNull()
    }

//    override fun attachBaseContext(base: Context?) {
//        super.attachBaseContext(base)
//        MultiDex.install(this)
//    }
//    override fun onCreate() {
//        super.onCreate()
//        //仅仅作为测试性能用
//        connectMemoryStatsPipe()
//    }
}