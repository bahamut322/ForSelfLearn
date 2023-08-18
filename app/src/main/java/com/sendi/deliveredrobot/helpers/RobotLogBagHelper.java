package com.sendi.deliveredrobot.helpers;

import android.os.Environment;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.BuildConfig;
import com.sendi.deliveredrobot.model.log.RobotAppLog;
import com.sendi.deliveredrobot.model.log.RobotBagLog;
import com.sendi.deliveredrobot.model.log.RobotLog;
import com.sendi.deliveredrobot.model.log.RobotNavLog;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.service.CloudMqttService;
import com.sendi.deliveredrobot.service.DeliverMqttService;
import com.sendi.deliveredrobot.utils.FileUtil;
import com.sendi.deliveredrobot.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;

/**
 * create by yujx
 *
 * @date 2023/02/28
 */
public class RobotLogBagHelper {

    public static final String REMOTEIP = "192.168.73.200";
    public static final int PORT = 22;
    public static final String USERNAME = "rpdzkj";
    public static final String PASSWORD = "sendi4008303030";
    public static final String BAG_PATH = "/home/rpdzkj/AppBag/";
    public static final String NAV_PATH = "/home/rpdzkj/AppData/navigation_logfiles/";
    public static final String APP_PATH = Environment.getExternalStorageDirectory().getPath()+"/logger/";
    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath()+"/tempLog/";
//    public static final String FILE_UPLOAD_URL = "http://172.168.201.34:9830/business/log/uploadFile";
    public static final String FILE_UPLOAD_URL = BuildConfig.UPLOAD_LOG_HTTP_HOST;

    public void queryLogBag() {
        Connection con = new Connection(REMOTEIP, PORT);
        try {
            con.connect();
            boolean isAuthed = con.authenticateWithPassword(USERNAME, PASSWORD);
            if (!isAuthed) {
                LogUtil.INSTANCE.e("用户名或密码验证失败");
            }

            SFTPv3Client sftpClient = new SFTPv3Client(con);
            //从底盘获取bag文件列表
            List<RobotBagLog> bagLogList = new ArrayList<>();
            try {
                Vector<SFTPv3DirectoryEntry> listBag = sftpClient.ls(BAG_PATH);
                for (SFTPv3DirectoryEntry directoryEntry : listBag) {
                    if (!directoryEntry.attributes.isDirectory()){
                        RobotBagLog robotBagLog = new RobotBagLog();
                        robotBagLog.setFilename(directoryEntry.filename);
                        robotBagLog.setSize(String.valueOf(directoryEntry.attributes.size));
                        bagLogList.add(robotBagLog);
                    }
                }
            } catch (IOException e){
                LogUtil.INSTANCE.e(BAG_PATH+ "不存在目录");
            }

            //从底盘获取nav日志列表
            List<RobotNavLog> navLogList = new ArrayList<>();
            try {
                Vector<SFTPv3DirectoryEntry> listNav = sftpClient.ls(NAV_PATH);
                for (SFTPv3DirectoryEntry sftPv3DirectoryEntry : listNav) {
                    if (sftPv3DirectoryEntry.attributes.isDirectory()){
                        //如果是目录，遍历下一级
                        Vector<SFTPv3DirectoryEntry> children = sftpClient.ls(NAV_PATH + sftPv3DirectoryEntry.filename);
                        for (SFTPv3DirectoryEntry child : children) {
                            if (!child.attributes.isDirectory()){
                                RobotNavLog robotNavLog = new RobotNavLog();
                                robotNavLog.setFilename(child.filename);
                                robotNavLog.setSize(String.valueOf(child.attributes.size));
                                robotNavLog.setPath(NAV_PATH + sftPv3DirectoryEntry.filename);
                                navLogList.add(robotNavLog);
                            }
                        }
                    }
                }
            } catch (IOException e){
                LogUtil.INSTANCE.e(NAV_PATH+ "不存在目录");
            }

            //获取APP日志列表
            List<RobotAppLog> appLogList = new ArrayList<>();
            File appLogFiles = new File(APP_PATH);
            File[] files = appLogFiles.listFiles();
            if (files != null){
                for (File file : files) {
                    RobotAppLog robotAppLog = new RobotAppLog();
                    robotAppLog.setFilename(file.getName());
                    robotAppLog.setSize(String.valueOf(file.length()));
                    appLogList.add(robotAppLog);
                }
            }

            RobotLog robotLog = new RobotLog();
            robotLog.setBag(bagLogList);
            robotLog.setNav(navLogList);
            robotLog.setApp(appLogList);

            JSONObject dto = new JSONObject();
            dto.put("type", "replyLogBag");
            dto.put("robotBagLog", robotLog);
            String jsonString = dto.toJSONString();
            // send Mqtt msg
            LogUtil.INSTANCE.i("日志上传Tips"+jsonString);
            DeliverMqttService.Companion.publish(jsonString, true, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void uploadLog(RobotLog robotLog) {
        LogUtil.INSTANCE.i(JSONObject.toJSONString(robotLog));
        Connection con = new Connection(REMOTEIP, PORT);
        File file = new File(SAVE_PATH);
        if (!file.exists()){
            file.mkdirs();
        }
        try {
            con.connect();
            boolean isAuthed = con.authenticateWithPassword(USERNAME, PASSWORD);
            if (!isAuthed) {
                LogUtil.INSTANCE.e("用户名或密码验证失败");
            }
            SCPClient scpClient = con.createSCPClient();
            if (robotLog.getBag() != null){
                for (RobotBagLog robotBagLog : robotLog.getBag()) {
                    //先从底盘把文件拿上来
                    scpClient.get(BAG_PATH + robotBagLog.getFilename(), SAVE_PATH);
                    //上传文件
                    FileUtil.INSTANCE.uploadFile(FILE_UPLOAD_URL,
                            SAVE_PATH + robotBagLog.getFilename(),
                            RobotStatus.INSTANCE.getSERIAL_NUMBER(),
                            robotBagLog.getPath()
                    );
                    //上传完后删除文件
                    File deleteFile = new File(SAVE_PATH + robotBagLog.getFilename());
                    deleteFile.delete();
                }
                LogUtil.INSTANCE.i("上传bag成功");
            }

            if (robotLog.getNav() != null){
                for (RobotNavLog robotNavLog : robotLog.getNav()) {
                    scpClient.get(robotNavLog.getPath() +'/'+ robotNavLog.getFilename(), SAVE_PATH);
                    //上传文件
                    FileUtil.INSTANCE.uploadFile(FILE_UPLOAD_URL,
                            SAVE_PATH + robotNavLog.getFilename(),
                            RobotStatus.INSTANCE.getSERIAL_NUMBER(),
                            robotNavLog.getPath()
                    );
                    //上传完后删除文件
                    File deleteFile = new File(SAVE_PATH + robotNavLog.getFilename());
                    deleteFile.delete();
                }
                LogUtil.INSTANCE.i("上传nav成功");
            }

            if (robotLog.getApp() != null){
                for (RobotAppLog robotAppLog : robotLog.getApp()) {
                    //上传文件
                    FileUtil.INSTANCE.uploadFile(FILE_UPLOAD_URL,
                            APP_PATH + robotAppLog.getFilename(),
                            RobotStatus.INSTANCE.getSERIAL_NUMBER(),
                            robotAppLog.getPath()
                    );
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
