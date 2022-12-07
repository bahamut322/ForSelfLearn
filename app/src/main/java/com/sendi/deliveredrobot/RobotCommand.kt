package com.sendi.deliveredrobot

/**
 *   @author: heky
 *   @date: 2021/8/22 16:17
 *   @describe: 机器人控制命令
 */
object RobotCommand {
    const val CMD_OPEN: Byte = 1
    const val CMD_CLOSE: Byte = 2
    const val CMD_CHECK: Byte = 3

    const val CMD_RUN = 1
    const val CMD_STOP = 2
    const val CMD_PAUSE = 3
    const val CMD_RESUME = 4
    const val CMD_OUT_DOCK = 5

    const val MANAGE_STATUS_STOP = 1 //终止
    const val MANAGE_STATUS_PAUSE = 2 //暂停
    const val MANAGE_STATUS_CONTINUE = 3 //继续

    var LIFT_CONTROL_TIME = 1
    var LIFT_OPEN_CONTROL_DOOR = 1
    var LIFT_RELEASE_CONTROL_DOOR = 2

    const val STOP_BUTTON_DEFAULT = -1
    const val STOP_BUTTON_UNPRESSED = 0
    const val STOP_BUTTON_PRESSED = 1
}