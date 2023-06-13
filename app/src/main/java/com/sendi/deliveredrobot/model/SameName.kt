package com.sendi.deliveredrobot.model

/**
 * @Author Swn
 * @describe
 * @Data 2023-05-09 11:08
 */
data class SameName(
    var SameAll: List<String?>? = null,
    //第一个数组中独有的文件
    var SameOne: List<String?>? = null,
    //第二个数组中独有的文件
    var SameTwo: List<String?>? = null
)
