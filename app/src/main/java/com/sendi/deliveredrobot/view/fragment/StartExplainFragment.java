package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Consumer;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.BuildConfig;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.adapter.ChangePointGridViewAdapter;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.databinding.FragmentStartExplantionBinding;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.DialogHelper;
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper;
import com.sendi.deliveredrobot.helpers.ReportDataHelper;
import com.sendi.deliveredrobot.model.ExplantionNameModel;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.navigationtask.BillManager;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.navigationtask.TaskQueues;
import com.sendi.deliveredrobot.service.TaskStageEnum;
import com.sendi.deliveredrobot.service.UpdateReturn;
import com.sendi.deliveredrobot.utils.CenterItemUtils;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.view.widget.Advance;
import com.sendi.deliveredrobot.view.widget.ChangingOverDialog;

import com.sendi.deliveredrobot.view.widget.ProcessClickDialog;
import com.sendi.deliveredrobot.view.widget.Stat;
import com.sendi.deliveredrobot.viewmodel.BaseViewModel;
import com.sendi.deliveredrobot.viewmodel.StartExplanViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observer;
import java.util.TimerTask;

/**
 * @author swn
 * @describe 开始智能讲解
 */
public class StartExplainFragment extends Fragment {

    FragmentStartExplantionBinding binding;
    NavController controller;
    private MAdapter mAdapter;
    private int centerToTopDistance; //RecyclerView高度的一半 ,也就是控件中间位置到顶部的距离 ，
    private final int childViewHalfCount = 0; //当前RecyclerView一半最多可以存在几个Item
    private StartExplanViewModel viewModel;
    private BaseViewModel baseViewModel;
    private ProcessClickDialog processClickDialog;
    private ChangingOverDialog changingOverDialog;
    private int beforePage = 0;
    boolean nextTaskToDo = true;
    Handler handler;
    String targetName = "";
    private View rootView;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private ExplantionNameModel explantionNameModel;
    private int itemTarget;
    Boolean pointArray = false;
    MutableLiveData<Integer> currentPosition1 = new MutableLiveData<>();
    MutableLiveData<Integer> totalDuration1 = new MutableLiveData<>();
    boolean array = false;
    ConstraintLayout.LayoutParams layoutParams;
    private boolean isButtonClickable = true;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("", "onAttach页面准备进入");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("", "onCreate页面进入");
        explantionNameModel = new ExplantionNameModel();
        Consumer<String> taskConsumer = task -> {
            // 执行任务的代码
            if (BuildConfig.IS_SPEAK) {
                BaiduTTSHelper.getInstance().speaks(task, "explanation");
            }
            Log.d("", "Task: " + task);
        };
        // 创建TaskQueue实例
        Universal.taskQueue = new TaskQueues<>(taskConsumer);

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
                Universal.taskQueue.pause();
                MediaPlayerHelper.pause();
                BaiduTTSHelper.getInstance().pause();
            } else if (Stat.getFlage() == 3) {
                //继续
                Universal.taskQueue.resume();
                MediaPlayerHelper.resume();
                BaiduTTSHelper.getInstance().resume();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setData(explantionNameModel);
        handler = new Handler();
        controller = Navigation.findNavController(view);
        nextTaskToDo = true;
        LogUtil.INSTANCE.d("onViewCreated进入讲解页面");
        status();
        baseViewModel = new ViewModelProvider(this).get(BaseViewModel.class);
        viewModel = new ViewModelProvider(this).get(StartExplanViewModel.class);
        RobotStatus.INSTANCE.getSpeakContinue().observe(getViewLifecycleOwner(), integer -> {
            if (integer == 1) {
                Log.d("", "onViewCreated: 讲解打断，重新计算" + RobotStatus.INSTANCE.getSpeakNumber().getValue());
                BaiduTTSHelper.getInstance().stop();
                splitString(RobotStatus.INSTANCE.getSpeakNumber().getValue(), Universal.BaiduSpeakLength);
                RobotStatus.INSTANCE.getSpeakContinue().postValue(0);
            }
        });
        viewModel.downTimer();
        processClickDialog = new ProcessClickDialog(getContext());
        changingOverDialog = new ChangingOverDialog(getContext());
        init();
        viewModel.mainScope();


        binding.finishBtn.setOnClickListener(v -> {
            if (isButtonClickable) {
                isButtonClickable = false;
                beforePage = 0;
                //返回
                Universal.taskNum = 0;
                Universal.progress = 0;
                //删除讲解队列
                if (Universal.taskQueue != null) {
                    Universal.taskQueue.clear();
                }
                viewModel.getCountDownTimer().pause();
                viewModel.finishTask();
                MediaPlayerHelper.stop();
                RobotStatus.INSTANCE.getSpeakNumber().postValue("");
                binding.acceptstationTv.stopPlay();
                processClickDialog.dismiss();
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
                if (Universal.taskQueue != null) {
                    Universal.taskQueue.clear();
                }
                RobotStatus.INSTANCE.getSpeakNumber().postValue("");
                MediaPlayerHelper.stop();
                viewModel.getCountDownTimer().pause();
                binding.acceptstationTv.stopPlay();
                viewModel.nextTask(true);
                processClickDialog.dismiss();
                new Handler().postDelayed(() -> {
                    // 恢复按钮可点击状态
                    isButtonClickable = true;
                }, 4000); // 设置延迟时间，避免按钮重复点击
            }
        });

        binding.ChangingOver.setOnClickListener(v -> {
            if (isButtonClickable) {
                isButtonClickable = false;
                viewModel.getCountDownTimer().pause();
                changeDialog(true);
                pointArray = true;
                processClickDialog.dismiss();
                new Handler().postDelayed(() -> {
                    // 恢复按钮可点击状态
                    isButtonClickable = true;
                }, 4000); // 设置延迟时间，避免按钮重复点击
            }
        });
        binding.parentCon.setOnClickListener(v -> {
            viewModel.getCountDownTimer().pause();
            processDialog();
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

        binding.pointList.postDelayed(() -> RobotStatus.INSTANCE.getTargetName().observe(getViewLifecycleOwner(), s -> {
            DialogHelper.loadingDialog.show();
            targetName = s;
            LogUtil.INSTANCE.i("当前讲解点: " + s);
            // 在这里处理新值，比如更新UI或执行其他逻辑
            for (int i = 0; i < viewModel.inForListData().size(); i++) {
                if (Objects.equals(viewModel.inForListData().get(i).getName(), s)) {
                    LogUtil.INSTANCE.i("onClick: " + i);
                    itemTarget = i;
                    scrollToCenter(i);
                    viewModel.secondScreenModel(i, viewModel.inForListData());
                    if (Universal.lastValue == null || !Universal.lastValue.equals(s)) { // 检查新值和上一个值是否相同
                        LogUtil.INSTANCE.i("init: 执行途经播报任务");
                        explanGonningSpeak(viewModel.inForListData(), i, true);
                        beforePage = 0;
                        Universal.lastValue = s; // 更新上一个值为新值
                    }
                }
            }
        }), 100);

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
                    int fi = linearLayoutManager.findFirstVisibleItemPosition();
                    int la = linearLayoutManager.findLastVisibleItemPosition();
                    Log.i("ccb", "onScrollStateChanged:首个item: " + fi + "  末尾item:" + la);
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
        position = Math.min(position, mAdapter.getItemCount() - 1);
        binding.pointList.scrollToPosition(position);
        mAdapter.setSelectPosition(position);
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
            }
            holder.itemView.setEnabled(false);
            if (selectPosition == position) {
                try {
                RobotStatus.INSTANCE.getPointItem().postValue(position);
                Log.e("TAG", "onBindViewHolderdangqian: " + position);
                LogUtil.INSTANCE.d("当前讲解点开始");
                DialogHelper.loadingDialog.dismiss();
                //当前点显示的文字&图片
                vh.tv.setTextColor(getResources().getColor(R.color.color_49DCFA));
                vh.tvDot.setBackgroundResource(R.drawable.lline_dot_normal);
                binding.goingName.setText(viewModel.inForListData().get(position).getName());
                if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type() == 4) {
                    binding.argPic.setVisibility(View.VISIBLE);
                    Glide.with(getContext()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_walkPic()).placeholder(R.drawable.ic_warming).into(binding.argPic);
                    //touch_walkPic
                } else {
                    binding.argPic.setVisibility(View.GONE);
                    binding.parentCon.setBackgroundResource(0);
                    binding.goingLin.setVisibility(View.VISIBLE);
                    binding.parentCon.setClickable(QuerySql.QueryBasic().getWhetherInterrupt());
                    binding.contentCL.setVisibility(View.GONE);
                    if (viewModel.inForListData().get(position).getTouch_imagefile() != null) {
                        //带光晕的背景图
                        Glide.with(getContext()).load(BaseViewModel.getFilesAllName(viewModel.inForListData().get(position).getTouch_imagefile()).get(0)).into(binding.startImg);//轮播
                    }
                }
                MediaPlayerHelper.setOnProgressListener((currentPosition, totalDuration) -> {
                    LogUtil.INSTANCE.i("currentPosition : " + currentPosition + " totalDuration :" + totalDuration);
                    if (!array) {
                        currentPosition1.postValue(currentPosition);
                        totalDuration1.postValue(totalDuration);
                    }
                });


                if (Universal.taskQueue.isTaskQueueCompleted()) {
                    //要执行的任务代码
                    try {
                        ReportDataHelper.INSTANCE.reportTaskDto(BillManager.INSTANCE.currentBill().currentTask().taskModel(), TaskStageEnum.FinishChannelBroadcast, new UpdateReturn().taskDto());
                    } catch (Exception ignored) {
                    }
                    Log.d("TAG", "onBindViewHolder: 任务完成");
                }
                //帧动画
                vh.tvTopLine.setImageResource(R.drawable.anim_login_start_loading);
                AnimationDrawable animationDrawable = (AnimationDrawable) vh.tvTopLine.getDrawable();
                animationDrawable.start();
                if (viewModel.inForListData().get(position).getWalktext() != null && !viewModel.inForListData().get(position).getWalktext().isEmpty()) {
                    Log.d("TAG", "途径播报内容：" + viewModel.inForListData().get(position).getWalktext());
                    RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer1 -> {
                        if (integer1 == 1 && Universal.taskQueue.isCompleted()) {
                            Log.d("TAG", "是否结束");
                            arrayToDo(viewModel.inForListData(), position);

                        }
                    });
                }
                if (viewModel.inForListData().get(position).getWalkvoice() != null && !viewModel.inForListData().get(position).getWalkvoice().isEmpty()) {
                    RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer -> {
                        if (integer == 1) {
                            LogUtil.INSTANCE.i("到点");
                            currentPosition1.observe(getViewLifecycleOwner(), integer1 -> totalDuration1.observe(getViewLifecycleOwner(), integer2 -> {
                                if ((integer2 - integer1) > 150 && array == false) {
                                    mHandler.post(() -> {
                                        if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type() == 4) {
                                            binding.argPic.setVisibility(View.VISIBLE);
                                            Glide.with(getContext()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_arrivePic()).placeholder(R.drawable.ic_warming).into(binding.argPic);
                                        } else {
                                            binding.argPic.setVisibility(View.GONE);
                                            try {
                                                getFilesAllName(BaseViewModel.getFilesAllName(viewModel.inForListData().get(position).getTouch_imagefile()).get(0));//轮播
                                            } catch (Exception ignored) {
                                            }
                                            try {
                                                layoutThis(viewModel.inForListData().get(position).getTouch_picplaytime(), viewModel.inForListData().get(position).getTouch_imagefile(), viewModel.inForListData().get(position).getTouch_type(), viewModel.inForListData().get(position).getTouch_textposition(), viewModel.inForListData().get(position).getTouch_fontlayout(), viewModel.inForListData().get(position).getTouch_fontcontent().toString(), viewModel.inForListData().get(position).getTouch_fontbackground(), viewModel.inForListData().get(position).getTouch_fontcolor().toString(), (int) viewModel.inForListData().get(position).getTouch_fontsize(), viewModel.inForListData().get(position).getTouch_pictype());
                                            } catch (Exception ignored) {
                                            }
                                        }
                                        binding.parentCon.setClickable(false);
                                        binding.contentLin.setVisibility(View.GONE);
                                        binding.contentCL.setVisibility(View.VISIBLE);
                                        binding.parentCon.setBackgroundResource(R.drawable.bg);
                                        binding.statusTv.setText("已到达:");
                                        binding.nowExplanation.setText(viewModel.inForListData().get(position).getName());
                                    });
                                } else if ((integer2 - integer1) <= 150 && array == false) {
                                    LogUtil.INSTANCE.i("到点并且播放完成");
                                    mHandler.post(() -> {
                                        MediaPlayerHelper.stop();
                                        try {
                                            ReportDataHelper.INSTANCE.reportTaskDto(BillManager.INSTANCE.currentBill().currentTask().taskModel(), TaskStageEnum.FinishChannelBroadcast, new UpdateReturn().taskDto());
                                        } catch (Exception ignored) {
                                        }
                                        arrayToDo(viewModel.inForListData(), position);
                                    });
                                }
                            }));
                        }
                    });
                }
                if (viewModel.inForListData().get(position).getWalkvoice() == null && viewModel.inForListData().get(position).getWalktext() == null) {
                    RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer1 -> {
                        if (integer1 == 1) {
                            Log.d("TAG", "是否结束");
                            arrayToDo(viewModel.inForListData(), position);
                        }
                    });
                }
                }catch (Exception e){}
            } else {
                vh.tv.setTextColor(getResources().getColor(R.color.white));
                vh.tvDot.setBackgroundResource(R.drawable.lline_dot_first);
            }
            vh.tv.setText(viewModel.inForListData().get(position).getName());

        }

        private int selectPosition = -1;

        @SuppressLint("NotifyDataSetChanged")
        public void setSelectPosition(int cposition) {
            selectPosition = cposition;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return viewModel.inForListData().size();
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

    public List<String> splitString(String input, int length) {
        try {
        List<String> result = new ArrayList<>();
        if (Universal.taskQueue != null) {
            Universal.taskQueue.clear();
        }
        int startIndex = 0;
        Log.d("TAG", "splitString: 进来咯");
        while (startIndex < input.length()) {
            int endIndex = startIndex + length;
            if (endIndex > input.length()) {
                endIndex = input.length();
            }
            String substring = input.substring(startIndex, endIndex);
            result.add(substring);
            startIndex = endIndex;
        }
        for (int i = 0; i < result.size(); i++) {
            // 添加任务到队列中
            Universal.taskQueue.enqueue(result.get(i));
            // 开始执行队列中的任务
            Universal.taskQueue.resume();
        }
            return result;
        }catch (Exception e){}

        return null;
    }

    private void explanGonningSpeak(ArrayList<MyResultModel> mDatas, int position, boolean tr) {
        array = false;
        LogUtil.INSTANCE.i("开始途径播报");
        try {
        //有讲解内容
        if (mDatas.get(position).getWalktext() != null && !mDatas.get(position).getWalktext().isEmpty()) {
            if (tr) {
                @SuppressLint("StaticFieldLeak")
                class MyAsyncTask extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected void onPreExecute() {
                        // 在执行后台任务之前执行，通常用于初始化操作
                        try {
                            ReportDataHelper.INSTANCE.reportTaskDto(BillManager.INSTANCE.currentBill().currentTask().taskModel(), TaskStageEnum.StartChannelBroadcast, new UpdateReturn().taskDto());
                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        Log.d("TAG", "doInBackground: 开始播报");
                        // 在后台线程中执行耗时操作，例如数据预加载
                        splitString(mDatas.get(position).getWalktext(), Universal.BaiduSpeakLength);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        RobotStatus.INSTANCE.getSpeakNumber().setValue(mDatas.get(position).getWalktext());
                    }
                }

                Runnable runnable = () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    MyAsyncTask task = new MyAsyncTask();
                    task.execute();
                };
                handler.postDelayed(runnable, 1000);


            }
        }
        //如果有mp3
        if (mDatas.get(position).getWalkvoice() != null && !mDatas.get(position).getWalkvoice().isEmpty()) {
            if (tr) {
                if (Universal.taskQueue != null) {
                    Universal.taskQueue.clear();
                }
                try {
                    ReportDataHelper.INSTANCE.reportTaskDto(BillManager.INSTANCE.currentBill().currentTask().taskModel(), TaskStageEnum.StartChannelBroadcast, new UpdateReturn().taskDto());
                } catch (Exception ignored) {
                }

                handler.postDelayed(() -> MediaPlayerHelper.play(mDatas.get(position).getWalkvoice(), "1"), 3000);

            }
        }
        }catch (Exception e){}
    }

    private void arrayToDo(ArrayList<MyResultModel> mDatas, int position) {
        try {
        array = true;
        LogUtil.INSTANCE.d("到点讲解：" + Universal.selectMapPoint);
        if (Universal.selectMapPoint) {
            return;
        }
        nextTaskToDo = true;
        Universal.progress = 0;
        currentPosition1.postValue(0);
        totalDuration1.postValue(0);
        requireActivity().runOnUiThread(() -> {
            // 在执行后台任务之前执行，通常用于初始化操作
            binding.parentCon.setClickable(false);
            binding.contentLin.setVisibility(View.VISIBLE);
            binding.contentCL.setVisibility(View.VISIBLE);
            binding.parentCon.setBackgroundResource(R.drawable.bg);
            binding.statusTv.setText("正在讲解:");
            binding.nowExplanation.setText(mDatas.get(position).getName());

            if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type() == 4) {
                binding.finishBtn.setEnabled(false);
                binding.ChangingOver.setEnabled(false);
                binding.nextTaskBtn.setEnabled(false);
                binding.argPic.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_arrivePic()).placeholder(R.drawable.ic_warming).into(binding.argPic);
            } else {
                binding.finishBtn.setEnabled(true);
                binding.ChangingOver.setEnabled(true);
                binding.nextTaskBtn.setEnabled(true);
                binding.argPic.setVisibility(View.GONE);
                try {
                    getFilesAllName(mDatas.get(position).getTouch_imagefile());//轮播
                } catch (Exception ignored) {
                }
                try {
                    layoutThis(mDatas.get(position).getTouch_picplaytime(), mDatas.get(position).getTouch_imagefile(), mDatas.get(position).getTouch_type(), mDatas.get(position).getTouch_textposition(), mDatas.get(position).getTouch_fontlayout(), mDatas.get(position).getTouch_fontcontent().toString(), mDatas.get(position).getTouch_fontbackground(), mDatas.get(position).getTouch_fontcolor().toString(), (int) mDatas.get(position).getTouch_fontsize(), mDatas.get(position).getTouch_pictype());
                } catch (Exception ignored) {
                }

            }
            if (mDatas.get(position).getExplanationtext() != null && !mDatas.get(position).getExplanationtext().isEmpty()) {
                try {
                    ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.StartArrayBroadcast, new UpdateReturn().taskDto());
                } catch (Exception ignored) {
                }
                binding.pointName.setText(mDatas.get(position).getName());
                LogUtil.INSTANCE.i("到点讲解文字:" + mDatas.get(position).getExplanationtext());
            }
            //这段你别问我哈，公司没钱。用短语音识别去读长语音，还要过渡显示，更过分的还要翻页诶。我也没办法，只能这样做，我也看不懂了。
            if (mDatas.get(position).getExplanationtext() != null && !mDatas.get(position).getExplanationtext().isEmpty()) {
                List<String> textEqually = viewModel.splitString(mDatas.get(position).getExplanationtext(), 135);
                //页数
                int page = textEqually.size();
//                Log.d("TAG", "内容列表长度: " + page + "当前内容：" + textEqually.get(beforePage));
                //显示第一页
                binding.acceptstationTv.setText(textEqually.get(beforePage));
                Log.d("TAG", "当前页数: " + beforePage);
                //将第一页的内容再次等分成BaiduTTS可以朗读的范围
                Universal.taskNum = splitString(textEqually.get(beforePage), Universal.BaiduSpeakLength).size();
                RobotStatus.INSTANCE.getProgress().observe(getViewLifecycleOwner(), integer -> {
                    Log.d("TAG", "当前进度: " + integer);
                    if (nextTaskToDo && integer <= textEqually.get(beforePage).length()) {
                        binding.acceptstationTv.startPlayLine(
                                integer,
                                textEqually.get(beforePage).length(),
                                ((long) QuerySql.QueryBasic().getSpeechSpeed() * textEqually.get(beforePage).length()) * 1000);
                    } else if (beforePage < textEqually.size() && integer > textEqually.get(beforePage).length()) {
                        Universal.progress = 0;
                    }
                    if (beforePage < textEqually.size() && integer == textEqually.get(beforePage).length() && TaskQueues.isCompleted() && integer != 0) {
                        Log.i("TAG", "页数, progress：" + beforePage + ";进度:" + integer + "当前进度：" + Universal.progress);
                        nextTaskToDo = false;
                        beforePage++;
                        if (beforePage <= page - 1) {
                            RobotStatus.INSTANCE.getProgress().postValue(0);
                            Universal.taskNum = 0;
                            Universal.progress = 0;
                            Log.d("TAG", "当前在多少页: " + beforePage);
                            binding.acceptstationTv.setText(textEqually.get(beforePage));
                            Universal.taskNum = splitString(textEqually.get(beforePage), Universal.BaiduSpeakLength).size();
                            nextTaskToDo = true;
                        }
                        RobotStatus.INSTANCE.getSpeakContinue().observe(getViewLifecycleOwner(), integer1 -> {
                            Log.d("TAG", "beforePage: " + beforePage + ",page:" + page + ",TaskQueues.isCompleted():" + TaskQueues.isCompleted() + ",integer1:" + integer1);
                            if (beforePage == page && TaskQueues.isCompleted() && integer1 == 3) {
                                ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.FinishArrayBroadcast, new UpdateReturn().taskDto());
                                Log.d("TAG", "arrayToDo: 进入倒计时");
                                binding.acceptstationTv.stopPlay();
                                viewModel.getCountDownTimer().start();
                                RobotStatus.INSTANCE.getProgress().postValue(0);
                                if (Universal.taskQueue != null) {
                                    Universal.taskQueue.clear();
                                }
                                beforePage = 0;
                                Universal.progress = 0;
                                RobotStatus.INSTANCE.getSpeakContinue().postValue(0);
                            }
                        });
                    }
                });
            } else if (!mDatas.get(position).getExplanationvoice().isEmpty()) {
                binding.contentLin.setVisibility(View.GONE);
                try {
                    ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.StartArrayBroadcast, new UpdateReturn().taskDto());
                } catch (Exception ignored) {
                }
                LogUtil.INSTANCE.i("到点讲解音频路径:" + mDatas.get(position).getExplanationvoice());
                MediaPlayerHelper.play(mDatas.get(position).getExplanationvoice(), "1");
                MediaPlayerHelper.setOnProgressListener((currentPosition, totalDuration) -> {
                    Log.d("TAG", "currentPosition: " + currentPosition + ",totalDuration: " + totalDuration);
                    if ((totalDuration - currentPosition) <= 150) {
                        try {
                            MediaPlayerHelper.stop();
                            ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.FinishArrayBroadcast, new UpdateReturn().taskDto());
                        } catch (Exception ignored) {
                        }
                        viewModel.getCountDownTimer().start();
                    }
                });
            } else if (mDatas.get(position).getExplanationvoice().isEmpty() && mDatas.get(position).getExplanationtext().isEmpty()) {
                viewModel.getCountDownTimer().start();
            }
        });
        }catch (Exception e){}
    }

    private void processDialog() {
        processClickDialog.show();
        processClickDialog.finishBtn.setOnClickListener(v -> {
            //返回
            Universal.taskNum = 0;
            Universal.progress = 0;
            //删除讲解队列
            if (Universal.taskQueue != null) {
                Universal.taskQueue.clear();
            }
            RobotStatus.INSTANCE.getSpeakNumber().postValue("");
            MediaPlayerHelper.stop();
            viewModel.finishTask();
            binding.acceptstationTv.stopPlay();
            processClickDialog.dismiss();
        });
        processClickDialog.nextBtn.setOnClickListener(v -> {
            //删除讲解队列
//            Universal.Model = "切换下一个点";
            if (Universal.taskQueue != null) {
                Universal.taskQueue.clear();
            }
            RobotStatus.INSTANCE.getSpeakNumber().postValue("");
            MediaPlayerHelper.stop();
            binding.acceptstationTv.stopPlay();
            viewModel.nextTask(false);
            processClickDialog.dismiss();

        });
        processClickDialog.otherBtn.setOnClickListener(v -> {
            processClickDialog.dismiss();
            changeDialog(false);
            pointArray = false;
        });

    }

    private void changeDialog(boolean array) {
        changingOverDialog.show();
        if (!array) {
            new UpdateReturn().pause();
        }
        ChangePointGridViewAdapter adapter = new ChangePointGridViewAdapter(getContext(), viewModel.inForListData(), targetName);
        changingOverDialog.pointGV.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        changingOverDialog.pointGV.setOnItemClickListener((parent, view, position, id) -> {
            if (position == itemTarget) {
                return;
            }
            LogUtil.INSTANCE.i("点击了列表Item");
            changingOverDialog.dialog_button.setVisibility(View.VISIBLE);
            changingOverDialog.askTv.setText(Objects.requireNonNull(viewModel.inForListData()).get(position).getName());
            changingOverDialog.Sure.setOnClickListener(v -> {
                if (Universal.taskQueue != null) {
                    Universal.taskQueue.clear();
                }
                Universal.taskNum = 0;
                beforePage = 0;
                Universal.progress = 0;
                MediaPlayerHelper.stop();
                binding.acceptstationTv.stopPlay();
                RobotStatus.INSTANCE.getSpeakNumber().postValue(null);
//                RobotStatus.INSTANCE.getProgress().postValue(0);
                binding.acceptstationTv.stopPlay();
                viewModel.recombine(Objects.requireNonNull(viewModel.inForListData()).get(position).getName(), pointArray);
                changingOverDialog.dismiss();
            });
            changingOverDialog.No.setOnClickListener(v -> changingOverDialog.dialog_button.setVisibility(View.GONE));
        });
        changingOverDialog.returnImg.setOnClickListener(v1 -> {
            changingOverDialog.dismiss();
            Objects.requireNonNull(viewModel.getCountDownTimer()).resume();
            if (!array) {
                new UpdateReturn().resume();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //TODO 轮播
    public void getFilesAllName(String path) {
        try {
            File file = new File(path);
            if (file.isFile()) {
                // This is a file
                List<Advance> fileList = new ArrayList<>();
                if (BaseViewModel.checkIsImageFile(file.getPath())) {
                    fileList.add(new Advance(file.getPath(), "2")); // image
                } else {
                    fileList.add(new Advance(file.getPath(), "1")); // video
                }
                binding.pointImage.setData(fileList);
            } else if (file.isDirectory()) {
                // This is a directory
                File[] files = file.listFiles();
                if (files != null) {
                    List<Advance> fileList = new ArrayList<>();
                    for (File value : files) {
                        if (BaseViewModel.checkIsImageFile(value.getPath())) {
                            fileList.add(new Advance(value.getPath(), "2")); // image
                        } else {
                            fileList.add(new Advance(value.getPath(), "1")); // video
                        }
                    }
                    binding.pointImage.setData(fileList);

                }
            }
        }catch (Exception e){}
    }

    /**
     * @param picPlayTime    轮播时间
     * @param file           路径
     * @param type           类型： 1-图片 2-视频 6-文字 7-图片+文字
     * @param textPosition   文字x位置
     * @param fontLayout     文字方向：1-横向，2-纵向
     * @param fontContent    文字
     * @param fontBackGround 背景颜色
     * @param fontColor      文字颜色
     * @param fontSize       文字大小：1-大，2-中，3-小,
     * @param PicType        图片样式
     */
    public void layoutThis(int picPlayTime, String file, int type, int textPosition, int fontLayout, String fontContent, String fontBackGround, String fontColor, int fontSize, int PicType) {
        switch (type) {
            case 1:
            case 2:
                //读取文件
                getFilesAllName(file);
                binding.verticalTV.setVisibility(View.GONE);
                binding.horizontalTV.setVisibility(View.GONE);
                binding.pointImage.setVisibility(View.VISIBLE);
                break;
            case 6:
                binding.pointImage.setVisibility(View.GONE);
                layoutParams = (ConstraintLayout.LayoutParams) binding.verticalTV.getLayoutParams();
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
                getFilesAllName(file);
                layoutParams = (ConstraintLayout.LayoutParams) binding.verticalTV.getLayoutParams();
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

}