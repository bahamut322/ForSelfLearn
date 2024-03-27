package com.sendi.deliveredrobot.entity;


import android.annotation.SuppressLint;

import androidx.lifecycle.MutableLiveData;

import com.sendi.deliveredrobot.BuildConfig;
import com.sendi.deliveredrobot.navigationtask.TaskQueues;

import java.io.File;
import java.util.ArrayList;
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
    //测温视频
    public static int TempVideoLayout = 480;
    //APP更新下载目录
    public static File AppVersion = new File("/mnt/sdcard/version");
    //默认GIF
    public static String gifDefault = "/storage/emulated/0/ResProvider/default/default_explain.gif";
    //广告默认图
    public static String advDefault = "/storage/emulated/0/ResProvider/default/advdefault.jpg";
    //门岗默认图
    public static String usherDefault = "/storage/emulated/0/ResProvider/default/usherdefault.jpg";
    //讲解默认图
    public static String explainDefault = "/storage/emulated/0/ResProvider/default/explandefault.jpg";
    //引领默认
    public static String guideDefault = "/storage/emulated/0/ResProvider/default/guidedefault.jpg";
    //导购默认
    public static String businessDefault = "/storage/emulated/0/ResProvider/default/businessdefault.jpg";
    //迎宾默认图
    public static String greetDefault = "/storage/emulated/0/ResProvider/default/default_greet.jpg";
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
    public static String pathDownload = BuildConfig.HTTP_HOST;
    //机器人主要存放的路径
    @SuppressLint("SdCardPath")
    public static String robotFile = "/mnt/sdcard/X8ROBOT/";
    //百度语音长语音指定id
    public static String speakTextId = "explanation";
    //引领返回是否播放表情标志位
    public static boolean guideTask = false;
    //导购返回是否播放表情标志位
    public static String businessTask = null;
    //人脸检测URL
    public static String POST_FAST = "http://192.168.73.204:5005/get_feat";
    //人脸识别URL
    public static String POST_IDENTIFY = "http://192.168.73.204:5005/get_similarity";


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
    //是否启用待机
    public static int sleep = 0;
    //图片布局
    public static int picType = 1;
    //是否下发机器人配置
    public static MutableLiveData<Boolean> mapType = new MutableLiveData<>(false);
    //密码
    public static String password;
    //播放进度
    public static int progress = 0;
    //观察当前在第几个队列
    public static int taskNum = 0;
    public static int explainTextLength = -1;
    public static String lastValue = null;
    public static boolean selectMapPoint = false;
    public static boolean twice = false;
    //轮播时间:
    public static int time;
    public static int nextPointGo = 0;
    public static int speakInt = 0;

    public static int allVideoAudio = 0;
    public static TaskQueues<String> taskQueue;
    public static List<Integer> explainSpeak = new ArrayList<>();
    //用来判断按下急停之后暂停讲解的状态
    public static Boolean speaking = false;//观察页面按钮暂停
    public static Boolean process = false;
    public static Boolean changing = false;
    public static Boolean finish = false;
    //用来处理按下点击暂停。并且急停之后，再松开，会顶着暂停页面走
    public static boolean explainUnSpeak = false;
    public static String shoppingName = "";
    public static int shoppingType = 0;
}

