package com.sendi.deliveredrobot.service;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskIdGenerator {
    // singleton
    public static TaskIdGenerator getInstance() {
        return innerClass.INSTANCE;
    }

    private TaskIdGenerator() {
    }

    private static final class innerClass {
        public static TaskIdGenerator INSTANCE = new TaskIdGenerator();
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public String generateTaskId(TaskTypeEnum taskType) {
        return generateTaskId(taskType, DoorEnum.NO_USE_DOOR, sdf.format(new Date()));
    }

    public String generateTaskId(TaskTypeEnum taskType, Date date) {
        return generateTaskId(taskType, DoorEnum.NO_USE_DOOR, sdf.format(date));
    }

    public String generateTaskId(TaskTypeEnum taskType, DoorEnum door) {
        return generateTaskId(taskType, door, sdf.format(new Date()));
    }

    public String generateTaskId(TaskTypeEnum taskType, DoorEnum door, String date) {
        return String.format("%s%s%s", taskType.getPrefix(), door.getCode(), date);
    }

    public String generateTaskId(TaskTypeEnum taskType, DoorEnum door, Date date){
        return generateTaskId(taskType, door, sdf.format(date));
    }
}
