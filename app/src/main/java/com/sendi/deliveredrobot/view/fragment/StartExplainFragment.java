package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.adapter.ChangePointGridViewAdapter;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.databinding.FragmentStartExplantionBinding;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.entity.entitySql.QuerySql;
import com.sendi.deliveredrobot.helpers.DialogHelper;
import com.sendi.deliveredrobot.helpers.ExplainManager;
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper;
import com.sendi.deliveredrobot.helpers.SpeakHelper;
import com.sendi.deliveredrobot.model.ExplainStatusModel;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.model.TopLevelConfig;
import com.sendi.deliveredrobot.navigationtask.BillManager;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.service.PlaceholderEnum;
import com.sendi.deliveredrobot.service.UpdateReturn;
import com.sendi.deliveredrobot.utils.CenterItemUtils;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.view.widget.ChangingOverDialog;
import com.sendi.deliveredrobot.view.widget.FinishTaskDialog;
import com.sendi.deliveredrobot.view.widget.ProcessClickDialog;
import com.sendi.deliveredrobot.view.widget.Stat;
import com.sendi.deliveredrobot.view.widget.TaskNext;
import com.sendi.deliveredrobot.viewmodel.BaseViewModel;
import com.sendi.deliveredrobot.viewmodel.StartExplainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author swn
 * @describe 开始智能讲解
 */
public class StartExplainFragment extends Fragment {
    FragmentStartExplantionBinding binding;
    NavController controller;
    private MAdapter mAdapter;
    private int centerToTopDistance; //RecyclerView高度的一半 ,也就是控件中间位置到顶部的距离 ，
    private StartExplainViewModel viewModel;
    private BaseViewModel baseViewModel;
    private ProcessClickDialog processClickDialog;
    private ChangingOverDialog changingOverDialog;
    boolean nextTaskToDo = true;
    Handler handler;
    private View rootView;
//    private static boolean isMethodExecuted = false;
    private int itemTarget;
    private boolean isButtonClickable = true;
    int clickCount = 0;
    private FinishTaskDialog finishTaskDialog;

    private List<String> currentTextQueue;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("", "onAttach页面准备进入");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("", "onCreate页面进入");
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.cancelMainScope();
    }


    private void status() {
        Stat.setOnChangeListener(() -> {
            if (Stat.getFlage() == 2) {
                //暂停
//                Universal.taskQueue.pause();
                MediaPlayerHelper.getInstance().pause();
                BaiduTTSHelper.getInstance().pause();
            } else if (Stat.getFlage() == 3) {
                //继续
//                Universal.taskQueue.resume();
                MediaPlayerHelper.getInstance().resume();
                BaiduTTSHelper.getInstance().resume();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler = new Handler();
        controller = Navigation.findNavController(view);
        nextTaskToDo = true;
        LogUtil.INSTANCE.d("onViewCreated进入讲解页面");
        status();
        baseViewModel = new ViewModelProvider(this).get(BaseViewModel.class);
        viewModel = new ViewModelProvider(this).get(StartExplainViewModel.class);
        finishTaskDialog = new FinishTaskDialog(getContext());
        viewModel.downTimer();
        processClickDialog = new ProcessClickDialog(getContext());
        changingOverDialog = new ChangingOverDialog(getContext());
        processClickDialog.setCountdownTime(QuerySql.QueryBasic().getExplainWhetherTime());
        init();
        //todo delete start 倒计时3s监听逻辑
        TaskNext.setOnChangeListener(() -> {
            if (Objects.equals(TaskNext.getToDo(), "1")) {
                changingOverDialog.dismiss();
                finishTaskDialog.dismiss();
                processClickDialog.dismiss();
            }
        });
        //todo delete end
        finishTaskDialog.confirmBtn.setOnClickListener(v1 -> {
            processClickDialog.dismiss();
            finishTaskDialog.dismiss();
            Universal.explainTextLength = -1;
            MediaPlayerHelper.getInstance().stop();
            nextTaskToDo = false;
            SpeakHelper.INSTANCE.releaseUserCallback(); // 释放任务链中设置的回调
            BaiduTTSHelper.getInstance().speaks(PlaceholderEnum.Companion.replaceText(QuerySql.QueryExplainConfig().getInterruptionText(),"",binding.nowExplanation.getText().toString(),ExplainManager.INSTANCE.getRoutes().get(0).getRoutename(),"智能讲解"));
            viewModel.finishTask();
        });

        finishTaskDialog.cancelBtn.setOnClickListener(v12 -> {
            if (clickCount % 2 != 1) {
                BaiduTTSHelper.getInstance().resume();
            }
            MediaPlayerHelper.getInstance().resume();
            Objects.requireNonNull(viewModel.getCountDownTimer()).resume();
            finishTaskDialog.dismiss();
        });
        binding.finishBtn.setOnClickListener(v -> {
            if (isButtonClickable) {
                isButtonClickable = false;
                Objects.requireNonNull(viewModel.getCountDownTimer()).pause();
                BaiduTTSHelper.getInstance().pause();
                MediaPlayerHelper.getInstance().pause();
                finishTaskDialog.show();
                new Handler().postDelayed(() -> {
                    // 恢复按钮可点击状态
                    isButtonClickable = true;
                }, 4000); // 设置延迟时间，避免按钮重复点击
            }
        });
        binding.nextTaskBtn.setOnClickListener(v -> {
            if (isButtonClickable) {
                isButtonClickable = false;
                DialogHelper.loadingDialog.show();
                viewModel.nextTask();
                processClickDialog.dismiss();
                new Handler().postDelayed(() -> {
                    // 恢复按钮可点击状态
                    isButtonClickable = true;
                }, 4000); // 设置延迟时间，避免按钮重复点击
            }
        });
        binding.changingOver.setOnClickListener(v -> {
            if (isButtonClickable) {
                isButtonClickable = false;
                Objects.requireNonNull(viewModel.getCountDownTimer()).pause();
                changeDialog(true);
                processClickDialog.dismiss();
                new Handler().postDelayed(() -> {
                    // 恢复按钮可点击状态
                    isButtonClickable = true;
                }, 4000); // 设置延迟时间，避免按钮重复点击
            }
        });
        binding.parentCon.setOnClickListener(v -> {
            Log.d("TAG", "onViewCreated: 点击");
            Objects.requireNonNull(viewModel.getCountDownTimer()).pause();
            processDialog();
        });
        //暂停讲解
        binding.pauseBtn.setOnClickListener(v -> {
            clickCount++;
            if (isButtonClickable) {
                isButtonClickable = false;
                if (clickCount % 2 == 1) {
                    Universal.speaking = true;
                    // 奇数次点击，执行暂停操作
                    BaiduTTSHelper.getInstance().pause();
                    MediaPlayerHelper.getInstance().pause();
                    binding.pauseBtn.setText("继续讲解");
                    binding.pauseTV.setVisibility(View.VISIBLE);
                } else {
                    // 偶数次点击，执行恢复操作
                    Universal.speaking = false;
                    BaiduTTSHelper.getInstance().resume();
                    MediaPlayerHelper.getInstance().resume();
                    binding.pauseTV.setVisibility(View.GONE);
                    binding.pauseBtn.setText("暂停讲解");
                }
                new Handler().postDelayed(() -> {
                    // 恢复按钮可点击状态
                    isButtonClickable = true;
                }, 1000); // 设置延迟时间，避免按钮重复点击
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_start_explantion, container, false);
        binding = DataBindingUtil.bind(rootView);
        return rootView;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 释放视图
        rootView = null;
    }
    @SuppressLint("SuspiciousIndentation")
    private void init() {
        //禁止滑动
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        binding.pointList.setLayoutManager(layoutManager);
        binding.pointList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.pointList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                centerToTopDistance = binding.pointList.getHeight();
                findView();
//                scrollToCenter(0); // 将第一个列表项滚动到顶部位置
            }
        });

        ExplainManager.INSTANCE.getExplainStatusModel().observe(getViewLifecycleOwner(), it -> {
            if (it == null) return;
            MyResultModel route = it.getRouteLiveData();
            if (route != null) {
                switch (it.getStatus()) {
                    case ExplainStatusModel.STATUS_ON_THE_WAY_BEFORE:{
                        Universal.speaking = false;
                        int position = it.getPosition();
                        itemTarget = position;
                        scrollToCenter(position);
                        break;
                    }
                    case ExplainStatusModel.STATUS_ON_THE_WAY_PROCESS:{
                        if (route.getTouch_type() == TopLevelConfig.TYPE_EXPRESSION) {
                            binding.argPic.setVisibility(View.VISIBLE);
                            Glide.with(requireActivity()).load(route.getTouch_arrivePic()).placeholder(R.drawable.ic_warming).into(binding.argPic);
                        } else {
                            binding.argPic.setVisibility(View.GONE);
                            try {
                                binding.pointImage.setData(ExplainManager.INSTANCE.getFilesAllName(BaseViewModel.getFilesAllName(route.getTouch_imagefile()).get(0), 2, 3));
                                layoutThis(route);
                            } catch (Exception e) {
                                LogUtil.INSTANCE.e("途径播报异常: " + e);
                            }
                        }
                        binding.parentCon.setClickable(false);
                        switch (it.getType()) {
                            case ExplainStatusModel.TYPE_TEXT:{
                                binding.contentLin.setVisibility(View.VISIBLE);
                            }
                            case ExplainStatusModel.TYPE_MP3:{
                                binding.contentLin.setVisibility(View.GONE);
                            }
                        }
                        binding.contentLin.setVisibility(View.GONE);
                        binding.contentCL.setVisibility(View.VISIBLE);
                        binding.parentCon.setBackgroundResource(R.drawable.bg);
                        binding.statusTv.setText("已到达:");
                        binding.nowExplanation.setText(route.getName());
                        break;
                    }
                    case ExplainStatusModel.STATUS_ARRIVE_BEFORE:{
                        LogUtil.INSTANCE.i("STATUS_ARRIVE_BEFORE");
                        currentTextQueue = null;
                        binding.parentCon.setClickable(false);
                        binding.contentLin.setVisibility(View.VISIBLE);
                        binding.contentCL.setVisibility(View.VISIBLE);
                        binding.parentCon.setBackgroundResource(R.drawable.bg);
                        if (BillManager.INSTANCE.billList().size() == 1) {
                            binding.nextTaskBtn.setVisibility(View.GONE);
                        } else {
                            binding.nextTaskBtn.setVisibility(View.VISIBLE);
                        }
                        binding.statusTv.setText("正在讲解:");
                        binding.nowExplanation.setText(route.getName());
                        if (route.getTouch_type() == TopLevelConfig.TYPE_EXPRESSION) {
                            binding.finishBtn.setEnabled(false);
                            binding.changingOver.setEnabled(false);
                            binding.nextTaskBtn.setEnabled(false);
                            binding.argPic.setVisibility(View.VISIBLE);
                            Glide.with(requireActivity()).load(route.getTouch_arrivePic()).placeholder(R.drawable.ic_warming).into(binding.argPic);
                        } else {
                            binding.finishBtn.setEnabled(true);
                            binding.changingOver.setEnabled(true);
                            binding.nextTaskBtn.setEnabled(true);
                            binding.argPic.setVisibility(View.GONE);
                            try {
                                binding.pointImage.setData(ExplainManager.INSTANCE.getFilesAllName(route.getTouch_imagefile(), route.getTouch_pictype(), route.getTouch_picplaytime()));
                                layoutThis(route);
                            } catch (Exception e) {
                                Log.d("TAG", "onBindViewHolder: " + e);
                            }
                        }
                        break;
                    }
                    case ExplainStatusModel.STATUS_ARRIVE_PROCESS:{
                        switch (it.getType()) {
                            case ExplainStatusModel.TYPE_TEXT:{
                                binding.pauseBtn.setVisibility(View.VISIBLE);
                                binding.pointName.setText(route.getName());
                                if (currentTextQueue == null) {
                                    String text = PlaceholderEnum.Companion.replaceText(
                                            route.getExplanationtext(),
                                            "",
                                            route.getName(),
                                            route.getRoutename(),
                                            "智能讲解");
                                    currentTextQueue = ExplainManager.INSTANCE.splitString(text, 135);
                                }
                                int progress = it.getTextProgress();
                                if (progress > -1) {
                                    int page = progress / 135;
                                    int currentTextProgress = progress % 135;
                                    String currentText = currentTextQueue.get(page);
//                                    LogUtil.INSTANCE.i("当前页数: " + page);
//                                    LogUtil.INSTANCE.i("当前进度: " + currentTextProgress);
//                                    LogUtil.INSTANCE.i("当前内容: " + currentText);
                                    binding.acceptStationTv.setText(currentText, currentTextProgress);
                                }
                                break;
                            }
                            case ExplainStatusModel.TYPE_MP3:{
                                binding.contentLin.setVisibility(View.GONE);
                                binding.pauseBtn.setVisibility(View.GONE);
                                break;
                            }
                        }
                    }
                }
            }


        });


    }
    private boolean isTouch = true;
    private final List<CenterItemUtils.CenterViewItem> centerViewItems = new ArrayList<>();
    @SuppressLint("ClickableViewAccessibility")
    private void findView() {
        mAdapter = new MAdapter();
        binding.pointList.setAdapter(mAdapter);
        binding.pointList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    assert linearLayoutManager != null;
                    int fi = linearLayoutManager.findFirstVisibleItemPosition();
                    int la = linearLayoutManager.findLastVisibleItemPosition();
                    LogUtil.INSTANCE.i("onScrollStateChanged:首个item: " + fi + "  末尾item:" + la);
                    if (isTouch) {
                        isTouch = false;
                        //获取顶部的Item View
                        View topView = recyclerView.getChildAt(0);
                        int topChildViewPosition = recyclerView.getChildAdapterPosition(topView); //获取当前所有条目中顶部的一个条目索引
                        centerViewItems.clear();
                        //遍历循环，获取到和顶部相差最小的条目索引
                        if (topChildViewPosition != 0) {
                            for (int i = topChildViewPosition - 1; i < topChildViewPosition + 2; i++) {
                                View cView = recyclerView.getLayoutManager().findViewByPosition(i);
                                assert cView != null;
                                int viewTop = cView.getTop();
                                centerViewItems.add(new CenterItemUtils.CenterViewItem(i, Math.abs(centerToTopDistance - viewTop)));
                            }

                            CenterItemUtils.CenterViewItem centerViewItem = CenterItemUtils.getMinDifferItem(centerViewItems);
                            topChildViewPosition = centerViewItem.position;
                        }
                        scrollToCenter(topChildViewPosition);
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

        binding.pointList.setOnTouchListener((view, motionEvent) -> {
            isTouch = true;
            return false;
        });
    }


    /**
     * 移动指定索引到中心处 ， 只可以移动可见区域的内容
     */
    private void scrollToCenter(int position) {
        position = Math.max(position, 0);
        position = Math.min(position, ExplainManager.INSTANCE.getRoutes().size() - 1);
        binding.pointList.scrollToPosition(position);
        if (mAdapter != null) {
            mAdapter.setSelectPosition(position);
        }
    }


    class MAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(getContext()).inflate(R.layout.item_start_explanation, parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            VH vh = (VH) holder;
            holder.itemView.setClickable(false);
            if (position == 0) {
                vh.tvTopLine.setVisibility(View.INVISIBLE);
            } else {
                vh.tvTopLine.setVisibility(View.VISIBLE);
            }
            holder.itemView.setEnabled(false);
            if (selectPosition == position) {
                binding.parentCon.setClickable(QuerySql.QueryBasic().getExplainInterrupt());
                AnimationDrawable animationDrawable = (AnimationDrawable) vh.tvTopLine.getDrawable();
                animationDrawable.start();
                    vh.tv.setTextColor(getResources().getColor(R.color.color_49DCFA));
                    vh.tvDot.setBackgroundResource(R.drawable.lline_dot_normal);
                    RobotStatus.INSTANCE.setPointItemIndex(position);
                    LogUtil.INSTANCE.d("当前讲解点开始");
                    DialogHelper.loadingDialog.dismiss();
                    //当前点显示的文字&图片
                    binding.goingName.setText(ExplainManager.INSTANCE.getRoutes().get(position).getName());
//                    Log.e("TAG", "onBindViewLandholder: " + QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRouteMapItemId()).get(position).getTouch_type());
                    if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRouteMapItemId()).get(position).getTouch_type() == TopLevelConfig.TYPE_EXPRESSION) {
                        binding.argPic.setVisibility(View.VISIBLE);
                        Glide.with(requireActivity()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRouteMapItemId()).get(position).getTouch_walkPic()).placeholder(R.drawable.ic_warming).into(binding.argPic);
                        //touch_walkPic
                    } else {
                        binding.argPic.setVisibility(View.GONE);
                        binding.parentCon.setBackgroundResource(0);
                        binding.goingLin.setVisibility(View.VISIBLE);
                        binding.contentCL.setVisibility(View.GONE);
                        if (ExplainManager.INSTANCE.getRoutes().get(position).getTouch_imagefile() != null) {
                            //带光晕的背景图(新加了默认值，直接try，就会显示布局中设置的图片)
                            try {
                                Glide.with(requireActivity()).load(BaseViewModel.getFilesAllName(Objects.requireNonNull(ExplainManager.INSTANCE.getRoutes()).get(position).getTouch_imagefile()).get(0)).into(binding.startImg);//轮播
                            }catch (Exception ignored){}
                        }
                    }
            } else {
                vh.tv.setTextColor(getResources().getColor(R.color.white));
                vh.tvDot.setBackgroundResource(R.drawable.lline_dot_first);
            }
            vh.tv.setText(Objects.requireNonNull(ExplainManager.INSTANCE.getRoutes()).get(position).getName());

        }

        private int selectPosition = -1;

        @SuppressLint("NotifyDataSetChanged")
        public void setSelectPosition(int cposition) {
            selectPosition = cposition;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return Objects.requireNonNull(ExplainManager.INSTANCE.getRoutes()).size();
        }

        class VH extends RecyclerView.ViewHolder {

            public TextView tv, tvDot;
            public ImageView tvTopLine;

            public VH(@NonNull View itemView) {
                super(itemView);
                // 第一行头的竖线不显示
                tvTopLine = itemView.findViewById(R.id.tvTopLine);
                tv = itemView.findViewById(R.id.tv);
                tvDot = itemView.findViewById(R.id.tvDot);
            }
        }

    }

    private void processDialog() {
        processClickDialog.show();
        finishTaskDialog.confirmBtn.setOnClickListener(v12 -> {
            Universal.explainTextLength = -1;
            MediaPlayerHelper.getInstance().stop();
            viewModel.finishTask();
            processClickDialog.dismiss();
            finishTaskDialog.dismiss();
            BaiduTTSHelper.getInstance().speaks(PlaceholderEnum.Companion.replaceText(QuerySql.QueryExplainConfig().getInterruptionText(),"",binding.nowExplanation.getText().toString(),ExplainManager.INSTANCE.getRoutes().get(0).getRoutename(),"智能讲解"));
        });
        finishTaskDialog.cancelBtn.setOnClickListener(v1 -> finishTaskDialog.dismiss());
        processClickDialog.finishBtn.setOnClickListener(v -> {
            finishTaskDialog.show();
        });
        processClickDialog.nextBtn.setOnClickListener(v -> {
            viewModel.nextTask();
            processClickDialog.dismiss();

        });
        processClickDialog.otherBtn.setOnClickListener(v -> {
            processClickDialog.dismiss();
            changeDialog(false);
        });

    }

    private void changeDialog(boolean array) {
        //暂停页面
        BaiduTTSHelper.getInstance().pause();
        BaiduTTSHelper.getInstance().pause();
        changingOverDialog.show();
        if (!array) {
            UpdateReturn.INSTANCE.pause();
        }
        ChangePointGridViewAdapter adapter = new ChangePointGridViewAdapter(requireActivity(), ExplainManager.INSTANCE.getRoutes(), BillManager.INSTANCE.currentBill().endTarget());
        changingOverDialog.pointGV.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        changingOverDialog.pointGV.setOnItemClickListener((parent, view, position, id) -> {
            if (position == itemTarget) {
                return;
            }
            changingOverDialog.askTv.setText(Objects.requireNonNull(ExplainManager.INSTANCE.getRoutes()).get(position).getName());
            MediaPlayerHelper.getInstance().stop();
            viewModel.recombine(Objects.requireNonNull(ExplainManager.INSTANCE.getRoutes()).get(position).getName());
            changingOverDialog.dismiss();
        });
        changingOverDialog.returnImg.setOnClickListener(v1 -> {
            returnFunction();
        });
        changingOverDialog.textViewReturn.setOnClickListener(v1 -> {
            returnFunction();
        });
    }

    /**
     * picPlayTime    轮播时间
     * file           路径
     * type           类型： 1-图片 2-视频 6-文字 7-图片+文字
     * textPosition   文字x位置
     * fontLayout     文字方向：1-横向，2-纵向
     * fontContent    文字
     * fontBackGround 背景颜色
     * fontColor      文字颜色
     * fontSize       文字大小：1-大，2-中，3-小,
     * picType        图片样式
     */
    public void layoutThis(MyResultModel route) {
        int picPlayTime = route.getTouch_picplaytime();
        String file = route.getTouch_imagefile();
        int type = route.getTouch_type();
        int textPosition = route.getTouch_textposition();
        int fontLayout = route.getTouch_fontlayout();
        String fontContent = route.getTouch_fontcontent();
        String fontBackGround = route.getTouch_fontbackground();
        String fontColor = route.getTouch_fontcolor();
        int fontSize = route.getTouch_fontsize();
        int picType = route.getTouch_pictype();
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.verticalTV.getLayoutParams();
        switch (type) {
            case 1:
            case 2:
                //读取文件
                binding.pointImage.setData(ExplainManager.INSTANCE.getFilesAllName(file, picType, picPlayTime));
                binding.verticalTV.setVisibility(View.GONE);
                binding.horizontalTV.setVisibility(View.GONE);
                binding.pointImage.setVisibility(View.VISIBLE);
                break;
            case 6:
                binding.pointImage.setVisibility(View.GONE);
                if (textPosition == 0) {
                    binding.horizontalTV.setGravity(Gravity.CENTER);//居中
                    textLayoutThis(fontLayout, fontContent, fontBackGround, fontColor, fontSize);
                } else if (textPosition == 1) {
                    binding.horizontalTV.setGravity(Gravity.TOP);//居上
                    layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
                    binding.verticalTV.setLayoutParams(layoutParams);
                    textLayoutThis(fontLayout, fontContent, fontBackGround, fontColor, fontSize);
                } else if (textPosition == 2) {
                    binding.horizontalTV.setGravity(Gravity.BOTTOM);//居下
                    layoutParams.topToTop = ConstraintLayout.LayoutParams.UNSET;
                    binding.verticalTV.setLayoutParams(layoutParams);
                    textLayoutThis(fontLayout, fontContent, fontBackGround, fontColor, fontSize);
                }
                break;
            case 7:
                //读取文件
                binding.pointImage.setData(ExplainManager.INSTANCE.getFilesAllName(file, picType, picPlayTime));
                if (textPosition == 0) {
                    binding.horizontalTV.setGravity(Gravity.CENTER);//居中
                    textLayoutThis(fontLayout, fontContent, fontBackGround, fontColor, fontSize);
                } else if (textPosition == 1) {
                    binding.horizontalTV.setGravity(Gravity.TOP);//居上
                    layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
                    binding.verticalTV.setLayoutParams(layoutParams);
                    textLayoutThis(fontLayout, fontContent, fontBackGround, fontColor, fontSize);
                } else if (textPosition == 2) {
                    binding.horizontalTV.setGravity(Gravity.BOTTOM);//居下
                    layoutParams.topToTop = ConstraintLayout.LayoutParams.UNSET;
                    binding.verticalTV.setLayoutParams(layoutParams);
                    textLayoutThis(fontLayout, fontContent, fontBackGround, fontColor, fontSize);
                }
                binding.pointImage.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * @param fontLayout     文字方向：1-横向，2-纵向
     * @param fontContent    文字
     * @param fontBackGround 背景颜色
     * @param fontColor      文字颜色
     * @param fontSize       文字大小：1-大，2-中，3-小,
     */
    private void textLayoutThis(int fontLayout, String fontContent, String fontBackGround, String fontColor, int fontSize) {

        //横向
        if (fontLayout == 1) {
            //隐藏纵向文字，显示横向文字
            binding.verticalTV.setVisibility(View.GONE);
            binding.horizontalTV.setVisibility(View.VISIBLE);
            //显示内容
            binding.horizontalTV.setText(baseViewModel.getLength(fontContent));
            //背景颜色&图片
            binding.horizontalTV.setBackgroundColor(Color.parseColor(fontBackGround + ""));
            //文字颜色
            binding.horizontalTV.setTextColor(Color.parseColor(fontColor + ""));
            //字体大小
            if (fontSize == 1) {
                binding.horizontalTV.setTextSize(30);
            } else if (fontSize == 2) {
                binding.horizontalTV.setTextSize(20);
            } else if (fontSize == 3) {
                binding.horizontalTV.setTextSize(10);
            }
        } else {
            //纵向
            //隐藏横向文字，显示纵向文字
            binding.verticalTV.setVisibility(View.VISIBLE);
            binding.horizontalTV.setVisibility(View.GONE);
            //显示内容
            binding.verticalTV.setText(fontContent);
            //背景颜色
            binding.verticalTV.setBackgroundColor(Color.parseColor(fontBackGround + ""));
            //文字颜色
            binding.verticalTV.setTextColor(Color.parseColor(fontColor + ""));
            //字体大小
            if (fontSize == 1) {
                binding.verticalTV.setTextSize(30);
            } else if (fontSize == 2) {
                binding.verticalTV.setTextSize(20);
            } else if (fontSize == 3) {
                binding.verticalTV.setTextSize(10);
            }
        }
    }

    /**
     * @description: 返回功能
     */
    private void returnFunction() {
        changingOverDialog.dismiss();
        if (clickCount % 2 != 1) {
            MediaPlayerHelper.getInstance().resume();
            BaiduTTSHelper.getInstance().resume();
        }
        Objects.requireNonNull(viewModel.getCountDownTimer()).resume();
        UpdateReturn.INSTANCE.resume();
    }
}