package com.sendi.deliveredrobot.service;

/**
 * 任务节点类型
 *
 * @author Sunzecong
 * @since 2021-11-09
 */
public enum TaskStageEnum {
    ALLStartTask("任务开始",0),
    FinishGuideTask("引领结束",1),
    FinishSendTask("送物结束",1),
    /**
     * 讲解
     */
    FinishExplainTask("讲解结束",1),
    StartExplaining("开始讲解",70),
    GoExplainPoint("前往讲解点中",71),
    StartChannelBroadcast("开始途径播报",72),
    FinishChannelBroadcast("途径播报完成",73),
    ArrayExplainPoint("到达讲解点",74),
    StartArrayBroadcast("开始到点播报",75),
    FinishArrayBroadcast("到点播报完成",76),
    InterruptExplain("中断讲解",77),
    AllFinishTask("全部任务结束",10),
    AllFinishSendTask("全部送物结束",2),
    AllFinishGuideTask("全部引领结束",3),
    BeginDockTask("开始对接充电桩",4),
    CallLiftTask("呼叫电梯",5),
    CallRoomFinishTask("拨打房间电话结束",6),
    CallRoomTask("拨打房间电话",7),
    DispatchWaitResumeTask("调度等待恢复",8),
    FinishDockTask("结束对接充电桩",9),
    GoBackFinishTask("返回结束",12),
    GoBackTask("返回",13),
    GuideArriveTask("引领到达",14),
    GuidingTask("引领中",15),
    IntoLiftFinishTask("进入电梯结束",16),
    IntoLiftTask("进入电梯",17),
    LiftMoveTask("乘坐电梯",18),
    NavToFarPointTask("前往修复点",19),
    OutDockTask("退出充电桩",20),
    OutLiftFinishTask("出电梯结束",21),
    OutLiftTask("出电梯",22),
    PauseGuideTask("暂停",23),
    ScanIntoLiftTask("扫描入梯",24),
    ScanOutLiftTask("扫描出梯",25),
    SendingTask("送物中",26),
    SwitchSubMapTask("切换地图",27),
    JudgeFloorTask("判断是否同层",28),
    CallTakeObjectTask("呼叫取件", 29),
    CallTakeObjectFinishTask("呼叫取件结束", 30),
    CallPutObjectTask("呼叫放件",31),
    CallPutObjectFinishTask("呼叫放件结束", 32),
    StartRemoteOrderPutTask("开始前往取件",33),
    FinishRemoteOrderPutTask("结束前往取件", 34),
    StartRemoteOrderSendTask("开始前往送件",35),
    FinishRemoteOrderSendTask("结束前往送件",36),
    JudgeExceptionReasonTask("判断异常原因",37),
    EarlyFinishGuideTask("中断引领", 38),
    PreLoadMapTask("预加载地图", 39),
    WaitForResume("等待恢复",40),
    Error("状态异常", -1),

    GoSummoningPoint("去往召唤点",60),
    ArraySummoningPoint("到达召唤点",61),
    Idle("空闲",-2);

    private final String name;

    private final int code;

    TaskStageEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}
