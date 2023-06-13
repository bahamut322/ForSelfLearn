package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe 目标点列表
 */
data class pointConfigVOList(
    val name : String?,
    val walkText : String? = null,
    val explanation : String? = null,
    val walkVoice : String? = null,
    val explanationVoice : String? = null,
    val scope : Int?,
    val bigScreenConfig :TopLevelConfig? = null,
    val touchScreenConfig : TopLevelConfig? = null
)
