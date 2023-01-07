package com.sendi.deliveredrobot.view.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.databinding.FragmentBasicSettingBinding;
import com.sendi.deliveredrobot.entity.BasicSetting;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.view.widget.MySeekBar;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.litepal.LitePal;

import java.util.List;

public class DebugBasicSettingFragment extends Fragment {
    String TAG = "TAGDebugBasicSettingFragment";
    FragmentBasicSettingBinding binding;
    private int musicVolume = 1; // 音乐音量
    private int voiceVolume = 1; // 语音音
    private boolean flag = false;
    private CheckBox red, blue, yellow, orange, black, white, all;
    public StringBuffer stringBuffer = new StringBuffer();
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private MySeekBar RobotVoice, VideoVoice;
    public AudioManager audiomanage;
    public String timbre = "男声";//默认音色
    View view;
    Float robotAudio;
    Float videoAudio;
    Float musicAudio;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = DataBindingUtil.bind(view);

        all = view.findViewById(R.id.all);
        red = view.findViewById(R.id.red);
        blue = view.findViewById(R.id.blue);
        yellow = view.findViewById(R.id.yellow);
        orange = view.findViewById(R.id.orange);
        black = view.findViewById(R.id.black);
        white = view.findViewById(R.id.white);
        binding.seekbarMusic.setRange(0, 100,0);//设置机器人语音调节范围
        binding.seekbarVoice.setRange(0, 100,0);//设置视频音量调节范围
        binding.musicSb.setRange(0, 100,0);//设置音乐音量调节范围

        //转换StringBuffer变量，去判断那些数据存在
//        String selectItem = sp.getString("SelectItem", "");//获取选中数据
        Boolean expressionApply = sp.getBoolean("expression",true);//从临时文件中获取是否开启动画
        robotAudio = sp.getFloat("robotAudio",0);//机器人语音
        videoAudio = sp.getFloat("videoAudio",0);//视频音量
        musicAudio = sp.getFloat("musicAudio",0);//音乐音量


        binding.seekbarMusic.setCur(robotAudio);
        binding.seekbarVoice.setCur(videoAudio);
        binding.musicSb.setCur(musicAudio);
        Log.d(TAG, "机器人功能选择: "+Universal.selectItem);
        Log.d(TAG, "机器人音色选择: "+Universal.RobotMode);
        timbres(Universal.RobotMode);//选中音色方法
        binding.expressionCB.setChecked(expressionApply);//是否开启表情
        if (Universal.selectItem != null) {
            for (int i = 0; i < Universal.selectItem.split(" ").length; i++) {
                check(Universal.selectItem.split(" ")[i]);
            }
            //判断数据长度来，判断全选是否勾选
            if (Universal.selectItem.split(" ").length == 6) {
                all.setChecked(true);
            }
        }

        AllCheckListener allCheckListener = new AllCheckListener();
        all.setOnClickListener(allCheckListener);


        BoxCheckListener boxCheckListener = new BoxCheckListener();
        red.setOnCheckedChangeListener(boxCheckListener);
        blue.setOnCheckedChangeListener(boxCheckListener);
        yellow.setOnCheckedChangeListener(boxCheckListener);
        orange.setOnCheckedChangeListener(boxCheckListener);
        black.setOnCheckedChangeListener(boxCheckListener);
        white.setOnCheckedChangeListener(boxCheckListener);
        //动画选择
        binding.expressionCB.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean("expression", b);
            editor.commit();

        });

        //机器人语音
        binding.seekbarMusic.setOnSeekBarChangeListener(current -> {
            robotAudio = binding.seekbarMusic.getCur();
            Log.d(TAG, "设置机器人语音：" + robotAudio);
            //将数据存储到临时文件
            editor.putFloat("robotAudio", robotAudio);
            editor.commit();
        });

        //音频音量
        binding.seekbarVoice.setOnSeekBarChangeListener(current -> {
            videoAudio = binding.seekbarVoice.getCur();
            Log.d(TAG, "设置音频音量：" + videoAudio);
            //将数据存储到临时文件
            editor.putFloat("videoAudio", videoAudio);
            editor.commit();
        });

        //音频音量
        binding.musicSb.setOnSeekBarChangeListener(current -> {
            musicAudio = binding.musicSb.getCur();
            Log.d(TAG, "设置音乐音量：" + musicAudio);
            //将数据存储到临时文件
            editor.putFloat("musicAudio", musicAudio);
            editor.commit();
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
        view =  inflater.inflate(R.layout.fragment_basic_setting, container, false);
        return view;
    }

    class AllCheckListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            CheckBox all = (CheckBox) v;
            red.setChecked(all.isChecked());
            blue.setChecked(all.isChecked());
            yellow.setChecked(all.isChecked());
            orange.setChecked(all.isChecked());
            black.setChecked(all.isChecked());
            white.setChecked(all.isChecked());

        }
    }

    class BoxCheckListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                all.setChecked(isChecked);
            }
            if (red.isChecked() && blue.isChecked() && yellow.isChecked() &&
                    orange.isChecked() && black.isChecked() && white.isChecked()) {
                all.setChecked(true);
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
        if ("礼仪迎宾".equals(checkName)) {
            red.setChecked(true);
        } else if ("智能引领".equals(checkName)) {
            blue.setChecked(true);
        } else if ("智能讲解".equals(checkName)) {
            yellow.setChecked(true);
        } else if ("智能问答".equals(checkName)) {
            orange.setChecked(true);
        } else if ("功能模块".equals(checkName)) {
            black.setChecked(true);
        } else if ("轻应用".equals(checkName)) {
            white.setChecked(true);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, ": onDestroy");
        if (getActivity()!=null) {
            //删除所有数据,并且清空变量
            stringBuffer = new StringBuffer();
            timbre = null;
            LitePal.deleteAll(BasicSetting.class);
            //统计勾线的ChexkBox，并且赋值给全局变量的StringBuffer
            if (red.isChecked()) {
                stringBuffer.append("礼仪迎宾 ");
            }
            if (blue.isChecked()) {
                stringBuffer.append("智能引领 ");
            }
            if (yellow.isChecked()) {
                stringBuffer.append("智能讲解 ");
            }
            if (orange.isChecked()) {
                stringBuffer.append("智能问答 ");
            }
            if (white.isChecked()) {
                stringBuffer.append("轻应用 ");
            }
            if (black.isChecked()) {
                stringBuffer.append("功能模块 ");
            }

            if (binding.BoyVoice.isChecked()) {
                timbre = "男声";
            }
            if (binding.FemaleVoice.isChecked()) {
                timbre = "女声";
            }
            if (binding.ChildVoice.isChecked()) {
                timbre = "童声";
            }
            //提交数据到数据库
            BasicSetting basicSetting=new BasicSetting();
            basicSetting.setRobotMode(timbre);
            basicSetting.setDefaultValue(stringBuffer.toString());
            Log.d(TAG, "最后存储的数据: "+stringBuffer.toString());
            basicSetting.save();

        }
    }
}
