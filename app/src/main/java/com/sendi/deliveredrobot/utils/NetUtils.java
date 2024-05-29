package com.sendi.deliveredrobot.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NetUtils {
    public NetUtils() {
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static String getOriginUrl(String referer) {
        String ou = referer;
        if (TextUtils.isEmpty(referer)) {
            return "";
        } else {
            try {
                URL url = new URL(ou);
                int port = url.getPort();
                ou = url.getProtocol() + "://" + url.getHost() + (port == -1 ? "" : ":" + port);
            } catch (Exception var4) {
            }

            return ou;
        }
    }

    public static Map<String, String> multimapToSingle(Map<String, List<String>> maps) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> map = new HashMap();

        Map.Entry entry;
        for(Iterator var3 = maps.entrySet().iterator(); var3.hasNext(); map.put((String) entry.getKey(), sb.toString())) {
            entry = (Map.Entry)var3.next();
            List<String> values = (List)entry.getValue();
            sb.delete(0, sb.length());
            if (values != null && values.size() > 0) {
                Iterator var6 = values.iterator();

                while(var6.hasNext()) {
                    String v = (String)var6.next();
                    sb.append(v);
                    sb.append(";");
                }
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }

        return map;
    }
}
