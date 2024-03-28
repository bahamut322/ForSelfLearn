package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2024-03-28
 * @param
 */
data class ExplainStatusModel(
    val routeLiveData: MyResultModel?,
    val status: Int,
    val type: Int,
    val position: Int
){
    companion object{
        const val STATUS_ON_THE_WAY_BEFORE = 0
        const val STATUS_ON_THE_WAY_PROCESS = 1
        const val STATUS_ARRIVE = 2
        const val TYPE_TEXT = 0
        const val TYPE_MP3 = 1
    }
}
