package com.sendi.deliveredrobot.room.entity

data class SendMapPoint(
    val mapTimeStamp: Long?,
    val mapName: String?,
    val floorList: List<SendFloor>
)

