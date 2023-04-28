package com.sendi.deliveredrobot;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.entity.AdvertisingConfigDB;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.AudioMngHelper;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.room.dao.DebugDao;
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao;
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap;

import com.sendi.deliveredrobot.service.UpdateReturn;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.view.widget.Advance;
import com.sendi.deliveredrobot.view.widget.AdvancePagerAdapter;
import com.sendi.deliveredrobot.view.widget.AdvanceView;
import com.sendi.deliveredrobot.view.widget.VerticalTextView;
import com.sendi.deliveredrobot.viewmodel.BaseViewModel;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * @author swn
 * 用于副屏显示
 */
public class BaseActivity extends AppCompatActivity {

    public Presentation mPresentation;
    public Display mDisplay;
    public MediaRouter mMediaRouter;
    public DisplayManager mDisplayManager;
    public ConstraintLayout frameLayout;//副屏Fragment
    public AdvanceView advanceView;//轮播图&视频控件
    public TextView horizontalTV;//横向文字
    public VerticalTextView verticalTV;//纵向文字
    ConstraintLayout constraintLayout2;//布局
    int videoAudio;
    public int flag = 0;    //用于双屏显示： 0.none 1. media—router 2.display-manager
    public static final String TAG = "BaseActivity";
    private BaseViewModel baseViewModel;
    AdvertisingConfigDB advertisingConfigDB;

    //onResume和onPause一般用来进行对presentation中的内容进行额外的处理
    @Override
    public void onResume() {
        super.onResume();
        // Listen for changes to media routes.
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
        // Register to receive events from the display manager.
        mDisplayManager.registerDisplayListener(mDisplayListener, null);
        Show(flag);
        Log.i(TAG, "双屏异显onResume");
        /**
         * 处理情况：
         * 自检通过之后回到主页面
         * 按下home键盘
         * 在次进入app时广告屏正常显示广告
         */
        //判断副屏是否存在再去启动轮播控件
        if (mPresentation != null) {
            advanceView.setResume();
        }
        //重启双屏异显
        ShowPresentationByMediarouter();
        ShowPresentationByDisplaymanager();
        RobotStatus.INSTANCE.getNewUpdata().postValue(1);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Listen for changes to media routes.
        mMediaRouter.removeCallback(mMediaRouterCallback);
        mDisplayManager.unregisterDisplayListener(mDisplayListener);
        if (mPresentation!=null) {
            advanceView.setPause();
        }
        Log.i(TAG, "双屏异显onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        // Dismiss the presentation when the activity is not visible.
        if (mPresentation != null) {
            Log.i(TAG, "活动不可见，取消双屏异显");
            mPresentation.dismiss();
            mPresentation = null;
            //  flag=0;
        }
    }

    @SuppressLint("SuspiciousIndentation")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaRouter = (MediaRouter) this.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        mDisplayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);
        SharedPreferences sp = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        videoAudio = (int) sp.getFloat("videoAudio", 0); // 视频音量
        advertisingConfigDB = LitePal.findFirst(AdvertisingConfigDB.class); //查询副屏第一条数据
        DebugDao debugDao = DataBaseDeliveredRobotMap.Companion.getDatabase(
                Objects.requireNonNull(
                        MyApplication.Companion.getInstance()
                )
        ).getDebug();
        baseViewModel = new ViewModelProvider(this).get(BaseViewModel.class);
    }

    private final class MyPresentation extends Presentation {

        public MyPresentation(Context context, Display display) {
            super(context, display);
        }

        //副屏的生命周期
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // Be sure to call the super class.
            super.onCreate(savedInstanceState);
            // Get the resources for the context of the presentation.
            // Notice that we are getting the resources from the context of the presentation.
//            Resources resources = getContext().getResources();
            mDisplayManager.getDisplay(90);
            // Inflate the layout.
            setContentView(R.layout.presentation_content);
            frameLayout = findViewById(R.id.frameLayout);
            constraintLayout2 = findViewById(R.id.constraintLayout2);
            advanceView = findViewById(R.id.Spread_out);
            horizontalTV = findViewById(R.id.horizontalTV);//横向文字
            verticalTV = findViewById(R.id.verticalTV);//纵向文字
//          AdvancePagerAdapter.time = Universal.picPlayTime;
            //一定要在副屏的生命中中设置一下音量，否则刷新副屏的时候默认为最大声音
            new AudioMngHelper(MyApplication.context).setVoice100((int) QuerySql.QueryBasic().getVideoVolume());
            //将控件设置成副屏尺寸，并且旋转270度
            constraintLayout2.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) constraintLayout2.getLayoutParams();
                params.height = 1920;
                params.width = 1080;
                constraintLayout2.setLayoutParams(params);
                constraintLayout2.setRotation(270);
            });
        }


        @Override
        protected void onStop() {
            super.onStop();
        }

        @Override
        protected void onStart() {
            super.onStart();
        }

        /**
         * 重新加载Activity
         */
        public void aReady() {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
    }

    public final DisplayManager.DisplayListener mDisplayListener =
            new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                    Log.d(TAG, "Display #" + displayId + " added.");
                    Show(flag);
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    Log.d(TAG, "Display #" + displayId + " changed.");
                    Show(flag);
                    //重新连接广告屏的时候F
                    RobotStatus.INSTANCE.getNewUpdata().postValue(1);
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                    Log.d(TAG, "Display #" + displayId + " removed.");
                    Toast.makeText(getApplication(), "广告屏连接失败", Toast.LENGTH_SHORT).show();
                    Show(flag);
                }
            };

    public void Show(int flag) {
        switch (flag) {
            case 1:
                ShowPresentationByMediarouter();
                break;
            case 2:
                ShowPresentationByDisplaymanager();
                break;
        }
    }

    public void ShowPresentationByDisplaymanager() {
        Display[] presentationDisplays = mDisplayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        if (presentationDisplays.length > 0) {
            // If there is more than one suitable presentation display, then we could consider
            // giving the user a choice.  For this example, we simply choose the first display
            // which is the one the system recommends as the preferred presentation display.
            Display display = presentationDisplays[0];
            showPresentation(display);
        }
    }

    public void ShowPresentationByMediarouter() {
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);//选择类型
        if (route != null) {
            mDisplay = route.getPresentationDisplay();
            showPresentation(mDisplay);
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
            if (mPresentation != null) {
                RobotStatus.INSTANCE.getMPresentation().postValue(1);
            }
            //  mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(TAG, "Couldn't show presentation!  Display was removed in "
                        + "the meantime.", ex);
                mPresentation = null;
            }
        }
    }

    //MediaRouter检测HDMI线的拔出和插入用的。
    public final MediaRouter.SimpleCallback mMediaRouterCallback =
            new MediaRouter.SimpleCallback() {
                @Override
                public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoute未选中: type=" + type + ", info=" + info);
                    Show(flag);
                }

                @Override
                public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoute未选中: type=" + type + ", info=" + info);
                    Show(flag);
                }

                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoutePresentation显示已更改: info=" + info);
                    Show(flag);
                }
            };


    public void getFilesAllName(String path) {
        File file = new File(path);
        if (mPresentation!=null) {
            advanceView.removeAllViews();
            advanceView.initView();
        }
        if (file.isFile()) {
            // This is a file
            List<Advance> fileList = new ArrayList<>();
            if (baseViewModel.checkIsImageFile(file.getPath())) {
                fileList.add(new Advance(file.getPath(), "2")); // image
            } else {
                fileList.add(new Advance(file.getPath(), "1")); // video
            }
            advanceView.setData(fileList);
        } else if (file.isDirectory()) {
            // This is a directory
            File[] files = file.listFiles();
            if (files != null) {
                List<Advance> fileList = new ArrayList<>();
                for (File value : files) {
                    if (baseViewModel.checkIsImageFile(value.getPath())) {
                        fileList.add(new Advance(value.getPath(), "2")); // image
                    } else {
                        fileList.add(new Advance(value.getPath(), "1")); // video
                    }
                }
                advanceView.setData(fileList);
            }
        }
    }


    /**
     * @param picPlayTime    轮播时间
     * @param file           路径
     * @param type           类型： 1-图片 2-视频 6-文字 7-图片+文字
     * @param textPosition   文字位置
     * @param fontLayout     文字方向：1-横向，2-纵向
     * @param fontContent    文字
     * @param fontBackGround 背景颜色
     * @param fontColor      文字颜色
     * @param fontSize       文字大小：1-大，2-中，3-小,
     */
    public void layoutThis(int picPlayTime, String file, int type, int textPosition, int fontLayout, String fontContent, String fontBackGround, String fontColor, int fontSize) {
        //轮播时间
        AdvancePagerAdapter.time = picPlayTime;
        switch (type) {
            case 1:
            case 2:
                //读取文件
                getFilesAllName(file);
                verticalTV.setVisibility(View.GONE);
                horizontalTV.setVisibility(View.GONE);
                advanceView.setVisibility(View.VISIBLE);
                break;
            case 6:
                advanceView.setVisibility(View.GONE);
                textLayoutThis(fontLayout, fontContent, fontBackGround, fontColor, fontSize);
                break;
            case 7:
                //读取文件
                getFilesAllName(file);
                if (textPosition == 0) {
                    textLayoutThis(fontLayout, fontContent, fontBackGround, fontColor, fontSize);
                    horizontalTV.setGravity(Gravity.CENTER | Gravity.LEFT);//居中
                } else if (textPosition == 1) {
                    textLayoutThis(fontLayout, fontContent, fontBackGround, fontColor, fontSize);
                    horizontalTV.setGravity(Gravity.TOP | Gravity.LEFT);//居上
                } else if (textPosition == 2) {
                    textLayoutThis(fontLayout, fontContent, fontBackGround, fontColor, fontSize);
                    horizontalTV.setGravity(Gravity.BOTTOM | Gravity.LEFT);//居下
                }
                advanceView.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * @param fontLayout     文字方向：1-横向，2-纵向
     * @param fontContent    文字
     * @param fontBackGround 背景颜色
     * @param fontColor      文字颜色
     * @param fontSize       文字大小：1-大，2-中，3-小,
     */
    private void textLayoutThis(int fontLayout, String fontContent, String fontBackGround, String fontColor, int fontSize) {

        //横向
        if (fontLayout == 1) {
            //隐藏纵向文字，显示横向文字
            verticalTV.setVisibility(View.GONE);
            horizontalTV.setVisibility(View.VISIBLE);
            //显示内容
            horizontalTV.setText(baseViewModel.getLength(fontContent));
            //背景颜色&图片
            constraintLayout2.setBackgroundColor(Color.parseColor(fontBackGround + ""));
            //文字颜色
            horizontalTV.setTextColor(Color.parseColor(fontColor + ""));
            //字体大小
            if (fontSize == 1) {
                horizontalTV.setTextSize(90);
            } else if (fontSize == 2) {
                horizontalTV.setTextSize(70);
            } else if (fontSize == 3) {
                horizontalTV.setTextSize(50);
            }
        } else {
            //纵向
            //隐藏横向文字，显示纵向文字
            verticalTV.setVisibility(View.VISIBLE);
            horizontalTV.setVisibility(View.GONE);
            //显示内容
            verticalTV.setText(fontContent);
            //背景颜色
            constraintLayout2.setBackgroundColor(Color.parseColor(fontBackGround + ""));
            //文字颜色
            verticalTV.setTextColor(Color.parseColor(fontColor + ""));
            //字体大小
            if (fontSize == 1) {
                verticalTV.setTextSize(80);
            } else if (fontSize == 2) {
                verticalTV.setTextSize(60);
            } else if (fontSize == 3) {
                verticalTV.setTextSize(40);
            }
        }
    }
}


