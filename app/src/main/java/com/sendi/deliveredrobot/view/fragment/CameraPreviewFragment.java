package com.sendi.deliveredrobot.view.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.infisense.iruvc.utils.SynchronizedBitmap;
import com.sendi.deliveredrobot.BaseFragment;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.camera.IRUVC;
import com.sendi.deliveredrobot.databinding.ActivityCameraPreviewBinding;
import com.sendi.deliveredrobot.entity.Abnormal;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.DialogHelper;
import com.sendi.deliveredrobot.model.Gatekeeper;
import com.sendi.deliveredrobot.service.CloudMqttService;
import com.sendi.deliveredrobot.utils.DateUtil;
import com.sendi.deliveredrobot.utils.ImgByteDealFunction;
import com.sendi.deliveredrobot.view.widget.ExitCameraDialog;


import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jni.Usbcontorl;

/**
 * @author swn
 * 人脸测温统计页面
 */
public class CameraPreviewFragment extends BaseFragment {

    ActivityCameraPreviewBinding binding;
    private final int cameraWidth = 256;//160;//256;
    private final int cameraHeight = 384;//240;//384;
    NavController controller;
    IRUVC p2camera;
    public boolean isrun = false;
    private SynchronizedBitmap syncimage = new SynchronizedBitmap();
    Camera.Parameters parameters;
    List<Abnormal> Abnormalbitmaps = new ArrayList<>();//用于统计异常人脸的数组
    Bitmap bm;//用于人脸识别的bitmap
    Camera c;
    Info info = null;
    ArrayList<Info> infoArrayList = null;//传递人脸数据，用于人脸识别
    Bitmap bmt;//用于获取截取人脸位置之后的bitmap
    int FaceAbnormal = 0;//异常人脸
    int TemperatureAbnormal = 0;//温度异常
    String TemperatureS = "";//体温异常
    int face = 0;//人脸检测统计
    int abnormalNum = 0;//0正常 1异常


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_camera_preview, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = Navigation.findNavController(view);
        binding = DataBindingUtil.bind(view);
        DialogHelper.loadingDialog.show();
        new TimeThread().start(); //启动新的线程
        initViews();
        //红外显示的方法
        initEvents();
        Surface();
        //双屏异显的方法
        ShowPresentationByDisplaymanager();
//        read();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            System.out.println("ok");
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }

        //弹窗
        ExitCameraDialog exitCameraDialog = new ExitCameraDialog(getContext());
        binding.exitIV.setOnClickListener(view14 -> {
            exitCameraDialog.show();
            //弹窗点击事件。回到主页面
            exitCameraDialog.YesExit.setOnClickListener(view1 -> {
                controller.navigate(R.id.action_cameraPreviewFragment_to_homeFragment);
                exitCameraDialog.dismiss();
            });
            //点击消除弹窗
            exitCameraDialog.NoExit.setOnClickListener(view12 -> exitCameraDialog.dismiss());
            exitCameraDialog.DialogClose.setOnClickListener(view13 -> exitCameraDialog.dismiss());
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isrun) {
            startUSB();
            //开启红外显示
            binding.cameraView.start();
            isrun = true;
        }
    }


    @Override
    public void onStop() {
        super.onStop();

        if (p2camera != null) {
            p2camera.unregisterUSB();
            //停止红外显示
            binding.cameraView.stop();

            p2camera.stop();
        }

        syncimage.valid = false;

        isrun = false;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Usbcontorl.isload)
            Usbcontorl.usb3803_mode_setting(0);//打开5V
        Log.e("Camera", "onDestroy");
        Abnormalbitmaps.clear();
        // 释放系统资源
//        player.release();
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    private void startUSB() {
        if (p2camera == null) {
            p2camera = new IRUVC(Universal.cameraHeight, Universal.cameraWidth, getActivity(), syncimage);
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

                int mode = ImgByteDealFunction.getRotateMode();

                if (mode == ImgByteDealFunction.Rotate_0 || mode == ImgByteDealFunction.Rotate_180) {
                    bit = Bitmap.createBitmap(colorBytes, cameraWidth, cameraHeight / 2, Bitmap.Config.ARGB_8888);
                } else {
                    bit = Bitmap.createBitmap(colorBytes, cameraHeight / 2, cameraWidth, Bitmap.Config.ARGB_8888);
                }

                binding.cameraView.setBitmap(bit);


            }


            @Override
            public void showTemparate(float min, float max) {
                //  Log.e("showTemplate",min+"  "+max);
                DecimalFormat decimalFormat = new DecimalFormat(".00");
                String pri = decimalFormat.format(min);
                String pri1 = decimalFormat.format(max);
                if (max > 0.0) {
                    DialogHelper.loadingDialog.dismiss();
                }

            }

            @Override
            public void showMaxOrMinTemparate(ImgByteDealFunction.MyPoint maxPoint, ImgByteDealFunction.MyPoint minPoint, float min, float max) {

            }

        });

    }


    @SuppressLint("SetTextI18n")
    private void Surface() {

        String faceDetectionModelPath = getActivity().getCacheDir().getAbsolutePath() + File.separator + "yolov5n_shuffle_256x320_quan.mnn";//人脸
        String ageAndGenderModelPath = getActivity().getCacheDir().getAbsolutePath() + File.separator + "ageAndGender.mnn";//年纪
        String faceRecognizermodelPath = getActivity().getCacheDir().getAbsolutePath() + File.separator + "resnet18_110.mnn";

        Utils.copyFileFromAsset(getContext(), "yolov5n_shuffle_256x320_quan.mnn", faceDetectionModelPath);
        Utils.copyFileFromAsset(getContext(), "ageAndGender.mnn", ageAndGenderModelPath);
        Utils.copyFileFromAsset(getContext(), "resnet18_110.mnn", faceRecognizermodelPath);

        FaceModule faceModule = new FaceModule(faceDetectionModelPath, ageAndGenderModelPath, faceRecognizermodelPath);

        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
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
            }else {
                c = Camera.open(1);//1，0代表前后摄像头
            }

        }

        c.setDisplayOrientation(0);//预览图与手机方向一致

        SurfaceHolder sh = binding.svCamera.getHolder();// 绑定SurfaceView，取得SurfaceHolder对象
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
                        parameters.setFocusMode(Camera.Parameters.FLASH_MODE_AUTO); //对焦设置为自动
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
        binding.svCamera.clearFocus();
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
                    binding.svCameraFace.infoArrayList = infoArrayList;
                    //人脸检测方法
                    if (infoArrayList.size() != 0) {
                        for (Info info : infoArrayList) {
                            //更具是否有人脸位置。来判断是否检测到人脸
                            if (info.getRect() != null) {
                                //打印日志
                                System.out.println("检测到人脸！！！" + info.getMaskState() + info.getRect());
                                long T1 = System.currentTimeMillis();
                                binding.svCameraFace.info = info;//将info值复给MySurfaceView
                                binding.svCameraFace.run();
                                //打印日志，方便检测人脸识别的时间，单位为ms
                                System.out.println("打印识别时间：" + (T1 - T0));
                                System.out.println("************************************");
                                //更具Rect截取图片
                                if (ImgByteDealFunction.Temperature > 0) {
                                    face = infoArrayList.size();
                                    Matrix matrix1 = new Matrix();
                                    if (info.getRect().width() > info.getRect().height()) {
                                        bmt = Bitmap.createBitmap(bm, info.getRect().left * 15 / 8,
                                                info.getRect().top * 15 / 8,
                                                info.getRect().width() * 15 / 8,
                                                info.getRect().width() * 15 / 8,
                                                matrix1,
                                                true);

                                    } else {
                                        bmt = Bitmap.createBitmap(bm, info.getRect().left * 15 / 8,
                                                info.getRect().top * 15 / 8,
                                                info.getRect().height() * 15 / 8,
                                                info.getRect().height() * 15 / 8,
                                                matrix1,
                                                true);
                                    }


//                                    binding.ExitIV.setImageBitmap(base64ToBitmap(bitmapToBase64(bmt)));
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
                                            binding.AbnormalTv1.setTextColor(Color.RED);
                                            binding.AbnormalTv2.setTextColor(Color.RED);
                                            binding.AbnormalTv3.setTextColor(Color.RED);
                                            //如果温度值异常则计入异常显示数据中
                                            TemperatureAbnormal = TemperatureAbnormal + 1;
                                            abnormalNum = 1;
                                        } else {
                                            //正常
                                            binding.AbnormalTv1.setTextColor(Color.parseColor("#00EFF"));
                                            binding.AbnormalTv2.setTextColor(Color.parseColor("#00EFF"));
                                            binding.AbnormalTv3.setTextColor(Color.parseColor("#00EFF"));
//                                        TemperatureS = "(正常)";
                                        }
                                        //判断是否带口罩，并且温度正常，如果是则也计入异常显示中
                                        if (info.getMaskState() == 0 || info.getMaskState() == 1 && ImgByteDealFunction.Temperature < Universal.TemperatureMax) {
                                            FaceAbnormal = FaceAbnormal + 1;
                                            abnormalNum = 1;
                                        }
                                        //正常
                                        if (info.getMaskState() == 2 && ImgByteDealFunction.Temperature < Universal.TemperatureMax) {
                                            abnormalNum = 0;
                                        }
                                        DecimalFormat temperatureNumber = new DecimalFormat(".0");
                                        JSONObject jsonObject = new JSONObject();//实例话JsonObject()
                                        String timeGetTime = new Date().getTime()+"";//时间戳
                                        jsonObject.put("type", "uploadGateRecord");//门岗记录type
                                        jsonObject.put("time", timeGetTime);//事件发生时间(ms)(时间撮)
                                        jsonObject.put("temperature", temperatureNumber.format(ImgByteDealFunction.Temperature));//温度
                                        jsonObject.put("maskState", info.getMaskState());//口罩佩戴情况
                                        jsonObject.put("state", abnormalNum);//状态
                                        jsonObject.put("face", bitmapToBase64(bmt));
                                        //发送Mqtt
                                        CloudMqttService.Companion.publish(JSONObject.toJSONString(jsonObject), true, 2);
                                        //数组中添加数据
                                        Abnormalbitmaps.add(abnormal);
                                        //图1
                                        binding.AbnormalImage1.setImageBitmap(Abnormalbitmaps.get(0).getBitmap());//异常人脸图片
                                        String temperature = decimalFormat.format(Abnormalbitmaps.get(0).getTemperature());//将温度的flat类型只取小数点后两位
                                        binding.AbnormalTv1.setText(temperature + TemperatureS);
                                        if (Abnormalbitmaps.get(0).getMask() == 0) {//更具数值判断是否佩戴口罩
                                            binding.AbnormalMake1.setText("未佩戴口罩");
                                        } else if (Abnormalbitmaps.get(0).getMask() == 1) {
                                            binding.AbnormalMake1.setText("佩戴口罩不规范");
                                        } else {
                                            binding.AbnormalMake1.setText("");
                                        }
                                        //图2
                                        binding.AbnormalImage2.setImageBitmap(Abnormalbitmaps.get(1).getBitmap());//异常人脸图片
                                        String temperature1 = decimalFormat.format(Abnormalbitmaps.get(1).getTemperature());//将温度的flat类型只取小数点后两位
                                        binding.AbnormalTv2.setText(temperature1 + TemperatureS);
                                        if (Abnormalbitmaps.get(1).getMask() == 0) {//更具数值判断是否佩戴口罩
                                            binding.AbnormalMake2.setText("未佩戴口罩");
                                        } else if (Abnormalbitmaps.get(1).getMask() == 1) {
                                            binding.AbnormalMake2.setText("佩戴口罩不规范");
                                        } else {
                                            binding.AbnormalMake2.setText("");
                                        }
                                        //图3
                                        binding.AbnormalImage3.setImageBitmap(Abnormalbitmaps.get(2).getBitmap());//异常人脸图片
                                        String temperature2 = decimalFormat.format(Abnormalbitmaps.get(2).getTemperature());//将温度的flat类型只取小数点后两位
                                        binding.AbnormalTv3.setText(temperature2 + TemperatureS);
                                        if (Abnormalbitmaps.get(2).getMask() == 0) {//更具数值判断是否佩戴口罩
                                            binding.AbnormalMake3.setText("未佩戴口罩");
                                        } else if (Abnormalbitmaps.get(2).getMask() == 1) {
                                            binding.AbnormalMake3.setText("佩戴口罩不规范");
                                        } else {
                                            binding.AbnormalMake3.setText("");
                                        }
                                        //因为屏幕中只有三张图片显示，则Abnormality大小大于3的时候则需要重新放入数据
                                        if (Abnormalbitmaps.size() > 3) {
                                            Abnormalbitmaps.clear();
                                        }
                                    }
                                }
                            }
                        }
                        //判断人脸是否有人脸，如果没有则取消自定义控件中方框的绘制
                    } else {
                        infoArrayList.size();
                        binding.svCameraFace.info = null;
                        binding.svCameraFace.run();
                    }
//                    }).start();
                    image = null;
                    stream.close();
                }
            } catch (Exception ignored) {

            }
        });
        //清空人脸数组
        infoArrayList = null;
    }


    /**
     * bitmap转为base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        write(result);
        return result;

    }

//    public static Bitmap base64ToBitmap(String base64Data) {
//        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//    }

    //    //读取操作
//    private void read() {
//        try {
//            File dir = Environment.getExternalStorageDirectory();
//            File dataFile = new File(dir,"Base64.txt");
//            FileInputStream fis = new FileInputStream(dataFile);
//            //读取本地小文件
//            byte[] bytes = new byte[fis.available()];
//            fis.read(bytes);
//            fis.close();
//            String str = new String(bytes,"utf-8");
//            System.out.println(str);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    //写入操作
//    private static void write(String base64) {
//
//        try {
//            //获取手机外部存储地址
//            File dir = Environment.getExternalStorageDirectory();
//            File dataFile = new File(dir,"Base64.txt");
//            if (!dataFile.exists()) {
//                dataFile.createNewFile();
//            }
//            FileOutputStream fos = new FileOutputStream(dataFile);
//            fos.write(new String("这是base64："+base64+"结束").getBytes("utf-8"));
//            fos.flush();
//            fos.close();
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    List<Gatekeeper> gatekeepers = JSONObject.parseArray(jsonObject, Gatekeeper.class);
    private void initViews() {
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
    private final Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                binding.time.setText(DateUtil.getNowDateTime()); //更新时间
                //更新异常人脸
                binding.AbnormalTv.setText((TemperatureAbnormal + FaceAbnormal) + "");
                //更新人脸识别
                binding.DetectedFaces.setText("检测数量：" + face);
            }
        }
    };

}
