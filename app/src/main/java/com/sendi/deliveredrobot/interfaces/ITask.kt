package com.sendi.deliveredrobot.interfaces

import com.sendi.deliveredrobot.service.TaskStageEnum

interface ITask {
    fun configEnum(): TaskStageEnum
    suspend fun execute()
}