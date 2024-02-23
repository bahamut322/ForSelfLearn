package com.iflytek.vtncaetest.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import com.iflytek.vtncaetest.ContextHolder;

public class NetWorkUtil {
    /**
     * 判断当前网络是否可用(6.0以上版本)
     * 实时
     *
     * @return
     */
    public static boolean isUsable() {
        boolean isNetUsable = false;
        ConnectivityManager manager = (ConnectivityManager)
                ContextHolder.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            NetworkCapabilities networkCapabilities =
                    manager.getNetworkCapabilities(manager.getActiveNetwork());
            if (networkCapabilities != null) {
                isNetUsable = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
        }
        return isNetUsable;
    }

}
