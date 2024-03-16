package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.databinding.FragmentBasicSettingBinding;
import com.sendi.deliveredrobot.entity.entitySql.QuerySql;
import com.sendi.deliveredrobot.entity.UpDataSQL;
import com.sendi.deliveredrobot.helpers.AudioMngHelper;
import com.sendi.deliveredrobot.model.BasicModel;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.viewmodel.SettingViewModel;

import java.util.Arrays;

public class DebugBasicSettingFragment extends Fragment {
    String TAG = "TAGDebugBasicSettingFragment";
    FragmentBasicSettingBinding binding;
    public StringBuffer stringBuffer = new StringBuffer();
    SettingViewModel viewModel;
    public String timbre = "男声";//默认音色
    View view;
    int robotAudio;
    int videoAudio;
    public float explainNum = 1;//讲解语速默认值
    ContentValues values;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = DataBindingUtil.bind(view);
        viewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        values = new ContentValues();
        binding.seekbarMusic.setRange(0, 100, 0);//设置机器人语音调节范围
        binding.seekbarVoice.setRange(0, 100, 0);//设置视频音量调节范围
        //讲解语速
        binding.explain.setRange(1, 15, 0);

        assert binding != null;
        BasicModel settingData = viewModel.settingData();

        binding.seekbarMusic.setCur(settingData.getVoiceVolume());
        binding.seekbarVoice.setCur(settingData.getVideoVolume());
        timbres(settingData.getRobotMode());//选中音色方法
        binding.explain.setCur(settingData.getSpeechSpeed());
        binding.expressionCB.setChecked(settingData.getExpression());//是否开启表情
        binding.cbEtiquette.setChecked(settingData.getEtiquette());//是否开启礼仪迎宾
        binding.cbIntelligent.setChecked(settingData.getIntelligent());//是否开启智能语音
        binding.oneKeyCallPhone.setChecked(settingData.getOneKeyCallPhone() == 1);//是否开启一键呼叫电话
        String defaultValue = settingData.getDefaultValue();
        if (defaultValue != null) {
            for (int i = 0; i < defaultValue.split(" ").length; i++) {
                check(defaultValue.split(" ")[i]);
            }
            //判断数据长度来，判断全选是否勾选
            if (defaultValue.split(" ").length == 5 && binding.oneKeyCallPhone.isChecked()) {
                binding.all.setChecked(true);
            }
        }

        AllCheckListener allCheckListener = new AllCheckListener();
        binding.all.setOnClickListener(allCheckListener);


        BoxCheckListener boxCheckListener = new BoxCheckListener();
        binding.leaderShip.setOnCheckedChangeListener(boxCheckListener);
        binding.explanation.setOnCheckedChangeListener(boxCheckListener);
//        binding.QA.setOnCheckedChangeListener(boxCheckListener);
        binding.business.setOnCheckedChangeListener(boxCheckListener);
        binding.EtiquetteWelcome.setOnCheckedChangeListener(boxCheckListener);
        binding.application.setOnCheckedChangeListener(boxCheckListener);
        binding.oneKeyCallPhone.setOnCheckedChangeListener(boxCheckListener);
        //动画选择
        binding.expressionCB.setOnCheckedChangeListener((compoundButton, b) -> values.put("expression", BooleanToInt(b)));
        binding.cbIntelligent.setOnCheckedChangeListener((compoundButton, b) -> values.put("intelligent", BooleanToInt(b)));
        binding.cbEtiquette.setOnCheckedChangeListener((compoundButton, b) -> {
            if (viewModel.isNumCharOne(4)) {
                Toast.makeText(getContext(), "未检测到RGB摄像头，人脸识别开启无效", Toast.LENGTH_LONG).show();
            } else {
                values.put("etiquette", BooleanToInt(b));
            }
        });

        //讲解语速
        binding.explain.setOnSeekBarChangeListener(current -> {
            explainNum = binding.explain.getCurInt();
            Log.d(TAG, "设置讲解语速: " + explainNum);
            values.put("speechspeed", explainNum);
            viewModel.timbres(explainNum + "");
        });

        //机器人语音
        binding.seekbarMusic.setOnSeekBarChangeListener(current -> {
            robotAudio = (int) binding.seekbarMusic.getCur();
            LogUtil.INSTANCE.d("设置机器人语音：" + robotAudio);
            values.put("voicevolume", robotAudio);
        });

        //音频音量
        binding.seekbarVoice.setOnSeekBarChangeListener(current -> {
            videoAudio = (int) binding.seekbarVoice.getCur();
            new AudioMngHelper(MyApplication.context).setVoice100((int) binding.seekbarVoice.getCur());
            LogUtil.INSTANCE.d("设置音频音量：" + videoAudio);
            values.put("videovolume", videoAudio);
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_basic_setting, container, false);
        return view;
    }

    class AllCheckListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            CheckBox all = (CheckBox) v;
            binding.leaderShip.setChecked(all.isChecked());
            binding.explanation.setChecked(all.isChecked());
//            binding.QA.setChecked(all.isChecked());
            binding.business.setChecked(all.isChecked());
            binding.EtiquetteWelcome.setChecked(all.isChecked());
            binding.application.setChecked(all.isChecked());
            binding.oneKeyCallPhone.setChecked(all.isChecked());
        }
    }

    class BoxCheckListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                binding.all.setChecked(false);
            }
            if (binding.leaderShip.isChecked() && binding.explanation.isChecked() && binding.application.isChecked() && binding.business.isChecked() && binding.EtiquetteWelcome.isChecked() && binding.oneKeyCallPhone.isChecked()) {
                binding.all.setChecked(true);
            }
            Log.d(TAG, "onCheckedChanged: 点击");
            stringBuffer = new StringBuffer();
            if (binding.leaderShip.isChecked()) {
                stringBuffer.append("智能引领 ");
            }
            if (binding.explanation.isChecked()) {
                stringBuffer.append("智能讲解 ");
            }
            if (binding.application.isChecked()) {
                stringBuffer.append("更多服务 ");
            }
            if (binding.business.isChecked()) {
                stringBuffer.append("业务办理 ");
            }
            if (binding.EtiquetteWelcome.isChecked()) {
                stringBuffer.append("礼仪迎宾 ");
            }
            values.put("defaultvalue", stringBuffer.toString());
            if (binding.oneKeyCallPhone.isChecked()) {
                values.put("onekeycallphone", 1);
            } else {
                values.put("onekeycallphone", 0);
            }
        }
    }

    //音色方法
    private void timbres(String timbreName) {
        if ("男声".equals(timbreName)) {
            binding.BoyVoice.setChecked(true);
        } else if ("女声".equals(timbreName)) {
            binding.FemaleVoice.setChecked(true);
        } else if ("童声".equals(timbreName)) {
            binding.ChildVoice.setChecked(true);
        }
    }

    //解析StringBuffer中的数据用来保存勾选
    private void check(String checkName) {
        if ("智能引领".equals(checkName)) {
            binding.leaderShip.setChecked(true);
        } else if ("智能讲解".equals(checkName)) {
            binding.explanation.setChecked(true);
        } else if ("更多服务".equals(checkName)) {
            binding.application.setChecked(true);
        } else if ("业务办理".equals(checkName)) {
            binding.business.setChecked(true);
        } else if ("礼仪迎宾".equals(checkName)) {
            binding.EtiquetteWelcome.setChecked(true);
        }

    }

    @SuppressLint("NewApi")
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, ": onDestroy");
        if (getActivity() != null && !values.isEmpty()) {
            String[] whereArgs = {String.valueOf(QuerySql.QueryBasicId())};
            UpDataSQL.update("table_basic", values, "id = ?", whereArgs);

        }
    }

    private static int BooleanToInt(Boolean data) {
        if (data) {
            return 1;
        } else {
            return 0;
        }
    }

}
