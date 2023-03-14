package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe 广告配置
 */
data class AdvertisingConfig(
    /**
    {
    "type": "replyAdvertisementConfig",
    "timeStamp": 123456789621,
    "argConfig":{...}
    }
     */
    val timeStamp: Long?,
    val argConfig: TopLevelConfig? = null

)
