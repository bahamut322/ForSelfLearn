package com.sendi.deliveredrobot.model

data class ReplyElevatorListModel(
    val transferMark: Int,
    val elevatorList: Array<ElevatorModel>
){
    data class ElevatorModel(
        val elevator: String,
        val floorList: Array<FloorModel>,
        var floorNameList: Array<String>,
        var transferList: Array<String>
    ){
        data class FloorModel(
            val name: String,
            val score: Double,
            val transfer: Int
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ElevatorModel

            if (elevator != other.elevator) return false
            if (!floorList.contentEquals(other.floorList)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = elevator.hashCode()
            result = 31 * result + floorList.contentHashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReplyElevatorListModel

        if (transferMark != other.transferMark) return false
        if (!elevatorList.contentEquals(other.elevatorList)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transferMark.hashCode()
        result = 31 * result + elevatorList.contentHashCode()
        return result
    }

}
