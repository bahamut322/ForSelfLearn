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
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import com.sendi.deliveredrobot.BuildConfig;
import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.entity.FaceTips;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;

import org.litepal.LitePal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FaceViewModel extends ViewModel {

    private final String TAG = "FaceRecongnition";
    ArrayList<Info> infoArrayList = null;
    Camera c;
    float[][] features;
    String[] stringArray;
    float[] floatArray;

    /**
     * 人脸识别方法
     * @param surfaceView 预览的surfaceView控件
     */
    public void suerfaceInit( SurfaceView surfaceView ) {
        //查询数据
        List<FaceTips> faceTipsList = LitePal.findAll(FaceTips.class);
        //二维数组初始化
        features = new float[faceTipsList.size()][512];
        //将String转为Float[],并且放入二维数组中
        for (int i = 0; i < faceTipsList.size(); i++) {
            stringArray = faceTipsList.get(i).getFaceCharacteristic().replaceAll("\\[|\\]", "").split(", ");
            floatArray = new float[stringArray.length];
            for (int i1 = 0; i1 < stringArray.length; i1++) {
                floatArray[i1] = Float.parseFloat(stringArray[i1]);
            }
            features[i] = floatArray;
        }

        CameraManager manager = (CameraManager) MyApplication.Companion.getInstance().getSystemService(Context.CAMERA_SERVICE);
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
//            Camera.Size size = camera.getParameters().getPreviewSize();
            try {
                YuvImage image = new YuvImage(data, ImageFormat.NV21, 640, 480, null);
                if (image != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compressToJpeg(new Rect(0, 0, 600, 480), 75, stream);//jpg图片数据size.width, size.height
                    //此时stream中是一张jpg图片字节数组，可直接使用
                    Bitmap bm;
                    bm = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());//转位图
                    float xScal = (float) bm.getWidth() / Utils.inputWidth;
                    float yScal = (float) bm.getHeight() / Utils.inputHeight;
                    //将原bitmap用4:3的比例截取
//                    System.out.println("************************************");
                    long T0 = System.currentTimeMillis();
                    try {
                           infoArrayList = MyApplication.faceModule.preidct(bm, xScal, yScal, false, true, features);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    long T1 = System.currentTimeMillis();
//                    System.out.println(T1 - T0);
//                    System.out.println("Hello MNN!");
//                    System.out.println("特征1：" + infoArrayList.size());
                    if (infoArrayList.size() != 0) {
                        for (Info info : infoArrayList) {
                            if (BuildConfig.IS_SPEAK) {
                                BaiduTTSHelper.getInstance().speak("你好:欢迎来到申迪");
                            }
//                            new Handler().postDelayed(() -> {
//                            },5000); //5秒
                            //打印日志
                            System.out.println("相似度：" + Arrays.toString(info.getConfList()) + "");
                            // 初始化最大值和位置
                            float max_value = Float.NEGATIVE_INFINITY;
                            int max_index = -1;
                            // 遍历数组并找到最大值
                            for (int i = 0; i < info.getConfList().length; i++) {
                                float val = info.getConfList()[i];
                                if (val > max_value) {
                                    max_value = val;
                                    max_index = i;
                                }
                            }
                            // 输出最大值和位置
                            System.out.println("Max value is: " + max_value);
                            System.out.println("Max value is in position " + (max_index + 1));
                            //查询相似度最大的那个的人脸特征
                            List<FaceTips> tipsList = LitePal.where("faceCharacteristic = ?", faceTipsList.get((max_index)).getFaceCharacteristic()).find(FaceTips.class);
                            if (max_value > 0.88f) {
                                Toast.makeText(MyApplication.Companion.getInstance(), "你好！" + tipsList.get(0).getName(), Toast.LENGTH_SHORT).show();
                                if (BuildConfig.IS_SPEAK) {
                                    BaiduTTSHelper.getInstance().speak("你好:"+tipsList.get(0).getName());
                                }
                                Log.e(TAG, "onCreate: " + tipsList.get(0).getName());
                            }

                        }
                    }else if (infoArrayList.size() == 0) {
                        if (RobotStatus.INSTANCE.getIdentifyFace().getValue()==1) {
                            BaiduTTSHelper.getInstance().stop();
                        }
                    }
                    image = null;
                    stream.close();
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error:" + ex.getMessage());
            }
        });
        infoArrayList = null;
    }

    public void onDestroy() {
        if(null != c) {
            c.setPreviewCallback(null);
            c.stopPreview();
            c.release();
            c = null;
        }
    }
}
