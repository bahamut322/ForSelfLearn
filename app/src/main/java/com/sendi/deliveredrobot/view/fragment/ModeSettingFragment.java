package com.sendi.deliveredrobot.view.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sendi.deliveredrobot.R;

import com.sendi.deliveredrobot.databinding.FragmentModeSettingBinding;

public class ModeSettingFragment extends Fragment {

    FragmentModeSettingBinding binding;
    String TAG = "TAG";
    SharedPreferences sp;
    View view;
    SharedPreferences.Editor editor;
    public String etiquetteWelcome = "VIP人脸识别";
    public String inaccessiblePoint = "选择跳过，直接下一个";
    public float leadingSpeedNum = 0.3f;//引领速度默认值
    public float ExplanationSpeedNum = 0.3f;//去往讲解点速度默认值
    public float explainNum = 1;//讲解语速默认值
    public float stayNum = 1;//逗留时间默认值
    public float BreakTaskNum = 1;//打断任务触控点暂停时间默认值
    public int endOfExplanation = 0;//0为再讲一遍，1为选择其他路线



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


        String etWelcome = sp.getString("etiquetteWelcome", "");
        String inPoint = sp.getString("inaccessiblePoint", "选择重复第二次到达");
        float leadSpeed = sp.getFloat("LeadingSpeed", 0);//读取引领速度值
        float explanationspeed = sp.getFloat("ExplanationSpeed", 0);//读取去往讲解点速度值
        float explainspeed = sp.getFloat("explain", 0);//读取讲解语速值
        float stayspeed = sp.getFloat("stay", 0);//读取逗留时间值
        float BreakTaskspeed = sp.getFloat("BreakTask", 0);//读取暂停时间值
        boolean interrupt = sp.getBoolean("interrupted", false);//读取引领过程中是否可以被打断
        boolean InExplanation = sp.getBoolean("InterruptionExplanation", false);//读取讲解过程中是否允许被打断
        int endNumber = sp.getInt("endOfExplanation", 0);//讲解结束允许

        if (endNumber == 0) {
            binding.AgainCB.setChecked(true);
        } else {
            binding.otherCB.setChecked(true);
        }
        binding.InterruptionExplanation.setChecked(InExplanation);
        binding.interrupted.setChecked(interrupt);
        binding.LeadingSpeed.setCur(leadSpeed);
        binding.ExplanationSpeed.setCur(explanationspeed);
        binding.explain.setCur(explainspeed);
        binding.stay.setCur(stayspeed);
        binding.BreakTask.setCur(BreakTaskspeed);

        if (inPoint == null) {
            iPoint(inaccessiblePoint);
        } else {
            iPoint(inPoint);
        }
        if (etWelcome == null) {
            //赋予一个默认值
            etWelcome(etiquetteWelcome);
        } else {
            etWelcome(etWelcome);
        }
        //礼仪迎宾radioGroup点击事件
        binding.etiquette.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == binding.VIPrb.getId()) {
                Log.d(TAG, "选择VIP人脸迎宾 ");
            } else if (i == binding.otherRb.getId()) {
                Log.d(TAG, "选择其他迎宾方式 ");
            }
        });
        //不能到达点的radioGroup点击事件
        binding.inaccessiblePoint.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == binding.skip.getId()) {
                Log.d(TAG, "选择跳过，直接下一个");
            } else if (i == binding.repeat.getId()) {
                Log.d(TAG, "选择重复第二次到达");
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
        //礼仪迎宾选择统计
        if (binding.VIPrb.isChecked()) {
            etiquetteWelcome = "VIP人脸识别";
        }
        if (binding.otherRb.isChecked()) {
            etiquetteWelcome = "其他方式迎宾";
        }
        if (binding.skip.isChecked()) {
            inaccessiblePoint = "选择跳过，直接下一个";
        }
        if (binding.repeat.isChecked()) {
            inaccessiblePoint = "选择重复第二次到达";
        }
        if (binding.AgainCB.isChecked()) {
            endOfExplanation = 0;
        }
        if (binding.otherCB.isChecked()) {
            endOfExplanation = 1;
        }
        //提交数据
        editor.putString("inaccessiblePoint", inaccessiblePoint);
        editor.putString("etiquetteWelcome", etiquetteWelcome);
        editor.putInt("endOfExplanation", endOfExplanation);
        editor.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
//        //礼仪迎宾选择统计
//        if (binding.VIPrb.isChecked()) {
//            etiquetteWelcome = "VIP人脸识别";
//        }
//        if (binding.otherRb.isChecked()) {
//            etiquetteWelcome = "其他方式迎宾";
//        }
//        if (binding.skip.isChecked()) {
//            inaccessiblePoint = "选择跳过，直接下一个";
//        }
//        if (binding.repeat.isChecked()) {
//            inaccessiblePoint = "选择重复第二次到达";
//        }
//        if (binding.AgainCB.isChecked()) {
//            endOfExplanation = 0;
//        }
//        if (binding.otherCB.isChecked()) {
//            endOfExplanation = 1;
//        }
//        //提交数据
//        editor.putString("inaccessiblePoint", inaccessiblePoint);
//        editor.putString("etiquetteWelcome", etiquetteWelcome);
//        editor.putInt("endOfExplanation", endOfExplanation);
//        editor.commit();
    }

    private void etWelcome(String etiquetteWelcomeSelect) {
        if ("VIP人脸识别".equals(etiquetteWelcomeSelect)) {
            binding.VIPrb.setChecked(true);
        } else if ("其他方式迎宾".equals(etiquetteWelcomeSelect)) {
            binding.otherRb.setChecked(true);
        }
    }

    private void iPoint(String inaccessiblePointSelect) {
        if ("选择跳过，直接下一个".equals(inaccessiblePointSelect)) {
            binding.skip.setChecked(true);
        } else if ("选择重复第二次到达".equals(inaccessiblePointSelect)) {
            binding.repeat.setChecked(true);
        }
    }

}

