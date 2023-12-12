package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.databinding.FragmentCatalogueExplantionBinding;
import com.sendi.deliveredrobot.entity.entitySql.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;

import com.sendi.deliveredrobot.helpers.ROSHelper;
import com.sendi.deliveredrobot.helpers.SpeakHelper;

import com.sendi.deliveredrobot.helpers.WakeupWordHelper;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.viewmodel.StartExplanViewModel;
import com.sendi.fooddeliveryrobot.BaseVoiceRecorder;

import java.util.List;
import java.util.Objects;


/**
 * @author swn
 * @describe 讲解目录
 */
public class CatalogueExplantionFragment extends Fragment {

    private StartExplanViewModel viewModel;
    private FragmentCatalogueExplantionBinding binding;
    private NavController controller;
    private CatalogueAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StartExplanViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = Navigation.findNavController(requireView());
        binding = DataBindingUtil.bind(view);
        mAdapter = new CatalogueAdapter(viewModel.inForListData(), getContext());
        RobotStatus.INSTANCE.getRobotConfig().observe(getViewLifecycleOwner(), newUpdata -> {
            binding.bubbleTv.setText(String.format(getString(R.string.ask), newUpdata.getWakeUpWord()));
        });
        binding.CatalogueList.setAdapter(mAdapter);
        binding.toCatalog.setOnClickListener(v -> {
            viewModel.start();
            Universal.twice = true;
            SpeakHelper.INSTANCE.speak(QuerySql.QueryExplainConfig().getStartText());
        });
        ROSHelper.INSTANCE.setSpeed(QuerySql.QueryBasic().getGoExplanationPoint()+"");
        binding.returnHome.setOnClickListener(v -> controller.navigate(R.id.action_CatalogueExplantionFragment_to_ExplanationFragment));
        binding.bubbleTv.setOnClickListener(v -> {
            controller.navigate(R.id.conversationFragment);
        });
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_catalogue_explantion, container, false);
    }

    class CatalogueAdapter extends BaseAdapter {

        private final List<MyResultModel> mData;
        private final Context mContext;

        public CatalogueAdapter(List<MyResultModel> mData, Context mContext) {
            this.mData = mData;
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint({"SetTextI18n", "ViewHolder"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_catalogue_name, parent, false);
            ConstraintLayout conLayout = convertView.findViewById(R.id.conLayout);
            TextView txt_aNum = (TextView) convertView.findViewById(R.id.pointNum);
            TextView txt_aName = (TextView) convertView.findViewById(R.id.pointName);
            txt_aNum.setText("第" + viewModel.intToChinese(position + 1) + "站");
            txt_aName.setText(mData.get(position).getName());
            Glide.with(getContext()).load(mData.get(position).getBackgroundpic()).into(binding.pointImage);
            binding.pointCatalogue.setText(mData.get(position).getIntroduction());
            binding.pointNameTv.setText(mData.get(position).getRoutename());
            binding.selectName.setText("参观路线：" + mData.get(position).getRoutename());
            return convertView;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        BaseVoiceRecorder baseVoiceRecorder = BaseVoiceRecorder.Companion.getInstance();
        baseVoiceRecorder.setRecordCallback((s, pinyinString) -> {
            if (pinyinString.contains(Objects.requireNonNull(WakeupWordHelper.INSTANCE.getWakeupWordPinyin()))) {
                Log.i("AudioChannel", "包含"+WakeupWordHelper.INSTANCE.getWakeupWord());
                controller.navigate(R.id.conversationFragment);
            }
            return null;
        });
    }
}