package com.sendi.deliveredrobot.view.fragment;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sendi.deliveredrobot.R;

import com.sendi.deliveredrobot.databinding.FragmentModeSettingBinding;
import com.sendi.deliveredrobot.entity.entitySql.QuerySql;
import com.sendi.deliveredrobot.entity.UpDataSQL;
import com.sendi.deliveredrobot.model.BasicModel;
import com.sendi.deliveredrobot.viewmodel.SettingViewModel;

import java.util.Objects;

public class ModeSettingFragment extends Fragment {

    FragmentModeSettingBinding binding;
    String TAG = "ModeSettingFragment";
    View view;
    SettingViewModel viewModel;
    public int inaccessiblePoint = 0;
    public float leadingSpeedNum = 0.3f;//引领速度默认值
    public float ExplanationSpeedNum = 0.3f;//去往讲解点速度默认值
    public float businessSpeedNum = 0.3f;//去往导购点默认值
//    public float explainNum = 1;//讲解语速默认值
    public float stayNum = 1;//逗留时间默认值
    public float BreakTaskNum = 1;//打断任务触控点暂停时间默认值
    public float businessTimeNum = 1;//打断导购任务时间
//    public int endOfExplanation = 0;//0为再讲一遍，1为选择其他路线
//    int abnormalWarning;//异常警告方式
    StringBuffer stringBuffer = new StringBuffer();
    float patrolSpeedNum;
    float suspensionNum;
//    int tempMode;
    ContentValues values;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        values = new ContentValues();
        viewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        BasicModel settingData = viewModel.settingData();
        //引领速度
        binding.LeadingSpeed.setRange(0.3f, 1.2f, 1);
        //去往讲解点速度
        binding.ExplanationSpeed.setRange(0.3f, 1.2f, 1);
        //去往导购点速度
        binding.businessSpeed.setRange(0.3f,1.2f,1);
        //讲解语速
//        binding.explain.setRange(1, 15, 0);
        //逗留时间
        binding.stay.setRange(1, 180, 0);
        //打断任务触控点暂停时间
        binding.BreakTask.setRange(1, 180, 0);
        binding.timeBusiness.setRange(1,180,0);
        //巡逻速度
        binding.patrolSpeed.setRange(0.3f, 1.2f, 1);
        //巡逻过程中暂停时间
        binding.suspension.setRange(1, 180, 0);


        //讲解结束方式判断
        if (settingData.getExplanationFinish() == 0) {
            binding.AgainCB.setChecked(true);
        } else if (settingData.getExplanationFinish() == 1){
            binding.otherCB.setChecked(true);
        }else if (settingData.getExplanationFinish() == 2){
            binding.otherCB.setChecked(true);
            binding.AgainCB.setChecked(true);
        }
        //测温模式选择判断
        if (settingData.getTempMode() == 0) {
            binding.singleTemp.setChecked(true);
        } else {
            binding.multipleTemp.setChecked(true);
        }
        //异常警告方式判断
        if (Objects.equals(settingData.getError(), "1")) {
            binding.followNearby.setChecked(true);
        } else {
            binding.Announcements.setChecked(true);
        }
        if (settingData.getUnArrive() == 0) {
            binding.skip.setChecked(true);
        } else {
            binding.repeat.setChecked(true);
        }
        //巡逻方式判断
        if ( settingData.getPatrolContent()!= null) {
            for (int i = 0; i < settingData.getPatrolContent().split(" ").length; i++) {
                check(settingData.getPatrolContent().split(" ")[i]);
            }
        }
        binding.suspension.setCur(settingData.getPatrolStayTime());
        binding.patrolSpeed.setCur(settingData.getPatrolSpeed());
        binding.etiquette.setChecked(settingData.getIdentifyVip());
        binding.InterruptionExplanation.setChecked(settingData.getExplainInterrupt());
        binding.interruptionBusiness.setChecked(settingData.getBusinessInterrupt());
        binding.VoiceAnnouncements.setChecked(settingData.getVoiceAnnouncements());
        binding.LeadingSpeed.setCur(settingData.getLeadingSpeed());
        binding.ExplanationSpeed.setCur(settingData.getGoExplanationPoint());
        binding.businessSpeed.setCur(settingData.getGoBusinessPoint());
//        binding.explain.setCur(settingData.getSpeechSpeed());
        binding.stay.setCur(settingData.getStayTime());
        binding.BreakTask.setCur(settingData.getExplainWhetherTime());
        binding.timeBusiness.setCur(settingData.getBusinessWhetherTime());
        binding.checkBoxExplainFinishNotGoBack.setChecked(settingData.getExplainFinishedNotGoBack() == 1);


        //VIP人脸识别
        binding.etiquette.setOnCheckedChangeListener((compoundButton, b) -> {
//            editor.putBoolean("etiquetteWelcome", b);
//            editor.commit();
            values.put("identifyvip",BooleanToInt(b));
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
            values.put("leadingspeed",leadingSpeedNum);
        });
        //去往讲解点
        binding.ExplanationSpeed.setOnSeekBarChangeListener(current -> {
            ExplanationSpeedNum = binding.ExplanationSpeed.getCur();
            Log.d(TAG, "设置去往讲解点速度: " + ExplanationSpeedNum);
            values.put("goexplanationpoint",ExplanationSpeedNum);
        });
        binding.businessSpeed.setOnSeekBarChangeListener(current -> {
            businessSpeedNum = binding.businessSpeed.getCur();
            Log.d(TAG, "设置去往导购点速度: " + ExplanationSpeedNum);
            values.put("gobusinesspoint",businessSpeedNum);
        });
//        //讲解语速
//        binding.explain.setOnSeekBarChangeListener(current -> {
//            explainNum = binding.explain.getCurInt();
//            Log.d(TAG, "设置讲解语速: " + explainNum);
//            values.put("speechspeed",explainNum);
//            viewModel.timbres(explainNum+"");
//        });
        //逗留时间
        binding.stay.setOnSeekBarChangeListener(current -> {
            stayNum = binding.stay.getCurInt();
            Log.d(TAG, "设置逗留时间: " + stayNum);
            values.put("staytime",stayNum);
        });
        //打断任务触控点暂停时间
        binding.BreakTask.setOnSeekBarChangeListener(current -> {
            BreakTaskNum = binding.BreakTask.getCurInt();
            Log.d(TAG, "设置打断讲解任务触控点暂停时间: " + BreakTaskNum);
            values.put("explainwhethertime",BreakTaskNum);
        });
        binding.timeBusiness.setOnSeekBarChangeListener(current -> {
            businessTimeNum = binding.timeBusiness.getCurInt();
            Log.d(TAG, "设置打断导购任务触控点暂停时间: " + businessTimeNum);
            values.put("businesswhethertime",businessTimeNum);
        });
        //巡逻速度
        binding.patrolSpeed.setOnSeekBarChangeListener(current -> {
            patrolSpeedNum = binding.patrolSpeed.getCur();
            Log.d(TAG, "巡逻速度: " + patrolSpeedNum);
            values.put("patrolspeed",patrolSpeedNum);
        });
        //巡逻过程中暂停时间
        binding.suspension.setOnSeekBarChangeListener(current -> {
            suspensionNum = binding.suspension.getCurInt();
            values.put("patrolstaytime",suspensionNum);
        });
        //导购过程中是否允许被打断
        binding.interruptionBusiness.setOnCheckedChangeListener((compoundButton, b) -> {
            values.put("businessinterrupt",BooleanToInt(b));
        });

        //讲解过程中是否允许被打断
        binding.InterruptionExplanation.setOnCheckedChangeListener((compoundButton, b) -> {
            values.put("explaininterrupt",BooleanToInt(b));
        });
        binding.VoiceAnnouncements.setOnCheckedChangeListener((buttonView, isChecked) -> {
            values.put("voiceannouncements",BooleanToInt(isChecked));
        });
        //讲解结束允许：再讲一遍
        binding.AgainCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.AgainCB.setChecked(true);
            }
        });
        //讲解结束允许：选择其他路线
        binding.otherCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.otherCB.setChecked(true);
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
        binding.checkBoxExplainFinishNotGoBack.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                values.put("explainfinishednotgoback",1);
            }else {
                values.put("explainfinishednotgoback",0);
            }
        });
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
                values.put("unarrive",0);
            }
            if (binding.repeat.isChecked()) {
                values.put("unarrive",1);
            }
            //测温模式统计
            if (binding.singleTemp.isChecked()) {
                values.put("tempmode",0);
            }
            if (binding.multipleTemp.isChecked()) {
                values.put("tempmode",1);
            }
            //讲解结束允许统计
            if (binding.AgainCB.isChecked()&& !binding.otherCB.isChecked()) {
                values.put("explanationfinish",0);
            }
            if (binding.otherCB.isChecked()&&!binding.AgainCB.isChecked()) {
                values.put("explanationfinish",1);
            }
            if (binding.otherCB.isChecked()&& binding.AgainCB.isChecked()){
                values.put("explanationfinish",2);
            }
            //异常警告方式统计
            if (binding.Announcements.isChecked()) {
                values.put("error",0);
            }
            if (binding.followNearby.isChecked()) {
                values.put("error",1);
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
            values.put("patrolcontent",stringBuffer.toString());
            String[] whereArgs = {QuerySql.QueryBasicId()+""};
            UpDataSQL.update("table_basic", values, "id = ?", whereArgs);
        }
    }

    private static int BooleanToInt(Boolean data){
        if (data == true){
            return 1;
        }else {
            return 0;
        }
    }

}

