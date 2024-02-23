package com.iflytek.vtncaetest.utils;


import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Android运行linux命令，多麦克风算法才需要，单麦不需要
 */
public final class RootShell {
    private static final String TAG = "RootShell";

    /**
     * 执行命令但不关注结果输出
     */
    public static int execRootCmdSilent(String cmd) {
        Log.d(TAG, "run " + cmd);
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            Log.i(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
            Log.d(TAG, "run " + cmd + " result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
