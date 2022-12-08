package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.infisense.iruvc.utils.SynchronizedBitmap;
import com.sendi.deliveredrobot.BaseActivity;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.camera.IRUVC;
import com.sendi.deliveredrobot.entity.Abnormal;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.utils.AppUtils;
import com.sendi.deliveredrobot.utils.DateUtil;
import com.sendi.deliveredrobot.utils.ImgByteDealFunction;
import com.sendi.deliveredrobot.view.widget.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jni.Usbcontorl;

/**
 * @author swn
 * 人脸测温统计页面
 */
public class CameraPreviewActivity extends BaseActivity {

    CameraView cameraView;
    private int cameraWidth = 256;//160;//256;
    private int cameraHeight = 384;//240;//384;
    IRUVC p2camera;
    public boolean isrun = false;
    private SynchronizedBitmap syncimage = new SynchronizedBitmap();
    TextView time;//更新时间
    Camera.Parameters parameters;
    List<Abnormal> Abnormalbitmaps = new ArrayList();//用于统计异常人脸的数组

    Bitmap bm;//用于人脸识别的bitmap
    Camera c;
    SurfaceView sv_camera;
    MySurfaceView sv_camera_face;//自定义控件
    Info info = null;
    ArrayList<Info> infoArrayList = null;//传递人脸数据，用于人脸识别
    Bitmap bmt;//用于获取截取人脸位置之后的bitmap
    private ImageView AbnormalImage1, AbnormalImage2, AbnormalImage3;//异常人脸ImageView
    private TextView AbnormalTv1, AbnormalTv2, AbnormalTv3;//异常人脸温度情况
    private TextView AbnormalMake1, AbnormalMake2, AbnormalMake3;//异常人脸口罩情况
    private TextView AbnormalTv,textView7;
    int FaceAbnormal = 0;//异常人脸
    int TemperatureAbnormal = 0;//温度异常
    String TemperatureS = "";//体温异常
    private TextView DetectedFaces;//人脸检测
    int face = 0;//人脸检测统计
    private AudioManager audioMa;
    private MediaPlayer player;//供上一个调用
    final Timer timer = new Timer(true);
    CheckBox checkBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
//        AppUtils.checkPermission(this, 0);

        Toast.makeText(this, "OnCreate....", Toast.LENGTH_SHORT).show();
        new TimeThread().start(); //启动新的线程
        ShowPresentationByDisplaymanager();
        // 横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //设置窗体全屏，进行全屏显示。否则横屏时，会出现状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().setAttributes(params);

        initViews();
        //红外显示的方法
        initEvents();
        Surface();

    }
    TimerTask task = new TimerTask() {
        public void run() {
            player.start();
            //当获取成功后释放timer对象
            timer.cancel();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (!isrun) {
            startUSB();
            //开启红外显示
            cameraView.start();
            isrun = true;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (p2camera != null) {
            p2camera.unregisterUSB();
            //停止红外显示
            cameraView.stop();

            p2camera.stop();
        }

        syncimage.valid = false;

        isrun = false;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Usbcontorl.isload)
            Usbcontorl.usb3803_mode_setting(0);//打开5V
        Log.e("Camera", "onDestroy");
        Abnormalbitmaps.clear();
        // 释放系统资源
//        player.release();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private void startUSB() {
        if (p2camera == null) {
            p2camera = new IRUVC(Universal.cameraHeight, Universal.cameraWidth, CameraPreviewActivity.this, syncimage);
            p2camera.registerUSB();
        }

    }

    int index = 0;
    Bitmap bit;

    private void initEvents() {


        ImgByteDealFunction.setRotateMode(ImgByteDealFunction.Rotate_90);
        ImgByteDealFunction.setOriImgSize(ImgByteDealFunction.mOriImgWidth, ImgByteDealFunction.mOriImgHeight);
        ImgByteDealFunction.setmPseudoColorMode(ImgByteDealFunction.Rainbow3);
        ImgByteDealFunction.setmPseudoColorMode(ImgByteDealFunction.Rainbow3);


        //视频帧数据捕获
        ImgByteDealFunction.setiColorImg(new ImgByteDealFunction.IColorImg() {
            @Override
            public void show(int[] colorBytes) {


                //  ImgByteDealFunction.Template template = ImgByteDealFunction.getFaceTemplateInfo(index++,50,50,100,100);

                //   Log.e("template",template.aval+"  "+template.min+"  "+template.max +" "+ ImgByteDealFunction.mENVTemparate +"  "+ImgByteDealFunction.FaceTemperatureInfoList.size());

//                runOnUiThread(() -> {

                int mode = ImgByteDealFunction.getRotateMode();

                if (mode == ImgByteDealFunction.Rotate_0 || mode == ImgByteDealFunction.Rotate_180) {
                    bit = Bitmap.createBitmap(colorBytes, cameraWidth, cameraHeight / 2, Bitmap.Config.ARGB_8888);
                } else {
                    bit = Bitmap.createBitmap(colorBytes, cameraHeight / 2, cameraWidth, Bitmap.Config.ARGB_8888);
                }

                cameraView.setBitmap(bit);


//                });


            }


            @Override
            public void showTemparate(float min, float max) {
                //  Log.e("showTemparate",min+"  "+max);
                DecimalFormat decimalFormat=new DecimalFormat(".00");
                String pri=decimalFormat.format(min);
                String pri1=decimalFormat.format(max);
//                tvTem.setText("最低:" + min + "  最大:" + max);

            }

            @Override
            public void showMaxOrMinTemparate(ImgByteDealFunction.MyPoint maxPoint, ImgByteDealFunction.MyPoint minPoint, float min, float max) {

            }

        });

    }


    @SuppressLint("SetTextI18n")
    private void Surface() {

        String faceDetectionModelPath = getCacheDir().getAbsolutePath() + File.separator + "yolov5n_shuffle_256x320_quan.mnn";//人脸
        String ageAndGenderModelPath = getCacheDir().getAbsolutePath() + File.separator + "ageAndGender.mnn";//年纪
        String faceRecognizermodelPath = getCacheDir().getAbsolutePath() + File.separator + "resnet18_110.mnn";

        Utils.copyFileFromAsset(getApplicationContext(), "yolov5n_shuffle_256x320_quan.mnn", faceDetectionModelPath);
        Utils.copyFileFromAsset(getApplicationContext(), "ageAndGender.mnn", ageAndGenderModelPath);
        Utils.copyFileFromAsset(getApplicationContext(), "resnet18_110.mnn", faceRecognizermodelPath);

        FaceModule faceModule = new FaceModule(faceDetectionModelPath, ageAndGenderModelPath, faceRecognizermodelPath);

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIds = new String[0];
        try {
            cameraIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (cameraIds != null && cameraIds.length > 0) {
            //后置摄像头存在
            if (cameraIds[0] != null) {
                c = Camera.open(0);//1，0代表前后摄像头
            }

//            if (cameraIds[1] != null) {
//                c = Camera.open(1);//1，0代表前后摄像头
//            }
        }

//        c = Camera.open(0);//1，0代表前后摄像头
        c.setDisplayOrientation(0);//预览图与手机方向一致

        SurfaceHolder sh = sv_camera.getHolder();// 绑定SurfaceView，取得SurfaceHolder对象
        //启动预览，到这里就能正常预览
        sh.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    c.setPreviewDisplay(holder);
                    parameters = c.getParameters();
                    try {
                        //图片分辨率
                        parameters.setPictureSize(Universal.RGBWidth, Universal.RGBHeight);
                        //预览分辨率
                        parameters.setPreviewSize(Universal.RGBWidth, Universal.RGBHeight);
//                        parameters.setPictureFormat(PixelFormat.JPEG);
                        //parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight());  // 部分定制手机，无法正常识别该方法。
//                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        parameters.setFocusMode(Camera.Parameters.FLASH_MODE_AUTO); //对焦设置为自动
//                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
                        c.setParameters(parameters);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                c.startPreview();


            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                c.stopPreview();
                c.setPreviewCallback(null);
                c.release();

            }
        });
        sv_camera.clearFocus();
        //获取摄像实时数据传入人脸识别算法当中，并且接受算法返回的人脸数据
        c.setPreviewCallback((data, camera) -> {
            try {
                //去解析视频流，获取每一帧数据
                YuvImage image = new YuvImage(data, ImageFormat.NV21, Universal.RGBWidth, Universal.RGBHeight, null);
                if (image != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compressToJpeg(new Rect(0, 0, 600, 480), 75, stream);//jpg图片数据size.width, size.height
                    //此时stream中是一张jpg图片字节数组，可直接使用
                    bm = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());//转位图
                    Matrix matrix = new Matrix();

//                    //镜像翻转
//                    matrix.postScale(-1f, 1f); // 水平镜像翻转
//                    bm = Bitmap.createBitmap(bm, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                    float xScal = (float) bm.getWidth() / Utils.inputWidth;
                    float yScal = (float) bm.getHeight() / Utils.inputHeight;
                    long T0 = System.currentTimeMillis();
                    try {
                        face = 0;
                        TemperatureAbnormal = 0;
                        FaceAbnormal = 0;
                        //将视频流的每一帧数据传入算法中
                        infoArrayList = faceModule.preidct(bm, xScal, yScal, false, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //将方法放入子线程，防止主线程运行过载
//                    new Thread(() -> {
                    //将数组值传入自定义控件中，方便绘图
                    sv_camera_face.infoArrayList = infoArrayList;
                    //人脸检测方法
                    if (infoArrayList.size() != 0) {
                        for (Info info : infoArrayList) {
                            //更具是否有人脸位置。来判断是否检测到人脸
                            if (info.getRect() != null) {
                                //打印日志
                                System.out.println("检测到人脸！！！" + info.getMaskState() + info.getRect());
                                long T1 = System.currentTimeMillis();
                                sv_camera_face.info = info;//将info值复给MySurfaceView
                                sv_camera_face.run();
                                //打印日志，方便检测人脸识别的时间，单位为ms
                                System.out.println("打印识别时间：" + (T1 - T0));
                                System.out.println("************************************");
                                //更具Rect截取图片
                                if(ImgByteDealFunction.Temperature != 0){
                                    face = infoArrayList.size();
                                    Matrix matrix1 = new Matrix();
                                if (info.getRect().width() > info.getRect().height()) {
                                    bmt = Bitmap.createBitmap(bm, info.getRect().left*15/8,
                                            info.getRect().top*15/8,
                                            info.getRect().width()*15/8,
                                            info.getRect().width()*15/8,
                                            matrix1,
                                            true);
                                } else {
                                    bmt = Bitmap.createBitmap(bm, info.getRect().left*15/8,
                                            info.getRect().top*15/8,
                                            info.getRect().height()*15/8,
                                            info.getRect().height()*15/8,
                                            matrix1,
                                            true);
                                }

                                //将没带口罩/佩戴不规范/体温高于37的人截取显示在图片中
                                if (info.getMaskState() == 0 || info.getMaskState() == 1 || ImgByteDealFunction.Temperature > Universal.TemperatureMax) {
                                    DecimalFormat decimalFormat = new DecimalFormat(".00");
                                    //定义实体类
                                    Abnormal abnormal = new Abnormal();
                                    //将数据添加到实体类
                                    abnormal.setBitmap(bmt);
                                    abnormal.setMask(info.getMaskState());
                                    abnormal.setTemperature(ImgByteDealFunction.Temperature);
                                    //更具温度值判断温度是否异常
                                    if (ImgByteDealFunction.Temperature > Universal.TemperatureMax) {
                                        //异常
                                        AbnormalTv1.setTextColor(Color.RED);
                                        AbnormalTv2.setTextColor(Color.RED);
                                        AbnormalTv3.setTextColor(Color.RED);
                                        //如果温度值异常则计入异常显示数据中
                                        TemperatureAbnormal = TemperatureAbnormal + 1;
                                    } else {
                                        //正常
                                        AbnormalTv1.setTextColor(Color.parseColor("#00EEFF"));
                                        AbnormalTv2.setTextColor(Color.parseColor("#00EEFF"));
                                        AbnormalTv3.setTextColor(Color.parseColor("#00EEFF"));
//                                        TemperatureS = "(正常)";
                                    }
                                    //判断是否带口罩，并且温度正常，如果是则也计入异常显示中
                                    if (info.getMaskState() == 0 || info.getMaskState() == 1 && ImgByteDealFunction.Temperature < Universal.TemperatureMax) {
                                        FaceAbnormal = FaceAbnormal + 1;
                                    }
                                    //数组中添加数据
                                    Abnormalbitmaps.add(abnormal);
                                    //图1
                                    AbnormalImage1.setImageBitmap(Abnormalbitmaps.get(0).getBitmap());//异常人脸图片
                                    String temperature = decimalFormat.format(Abnormalbitmaps.get(0).getTemperature());//将温度的folat类型只取小数点后两位
                                    AbnormalTv1.setText(temperature + TemperatureS);
                                    if (Abnormalbitmaps.get(0).getMask() == 0) {//更具数值判断是否佩戴口罩
                                        AbnormalMake1.setText("未佩戴口罩");
                                    } else if (Abnormalbitmaps.get(0).getMask() == 1) {
                                        AbnormalMake1.setText("佩戴口罩不规范");
                                    } else {
                                        AbnormalMake1.setText("");
                                    }
                                    //图2
                                    AbnormalImage2.setImageBitmap(Abnormalbitmaps.get(1).getBitmap());//异常人脸图片
                                    String temperature1 = decimalFormat.format(Abnormalbitmaps.get(1).getTemperature());//将温度的folat类型只取小数点后两位
                                    AbnormalTv2.setText(temperature + TemperatureS);
                                    if (Abnormalbitmaps.get(1).getMask() == 0) {//更具数值判断是否佩戴口罩
                                        AbnormalMake2.setText("未佩戴口罩");
                                    } else if (Abnormalbitmaps.get(1).getMask() == 1) {
                                        AbnormalMake2.setText("佩戴口罩不规范");
                                    } else {
                                        AbnormalMake2.setText("");
                                    }
                                    //图3
                                    AbnormalImage3.setImageBitmap(Abnormalbitmaps.get(2).getBitmap());//异常人脸图片
                                    String temperature2 = decimalFormat.format(Abnormalbitmaps.get(2).getTemperature());//将温度的folat类型只取小数点后两位
                                    AbnormalTv3.setText(temperature2 + TemperatureS);
                                    if (Abnormalbitmaps.get(2).getMask() == 0) {//更具数值判断是否佩戴口罩
                                        AbnormalMake3.setText("未佩戴口罩");
                                    } else if (Abnormalbitmaps.get(2).getMask() == 1) {
                                        AbnormalMake3.setText("佩戴口罩不规范");
                                    } else {
                                        AbnormalMake3.setText("");
                                    }
                                    //因为屏幕中只有三张图片显示，则Abnormalbitmaps大小大于3的时候则需要重新放入数据
                                    if (Abnormalbitmaps.size() > 3) {
                                        Abnormalbitmaps.clear();
                                    }
                                }
                            }
                            }
                        }
                        //判断人脸是否有人脸，如果没有则取消自定义控件中方框的绘制
                    } else if (infoArrayList.size() == 0) {
                        sv_camera_face.info = null;
                        sv_camera_face.run();
                    }
//                    }).start();
                    image = null;
                    stream.close();
                }
            } catch (Exception ex) {

            }
        });
        //清空人脸数组
        infoArrayList = null;
    }

    private void initViews() {
        //绑定红外控件
        cameraView = findViewById(R.id.cameraView);
        cameraView.setSyncimage(syncimage);
//        tvTem = findViewById(R.id.tv_tem);
        //异常人脸的ImageView
        AbnormalTv = findViewById(R.id.AbnormalTv);
        //异常人脸图
        AbnormalImage1 = findViewById(R.id.AbnormalImage1);
        AbnormalImage2 = findViewById(R.id.AbnormalImage2);
        AbnormalImage3 = findViewById(R.id.AbnormalImage3);
        //异常人脸温度
        AbnormalTv1 = findViewById(R.id.AbnormalTv1);
        AbnormalTv2 = findViewById(R.id.AbnormalTv2);
        AbnormalTv3 = findViewById(R.id.AbnormalTv3);
        //异常人脸口罩
        AbnormalMake1 = findViewById(R.id.AbnormalMake1);
        AbnormalMake2 = findViewById(R.id.AbnormalMake2);
        AbnormalMake3 = findViewById(R.id.AbnormalMake3);
        //检测人脸数：
        DetectedFaces = findViewById(R.id.DetectedFaces);
//        recyclerView= (RecyclerView) findViewById(R.id.AbnormalRV);
        //时间
        time = findViewById(R.id.time);
        time.setAlpha(0.6f);//60%的透明度
        textView7 = findViewById(R.id.textView7);
        textView7.setAlpha(0.6f);//60%的透明度

        sv_camera_face = findViewById(R.id.sv_camera_face);
        sv_camera = findViewById(R.id.group_divider);

        if (Usbcontorl.isload)
            Usbcontorl.usb3803_mode_setting(1);//打开5V
    }

    //写一个线程，线程里面无限循环，每隔一秒发送一个消息,在主线程里面处理消息并更新时间。
    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(10);
                    Message msg = new Message();
                    msg.what = 1;  //消息(一个整型值)
                    mHandler.sendMessage(msg);// 每隔1秒发送一个msg给mHandler
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    //在主线程里面处理消息并更新UI界面
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
//                    long sysTime = System.currentTimeMillis();//获取系统时间
//                    CharSequence sysTimeStr = DateFormat.format("yyyy/MM/dd  HH:MM:ss", sysTime);//时间显示格式

                    time.setText(DateUtil.getNowDateTime()); //更新时间
                    //更新异常人脸
                    AbnormalTv.setText((TemperatureAbnormal + FaceAbnormal) + "");
                    //更新人脸识别
                    DetectedFaces.setText("检测数量：" + face);
                    break;
                default:
                    break;
            }
        }
    };

}
