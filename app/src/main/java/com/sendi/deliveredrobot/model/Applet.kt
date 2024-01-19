package com.sendi.deliveredrobot.model

import com.google.gson.annotations.SerializedName

data class Applet(
    val icon: String?,
    @SerializedName("id")val appletId: Int?,
    val name: String?,
    val timeStamp: Long?,
    val type: Int?,
    val url: String?,
    val bigScreenConfig: TopLevelConfig?
)