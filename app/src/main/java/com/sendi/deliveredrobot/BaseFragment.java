package com.sendi.deliveredrobot;

import android.app.Presentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.AudioMngHelper;
import com.sendi.deliveredrobot.view.inputfilter.IMainView;
import com.sendi.deliveredrobot.view.inputfilter.MainPresenter;
import com.sendi.deliveredrobot.view.widget.Advance;
import com.sendi.deliveredrobot.view.widget.AdvancePagerAdapter;
import com.sendi.deliveredrobot.view.widget.AdvanceView;
import com.sendi.deliveredrobot.view.widget.VerticalTextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author swn
 * 用于副屏显示
 */
public class BaseFragment extends Fragment {

    public Presentation mPresentation;
    public Display mDisplay;
    public MediaRouter mMediaRouter;
    public DisplayManager mDisplayManager;
    public ConstraintLayout frameLayout;//副屏Fragment
    public AdvanceView advanceView;//轮播图&视频控件
    public TextView horizontalTV;//横向文字
    public VerticalTextView verticalTV;//纵向文字
    ConstraintLayout constraintLayout2;//布局
    Float robotAudio;
    int videoAudio;
    Float musicAudio;
    MainActivity mainActivity;
    Context context;
    public int flag = 0;    //witch method used to show presentation 0.none 1. mediarouter 2.displaymanager
    public static final String TAG = "BaseAcyivity";



    //onResume和onPause一般用来进行对presentsatoin中的内容进行额外的处理
    @Override
    public void onResume() {
        super.onResume();
        // Listen for changes to media routes.
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
        // Register to receive events from the display manager.
        mDisplayManager.registerDisplayListener(mDisplayListener, null);
        Show(flag);
        advanceView.setResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Listen for changes to media routes.
        mMediaRouter.removeCallback(mMediaRouterCallback);
        mDisplayManager.unregisterDisplayListener(mDisplayListener);
        advanceView.setPause();
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaRouter = (MediaRouter) getActivity().getSystemService(Context.MEDIA_ROUTER_SERVICE);
        mDisplayManager = (DisplayManager) getActivity().getSystemService(Context.DISPLAY_SERVICE);
        SharedPreferences sp = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        videoAudio = (int) sp.getFloat("videoAudio", 0); // 视频音量
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
            Resources r = getContext().getResources();
            mDisplayManager.getDisplay(90);
            // Inflate the layout.
            setContentView(R.layout.presentation_content);
            frameLayout = findViewById(R.id.frameLayout);
            constraintLayout2 = findViewById(R.id.constraintLayout2);
            advanceView = findViewById(R.id.Spread_out);
            horizontalTV = findViewById(R.id.horizontalTV);//横向文字
            verticalTV = findViewById(R.id.verticalTV);//纵向文字
//            AdvancePagerAdapter.time = Universal.picPlayTime;
            //轮播时间
            AdvancePagerAdapter.time = Universal.picPlayTime;
            Log.e(TAG, "oooooooo: "+Universal.bigScreenType );
            //将视频/图片放到数组中
//            List<Advance> data = new ArrayList<>();
//            //视频&图片数据
//           data.add(new Advance("android.resource://" + getContext().getPackageName() + "/" + R.raw.test, "1"));
//            advanceView.setData(data);//将数据传入到控件中显示
            if (Universal.videoAudio == 1) {
                new AudioMngHelper(requireContext()).setVoice100(videoAudio);//设置视频音量
            } else {
                new AudioMngHelper(requireContext()).setVoice100(0);//设置视频音量
            }
            //将控件旋转270度
            constraintLayout2.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) constraintLayout2.getLayoutParams();
                params.height = 1920;
                params.width = 1080;
                constraintLayout2.setLayoutParams(params);
                constraintLayout2.setRotation(270);
            });
            Layout();
        }

        private void Layout() {
            //1-图片 2-视频 3-文字 4-图片+文字
            switch (Universal.bigScreenType) {
                case 1:
                case 2:
                    verticalTV.setVisibility(View.GONE);
                    horizontalTV.setVisibility(View.GONE);
                    advanceView.setVisibility(View.VISIBLE);
                    //读取文件
                    getFilesAllName(Universal.Secondary);
                    break;
                case 3:
                    advanceView.setVisibility(View.GONE);
                    textLayout();
                    break;
                case 4:
                    advanceView.setVisibility(View.VISIBLE);
                    //读取文件
                    getFilesAllName(Universal.Secondary);
                    textLayout();
                    if (Universal.textPosition == 0) {
                        horizontalTV.setGravity(Gravity.CENTER | Gravity.LEFT);//居中
                    } else if (Universal.textPosition == 1) {
                        horizontalTV.setGravity(Gravity.TOP | Gravity.LEFT);//居上
                    } else if (Universal.textPosition == 2) {
                        horizontalTV.setGravity(Gravity.BOTTOM | Gravity.LEFT);//居下
                    }
                    break;
            }
        }

        private void textLayout() {
            //横向
            if (Universal.fontLayout == 1) {
                //隐藏纵向文字，显示横向文字
                verticalTV.setVisibility(View.GONE);
                horizontalTV.setVisibility(View.VISIBLE);
                //显示内容
                horizontalTV.setText(Universal.fontContent);
                //背景颜色&图片
                constraintLayout2.setBackgroundColor(Color.parseColor(Universal.fontBackGround + ""));
                //文字颜色
                horizontalTV.setTextColor(Color.parseColor(Universal.fontColor + ""));
                //字体大小
                if (Universal.fontSize == 1) {
                    horizontalTV.setTextSize(90);
                } else if (Universal.fontSize == 2) {
                    horizontalTV.setTextSize(60);
                } else if (Universal.fontSize == 3) {
                    horizontalTV.setTextSize(40);
                }
            } else if (Universal.fontLayout == 2) {
                //纵向
                //隐藏横向文字，显示纵向文字
                verticalTV.setVisibility(View.VISIBLE);
                horizontalTV.setVisibility(View.GONE);
                //显示内容
                verticalTV.setText(Universal.fontContent);
                //背景颜色
                constraintLayout2.setBackgroundColor(Color.parseColor(Universal.fontBackGround + ""));
                //文字颜色
                verticalTV.setTextColor(Color.parseColor(Universal.fontColor + ""));
                //字体大小
                if (Universal.fontSize == 1) {
                    verticalTV.setTextSize(80);
                } else if (Universal.fontSize == 2) {
                    verticalTV.setTextSize(60);
                } else if (Universal.fontSize == 3) {
                    verticalTV.setTextSize(40);
                }
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
        }

        @Override
        protected void onStart() {
            super.onStart();
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
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                    Log.d(TAG, "Display #" + displayId + " removed.");
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
            mPresentation = new MyPresentation(getContext(), presentationDisplay);
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
                    Log.d(TAG, "onRouteSelected: type=" + type + ", info=" + info);
                    Show(flag);
                }

                @Override
                public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
                    Show(flag);
                }

                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
                    Show(flag);
                }
            };


    public List<String> getFilesAllName(String path) {
        //传入指定文件夹的路径
        File file = new File(path);
        File[] files = file.listFiles();
        List<Advance> imagePaths = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (checkIsImageFile(files[i].getPath())) {
                //图片
                imagePaths.add(new Advance(files[i].getPath(), "2"));
            } else {
                //视频
                imagePaths.add(new Advance(files[i].getPath(), "1"));
            }
            advanceView.setData(imagePaths);//将数据传入到控件中显示
        }
        return null;
    }

    /**
     * 判断是否是照片
     */
    public static boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        //获取拓展名
        String fileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (fileEnd.equals("jpg") || fileEnd.equals("png") || fileEnd.equals("gif")
                || fileEnd.equals("jpeg") || fileEnd.equals("bmp")) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }


}


