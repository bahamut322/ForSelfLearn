package com.sendi.deliveredrobot;

import android.content.Context;
import android.os.Environment;

import com.sendi.deliveredrobot.helpers.CommonHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @describe 电梯指令
 * @author heky
 * @date 2021/12/13
 */
public class LiftCommand {
    private static volatile LiftCommand ourInstance;

    private String liftDoorRelease;

    private String liftDoorOpen;

    private String liftControlTime;

    private LiftCommand(Context context) {
        try {
            Properties prop = load(context);
            liftDoorRelease = getProperty(prop, "liftDoorRelease");
            liftDoorOpen = getProperty(prop, "liftDoorOpen");
            liftControlTime = getProperty(prop, "liftControlTime");
        }catch (Exception e){}
    }

    public static LiftCommand getInstance(Context context) {
        if (ourInstance == null) {
            synchronized (LiftCommand.class) {
                ourInstance = new LiftCommand(context);
            }
        }
        return ourInstance;
    }

    public String getLiftDoorRelease() {
        return liftDoorRelease;
    }

    public String getLiftDoorOpen() {
        return liftDoorOpen;
    }

    public String getLiftControlTime() {
        return liftControlTime;
    }

    private String getProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new LiftCommandCheckException("在 assets/lift_command.properties里没有设置 " + key);
        }
        return value.trim();
    }

    private synchronized Properties load(Context context) {
        try {
            String path = Environment.getExternalStorageDirectory().getPath() + "/config/lift_command.properties";
            File file = new File(path);
            CommonHelper.INSTANCE.checkLiftCommandProperties(context, file);
            InputStream is = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(is);
            is.close();
            return prop;
        } catch (IOException e) {
            e.printStackTrace();
            throw new LiftCommandCheckException(e);
        }
    }

    public static class LiftCommandCheckException extends RuntimeException {
        public LiftCommandCheckException(String message) {
            super(message);
        }

        public LiftCommandCheckException(Throwable cause) {
            super(cause);
        }
    }
}
