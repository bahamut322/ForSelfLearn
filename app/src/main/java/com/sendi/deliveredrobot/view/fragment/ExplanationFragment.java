package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.adapter.ExplantionAdapter;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.databinding.FragmentExplanationBinding;
import com.sendi.deliveredrobot.entity.FunctionSkip;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.entity.entitySql.QuerySql;
import com.sendi.deliveredrobot.helpers.DialogHelper;
import com.sendi.deliveredrobot.helpers.SpeakHelper;
import com.sendi.deliveredrobot.model.RouteMapList;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.service.PlaceholderEnum;
import com.sendi.deliveredrobot.utils.CenterItemUtils;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.utils.UiUtils;
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author swn
 * @describe 智能讲解
 */
public class ExplanationFragment extends BaseFragment {

    FragmentExplanationBinding binding;
    private ExplantionAdapter mAdapter;
    private int centerToLiftDistance; //RecyclerView款度的一半 ,也就是控件中间位置到左部的距离 ，
    private int childViewHalfCount = 0; //当前RecyclerView一半最多可以存在几个Item

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FromeSettingDialog fromeSettingDialog = new FromeSettingDialog(getContext());
        //是否是第一个页面
        if (FunctionSkip.selectFunction() == 4) {
            binding.firstFragment.setVisibility(View.GONE);
            binding.llReturn.setVisibility(View.VISIBLE);
        } else {
            binding.firstFragment.setVisibility(View.VISIBLE);
            binding.llReturn.setVisibility(View.GONE);
        }
        updateDataAndRefreshList();
        BaiduTTSHelper.getInstance().speak( PlaceholderEnum.Companion.replaceText(QuerySql.QueryExplainConfig().getRouteListText(),"","","","智能讲解"),"");
        binding.tvExplanationName.setText(QuerySql.QueryExplainConfig().getSlogan());
        //返回主页面
        binding.llReturn.setOnClickListener(v -> navigateToFragment(R.id.action_explanationFragment_to_homeFragment, null));
        binding.imageViewSetting.setOnClickListener(v -> {
            fromeSettingDialog.show();
            RobotStatus.INSTANCE.getPassWordToSetting().observe(getViewLifecycleOwner(), it -> {
                if (Boolean.TRUE.equals(RobotStatus.INSTANCE.getPassWordToSetting().getValue())) {
                    try {
                        navigateToFragment(R.id.action_explanationFragment_to_settingHomeFragment, null);
                    } catch (Exception ignored) {
                    }
                    fromeSettingDialog.dismiss();
                    RobotStatus.INSTANCE.getPassWordToSetting().postValue(false);
                }
            });
        });
        binding.bubbleTv.setOnClickListener(v -> {
            navigateToFragment(R.id.conversationFragment, null);
        });
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explanation, container, false);
        binding = DataBindingUtil.bind(view);
        return view;
    }

    private void init() {
//        new UpdateReturn().method();
        //回到主页面的时候初始化一下选择讲解点的值
        RobotStatus.INSTANCE.setSelectRouteMapItemId(-1);
        RobotStatus.INSTANCE.setPointItemIndex(-1);
        binding.explainRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.explainRv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.explainRv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                centerToLiftDistance = binding.explainRv.getWidth() / 2;
                int childViewHeight = UiUtils.dip2px(getContext(), 400); //400是当前已知的 Item的高度
                childViewHalfCount = (binding.explainRv.getWidth() / childViewHeight + 1) / 2;
                initData();
                findView();
            }
        });
        try {
            binding.explainRv.postDelayed(() -> scrollToCenter(childViewHalfCount), 100L);
        } catch (Exception e) {
            Log.d("TAG", "列表为空: " + e);
        }
    }

    private List<RouteMapList> mDatas;

    private void initData() {
        if (mDatas != null) {
            mDatas.clear();
        }
        mDatas = QuerySql.queryRoute(QuerySql.robotConfig().getMapName());
        for (int j = 0; j < childViewHalfCount; j++) { //头部的空布局
            mDatas.add(0, null);
        }
        for (int k = 0; k < childViewHalfCount; k++) {  //尾部的空布局
            mDatas.add(null);
        }
    }

    private boolean isTouch = false;

    private List<CenterItemUtils.CenterViewItem> centerViewItems = new ArrayList<>();

    @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
    private void findView() {
        ExplantionAdapter.OnItemClickListener listener = (position, routeMap) -> {
            Universal.lastValue = null;
            if (Boolean.FALSE.equals(RobotStatus.INSTANCE.getChargeStatus().getValue()) || QuerySql.robotConfig().getChargePointName().isEmpty() || QuerySql.robotConfig().getWaitingPointName().isEmpty()) {
                DialogHelper.briefingDialog.show();
            } else {
                scrollToCenter(position);
                RobotStatus.INSTANCE.setSelectRouteMapItemId(routeMap.getId());
                navigateToFragment(R.id.action_explanationFragment_to_CatalogueExplantionFragment, null);
                SpeakHelper.INSTANCE.speak(PlaceholderEnum.Companion.replaceText(QuerySql.QueryExplainConfig().getPointListText(),"","", routeMap.getRouteName(),"智能讲解"));
            }
        };
        mAdapter = new ExplantionAdapter(requireContext(), listener);
        mAdapter.setData(mDatas);
        binding.explainRv.setAdapter(mAdapter);

        binding.explainRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int fi = linearLayoutManager.findFirstVisibleItemPosition();
                    int la = linearLayoutManager.findLastVisibleItemPosition();
                    LogUtil.INSTANCE.i("onScrollStateChanged:首个item: " + fi + "  末尾item:" + la);
                    if (isTouch) {
                        isTouch = false;
                        //获取最中间的Item View
                        int centerPositionDiffer = (la - fi) / 2;
                        int centerChildViewPosition = fi + centerPositionDiffer; //获取当前所有条目中中间的一个条目索引
                        centerViewItems.clear();
                        //遍历循环，获取到和中线相差最小的条目索引(精准查找最居中的条目)
                        if (centerChildViewPosition != 0) {
                            for (int i = centerChildViewPosition - 1; i < centerChildViewPosition + 2; i++) {
                                View cView = recyclerView.getLayoutManager().findViewByPosition(i);
                                int viewLeft = cView.getLeft() + (cView.getWidth() / 2);
                                centerViewItems.add(new CenterItemUtils.CenterViewItem(i, Math.abs(centerToLiftDistance - viewLeft)));
                            }

                            CenterItemUtils.CenterViewItem centerViewItem = CenterItemUtils.getMinDifferItem(centerViewItems);
                            centerChildViewPosition = centerViewItem.position;
                        }

                        scrollToCenter(centerChildViewPosition);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    recyclerView.getChildAt(i).invalidate();
                }
            }
        });

        binding.explainRv.setOnTouchListener((view, motionEvent) -> {
            isTouch = true;
            return false;
        });
    }

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

    /**
     * 移动指定索引到中心处 ， 只可以移动可见区域的内容
     *
     * @param position
     */
    private void scrollToCenter(int position) {
        if (mAdapter == null) return;
        position = position < childViewHalfCount ? childViewHalfCount : position;
        position = position < mAdapter.getItemCount() - childViewHalfCount - 1 ? position : mAdapter.getItemCount() - childViewHalfCount - 1;

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.explainRv.getLayoutManager();
        View childView = linearLayoutManager.findViewByPosition(position);
        //把当前View移动到居中位置
        if (childView == null) return;
        int childVhalf = childView.getWidth() / 2;
        int childViewLeft = childView.getLeft();
        int viewCTop = centerToLiftDistance;
        int smoothDistance = childViewLeft - viewCTop + childVhalf;
        binding.explainRv.smoothScrollBy(smoothDistance, 0, decelerateInterpolator);
        mAdapter.setSelectPosition(position);
//        LogUtil.INSTANCE.d("当前选中:" + mDatas.get(position));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateDataAndRefreshList() {
        // 更新了地图配置
        Universal.mapType.observe(getViewLifecycleOwner(), mapType -> {
            if (mapType) {
                // 初始化适配器和其他相关组件
                init();
                // 检查 mAdapter 是否非空，然后调用 notifyDataSetChanged()
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged(); // 刷新列表
                }
                // 检查 RecyclerView 和它的适配器是否非空，然后调用 notifyDataSetChanged()
                if (binding.explainRv.getAdapter() != null) {
                    binding.explainRv.getAdapter().notifyDataSetChanged();
                }
            }
        });
        // 更新了引领配置
        RobotStatus.INSTANCE.getNewUpdate().observe(getViewLifecycleOwner(), newUpdate -> {
            if (newUpdate == null) {
                return;
            }
            if (newUpdate == 1 || newUpdate == 2) {
                // 初始化适配器和其他相关组件
                init();
                // 检查 mAdapter 是否非空，然后调用 notifyDataSetChanged()
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged(); // 刷新列表
                }
                // 检查 RecyclerView 和它的适配器是否非空，然后调用 notifyDataSetChanged()
                if (binding.explainRv.getAdapter() != null) {
                    binding.explainRv.getAdapter().notifyDataSetChanged();
                }
                String slogan = (RobotStatus.INSTANCE.getExplainConfig() == null
                        ? RobotStatus.INSTANCE.getExplainConfig().getSlogan()
                        : QuerySql.QueryExplainConfig().getSlogan());
                binding.tvExplanationName.setText(slogan);
            }
        });
        RobotStatus.INSTANCE.getRobotConfig().observe(getViewLifecycleOwner(), newUpdate -> {
            binding.bubbleTv.setText(String.format(getString(R.string.ask), newUpdate.getWakeUpWord()));
        });
    }
}