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

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.databinding.FragmentBasicSettingBinding;
import com.sendi.deliveredrobot.view.widget.MySeekBar;

public class DebugBasicSettingFragment extends Fragment {
    String TAG = "TAG111111";
    FragmentBasicSettingBinding binding;
    private int musicVolume = 1; // 音乐音量
    private int voiceVolume = 1; // 语音音
    //    private String newVersionUrl = ""; //新版本的下载地址
//    private String newVersion = ""; //新的版本号
    private boolean flag = false;
    //    private String chassisVersion = ""; //底盘版本号
    private CheckBox red, blue, yellow, orange, black, white, all;
    public StringBuffer stringBuffer = new StringBuffer();
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private MySeekBar RobotVoice, VideoVoice;
    public AudioManager audiomanage;
    public String timbre = "男声";//默认音色
    public String expression = "是";//是否开启表情

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = DataBindingUtil.bind(view);
        //获取sp对象，参数data表示文件名，MODE_PRIVATE表示文件操作模式


        all = view.findViewById(R.id.all);
        red = view.findViewById(R.id.red);
        blue = view.findViewById(R.id.blue);
        yellow = view.findViewById(R.id.yellow);
        orange = view.findViewById(R.id.orange);
        black = view.findViewById(R.id.black);
        white = view.findViewById(R.id.white);
        //转换StringBuffer变量，去判断那些数据存在
        String selectItem = sp.getString("SelectItem", "");//获取选中数据
        String timbreApply = sp.getString("timbre", "");//从临时文件中获取音色信息
        String expressionApply = sp.getString("expression", "");//从临时文件中获取是否开启动画
        timbres(timbreApply);//选中音色方法
        expressions(expressionApply);//选择是否开启动画方法
        if (selectItem != null) {
            for (int i = 0; i < selectItem.split(" ").length; i++) {
                check(selectItem.split(" ")[i]);
            }
        }
        //判断数据长度来，判断全选是否勾选
        if (selectItem.toString().split(" ").length == 6) {
            all.setChecked(true);
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
        binding.expressionGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == binding.expressionYes.getId()) {
                Toast.makeText(getContext(), "选是", Toast.LENGTH_SHORT).show();

//                    expression = "是";
            } else if (i == binding.expressionNo.getId()) {
                Toast.makeText(getContext(), "选否", Toast.LENGTH_SHORT).show();

//                    expression = "否";
            }
        });
        //音色选择
        binding.express.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == binding.BoyVoice.getId()) {
                Toast.makeText(getContext(), "男声", Toast.LENGTH_SHORT).show();
//                    timbre = "男声";
            } else if (i == binding.FemaleVoice.getId()) {
                Toast.makeText(getContext(), "女声", Toast.LENGTH_SHORT).show();
//                    timbre = "女声";
            } else if (i == binding.ChildVoice.getId()) {
                Toast.makeText(getContext(), "童声", Toast.LENGTH_SHORT).show();
//                    timbre = "童声";
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
        return inflater.inflate(R.layout.fragment_basic_setting, container, false);
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

    private void expressions(String expressionSelect) {
        if ("是".equals(expressionSelect)) {
            binding.expressionYes.setChecked(true);
        } else if ("否".equals(expressionSelect)) {
            binding.expressionNo.setChecked(true);
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
        } else if ("轻应用".equals(checkName)) {
            black.setChecked(true);
        } else if ("功能模块".equals(checkName)) {
            white.setChecked(true);
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        if (black.isChecked()) {
            stringBuffer.append("轻应用 ");
        }
        if (white.isChecked()) {
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
        if (binding.expressionYes.isChecked()) {
            expression = "是";
        }
        if (binding.expressionNo.isChecked()) {
            expression = "否";
        }
        editor.putString("SelectItem", stringBuffer.toString());// 存入String类型数据
        editor.putString("timbre", timbre);//音色
        editor.putString("expression", expression);//是否开启表情
        editor.commit();// 提交修改
    }

    @Override
    public void onPause() {
        super.onPause();
        //清空原有数据
        Log.d(TAG, ": onPause");

    }
}
