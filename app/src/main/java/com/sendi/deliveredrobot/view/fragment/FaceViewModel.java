package com.sendi.deliveredrobot.view.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.sendi.deliveredrobot.BuildConfig;
import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.helpers.SpeakHelper;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceViewModel extends ViewModel {

    private final String TAG = "FaceRecongnition";
    ArrayList<Info> infoArrayList = null;
    Camera c;
    float[][] features;
    String[] stringArray;
    float[] floatArray;
    int speakNum = 0;
    CameraManager manager = (CameraManager) MyApplication.Companion.getInstance().getSystemService(Context.CAMERA_SERVICE);
    ByteArrayOutputStream stream;
    YuvImage image;

    /**
     * 人脸识别方法
     *
     * @param surfaceView 预览的surfaceView控件
     */
    public void suerfaceInit(SurfaceView surfaceView) {

//        //查询录入人脸的数据
//        List<FaceTips> faceTipsList = LitePal.findAll(FaceTips.class);
//        //二维数组初始化
//        features = new float[faceTipsList.size()][512];
//        //将String转为Float[],并且放入二维数组中
//        for (int i = 0; i < faceTipsList.size(); i++) {
//            stringArray = faceTipsList.get(i).getFaceCharacteristic().replaceAll("\\[|\\]", "").split(", ");
//            floatArray = new float[stringArray.length];
//            for (int i1 = 0; i1 < stringArray.length; i1++) {
//                floatArray[i1] = Float.parseFloat(stringArray[i1]);
//            }
//            features[i] = floatArray;
//        }
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
            } else {
                c = Camera.open(1);//1，0代表前后摄像头
            }

        }
        c.setDisplayOrientation(0);//预览图与手机方向一致

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        //启动预览，到这里就能正常预览
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                RobotStatus.INSTANCE.getIdentifyFace().postValue(0);
                try {
                    c.setPreviewDisplay(surfaceHolder);
                    Camera.Parameters parameters = c.getParameters();
                    try {
                        //图片分辨率
                        parameters.setPictureSize(640, 480);
                        //预览分辨率
                        parameters.setPreviewSize(640, 480);
                        c.setParameters(parameters);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                c.startPreview(); //开始预览
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i(TAG, "surfaceChanged: " + width + " " + height);

            }

            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        //获取摄像实时数据
        c.setPreviewCallback((data, camera) -> {
            if (data == null) return;
            try {
                image = new YuvImage(data, ImageFormat.NV21, 640, 480, null);
                if (image != null) {
                    stream = new ByteArrayOutputStream();
                    Log.d(TAG, "stream size: " + stream.size());
                    image.compressToJpeg(new Rect(0, 0, 640, 480), 75, stream);//jpg图片数据size.width, size.height
                    byte[] jpegData = stream.toByteArray();
                    // 使用BitmapFactory.decodeByteArray()方法将字节数组解码为Bitmap对象
                    Bitmap bm = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
                    if (bm == null) {
                        return;
                    }
                    float xScal = (float) bm.getWidth() / Utils.inputWidth;
                    float yScal = (float) bm.getHeight() / Utils.inputHeight;
                    //将原bitmap用4:3的比例截取
                    System.out.println("************************************");
                    try {
                        infoArrayList = new ArrayList<>();
                        infoArrayList = MyApplication.faceModule.preidct(bm, xScal, yScal, false, false, features);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (infoArrayList.size() != 0) {
//                        new Thread(() -> {
//                            if (BuildConfig.IS_SPEAK && speakNum == 0) {
//                                speakNum++;
//
//                                try {
//                                    Thread.sleep(5000);  // 延迟五秒钟
//                                } catch (InterruptedException e) {
//                                    throw new RuntimeException(e);
//                                }
//                                speakNum = 0;  // 将peakNum设置为0
//                            }
//                        }).start();
                        SpeakHelper.INSTANCE.speak("嗨，我是小迪，欢迎前来了解智能服务机器人");
                    } else if (infoArrayList.size() == 0) {
                        bm.recycle();
                        image = null;
                        stream.close();
//                        if (!BuildConfig.IS_SPEAK ) {
//                            BaiduTTSHelper.getInstance().stop();
//                        }
                    }
                    bm.recycle();
                }
                infoArrayList.clear();
                image = null;
                stream.close();
            } catch (Exception ex) {
                Log.d(TAG, "surfaceInit: " + ex);
            }
        });
        infoArrayList = null;
    }

    public void onDestroy() {
        if (null != c) {
            BaiduTTSHelper.getInstance().stop();
            c.setPreviewCallback(null);
            c.stopPreview();
            c.release();
            c = null;
        }
    }
}
