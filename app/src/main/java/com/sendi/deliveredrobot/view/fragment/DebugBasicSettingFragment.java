package com.sendi.deliveredrobot.view.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.databinding.FragmentBasicSettingBinding;
import com.sendi.deliveredrobot.entity.BasicSetting;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.entity.UpDataSQL;
import com.sendi.deliveredrobot.helpers.AudioMngHelper;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.view.widget.Order;
import com.sendi.deliveredrobot.viewmodel.BaseViewModel;
import com.sendi.deliveredrobot.viewmodel.SettingViewModel;

import org.litepal.LitePal;

import java.lang.reflect.Array;

public class DebugBasicSettingFragment extends Fragment {
    String TAG = "TAGDebugBasicSettingFragment";
    FragmentBasicSettingBinding binding;
    public StringBuffer stringBuffer = new StringBuffer();

    SettingViewModel viewModel;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    public String timbre = "男声";//默认音色
    View view;
    Float robotAudio;
    Float videoAudio;
    ContentValues values;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = DataBindingUtil.bind(view);
        viewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        values = new ContentValues();

        assert binding != null;
        binding.seekbarMusic.setRange(0, 100, 0);//设置机器人语音调节范围
        binding.seekbarVoice.setRange(0, 100, 0);//设置视频音量调节范围


        binding.seekbarMusic.setCur(QuerySql.QueryBasic().getVoiceVolume());
        binding.seekbarVoice.setCur(QuerySql.QueryBasic().getVideoVolume());
        timbres(QuerySql.QueryBasic().getRobotMode());//选中音色方法
        binding.expressionCB.setChecked(QuerySql.QueryBasic().getExpression());//是否开启表情
        binding.cbEtiquette.setChecked(QuerySql.QueryBasic().getEtiquette());//是否开启礼仪迎宾
        binding.cbIntelligent.setChecked(QuerySql.QueryBasic().getIntelligent());//是否开启智能语音

        if (QuerySql.QueryBasic().getDefaultValue() != null) {
            for (int i = 0; i < QuerySql.QueryBasic().getDefaultValue().split(" ").length; i++) {
                check(QuerySql.QueryBasic().getDefaultValue().split(" ")[i]);
            }
            //判断数据长度来，判断全选是否勾选
            if (QuerySql.QueryBasic().getDefaultValue().split(" ").length == 4) {
                binding.all.setChecked(true);
            }
        }

        AllCheckListener allCheckListener = new AllCheckListener();
        binding.all.setOnClickListener(allCheckListener);


        BoxCheckListener boxCheckListener = new BoxCheckListener();
        binding.leaderShip.setOnCheckedChangeListener(boxCheckListener);
        binding.explanation.setOnCheckedChangeListener(boxCheckListener);
        binding.QA.setOnCheckedChangeListener(boxCheckListener);
        binding.application.setOnCheckedChangeListener(boxCheckListener);
        //动画选择
        binding.expressionCB.setOnCheckedChangeListener((compoundButton, b) -> {
            values.put("expression",BooleanToInt(b));

        });
        binding.cbIntelligent.setOnCheckedChangeListener((compoundButton, b) -> {
            values.put("intelligent",BooleanToInt(b));
        });
        binding.cbEtiquette.setOnCheckedChangeListener((compoundButton, b) -> {
            values.put("etiquette",BooleanToInt(b));
        });

        //机器人语音
        binding.seekbarMusic.setOnSeekBarChangeListener(current -> {
            robotAudio = binding.seekbarMusic.getCur();
            Log.d(TAG, "设置机器人语音：" + robotAudio);
            values.put("voicevolume",robotAudio);
        });

        //音频音量
        binding.seekbarVoice.setOnSeekBarChangeListener(current -> {
            videoAudio = binding.seekbarVoice.getCur();
            new AudioMngHelper(MyApplication.Companion.getInstance()).setVoice100((int)binding.seekbarVoice.getCur());
            Log.d(TAG, "设置音频音量：" + videoAudio);
            values.put("videovolume",videoAudio);
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
        view = inflater.inflate(R.layout.fragment_basic_setting, container, false);
        return view;
    }

    class AllCheckListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            CheckBox all = (CheckBox) v;
            binding.leaderShip.setChecked(all.isChecked());
            binding.explanation.setChecked(all.isChecked());
            binding.QA.setChecked(all.isChecked());
            binding.application.setChecked(all.isChecked());

        }
    }

    class BoxCheckListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                binding.all.setChecked(false);
            }
            if ( binding.leaderShip.isChecked() && binding.explanation.isChecked() &&
                    binding.QA.isChecked()  && binding.application.isChecked()) {
                binding.all.setChecked(true);
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
        } else if ("智能问答".equals(checkName)) {
            binding.QA.setChecked(true);
        }  else if ("轻应用".equals(checkName)) {
            binding.application.setChecked(true);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, ": onDestroy");
        if (getActivity() != null) {
            //删除所有数据,并且清空变量
            stringBuffer = new StringBuffer();
            timbre = null;
            if (binding.leaderShip.isChecked()) {
                stringBuffer.append("智能引领 ");
            }
            if (binding.explanation.isChecked()) {
                stringBuffer.append("智能讲解 ");
            }
            if (binding.QA.isChecked()) {
                stringBuffer.append("智能问答 ");
            }
            if (binding.application.isChecked()) {
                stringBuffer.append("轻应用 ");
            }

            if (binding.BoyVoice.isChecked()) {
                timbre = "男声";
                viewModel.randomVoice(2,QuerySql.QueryBasic().getSpeechSpeed()+"");
            }
            if (binding.FemaleVoice.isChecked()) {
                timbre = "女声";
                viewModel.randomVoice(1,QuerySql.QueryBasic().getSpeechSpeed()+"");
            }
            if (binding.ChildVoice.isChecked()) {
                timbre = "童声";
                viewModel.randomVoice(3,QuerySql.QueryBasic().getSpeechSpeed()+"");
            }
            values.put("defaultvalue", stringBuffer.toString());
            values.put("robotmode", timbre);
            String[] whereArgs = {QuerySql.QueryBasicId()+""};
            UpDataSQL.update("basicsetting", values, "id = ?", whereArgs);

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
