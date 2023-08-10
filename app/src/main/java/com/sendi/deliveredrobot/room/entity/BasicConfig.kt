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
    @ColumnInfo(name = "guide_mode_verify_password", defaultValue = "1")var guideModeVerifyPassword: Int,
    @ColumnInfo(name = "usher_volume", defaultValue = "60")var usherVolume:Int,
    @ColumnInfo(name = "usher_mode_open", defaultValue = "1")var usherModeOpen: Int,
    @ColumnInfo(name = "usher_mode_verify_password", defaultValue = "1")var usherModeVerifyPassword: Int,
    @ColumnInfo(name = "usher_id", defaultValue = "-1")var usherId: Int? = -1,
    @ColumnInfo(name = "usher_file_info_id", defaultValue = "-1")var usherFileInfoId: Int? = 0,
    @ColumnInfo(name = "usher_content", defaultValue = "欢迎光临")var usherContent: String? = "",
    @ColumnInfo(name = "usher_timing", defaultValue = "0")var usherTiming: Int = 0,
    @ColumnInfo(name = "usher_duration", defaultValue = "1.0")var usherDuration: Float = 1f,
    @ColumnInfo(name = "usher_speed", defaultValue = "0.6")var usherSpeed: Float = 0.6f,
    @ColumnInfo(name = "room_number_length", defaultValue = "4")var roomNumberLength: Int = 4
)
