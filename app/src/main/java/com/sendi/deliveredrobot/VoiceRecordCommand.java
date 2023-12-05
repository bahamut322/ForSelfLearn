package com.sendi.deliveredrobot;

import android.content.Context;
import android.os.Environment;

import com.sendi.deliveredrobot.helpers.CommonHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

/**
 * @describe
 * @author heky
 * @date 2021/12/13
 */
public class VoiceRecordCommand {
    private static volatile VoiceRecordCommand ourInstance;

    private String voiceRecordType;

    private VoiceRecordCommand(Context context) {
        try {
            Properties prop = load(context);
            voiceRecordType = getProperty(prop);
        }catch (Exception ignored){}
    }

    public String getVoiceRecordType() {
        return voiceRecordType;
    }

    public static VoiceRecordCommand getInstance(Context context) {
        if (ourInstance == null) {
            synchronized (VoiceRecordCommand.class) {
                ourInstance = new VoiceRecordCommand(context);
            }
        }
        return ourInstance;
    }

    private String getProperty(Properties properties) {
        String value = properties.getProperty("voiceRecordType");
        if (value == null) {
            throw new VoiceRecordCheckException("在 assets/voice_record_command.properties里没有设置 " + "voiceRecordType");
        }
        return value.trim();
    }

    private synchronized Properties load(Context context) {
        try {
            String path = Environment.getExternalStorageDirectory().getPath() + "/config/voice_record_command.properties";
            File file = new File(path);
            CommonHelper.INSTANCE.checkVoiceRecordCommandProperties(context, file);
            InputStream is = Files.newInputStream(file.toPath());
            Properties prop = new Properties();
            prop.load(is);
            is.close();
            return prop;
        } catch (IOException e) {
            e.printStackTrace();
            throw new VoiceRecordCheckException(e);
        }
    }

    public static class VoiceRecordCheckException extends RuntimeException {
        public VoiceRecordCheckException(String message) {
            super(message);
        }

        public VoiceRecordCheckException(Throwable cause) {
            super(cause);
        }
    }
}
