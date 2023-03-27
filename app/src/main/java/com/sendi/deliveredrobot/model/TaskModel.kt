package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.navigationtask.ITaskBill
import com.sendi.deliveredrobot.room.entity.QueryAllPointEntity
import com.sendi.deliveredrobot.room.entity.QueryPointEntity

data class TaskModel(
    var location: QueryPointEntity? = null,
    var location2: QueryPointEntity? = null,
    var remoteOrderModel: RemoteOrderModel? = null,
    var remoteOrderType: String = "",
    var endTarget: String = "",
    var taskId: String = "",
    var bill: ITaskBill? = null,
    var elevator: String? = null,
    var walkSpeak : String? = null,//途径播报内容
    var walkMp3 : String? = null,//途径播报Mp3
    var arraySpeak : String? = null,//到点播报
    var arrayMp3 : String? = null//到点播报mp3
)
