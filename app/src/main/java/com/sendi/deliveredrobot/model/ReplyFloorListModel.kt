package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2022-10-18
 * @description 云平台下发楼层列表
 */
data class ReplyFloorListModel(
    val floorList: Array<FloorModel>? = null,
    val elevatorList: Array<String>? = null

){
    data class FloorModel(
        val floor: String,
        val score: Double
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReplyFloorListModel

        if (!floorList.contentEquals(other.floorList)) return false
        if (!elevatorList.contentEquals(other.elevatorList)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = floorList?.contentHashCode() ?: 0
        result = 31 * result + (elevatorList?.contentHashCode() ?: 0)
        return result
    }


}
