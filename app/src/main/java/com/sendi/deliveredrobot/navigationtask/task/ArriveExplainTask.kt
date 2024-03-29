package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.helpers.ExplainManager
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.ExplainStatusModel
import com.sendi.deliveredrobot.model.MyResultModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.ExplainTaskBill
import com.sendi.deliveredrobot.service.PlaceholderEnum
import com.sendi.deliveredrobot.service.PlaceholderEnum.Companion.replaceText
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.MediaStatusManager
import kotlinx.coroutines.delay

/**
 * @author heky
 * @date 2024-03-29
 * @describe 到点讲解任务
 */
class ArriveExplainTask(taskModel: TaskModel): AbstractTask(taskModel) {
    private var route: MyResultModel? = null
    private var status = ExplainStatusModel.STATUS_ARRIVE_BEFORE
    private var type: Int = -1
    private var position: Int = -1
    init {
        if (taskModel.bill is ExplainTaskBill) {
            val bill = taskModel.bill as ExplainTaskBill
            route = bill.route
            position = bill.position
        }
    }
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.StartArrayBroadcast
    }

    override suspend fun execute() {
        if (route != null) {
            notifyFragmentUpdate()
            status = ExplainStatusModel.STATUS_ARRIVE_PROCESS
            if(route!!.explanationtext.isNullOrEmpty() && route!!.explanationvoice.isNullOrEmpty()){
                return
            }
            if (!route!!.explanationtext.isNullOrEmpty()) {
                type = ExplainStatusModel.TYPE_TEXT
                delay(1000)
                LogUtil.i("开始 ${taskModel?.endTarget?:""} 到点播报（文本）")
                val text = replaceText(
                    route?.explanationtext,
                    "",
                    route?.name,
                    route?.routename,
                    "智能讲解"
                )
                SpeakHelper.speakUserCallback = object : SpeakHelper.SpeakUserCallback {
                    override fun speakAllFinish() {
                        taskModel?.bill?.executeNextTask()
                        LogUtil.i("结束 ${taskModel?.endTarget?:""} 到点播报（文本）")
                    }

                    override fun progressChange(utteranceId: String, progress: Int) {
                        notifyFragmentUpdate(progress)
                    }
                }
                SpeakHelper.speakWithoutStop(text)
            }
            if (!route!!.explanationvoice.isNullOrEmpty()) {
                type = ExplainStatusModel.TYPE_MP3
                try {
                    LogUtil.i("开始 ${taskModel?.endTarget?:""} 到点播报（MP3）")
                    MediaPlayerHelper.getInstance().setOnCompletionListener {
                        // 2.监听途径播报进度，如果播报完成，则设置finished为true
                        LogUtil.i("结束 ${taskModel?.endTarget?:""} 到点播报（MP3）")
                        MediaStatusManager.stopMediaPlay(false)
                    }
                    MediaPlayerHelper.getInstance().play(route!!.walkvoice)
                } catch (ignored: Exception) {
                    LogUtil.i("异常 ${taskModel?.endTarget?:""} 到点播报（MP3）")
                }
            }
        }else{
            LogUtil.i("route为空")
            taskModel?.bill?.executeNextTask()
        }
    }

    /**
     * @describe 已到点，通知fragment更新
     */
    private fun notifyFragmentUpdate(textProgress:Int = -1){
        ExplainManager.explainStatusModel.postValue(
            ExplainStatusModel(
                routeLiveData = route,
                status = status,
                type = type,
                position = position,
                textProgress = textProgress
            )
        )
    }
}