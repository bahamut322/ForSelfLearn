package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "basic_config")
class BasicConfig(
    @PrimaryKey(autoGenerate = true) val id:Int = 0,
    @ColumnInfo(name = "app_version")var appVersion:String?,
    @ColumnInfo(name = "brightness") var brightness:Int?,
    @ColumnInfo(name = "send_speed")var sendSpeed:Float?,
    @ColumnInfo(name = "send_volume")var sendVolume:Int?,
    @ColumnInfo(name = "send_volume_2", defaultValue = "60")var sendVolumeLobby:Int,
    @ColumnInfo(name = "send_volume_3", defaultValue = "60")var sendVolumeLift:Int,
    @ColumnInfo(name = "send_put_object_time")var sendPutObjectTime:Int?,
    @ColumnInfo(name = "send_wait_take_object_time")var sendWaitTakeObjectTime:Int?,
    @ColumnInfo(name = "send_take_object_time")var sendTakeObjectTime:Int?,
    @ColumnInfo(name = "need_take_object_password")var needTakeObjectPassword:Int?, //0-不需要验证码，1-需要验证码
    @ColumnInfo(name = "guide_speed")var guideSpeed:Float?,
    @ColumnInfo(name="guide_volume")var guideVolume:Int?,
    @ColumnInfo(name = "guide_volume_2", defaultValue = "60")var guideVolumeLobby:Int,
    @ColumnInfo(name = "guide_volume_3", defaultValue = "60")var guideVolumeLift:Int,
    @ColumnInfo(name = "guide_walk_pause_time")var guideWalkPauseTime:Int?,
    @ColumnInfo(name = "robot_use_deadline")var robotUseDeadLine:String?,
    @ColumnInfo(name = "verify_password")var verifyPassword:String?,
    @ColumnInfo(name = "wifi_open")var wifiOpen:Int?,
    @ColumnInfo(name = "send_mode_open", defaultValue = "1")var sendModeOpen: Int,
    @ColumnInfo(name = "send_mode_verify_password", defaultValue = "1")var sendModeVerifyPassword: Int,
    @ColumnInfo(name = "guide_mode_open", defaultValue = "1")var guideModeOpen: Int,
    @ColumnInfo(name = "guide_mode_verify_password", defaultValue = "1")var guideModeVerifyPassword: Int
)
