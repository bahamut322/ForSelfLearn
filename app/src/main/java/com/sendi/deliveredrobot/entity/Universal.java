package com.sendi.deliveredrobot.entity;


import android.annotation.SuppressLint;

import com.sendi.deliveredrobot.navigationtask.TaskQueues;

import java.util.List;

/**
 * @author swn
 * 全局通用变量
 */

public class Universal {
    //异常人脸温度的界值
    public static float TemperatureMax = 38;
    //红外摄像头的分辨率Width
    public static int cameraWidth = 256;//160;//256;
    //红外摄像头的分辨率Height
    public static int cameraHeight = 384;//240;//384;
    //RGB摄像头的分辨率Width
    public static int RGBWidth = 640;
    //RGB摄像头的分辨率Height
    public static int RGBHeight = 480;
    //存储选择首页显示功能♨信息
    public static String selectItem = null;
    //机器人音色选择
    public static String RobotMode = "男声";
    //测温视频
    public static int TempVideoLayout = 480;
    //存放待机视频/图片/GIF的目录
    @SuppressLint("SdCardPath")
    public static String Standby = "/mnt/sdcard/X8ROBOT/AppStandby/";
    //存放正常情况下的图片/视频
    @SuppressLint("SdCardPath")
    public static String Secondary = "/mnt/sdcard/X8ROBOT/AppSecondary/";
    @SuppressLint("SdCardPath")
    public static String SelfCheck= "/mnt/sdcard/X8ROBOT/SelfCheck.txt";
    //存放副屏广告图
    @SuppressLint("SdCardPath")
    public static String advertisement = "/mnt/sdcard/X8ROBOT/robotADV/";
    //下载地址
    public static String pathDownload = "http://172.168.201.34:9055/management_res/";
    //机器人主要存放的路径
    @SuppressLint("SdCardPath")
    public static String robotFile = "/mnt/sdcard/X8ROBOT/";
    //待机图片名字
    public static String sleepContentName = "";
    //图片名字
    public static String pics = "";
    //视频名字
    public static String videoFile = "";
    //广告图片名字
    public static String advPics = "";
    //广告视频名字
    public static String advVideoFile = "";
    //当前使用的总图
    public static String MapName = "";


    //TODO 门岗管理
    //副屏轮播间隔时间
    public static int picPlayTime = 3;
    //副屏视频是否播放声音
    public static int videoAudio = 1;
    //副屏文字
    public static String fontContent = "";
    //副屏文字颜色
    public static String fontColor = "#226DE8";
    //副屏文字大小
    public static int fontSize = 2;
    //副屏文字布局
    public static int fontLayout = 1;
    //副屏背景颜色
    public static String fontBackGround = "#FFFFFF";
    //温度正常提示音
    public static String tipsTemperatureInfo = "";
    //温度异常提示音
    public static String tipsTemperatureWarn = "";
    //口罩异常提示音
    public static String tipsMaskWarn = "";
    //门岗管理时间戳
    public static long timeStampReplyGateConfig;
    //大屏幕应用类型
    public static int bigScreenType = 1;
    //图片样式
    public static int picTypeNum = 0;
    //文字位置
    public static int textPosition = 0;

    //机器人声音类型
    public static int audioType = 1;
    //机器人唤醒词
    public static String wakeUpWord = "";
    //是否启用待机
    public static int sleep = 0;
    //多少分钟没操作进入待机
    public static int sleepTime;
    //唤醒方式
    public static String wakeUpType = "";
    //待机内容
    public static int sleepType = 1;
    //图片布局
    public static int picType = 1;
    //机器人默认配置时间戳
    public static long timeStampRobotConfigSql;
    //下发总图名字
    public static String mapName;
    //密码
    public static String password;
    //播放进度
    public static int progress = 0;
    //观察当前在第几个队列
    public static int taskNum = 0;
    public static String lastValue = null;
    public static boolean selectMapPoint = false;
    public static boolean twice = false;
    //轮播时间:
    public static int time;
    public static int nextPointGo = 0;
    //图片布局
    public static int pic = 1;
    public static int BaiduSpeakLength = 45;

    public static int videolayout = 0;

    public static int AllvideoAudio = 0;
    public static TaskQueues<String> taskQueue;
}

