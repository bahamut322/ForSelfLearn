package com.sendi.deliveredrobot.ros;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.sendi.deliveredrobot.ros.constant.Constant;
import com.sendi.deliveredrobot.utils.LogUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * web socket 连接类
 *
 * @author sos0707
 **/
public class RosWebSocketManager {
    public static final int CODE_NORMAL_DISCONNECT = 1000;
    public static final int CODE_DISCONNECT_FOR_RECONNECT = 1001;
    public static final int CODE_RECEIVED_MSG_TYPE = 2000;
    public static final int CODE_MSG_WHAT_TICK = 3000;

    private static volatile RosWebSocketManager INSTANCE;

    private WebSocket mWebSocket;
    private OkHttpClient mOkHttpClient;
    private boolean isReceiveTick;

    public static final AtomicBoolean isConnect = new AtomicBoolean(false);
    private final String mWebSocketUrl = Constant.WS_URL;
    private static final long TICK_DELAY = 300 * 1000;

    private static Handler mMainHandler;


    // DLC单例 懒加载 线程安全 反射获取？
    public static RosWebSocketManager getInstance() {
        if (INSTANCE == null) {
            synchronized (RosWebSocketManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RosWebSocketManager();
                }
            }
        }
        return INSTANCE;
    }

    private RosWebSocketManager() {
    }

    public void init() {
        mMainHandler = new Handler(Looper.getMainLooper());
//        mWebSocketUrl = wsUrl;
        mOkHttpClient = new OkHttpClient.Builder()
//                .pingInterval(10, TimeUnit.SECONDS)
                .build();
        connect();
    }

    public void connect() {
        System.out.println("============================================connect========================================================");
        Request request = new Request.Builder()
                .url(mWebSocketUrl)
                .header("Origin", mWebSocketUrl)
                .build();
        mOkHttpClient.newWebSocket(request, new MyWebSocketListener());
        mOkHttpClient.dispatcher().executorService().shutdown();
    }

    public synchronized void reConnect() {
        System.out.println("============================================reConnect========================================================");
        if (mWebSocket != null) {
//            LogUtil.d("reConnect 1");
            mWebSocket.close(CODE_DISCONNECT_FOR_RECONNECT, "close web socket for reconnect");
            mWebSocket = null;
            reConnect();
        } else {
//            init(mWebSocketUrl);
//            LogUtil.d("reConnect 2");
            mOkHttpClient = new OkHttpClient.Builder().build();
            Request request = new Request.Builder()
                    .url(mWebSocketUrl)
                    .header("Origin", mWebSocketUrl)
                    .build();
            mOkHttpClient.newWebSocket(request, new MyWebSocketListener());
            mOkHttpClient.dispatcher().executorService().shutdown();
        }
    }

    public void disConnect() {
        System.out.println("============================================disConnect========================================================");
        if (mWebSocket != null) {
            mWebSocket.close(CODE_NORMAL_DISCONNECT, "close web socket");
        }
    }

    public void disConnect(int code, String reason) {
        if (mWebSocket != null) {
            mWebSocket.close(code, reason);
        }
    }

    public boolean send(String sendData) {
        boolean flag = false;
        if (mWebSocket != null) {
            flag = mWebSocket.send(sendData);
        }
        LogUtil.INSTANCE.i("【SEND】 " + sendData + "   (发送结果: " + flag + ")");
        return flag;
    }

    class MyWebSocketListener extends WebSocketListener {

        // 连接已关闭
        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            LogUtil.INSTANCE.i("============================================onClose========================================================");
            super.onClosed(webSocket, code, reason);
//            LogUtil.d("onClosed code=" + code + " reason=" + reason);
            tickHandler.removeMessages(CODE_MSG_WHAT_TICK);
            reConnect();
        }

        // 连接关闭中
        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            LogUtil.INSTANCE.i("============================================onClosing========================================================");
            super.onClosing(webSocket, code, reason);
//            LogUtil.d("onClosing code=" + code + " reason=" + reason);
            tickHandler.removeMessages(CODE_MSG_WHAT_TICK);
        }

        // 连接失败
        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            LogUtil.INSTANCE.i("============================================onFailure========================================================");
            super.onFailure(webSocket, t, response);
            t.printStackTrace();
//            LogUtil.d("onFailure Throwable=" + t.getMessage() + " Response=" + response);
            //
//            webSocketHandler.sendEmptyMessageDelayed(0, 1000);
//            reConnect();
            mMainHandler.postDelayed(RosWebSocketManager.this::reConnect, 2000);
            isConnect.compareAndSet(true, false);
        }

        // 收到消息 String
        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            DispatchService.getInstance().messageHandler(text);
            isReceiveTick = true;
        }

        // 收到消息 ByteString
        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
            if (String.valueOf(bytes).contains("\"tick\"")) {
                // 心跳
                isReceiveTick = true;
                return;
            }
            isReceiveTick = true;
        }

        // 连接成功
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            LogUtil.INSTANCE.i("============================================onOpen========================================================");
            super.onOpen(webSocket, response);
//            LogUtil.d("onOpen Response=" + response);
            mWebSocket = webSocket;
            isReceiveTick = true;
            tickHandler.sendEmptyMessageDelayed(CODE_MSG_WHAT_TICK, TICK_DELAY);
            isConnect.compareAndSet(false, true);
            // 2021/8/13 添加短线后重新订阅ros topic
            if (DispatchService.isSubPreTopicList.get()) {
                SubManager.subTopics(DispatchService.preTopicList);
            }
        }
    }

    // 发送心跳包
    Handler tickHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what != CODE_MSG_WHAT_TICK) {
                return false;
            }
            if (isReceiveTick) {
                isReceiveTick = false;
                String tickMessage = "{\"op\":\"call_service\", \"service\":\"/rosapi/get_time\"}";
                send(tickMessage);
                tickHandler.sendEmptyMessageDelayed(CODE_MSG_WHAT_TICK, TICK_DELAY);
            } else {
                //没有收到tick,重连
                disConnect(CODE_DISCONNECT_FOR_RECONNECT, "断线重连");
            }
            return false;
        }
    });
}