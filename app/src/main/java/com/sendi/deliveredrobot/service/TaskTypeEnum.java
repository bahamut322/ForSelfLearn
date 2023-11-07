package com.sendi.deliveredrobot.service;

/**
 * 任务类型
 *
 * @author Sunzecong
 * @since 2021-11-09
 */
public enum TaskTypeEnum {
    DELIVERY("送物模式", 1, "D"),
    GUIDING("引领模式", 2, "G"),
    USHER("迎宾模式", 3, "U"),
    EXPLAIN("讲解模式",4,"E"),
    BUSINESS("业务办理",5,"B");


    private String name;

    private int code;

    private String prefix;

    TaskTypeEnum(String name, int code, String prefix) {
        this.name = name;
        this.code = code;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public String getPrefix() {
        return prefix;
    }
}
