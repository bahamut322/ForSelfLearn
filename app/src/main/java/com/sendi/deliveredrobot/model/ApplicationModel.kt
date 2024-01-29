package com.sendi.deliveredrobot.model

/**
 * @Author Swn
 * @Data 2023/12/6
 * @describe 更多服务Model
 */
 data class ApplicationModel (
    var name : String? = "",
    var url : String? = "",
    var icon: String? = "",
    var secondModel :SecondModel? = null
 )

