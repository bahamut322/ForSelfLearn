package com.sendi.deliveredrobot.navigationtask.task

import android.util.Log
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ExplainManager
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.ExplainStatusModel
import com.sendi.deliveredrobot.model.MyResultModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.ExplainTaskBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.PlaceholderEnum
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.MediaStatusManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * @author heky
 * @date 2024-03-27
 * @description 途径播报任务
 */
class OnTheWayExplainTask(taskModel: TaskModel): AbstractTask(taskModel) {
    private var finished = false
    private var route: MyResultModel? = null
    private var type: Int = -1
    private var position: Int = -1
    private var status = ExplainStatusModel.STATUS_ON_THE_WAY_BEFORE
    init {
        if (taskModel.bill is ExplainTaskBill) {
            val bill = taskModel.bill as ExplainTaskBill
            route = bill.route
            position = bill.position
        }
    }
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.StartChannelBroadcast
    }

    override suspend fun execute() {
        DialogHelper.loadingDialog.show()
        // 1.开始途径播报
        if (route != null) {
            notifyFragmentUpdate()
            ExplainManager.secondScreenModel(route)
            status = ExplainStatusModel.STATUS_ON_THE_WAY_PROCESS
            if(route!!.walktext.isNullOrEmpty() && route!!.walkvoice.isNullOrEmpty()){
                finishExplain()
                DialogHelper.loadingDialog.dismiss()
                LogUtil.i("walktext和walkvoice为空")
                return
            }
            if (!route!!.walktext.isNullOrEmpty()) {
                type = ExplainStatusModel.TYPE_TEXT
                delay(1000)
                LogUtil.i("开始 ${taskModel?.endTarget?:""} 途径播报（文本）")
                val text = PlaceholderEnum.replaceText(
                    route!!.walktext,
                    "",
                    route!!.name,
                    route!!.routename,
                    "智能讲解"
                )
                SpeakHelper.setUserCallback(object : SpeakHelper.SpeakUserCallback {
                    override fun speakAllFinish() {
                        // 2.监听途径播报进度，如果播报完成，则设置finished为true
                        SpeakHelper.releaseUserCallback()
                        LogUtil.i("结束 ${taskModel?.endTarget?:""} 途径播报（文本）")
                        finishExplain()
                    }

                    override fun progressChange(utteranceId: String, progress: Int) {
                        // do nothing
                    }
                })
                SpeakHelper.speakWithoutStop(text)
            }
            if (!route!!.walkvoice.isNullOrEmpty()) {
                type = ExplainStatusModel.TYPE_MP3
                try {
                    LogUtil.i("开始 ${taskModel?.endTarget?:""} 途径播报（MP3）")
                    MediaPlayerHelper.getInstance().setOnCompletionListener {
                        // 2.监听途径播报进度，如果播报完成，则设置finished为true
                        LogUtil.i("结束 ${taskModel?.endTarget?:""} 途径播报（MP3）")
                        MediaStatusManager.stopMediaPlay(false)
                        finishExplain()
                    }
                    MediaPlayerHelper.getInstance().play(route!!.walkvoice)
                } catch (ignored: Exception) {
                    LogUtil.i("异常 ${taskModel?.endTarget?:""} 途径播报（MP3）")
                    finishExplain()
                }
            }
        }else{
            LogUtil.i("route为空")
            finishExplain()
        }
        DialogHelper.loadingDialog.dismiss()
    }

    fun oneWayExplainFinish(): Boolean {
        return finished
    }

    private fun judgeFinishNavigate(): Boolean {
        return RobotStatus.manageStatus == RobotCommand.MANAGE_STATUS_STOP
    }

    private fun finishExplain(){
        finished = true
        if (judgeFinishNavigate()){
            taskModel?.bill?.executeNextTask()
        }
    }

    /**
     * @describe 已到点，通知fragment更新
     */
    fun notifyFragmentUpdate(){
        ExplainManager.explainStatusModel.postValue(
            ExplainStatusModel(
                routeLiveData = route,
                status = status,
                type = type,
                position = position
            )
        )
    }
}