package com.sendi.deliveredrobot.view.fragment;

import static com.sendi.deliveredrobot.ConstsKt.TYPE_EXCEPTION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.infisense.iruvc.utils.SynchronizedBitmap;
import com.sendi.deliveredrobot.BuildConfig;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.RobotCommand;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.camera.IRUVC;
import com.sendi.deliveredrobot.databinding.ActivityCameraPreviewBinding;
import com.sendi.deliveredrobot.entity.Abnormal;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.interfaces.FaceDataListener;
import com.sendi.deliveredrobot.model.FaceModel;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.service.CloudMqttService;
import com.sendi.deliveredrobot.utils.DateUtil;
import com.sendi.deliveredrobot.utils.ImgByteDealFunction;
import com.sendi.deliveredrobot.view.widget.ExitCameraDialog;


import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.view.widget.FaceRecognition;

import java.io.ByteArrayOutputStream;
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
public class CameraPreviewFragment extends Fragment {

    ActivityCameraPreviewBinding binding;
    NavController controller;
    IRUVC p2camera;
    public boolean isrun = false;
    private SynchronizedBitmap syncimage = new SynchronizedBitmap();
    Camera.Parameters parameters;
    List<Abnormal> Abnormalbitmaps = new ArrayList();//用于统计异常人脸的数组
    Bitmap bmt;//用于获取截取人脸位置之后的bitmap
    int FaceAbnormal = 0;//异常人脸
    int TemperatureAbnormal = 0;//温度异常
    String TemperatureS = "";//体温异常
    int face = 0;//人脸检测统计
    int abnormalNum = 0;//0正常 1异常


    float[][] features;

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
//        DialogHelper.loadingDialog.show();
        new FaceRecognition().suerFaceInit(false, 800, 600, false,this,false);
        new TimeThread().start(); //启动新的线程
//        initViews();
        //副屏状态
        RobotStatus.INSTANCE.getSdScreenStatus().postValue(1);
        features = new float[1][512];
        //红外显示的方法
        initEvents();
        Surface();
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
        FaceDataListener.removeOnChangeListener();
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
        new FaceRecognition().onDestroy();
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
                    bit = Bitmap.createBitmap(colorBytes, 800, 600, Bitmap.Config.ARGB_8888);
                } else {
                    bit = Bitmap.createBitmap(colorBytes, 800, 600, Bitmap.Config.ARGB_8888);
                }

                binding.cameraView.setBitmap(bit);


            }


            @Override
            public void showTemparate(float min, float max) {
                //  Log.e("showTemparate",min+"  "+max);
                DecimalFormat decimalFormat = new DecimalFormat(".00");
                String pri = decimalFormat.format(min);
                String pri1 = decimalFormat.format(max);
                if (max > 0.0) {
//                    DialogHelper.loadingDialog.dismiss();
                }

            }

            @Override
            public void showMaxOrMinTemparate(ImgByteDealFunction.MyPoint maxPoint, ImgByteDealFunction.MyPoint minPoint, float min, float max) {

            }

        });

    }


    @SuppressLint("SetTextI18n")
    private void Surface() {
        FaceDataListener.setOnChangeListener(() -> {
            List<FaceModel> faceModules = FaceDataListener.getFaceModels();
            binding.svCameraFace.infoArrayList = faceModules;
            for (int i = 0; i < faceModules.size(); i++) {
                // Handle FaceModule instances
                if (faceModules.get(i).getBox() != null) {
                    //打印日志
                    binding.svCameraFace.info = faceModules.get(i);//将info值复给MySurfaceView
                    binding.svCameraFace.run();
                    //打印日志，方便检测人脸识别的时间，单位为ms
                    //更具Rect截取图片
                    if (ImgByteDealFunction.Temperature > 0) {
                        face = faceModules.size();
                        Matrix matrix1 = new Matrix();
                        if (faceModules.get(i).getBox().width() > faceModules.get(i).getBox().height()) {
                            bmt = Bitmap.createBitmap(FaceDataListener.getFaceBit(), faceModules.get(i).getBox().left,
                                    faceModules.get(i).getBox().top,
                                    faceModules.get(i).getBox().width(),
                                    faceModules.get(i).getBox().width(),
                                    matrix1,
                                    true);

                        } else {
                            bmt = Bitmap.createBitmap(FaceDataListener.getFaceBit(), faceModules.get(i).getBox().left,
                                    faceModules.get(i).getBox().top,
                                    faceModules.get(i).getBox().height(),
                                    faceModules.get(i).getBox().height(),
                                    matrix1,
                                    true);
                        }
//                                    binding.ExitIV.setImageBitmap(base64ToBitmap(bitmapToBase64(bmt)));
                        //将没带口罩/佩戴不规范/体温高于37的人截取显示在图片中
                        if (faceModules.get(i).getMaskState() == 0 || faceModules.get(i).getMaskState() == 1 || ImgByteDealFunction.Temperature > Universal.TemperatureMax) {
                            DecimalFormat decimalFormat = new DecimalFormat(".00");
                            //定义实体类
                            Abnormal abnormal = new Abnormal();
                            //将数据添加到实体类
                            abnormal.setBitmap(bmt);
                            abnormal.setMask(faceModules.get(i).getMaskState());
                            abnormal.setTemperature(ImgByteDealFunction.Temperature);
                            //更具温度值判断温度是否异常
                            if (ImgByteDealFunction.Temperature >= Universal.TemperatureMax) {
                                //如果机器人为异常，则不往下进行
                                if (RobotStatus.INSTANCE.getCurrentStatus() == TYPE_EXCEPTION)
                                    return;
                                //当机器人没按下急停按钮的时候
                                if (RobotStatus.INSTANCE.getStopButtonPressed().getValue() == RobotCommand.STOP_BUTTON_PRESSED)
                                    return;
                                //通过tts语音合成播放文字
                                if (BuildConfig.IS_SPEAK) {
                                    BaiduTTSHelper.getInstance().speak(Universal.tipsTemperatureWarn);
                                }
                                //异常
                                binding.AbnormalTv1.setTextColor(Color.RED);
                                binding.AbnormalTv2.setTextColor(Color.RED);
                                binding.AbnormalTv3.setTextColor(Color.RED);
                                //如果温度值异常则计入异常显示数据中
                                TemperatureAbnormal = TemperatureAbnormal + 1;
                                abnormalNum = 1;
                            } else {
                                //正常
                                binding.AbnormalTv1.setTextColor(Color.parseColor("#00EEFF"));
                                binding.AbnormalTv2.setTextColor(Color.parseColor("#00EEFF"));
                                binding.AbnormalTv3.setTextColor(Color.parseColor("#00EEFF"));
//                                        TemperatureS = "(正常)";
                            }
                            //判断是否带口罩，并且温度正常，如果是则也计入异常显示中
                            if (faceModules.get(i).getMaskState() == 0 || faceModules.get(i).getMaskState() == 1 && ImgByteDealFunction.Temperature < Universal.TemperatureMax) {
                                FaceAbnormal = FaceAbnormal + 1;
                                //如果机器人为异常，则不往下进行
                                if (RobotStatus.INSTANCE.getCurrentStatus() == TYPE_EXCEPTION)
                                    return;
                                //当机器人没按下急停按钮的时候
                                if (RobotStatus.INSTANCE.getStopButtonPressed().getValue() == RobotCommand.STOP_BUTTON_PRESSED)
                                    return;
                                //通过tts语音合成播放文字
                                if (BuildConfig.IS_SPEAK) {
                                    BaiduTTSHelper.getInstance().speak(Universal.tipsMaskWarn);
                                }
                                abnormalNum = 1;
                            }
                            //正常
                            if (faceModules.get(i).getMaskState() == 2 && ImgByteDealFunction.Temperature < Universal.TemperatureMax) {
                                abnormalNum = 0;
                            }
                            DecimalFormat temperatureNumber = new DecimalFormat(".0");
                            JSONObject jsonObject = new JSONObject();//实例话JsonObject()
                            String timeGetTime = new Date().getTime() + "";//时间戳
                            jsonObject.put("type", "uploadGateRecord");//门岗记录type
                            jsonObject.put("time", timeGetTime);//事件发生时间(ms)(时间撮)
                            jsonObject.put("temperature", temperatureNumber.format(ImgByteDealFunction.Temperature));//温度
                            jsonObject.put("maskState", faceModules.get(i).getMaskState());//口罩佩戴情况
                            jsonObject.put("state", abnormalNum);//状态
                            jsonObject.put("face", bitmapToBase64(bmt));
                            //发送Mqtt
                            CloudMqttService.Companion.publish(JSONObject.toJSONString(jsonObject), true, 2);
                            //数组中添加数据
                            Abnormalbitmaps.add(abnormal);
                            //图1
                            try {
                                binding.AbnormalImage1.setImageBitmap(Abnormalbitmaps.get(0).getBitmap());//异常人脸图片
                                String temperature = decimalFormat.format(Abnormalbitmaps.get(0).getTemperature());//将温度的folat类型只取小数点后两位
                                binding.AbnormalTv1.setText(temperature + TemperatureS);
                                if (Abnormalbitmaps.get(0).getMask() == 0) {//更具数值判断是否佩戴口罩
                                    binding.AbnormalMake1.setText("未佩戴口罩");
                                } else if (Abnormalbitmaps.get(0).getMask() == 1) {
                                    binding.AbnormalMake1.setText("佩戴口罩不规范");
                                } else {
                                    binding.AbnormalMake1.setText("");
                                }
                            } catch (Exception ignored) {
                            }
                            //图2
                            try {
                                binding.AbnormalImage2.setImageBitmap(Abnormalbitmaps.get(1).getBitmap());//异常人脸图片
                                String temperature1 = decimalFormat.format(Abnormalbitmaps.get(1).getTemperature());//将温度的folat类型只取小数点后两位
                                binding.AbnormalTv2.setText(temperature1 + TemperatureS);
                                if (Abnormalbitmaps.get(1).getMask() == 0) {//更具数值判断是否佩戴口罩
                                    binding.AbnormalMake2.setText("未佩戴口罩");
                                } else if (Abnormalbitmaps.get(1).getMask() == 1) {
                                    binding.AbnormalMake2.setText("佩戴口罩不规范");
                                } else {
                                    binding.AbnormalMake2.setText("");
                                }
                            } catch (Exception ignored) {
                            }
                            //图3
                            try {
                                binding.AbnormalImage3.setImageBitmap(Abnormalbitmaps.get(2).getBitmap());//异常人脸图片
                                String temperature2 = decimalFormat.format(Abnormalbitmaps.get(2).getTemperature());//将温度的folat类型只取小数点后两位
                                binding.AbnormalTv3.setText(temperature2 + TemperatureS);
                                if (Abnormalbitmaps.get(2).getMask() == 0) {//更具数值判断是否佩戴口罩
                                    binding.AbnormalMake3.setText("未佩戴口罩");
                                } else if (Abnormalbitmaps.get(2).getMask() == 1) {
                                    binding.AbnormalMake3.setText("佩戴口罩不规范");
                                } else {
                                    binding.AbnormalMake3.setText("");
                                }
                            } catch (Exception ignored) {
                            }
                            //因为屏幕中只有三张图片显示，则Abnormalbitmaps大小大于3的时候则需要重新放入数据
                            if (Abnormalbitmaps.size() > 3) {
                                Abnormalbitmaps.clear();
                            }
                        }
                    }
                }
            }
        });
        binding.svCamera.clearFocus();
    }


    /**
     * bitmap转为base64
     *
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

        return result;

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
            switch (msg.what) {
                case 1:
                    binding.time.setText(DateUtil.getNowDateTime()); //更新时间
                    //更新异常人脸
                    binding.AbnormalTv.setText((TemperatureAbnormal + FaceAbnormal) + "");
                    //更新人脸识别
                    binding.DetectedFaces.setText("检测数量：" + face);
                    break;
                default:
                    break;
            }
        }
    };

}
