package com.sendi.deliveredrobot.model

import com.google.gson.Gson

data class UploadMapDataModel(
    val areas: List<Area>,
    val curMapName: String,
    //待命点名称
    val waitingPointName : String,
    //充电桩点名称
    val chargePointName : String,
    val maps: List<Map>,
    val type: String = "uploadMapData"
){
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

data class Area(
    val id: Int,
    val name: String
)

data class Map(
    val floorList: List<Floor>,
    val mapName: String,
    val mapTimeStamp: Long
)

data class Floor(
    val floorName: String,
    val pointList: List<Point>
)

data class Point(
    val areaId: Int,
    val pointName: String,
    val w: Double,
    val x: Double,
    val y: Double
)