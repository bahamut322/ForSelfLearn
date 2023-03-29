package com.sendi.deliveredrobot.navigationtask;

import androidx.core.util.Consumer;

import java.util.LinkedList;
import java.util.Queue;


public class TaskQueues<T> {

    private final Queue<T> queue = new LinkedList<>();
    private final Consumer<T> taskConsumer;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private int totalTasks = 0;
    private int completedTasks = 0;

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

    public synchronized void pause() {
        isPaused = true;
    }

    public synchronized void resume() {
        isPaused = false;
        runNextTask();
    }

    public synchronized void clear() {
        queue.clear();
        isRunning = false;
        isPaused = false;
        totalTasks = 0;
        completedTasks = 0;
    }

    public synchronized boolean isCompleted() {
        return completedTasks == totalTasks;
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

