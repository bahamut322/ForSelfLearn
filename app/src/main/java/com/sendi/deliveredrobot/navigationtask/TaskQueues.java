package com.sendi.deliveredrobot.navigationtask;

import androidx.core.util.Consumer;

import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.helpers.ReportDataHelper;
import com.sendi.deliveredrobot.service.TaskStageEnum;
import com.sendi.deliveredrobot.service.UpdateReturn;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.view.widget.Order;
import com.sendi.deliveredrobot.viewmodel.StartExplanViewModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class TaskQueues<T> {

    private final Queue<T> queue = new LinkedList<>();
    private final Consumer<T> taskConsumer;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private static int totalTasks = 0;
    private static int completedTasks = 0;

    public TaskQueues(Consumer<T> taskConsumer) {
        this.taskConsumer = taskConsumer;
    }

    public synchronized void enqueue(T task) {
        queue.offer(task);
        totalTasks++;
        if (!isRunning) {
            isRunning = true;
            runNextTask();
        }
    }
    public static synchronized boolean hasTasks() {
        return totalTasks > completedTasks;
    }
    public synchronized void pause() {
        isPaused = true;
    }

    public synchronized void resume() {
        isPaused = false;
        runNextTask();
    }
    //剩余任务数（不包括当前任务）
    public synchronized int getRemainingTasks() {
        return queue.size();
    }
    //剩余任务内容（不包括当前任务）
    public synchronized List<T> getTaskContent() {
        return new ArrayList<>(queue);
    }
    //当前任务
    public synchronized T getCurrentTaskContent() {
        return queue.peek();
    }
    public synchronized void clear() {
        BaiduTTSHelper.getInstance().stop();
        if (queue!=null) {
            queue.clear();
            isRunning = false;
            isPaused = false;
            totalTasks = 0;
            completedTasks = 0;
        }
    }

    public static synchronized boolean isCompleted() {
        return completedTasks == totalTasks;
    }

    public static synchronized boolean isTaskQueueCompleted() {
        return completedTasks != 0 && totalTasks != 0 && isCompleted();
    }

    private void runNextTask() {
        T task = queue.poll();
        if (task != null) {
            if (!isPaused) {
                taskConsumer.accept(task);
                completedTasks++;
                if (isCompleted()) {
                    isRunning = false;
                } else {
                    runNextTask();
                }
            }
        } else {
            isRunning = false;
        }
    }
}