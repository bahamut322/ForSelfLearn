package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.databinding.FragmentCatalogueExplantionBinding;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.entity.entitySql.QuerySql;
import com.sendi.deliveredrobot.helpers.ROSHelper;
import com.sendi.deliveredrobot.helpers.SpeakHelper;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.service.PlaceholderEnum;
import com.sendi.deliveredrobot.viewmodel.StartExplainViewModel;

import java.util.List;


/**
 * @author swn
 * @describe 讲解目录
 */
public class CatalogueExplanationFragment extends BaseFragment {

    private StartExplainViewModel viewModel;
    private FragmentCatalogueExplantionBinding binding;
    private NavController controller;
    private CatalogueAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StartExplainViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = Navigation.findNavController(requireView());
        binding = DataBindingUtil.bind(view);
        mAdapter = new CatalogueAdapter(viewModel.inForListData(), getContext());
        RobotStatus.INSTANCE.getRobotConfig().observe(getViewLifecycleOwner(), newUpdate -> {
            binding.bubbleTv.setText(String.format(getString(R.string.ask), newUpdate.getWakeUpWord()));
        });
        binding.CatalogueList.setAdapter(mAdapter);
        binding.toCatalog.setOnClickListener(v -> {
            quitFragment();
            viewModel.start();
            Universal.twice = true;
            SpeakHelper.INSTANCE.stop();
            SpeakHelper.INSTANCE.speakWithoutStop(PlaceholderEnum.Companion.replaceText(QuerySql.QueryExplainConfig().getStartText(),"","",viewModel.inForListData().get(0).getRoutename(),"智能讲解"));
        });
        ROSHelper.INSTANCE.setSpeed(QuerySql.QueryBasic().getGoExplanationPoint()+"");
        binding.returnHome.setOnClickListener(v -> navigateToFragment(R.id.action_CatalogueExplantionFragment_to_ExplanationFragment,null));
        binding.bubbleTv.setOnClickListener(v -> {
            navigateToFragment(R.id.conversationFragment,null);
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
}