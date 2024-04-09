package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2024-03-28
 * @param
 */
data class ExplainStatusModel(
    val routeLiveData: MyResultModel?,  //途径点信息
    val status: Int,                    //播报状态
    val type: Int,                      //播报类型
    val position: Int,                  //队列中的位置（队列顺序固定）
    val textProgress: Int = -1          //到点TEXT播报进度
){
    companion object{
        const val STATUS_ON_THE_WAY_BEFORE = 0  //途径播报开始前
        const val STATUS_ON_THE_WAY_PROCESS = 1 //途径播报进行中
        const val STATUS_ARRIVE_BEFORE = 2      //到达播报开始前
        const val STATUS_ARRIVE_PROCESS = 3     //到达播报进行中
        const val TYPE_TEXT = 0
        const val TYPE_MP3 = 1
    }
}
