package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.navigationtask.ITaskBill
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
)
