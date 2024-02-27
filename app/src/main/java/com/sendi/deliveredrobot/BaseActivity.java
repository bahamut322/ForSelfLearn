package com.sendi.deliveredrobot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaRouter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.view.widget.MyPresentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


/**
 * @author swn
 * 用于副屏显示
 */
public class BaseActivity extends AppCompatActivity {

    public MyPresentation mPresentation;
    public MediaRouter mMediaRouter;
//    public DisplayManager mDisplayManager;
    public int flag = 0;    //用于双屏显示： 0.none 1. media—router 2.display-manager
    public static final String TAG = "BaseActivity";
    //onResume和onPause一般用来进行对presentation中的内容进行额外的处理
    @Override
    public void onResume() {
        super.onResume();
        // Listen for changes to media routes.
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
        // Register to receive events from the display manager.
//        mDisplayManager.registerDisplayListener(mDisplayListener, null);
//        Show(flag);
        /*
         * 处理情况：
         * 自检通过之后回到主页面
         * 按下home键盘
         * 在次进入app时广告屏正常显示广告
         */
        //判断副屏是否存在再去启动轮播控件
        if (mPresentation != null) {
            LogUtil.INSTANCE.i( "双屏异显onResume");
            Objects.requireNonNull(mPresentation.getAdvanceView()).setResume();
        }
        //重启双屏异显
//        showPresentationByMediaRouter();
//        ShowPresentationByDisplaymanager();
        RobotStatus.INSTANCE.getNewUpdata().postValue(1);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Listen for changes to media routes.
        mMediaRouter.removeCallback(mMediaRouterCallback);
//        mDisplayManager.unregisterDisplayListener(mDisplayListener);
//        if (mPresentation != null) {
//            Objects.requireNonNull(mPresentation.getAdvanceView()).setPause();
//        }
        LogUtil.INSTANCE.i("双屏异显onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        // Dismiss the presentation when the activity is not visible.
//        if (mPresentation != null) {
//            LogUtil.INSTANCE.i("活动不可见，取消双屏异显");
//            mPresentation.dismiss();
//            mPresentation = null;
//            //  flag=0;
//        }
    }

    @SuppressLint("SuspiciousIndentation")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaRouter = (MediaRouter) this.getSystemService(Context.MEDIA_ROUTER_SERVICE);
//        mDisplayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);
    }

    public void showPresentationByMediaRouter() {
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);//选择类型
        if (route != null) {
            Display display = route.getPresentationDisplay();
            showPresentation(display);
        }
    }

    public void showPresentation(Display presentationDisplay) {
        // Dismiss the current presentation if the display has changed.
        if (mPresentation != null && mPresentation.getDisplay() != presentationDisplay) {
            Log.i(TAG, "Dismissing presentation because the current route no longer "
                    + "has a presentation display.");
            mPresentation.dismiss();
            mPresentation = null;
        }
        // Show a new presentation if needed.
        if (mPresentation == null && presentationDisplay != null) {
            Log.i(TAG, "Showing presentation on display: " + presentationDisplay);
            mPresentation = new MyPresentation(getApplicationContext(), presentationDisplay);
//            RobotStatus.INSTANCE.getMPresentation().postValue(1);
            //  mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(TAG, "Couldn't show presentation!  Display was removed in "
                        + "the meantime.", ex);
                mPresentation = null;
            }
        }
    }

    //MediaRouter检测HDMI线的拔出和插入用的。
    private final MediaRouter.SimpleCallback mMediaRouterCallback =
            new MediaRouter.SimpleCallback() {
                @Override
                public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoute未选中: type=" + type + ", info=" + info);
//                    Show(flag);
                }

                @Override
                public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoute未选中: type=" + type + ", info=" + info);
//                    Show(flag);
                }

                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoutePresentation显示已更改: info=" + info);
//                    Show(flag);
                }
            };


    public void pushImage(String[] fileNames) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/ResProvider/default");
        if (!file.exists()) {
            file.mkdirs();
        }

        for (String fileName : fileNames) {
            String filePath = file.getAbsolutePath() + File.separator + fileName;
            File imageFile = new File(filePath);
            // 如果文件存在，则删除它
            if (imageFile.exists()) {
                imageFile.delete();
            }
            // 无论文件是否存在，都创建新文件
            InputStream inputStream = MyApplication.context.getAssets().open(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);

            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
            }

            fileOutputStream.close();
            inputStream.close();
        }
    }


}


