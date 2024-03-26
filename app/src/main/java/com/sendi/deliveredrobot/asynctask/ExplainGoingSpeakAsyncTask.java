package com.sendi.deliveredrobot.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper;
import com.sendi.deliveredrobot.helpers.ReportDataHelper;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.navigationtask.BillManager;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.service.Placeholder;
import com.sendi.deliveredrobot.service.TaskDto;
import com.sendi.deliveredrobot.service.TaskStageEnum;
import com.sendi.deliveredrobot.utils.LogUtil;

import java.util.ArrayList;
import java.util.Objects;

public class ExplainGoingSpeakAsyncTask extends AsyncTask<Void, Void, Void> {
    ArrayList<MyResultModel> mDatas;
    int position;
    public ExplainGoingSpeakAsyncTask(ArrayList<MyResultModel> mDatas, int position){
        this.mDatas = mDatas;
        this.position = position;
    }

    @Override
    protected void onPreExecute() {
        // 在执行后台任务之前执行，通常用于初始化操作
        reportTaskDto();
    }

    @Override
    protected Void doInBackground(Void... params) {
        // 在后台线程中执行耗时操作，例如数据预加载
        if (mDatas.get(position).getWalktext() != null && !mDatas.get(position).getWalktext().isEmpty()) {
            BaiduTTSHelper.getInstance().speaks(Placeholder.Companion.replaceText(mDatas.get(position).getWalktext(),"",mDatas.get(position).getName(),mDatas.get(position).getRoutename(),"智能讲解"));
//                            viewModel.splitTextByPunctuation(mDatas.get(position).getWalktext());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mDatas.get(position).getWalktext() != null && !mDatas.get(position).getWalktext().isEmpty()) {
        }
        if (mDatas.get(position).getWalkvoice() != null && !mDatas.get(position).getWalkvoice().isEmpty()) {
            try {
                MediaPlayerHelper.getInstance().play(mDatas.get(position).getWalkvoice(), "1");
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 任务上报
     */
    private void reportTaskDto(){
        TaskDto taskDto = new TaskDto();
        taskDto.setStatus(1);
        try{
            ReportDataHelper.INSTANCE.reportTaskDto(
                    Objects.requireNonNull(
                            Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()
                    ).taskModel(), TaskStageEnum.StartChannelBroadcast,
                    taskDto
            );
        }catch (NullPointerException e){
            LogUtil.INSTANCE.e("reportTaskDto exception");
        }
    }
}
