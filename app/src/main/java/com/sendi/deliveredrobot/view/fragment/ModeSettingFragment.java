package com.sendi.deliveredrobot.view.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sendi.deliveredrobot.R;

import com.sendi.deliveredrobot.databinding.FragmentModeSettingBinding;
import com.sendi.deliveredrobot.entity.Universal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModeSettingFragment extends Fragment {

    FragmentModeSettingBinding binding;
    String TAG = "ModeSettingFragment";
    SharedPreferences sp;
    View view;
    SharedPreferences.Editor editor;
    public int inaccessiblePoint = 0;
    public float leadingSpeedNum = 0.3f;//引领速度默认值
    public float ExplanationSpeedNum = 0.3f;//去往讲解点速度默认值
    public float explainNum = 1;//讲解语速默认值
    public float stayNum = 1;//逗留时间默认值
    public float BreakTaskNum = 1;//打断任务触控点暂停时间默认值
    public int endOfExplanation = 0;//0为再讲一遍，1为选择其他路线
    int abnormalWarning;//异常警告方式
    StringBuffer stringBuffer = new StringBuffer();
    float patrolSpeedNum;
    float suspensionNum;
    int tempMode;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //引领速度
        binding.LeadingSpeed.setRange(0.3f, 1.2f, 1);
        //去往讲解点速度
        binding.ExplanationSpeed.setRange(0.3f, 1.2f, 1);
        //讲解语速
        binding.explain.setRange(1, 6, 0);
        //逗留时间
        binding.stay.setRange(1, 180, 0);
        //打断任务触控点暂停时间
        binding.BreakTask.setRange(1, 180, 0);
        //巡逻速度
        binding.patrolSpeed.setRange(0.3f, 1.2f, 1);
        //巡逻过程中暂停时间
        binding.suspension.setRange(1, 180, 0);


        Boolean etWelcome = sp.getBoolean("etiquetteWelcome", false);
        inaccessiblePoint = sp.getInt("inaccessiblePoint", 0);//不能到达点：0：直接下一个;1：重复第二次到达
        float leadSpeed = sp.getFloat("LeadingSpeed", 0.3f);//读取引领速度值
        float explanationspeed = sp.getFloat("ExplanationSpeed", 0.3f);//读取去往讲解点速度值
        float explainspeed = sp.getFloat("explain", 1);//读取讲解语速值
        float stayspeed = sp.getFloat("stay", 1);//读取逗留时间值
        float BreakTaskspeed = sp.getFloat("BreakTask", 1);//读取暂停时间值
        boolean interrupt = sp.getBoolean("interrupted", false);//读取引领过程中是否可以被打断
        boolean InExplanation = sp.getBoolean("InterruptionExplanation", false);//读取讲解过程中是否允许被打断
        int endNumber = sp.getInt("endOfExplanation", 0);//讲解结束允许
        String patrolContent = sp.getString("patrolContent", "1 ");//巡逻内容: 0：口罩检测;1：体温检测;2：人脸识别;
        abnormalWarning = sp.getInt("abnormalWarning", 1);//异常警告方式：0：语音播报;1：就近跟随;
        patrolSpeedNum = sp.getFloat("patrolSpeed", 0.3f);//巡逻速度
        suspensionNum = sp.getFloat("suspension", 1);//巡逻过程中暂停时间
        tempMode = sp.getInt("tempMode", 0);//测温模式选择：0：单人测温;1：多人测温
        boolean VoiceAnnouncements = sp.getBoolean("VoiceAnnouncements", false);//智能测温语音播报
        //讲解结束方式判断
        if (endNumber == 0) {
            binding.AgainCB.setChecked(true);
        } else {
            binding.otherCB.setChecked(true);
        }
        //测温模式选择判断
        if (tempMode == 0) {
            binding.singleTemp.setChecked(true);
        } else {
            binding.multipleTemp.setChecked(true);
        }
        //异常警告方式判断
        if (abnormalWarning == 1) {
            binding.followNearby.setChecked(true);
        } else {
            binding.Announcements.setChecked(true);
        }
        if (inaccessiblePoint == 0) {
            binding.skip.setChecked(true);
        } else {
            binding.repeat.setChecked(true);
        }
        //巡逻方式判断
        if (patrolContent != null) {
            for (int i = 0; i < patrolContent.split(" ").length; i++) {
                check(patrolContent.split(" ")[i]);
            }
        }
        binding.suspension.setCur(suspensionNum);
        binding.patrolSpeed.setCur(patrolSpeedNum);
        binding.etiquette.setChecked(etWelcome);
        binding.InterruptionExplanation.setChecked(InExplanation);
        binding.VoiceAnnouncements.setChecked(VoiceAnnouncements);
        binding.interrupted.setChecked(interrupt);
        binding.LeadingSpeed.setCur(leadSpeed);
        binding.ExplanationSpeed.setCur(explanationspeed);
        binding.explain.setCur(explainspeed);
        binding.stay.setCur(stayspeed);
        binding.BreakTask.setCur(BreakTaskspeed);


        //VIP人脸识别
        binding.etiquette.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean("etiquetteWelcome", b);
            editor.commit();
        });

        //不能到达点的radioGroup点击事件
        binding.inaccessiblePoint.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == binding.skip.getId()) {
                Log.d(TAG, "选择跳过，直接下一个");
            } else if (i == binding.repeat.getId()) {
                Log.d(TAG, "选择重复第二次到达");
            }
        });
        //测温模式
        binding.tempMode.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == binding.singleTemp.getId()) {
                Log.d(TAG, "选择单人测温");
            } else if (i == binding.multipleTemp.getId()) {
                Log.d(TAG, "选择多人测温");
            }
        });
        //引领
        binding.LeadingSpeed.setOnSeekBarChangeListener(current -> {
            leadingSpeedNum = binding.LeadingSpeed.getCur();
            Log.d(TAG, "设置引领速度：" + leadingSpeedNum);
            //将数据存储到临时文件
            editor.putFloat("LeadingSpeed", leadingSpeedNum);
            editor.commit();
        });
        //去往讲解点
        binding.ExplanationSpeed.setOnSeekBarChangeListener(current -> {
            ExplanationSpeedNum = binding.ExplanationSpeed.getCur();
            Log.d(TAG, "设置去往讲解点速度: " + ExplanationSpeedNum);
            //将数据存储到临时文件
            editor.putFloat("ExplanationSpeed", ExplanationSpeedNum);
            editor.commit();
        });
        //讲解语速
        binding.explain.setOnSeekBarChangeListener(current -> {
            explainNum = binding.explain.getCurInt();
            Log.d(TAG, "设置讲解语速: " + explainNum);
            //将数据存储到临时文件
            editor.putFloat("explain", explainNum);
            editor.commit();
        });
        //逗留时间
        binding.stay.setOnSeekBarChangeListener(current -> {
            stayNum = binding.stay.getCurInt();
            Log.d(TAG, "设置逗留时间: " + stayNum);
            //将数据存储到临时文件
            editor.putFloat("stay", stayNum);
            editor.commit();
        });
        //打断任务触控点暂停时间
        binding.BreakTask.setOnSeekBarChangeListener(current -> {
            BreakTaskNum = binding.BreakTask.getCurInt();
            Log.d(TAG, "设置打断任务触控点暂停时间: " + BreakTaskNum);
            //将数据存储到临时文件
            editor.putFloat("BreakTask", BreakTaskNum);
            editor.commit();
        });
        //巡逻速度
        binding.patrolSpeed.setOnSeekBarChangeListener(current -> {
            patrolSpeedNum = binding.patrolSpeed.getCur();
            Log.d(TAG, "巡逻速度: " + patrolSpeedNum);
            //将数据存储到临时文件
            editor.putFloat("patrolSpeed", patrolSpeedNum);
            editor.commit();
        });
        //巡逻过程中暂停时间
        binding.suspension.setOnSeekBarChangeListener(current -> {
            suspensionNum = binding.suspension.getCurInt();
            Log.d(TAG, "巡逻速度: " + suspensionNum);
            //将数据存储到临时文件
            editor.putFloat("suspension", suspensionNum);
            editor.commit();
        });
        //引领过程中是否允许被打断
        binding.interrupted.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean("interrupted", b);
            editor.commit();
        });

        //讲解过程中是否允许被打断
        binding.InterruptionExplanation.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean("InterruptionExplanation", b);
            editor.commit();
        });
        binding.VoiceAnnouncements.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("VoiceAnnouncements", isChecked);
            editor.commit();
        });
        //讲解结束允许：再讲一遍
        binding.AgainCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.AgainCB.setChecked(true);
                binding.otherCB.setChecked(false);
            } else {
                binding.AgainCB.setChecked(false);
            }
        });
        //讲解结束允许：选择其他路线
        binding.otherCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.otherCB.setChecked(true);
                binding.AgainCB.setChecked(false);
            } else {
                binding.otherCB.setChecked(false);
            }
        });
        //异常警告方式：语音播报
        binding.Announcements.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.Announcements.setChecked(true);
                binding.followNearby.setChecked(false);
            } else {
                binding.Announcements.setChecked(false);
            }
        });
        //异常警告方式：就近跟随
        binding.followNearby.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.Announcements.setChecked(false);
                binding.followNearby.setChecked(true);
            } else {
                binding.followNearby.setChecked(false);
            }
        });
        //巡逻内容：口罩检测
        binding.maskDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.maskDetection.setChecked(isChecked);
        });
        //巡逻内容：体温检测
        binding.tempDetection.setOnCheckedChangeListener((buttonView, isChecked) -> binding.tempDetection.setChecked(isChecked));
        //巡逻内容：人脸识别
        binding.faceRecognition.setOnCheckedChangeListener((buttonView, isChecked) -> binding.faceRecognition.setChecked(isChecked));
    }

    //解析StringBuffer中的数据用来保存勾选
    private void check(String checkName) {
        if ("0".equals(checkName)) {
            binding.maskDetection.setChecked(true);
        } else if ("1".equals(checkName)) {
            binding.tempDetection.setChecked(true);
        } else if ("2".equals(checkName)) {
            binding.faceRecognition.setChecked(true);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        // 获取编辑器
        editor = sp.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mode_setting, container, false);
        binding = DataBindingUtil.bind(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (getActivity() != null) {
            stringBuffer = new StringBuffer();
            //礼仪迎宾选择统计
            if (binding.skip.isChecked()) {
                inaccessiblePoint = 0;
            }
            if (binding.repeat.isChecked()) {
                inaccessiblePoint = 1;
            }
            //测温模式统计
            if (binding.singleTemp.isChecked()) {
                tempMode = 0;
            }
            if (binding.multipleTemp.isChecked()) {
                tempMode = 1;
            }
            //讲解结束允许统计
            if (binding.AgainCB.isChecked()) {
                endOfExplanation = 0;
            }
            if (binding.otherCB.isChecked()) {
                endOfExplanation = 1;
            }
            //异常警告方式统计
            if (binding.Announcements.isChecked()) {
                abnormalWarning = 0;
            }
            if (binding.followNearby.isChecked()) {
                abnormalWarning = 1;
            }
            //巡逻内容数据统计
            if (binding.maskDetection.isChecked()) {
                stringBuffer.append("0 ");
            }
            if (binding.tempDetection.isChecked()) {
                stringBuffer.append("1 ");
            }
            if (binding.faceRecognition.isChecked()) {
                stringBuffer.append("2 ");
            }

            //提交数据
            editor.putInt("inaccessiblePoint", inaccessiblePoint);
            editor.putInt("endOfExplanation", endOfExplanation);
            editor.putInt("abnormalWarning", abnormalWarning);
            editor.putString("patrolContent", stringBuffer.toString());
            editor.putInt("tempMode", tempMode);
            editor.commit();
        }
    }
}

