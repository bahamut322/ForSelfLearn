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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

import com.sendi.deliveredrobot.view.widget.FinishTaskDialog;
import com.sendi.deliveredrobot.view.widget.Order;
import com.sendi.deliveredrobot.view.widget.ProcessClickDialog;
import com.sendi.deliveredrobot.view.widget.Stat;
import com.sendi.deliveredrobot.view.widget.TaskNext;
import com.sendi.deliveredrobot.viewmodel.BaseViewModel;
import com.sendi.deliveredrobot.viewmodel.StartExplanViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author swn
 * @describe 开始智能讲解
 */
public class StartExplainFragment extends Fragment {

    FragmentStartExplantionBinding binding;
    NavController controller;
    private MAdapter mAdapter;
    private int centerToTopDistance; //RecyclerView高度的一半 ,也就是控件中间位置到顶部的距离 ，
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
    int clickCount = 0;
    private FinishTaskDialog finishTaskDialog;

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
            LogUtil.INSTANCE.i("Task: " + task);
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
        finishTaskDialog = new FinishTaskDialog(getContext());
        viewModel.downTimer();
        processClickDialog = new ProcessClickDialog(getContext());
        changingOverDialog = new ChangingOverDialog(getContext());
        init();
        binding.parentCon.setClickable(QuerySql.QueryBasic().getWhetherInterrupt());
        viewModel.mainScope();

        TaskNext.setOnChangeListener(() -> {
            if (Objects.equals(TaskNext.getToDo(), "1")) {
                changingOverDialog.dismiss();
                finishTaskDialog.dismiss();
                processClickDialog.dismiss();
            }
        });

        handler.postDelayed(() -> {
            // 延迟一秒后执行的代码
            Objects.requireNonNull(RobotStatus.INSTANCE.getSpeakContinue()).observe(getViewLifecycleOwner(), integer -> {
                if (integer == 1 && RobotStatus.INSTANCE.getSpeakNumber().getValue().substring(RobotStatus.INSTANCE.getProgress().getValue()).length()>0) {
                    BaiduTTSHelper.getInstance().stop();
                    viewModel.splitTextByPunctuation(Objects.requireNonNull(RobotStatus.INSTANCE.getSpeakNumber().getValue()).substring(RobotStatus.INSTANCE.getProgress().getValue()));
                    RobotStatus.INSTANCE.getSpeakContinue().postValue(0);
                }
            });
        }, 500);

        binding.finishBtn.setOnClickListener(v -> {
            if (isButtonClickable) {
                isButtonClickable = false;
                BaiduTTSHelper.getInstance().pause();
                MediaPlayerHelper.pause();
                finishTaskDialog.show();
                finishTaskDialog.YesExit.setOnClickListener(v1 -> {
                    beforePage = 0;
                    //返回
                    Universal.taskNum = 0;
                    Universal.progress = 0;
                    BaiduTTSHelper.getInstance().stop();
                    //删除讲解队列
                    if (Universal.taskQueue != null) {
                        Universal.taskQueue.clear();
                    }

                    MediaPlayerHelper.stop();
                    RobotStatus.INSTANCE.getSpeakNumber().postValue("");
                    binding.acceptstationTv.stopPlay();
                    processClickDialog.dismiss();
                    finishTaskDialog.dismiss();
                    viewModel.finishTask();
                });

                finishTaskDialog.NoExit.setOnClickListener(v12 -> {
                    if (clickCount % 2 != 1) {
                        BaiduTTSHelper.getInstance().resume();
                    }
                    MediaPlayerHelper.resume();
                    finishTaskDialog.dismiss();
                });

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
                Objects.requireNonNull(viewModel.getCountDownTimer()).pause();
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
                Objects.requireNonNull(viewModel.getCountDownTimer()).pause();
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
            Log.d("TAG", "onViewCreated: 点击");
            Objects.requireNonNull(viewModel.getCountDownTimer()).pause();
            processDialog();
        });
        //暂停讲解
        binding.PauseBtn.setOnClickListener(v -> {
            clickCount++;
            if (isButtonClickable) {
                isButtonClickable = false;
                if (clickCount % 2 == 1) {
                    Universal.speakIng =true;
                    // 奇数次点击，执行暂停操作
                    BaiduTTSHelper.getInstance().pause();
                    binding.PauseBtn.setText("继续讲解");
                    binding.PauseTV.setVisibility(View.VISIBLE);
                } else {
                    // 偶数次点击，执行恢复操作
                    Universal.speakIng =false;
                    BaiduTTSHelper.getInstance().resume();
                    binding.PauseTV.setVisibility(View.GONE);
                    binding.PauseBtn.setText("暂停讲解");
                }
                new Handler().postDelayed(() -> {
                    // 恢复按钮可点击状态
                    isButtonClickable = true;
                }, 1000); // 设置延迟时间，避免按钮重复点击
            }
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
            for (int i = 0; i < Objects.requireNonNull(viewModel.inForListData()).size(); i++) {
                if (Objects.equals(Objects.requireNonNull(viewModel.inForListData()).get(i).getName(), s)) {
                    LogUtil.INSTANCE.i("onClick: " + i);
                    itemTarget = i;
                    scrollToCenter(i);
                    if (Universal.lastValue == null || !Universal.lastValue.equals(s)) { // 检查新值和上一个值是否相同
                        LogUtil.INSTANCE.i("init: 执行途经播报任务");
                        viewModel.secondScreenModel(i, Objects.requireNonNull(viewModel.inForListData()));
                        explainGoingSpeak(viewModel.inForListData(), i, true);
                        beforePage = 0;
                        Universal.lastValue = s; // 更新上一个值为新值
                    }
                }
            }
        }),0);

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
            } else {
                vh.tvTopLine.setVisibility(View.VISIBLE);
            }
            holder.itemView.setEnabled(false);
            if (selectPosition == position) {
                Universal.explainArray = false;
                AnimationDrawable animationDrawable = (AnimationDrawable) vh.tvTopLine.getDrawable();
                animationDrawable.start();
                try {
                    vh.tv.setTextColor(getResources().getColor(R.color.color_49DCFA));
                    vh.tvDot.setBackgroundResource(R.drawable.lline_dot_normal);

                    RobotStatus.INSTANCE.getPointItem().postValue(position);
                    LogUtil.INSTANCE.d("当前讲解点开始");
                    DialogHelper.loadingDialog.dismiss();
                    //当前点显示的文字&图片
                    binding.goingName.setText(viewModel.inForListData().get(position).getName());
                    Log.e("TAG", "onBindViewLandholder: " + QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type());
                    if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type() == 4) {
                        binding.argPic.setVisibility(View.VISIBLE);
                        Glide.with(requireActivity()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_walkPic()).placeholder(R.drawable.ic_warming).into(binding.argPic);
                        //touch_walkPic
                    } else {
                        binding.argPic.setVisibility(View.GONE);
                        binding.parentCon.setBackgroundResource(0);
                        binding.goingLin.setVisibility(View.VISIBLE);
                        binding.contentCL.setVisibility(View.GONE);
                        if (viewModel.inForListData().get(position).getTouch_imagefile() != null) {
                            //带光晕的背景图
                            Glide.with(requireActivity()).load(BaseViewModel.getFilesAllName(Objects.requireNonNull(viewModel.inForListData()).get(position).getTouch_imagefile()).get(0)).into(binding.startImg);//轮播
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
                            ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.FinishChannelBroadcast, new UpdateReturn().taskDto());
                        } catch (Exception ignored) {
                        }
                        Log.d("TAG", "onBindViewHolder: 任务完成");
                    }


                    if (Objects.requireNonNull(viewModel.inForListData()).get(position).getWalktext() != null && !Objects.requireNonNull(viewModel.inForListData()).get(position).getWalktext().isEmpty()) {
                        LogUtil.INSTANCE.i("途径播报内容：" + viewModel.inForListData().get(position).getWalktext());

                        RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer1 -> RobotStatus.INSTANCE.getProgress().observe(getViewLifecycleOwner(), integer -> {
                            Log.d("TAG", "onBindViewHolder111: "+integer+","+Universal.ExplainLength);
                            if (integer1 == 1  && !Universal.explainArray && integer != Universal.ExplainLength){
                                binding.PauseBtn.setVisibility(View.GONE);
                                mHandler.post(() -> {
                                    if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type() == 4) {
                                        binding.argPic.setVisibility(View.VISIBLE);
                                        Glide.with(requireActivity()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_arrivePic()).placeholder(R.drawable.ic_warming).into(binding.argPic);
                                    } else {
                                        binding.argPic.setVisibility(View.GONE);
                                        try {
                                            getFilesAllName(BaseViewModel.getFilesAllName(Objects.requireNonNull(viewModel.inForListData()).get(position).getTouch_imagefile()).get(0),2);//轮播
                                        } catch (Exception ignored) {
                                        }
                                        try {
                                            layoutThis(Objects.requireNonNull(viewModel.inForListData()).get(position).getTouch_picplaytime(), viewModel.inForListData().get(position).getTouch_imagefile(), viewModel.inForListData().get(position).getTouch_type(), viewModel.inForListData().get(position).getTouch_textposition(), viewModel.inForListData().get(position).getTouch_fontlayout(), viewModel.inForListData().get(position).getTouch_fontcontent().toString(), viewModel.inForListData().get(position).getTouch_fontbackground(), viewModel.inForListData().get(position).getTouch_fontcolor().toString(), (int) viewModel.inForListData().get(position).getTouch_fontsize(), viewModel.inForListData().get(position).getTouch_pictype());
                                        } catch (Exception ignored) {
                                        }
                                    }
                                    binding.parentCon.setClickable(false);
                                    binding.contentCL.setVisibility(View.VISIBLE);
                                    binding.parentCon.setBackgroundResource(R.drawable.bg);
                                    binding.statusTv.setText("已到达:");
                                    binding.nowExplanation.setText(Objects.requireNonNull(viewModel.inForListData()).get(position).getName());
                                });
                            }
                            if (integer == Universal.ExplainLength){
                                //恢复视频声音
                                Order.setFlage("0");
                            }
                            if (integer1 == 1 && Universal.taskQueue.isCompleted() && integer== Universal.ExplainLength && !Universal.explainArray) {
                                LogUtil.INSTANCE.i("途径播报结束（文本）");
                                arrayToDo(viewModel.inForListData(), position);
                            }
                        }));
                    }
                    if (viewModel.inForListData().get(position).getWalkvoice() != null && !viewModel.inForListData().get(position).getWalkvoice().isEmpty()) {
                        RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer -> {
                            if (integer == 1) {
                                LogUtil.INSTANCE.i("讲解->到点");
                                currentPosition1.observe(getViewLifecycleOwner(), integer1 -> totalDuration1.observe(getViewLifecycleOwner(), integer2 -> {
                                    if ((integer2 - integer1) > 150 && !array) {
                                        mHandler.post(() -> {
                                            if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type() == 4) {
                                                binding.argPic.setVisibility(View.VISIBLE);
                                                Glide.with(requireActivity()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_arrivePic()).placeholder(R.drawable.ic_warming).into(binding.argPic);
                                            } else {
                                                binding.argPic.setVisibility(View.GONE);
                                                try {
                                                    getFilesAllName(BaseViewModel.getFilesAllName(Objects.requireNonNull(viewModel.inForListData()).get(position).getTouch_imagefile()).get(0),2);//轮播
                                                } catch (Exception ignored) {
                                                }
                                                try {
                                                    layoutThis(Objects.requireNonNull(viewModel.inForListData()).get(position).getTouch_picplaytime(), viewModel.inForListData().get(position).getTouch_imagefile(), viewModel.inForListData().get(position).getTouch_type(), viewModel.inForListData().get(position).getTouch_textposition(), viewModel.inForListData().get(position).getTouch_fontlayout(), viewModel.inForListData().get(position).getTouch_fontcontent().toString(), viewModel.inForListData().get(position).getTouch_fontbackground(), viewModel.inForListData().get(position).getTouch_fontcolor().toString(), (int) viewModel.inForListData().get(position).getTouch_fontsize(), viewModel.inForListData().get(position).getTouch_pictype());
                                                } catch (Exception ignored) {
                                                }
                                            }
                                            binding.parentCon.setClickable(false);
                                            binding.contentLin.setVisibility(View.GONE);
                                            binding.contentCL.setVisibility(View.VISIBLE);
                                            binding.parentCon.setBackgroundResource(R.drawable.bg);
                                            binding.statusTv.setText("已到达:");
                                            binding.nowExplanation.setText(Objects.requireNonNull(viewModel.inForListData()).get(position).getName());
                                        });
                                    } else if ((integer2 - integer1) <= 150 && !array) {
                                        LogUtil.INSTANCE.i("到点并且播放完成");
                                        mHandler.post(() -> {
                                            MediaPlayerHelper.stop();
                                            try {
                                                ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.FinishChannelBroadcast, new UpdateReturn().taskDto());
                                            } catch (Exception ignored) {
                                            }
                                            arrayToDo(viewModel.inForListData(), position);
                                        });
                                    }
                                }));
                            }
                        });
                    }
                    if (viewModel.inForListData().get(position).getWalkvoice() == null && Objects.requireNonNull(viewModel.inForListData()).get(position).getWalktext() == null) {
                        RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer1 -> {
                            if (integer1 == 1) {
                                LogUtil.INSTANCE.i("到点播报结束（MP3）");
                                arrayToDo(viewModel.inForListData(), position);
                            }
                        });
                    }
                } catch (Exception ignored) {
                }
            } else {
                vh.tv.setTextColor(getResources().getColor(R.color.white));
                vh.tvDot.setBackgroundResource(R.drawable.lline_dot_first);
            }
            vh.tv.setText(Objects.requireNonNull(viewModel.inForListData()).get(position).getName());

        }

        private int selectPosition = -1;

        @SuppressLint("NotifyDataSetChanged")
        public void setSelectPosition(int cposition) {
            selectPosition = cposition;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return Objects.requireNonNull(viewModel.inForListData()).size();
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


    private void explainGoingSpeak(ArrayList<MyResultModel> mDatas, int position, boolean tr) {
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
                                ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.StartChannelBroadcast, new UpdateReturn().taskDto());
                            } catch (Exception ignored) {
                            }
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            Log.d("TAG", "doInBackground: 开始播报");
                            // 在后台线程中执行耗时操作，例如数据预加载
                            viewModel.splitTextByPunctuation(mDatas.get(position).getWalktext());
//                        splitString(mDatas.get(position).getWalktext(), Universal.BaiduSpeakLength);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            RobotStatus.INSTANCE.getSpeakNumber().setValue(mDatas.get(position).getWalktext());
                        }
                    }

                    Runnable runnable = () -> {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        MyAsyncTask task = new MyAsyncTask();
                        task.execute();
                    };
                    handler.postDelayed(runnable, 1);


                }
            }
            //如果有mp3
            if (mDatas.get(position).getWalkvoice() != null && !mDatas.get(position).getWalkvoice().isEmpty()) {
                if (tr) {
                    if (Universal.taskQueue != null) {
                        Universal.taskQueue.clear();
                    }
                    try {
                        ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.StartChannelBroadcast, new UpdateReturn().taskDto());
                    } catch (Exception ignored) {
                    }

                    handler.postDelayed(() -> MediaPlayerHelper.play(mDatas.get(position).getWalkvoice(), "1"), 3000);

                }
            }
        } catch (Exception ignored) {
        }
    }

    private void arrayToDo(ArrayList<MyResultModel> mData, int position) {
        try {
            Universal.explainArray = true;
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
                if (BillManager.INSTANCE.billList().size() == 1) {
                    binding.nextTaskBtn.setVisibility(View.GONE);
                } else {
                    binding.nextTaskBtn.setVisibility(View.VISIBLE);
                }
                binding.statusTv.setText("正在讲解:");
                binding.nowExplanation.setText(mData.get(position).getName());

                if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type() == 4) {
                    binding.finishBtn.setEnabled(false);
                    binding.ChangingOver.setEnabled(false);
                    binding.nextTaskBtn.setEnabled(false);
                    binding.argPic.setVisibility(View.VISIBLE);
                    Glide.with(requireActivity()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_arrivePic()).placeholder(R.drawable.ic_warming).into(binding.argPic);
                } else {
                    binding.finishBtn.setEnabled(true);
                    binding.ChangingOver.setEnabled(true);
                    binding.nextTaskBtn.setEnabled(true);
                    binding.argPic.setVisibility(View.GONE);
                    try {
                        getFilesAllName(mData.get(position).getTouch_imagefile(),mData.get(position).getTouch_pictype());//轮播
                    } catch (Exception ignored) {
                    }
                    try {
                        layoutThis(mData.get(position).getTouch_picplaytime(), mData.get(position).getTouch_imagefile(), mData.get(position).getTouch_type(), mData.get(position).getTouch_textposition(), mData.get(position).getTouch_fontlayout(), mData.get(position).getTouch_fontcontent().toString(), mData.get(position).getTouch_fontbackground(), mData.get(position).getTouch_fontcolor().toString(), (int) mData.get(position).getTouch_fontsize(), mData.get(position).getTouch_pictype());
                    } catch (Exception ignored) {
                    }

                }
                //这段你别问我哈，公司没钱。用短语音识别去读长语音，还要过渡显示，更过分的还要翻页诶。我也没办法，只能这样做，我也看不懂了。
                if (mData.get(position).getExplanationtext() != null && !mData.get(position).getExplanationtext().isEmpty()) {
                    binding.PauseBtn.setVisibility(View.VISIBLE);
                    try {
                        ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.StartArrayBroadcast, new UpdateReturn().taskDto());
                    } catch (Exception ignored) {
                    }
                    binding.pointName.setText(mData.get(position).getName());
                    LogUtil.INSTANCE.i("到点讲解文字:" + mData.get(position).getExplanationtext());

                    List<String> textEqually = viewModel.splitString(mData.get(position).getExplanationtext(), 135);
                    //页数（AtomicInteger为了线程安全咯）
                    AtomicInteger page = new AtomicInteger(textEqually.size());
//                Log.d("TAG", "内容列表长度: " + page + "当前内容：" + textEqually.get(beforePage));
                    //显示第一页
                    binding.acceptstationTv.setText(textEqually.get(beforePage));
                    LogUtil.INSTANCE.i("当前页数: " + beforePage);
                    //将第一页的内容再次等分成BaiduTTS可以朗读的范围
//                Universal.taskNum = viewModel.splitString(textEqually.get(beforePage), Universal.BaiduSpeakLength).size();
                    viewModel.splitTextByPunctuation(mData.get(position).getExplanationtext());

                    RobotStatus.INSTANCE.getProgress().observe(getViewLifecycleOwner(), integer -> {
                        if (nextTaskToDo) {
                            LogUtil.INSTANCE.i("当前进度: " + (integer - ((beforePage) * 135)));
                            binding.acceptstationTv.startPlayLine(
                                    (integer - ((beforePage) * 135)),
                                    textEqually.get(beforePage).length(),
                                    ((long) QuerySql.QueryBasic().getSpeechSpeed() * textEqually.get(beforePage).length()) * 1000);
                        }
                        if (integer >= ((beforePage + 1) * 135) && integer != 0) {
                            Log.i("TAG", "页数, progress：" + beforePage + ";进度:" + integer + "当前进度：" + Universal.progress);
                            nextTaskToDo = false;
                            if (beforePage <= page.get() - 1) {
                                RobotStatus.INSTANCE.getProgress().postValue(0);
                                beforePage++;
                                binding.acceptstationTv.setText(textEqually.get(beforePage));
                                nextTaskToDo = true;
                            }
                        }
                        if (beforePage == (page.get() - 1) && TaskQueues.isCompleted() && integer == Universal.ExplainLength) {
                            LogUtil.INSTANCE.d("Tips: 讲解内容完成,进入倒计时");
                            ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.FinishArrayBroadcast, new UpdateReturn().taskDto());
                            binding.acceptstationTv.stopPlay();
                            Objects.requireNonNull(viewModel.getCountDownTimer()).startCountDown();
                            if (Universal.taskQueue != null) {
                                Universal.taskQueue.clear();
                            }
                            //恢复视频声音
                            Order.setFlage("0");
                            beforePage = 0;
                            page.set(0);
                            Universal.progress = 0;
                            RobotStatus.INSTANCE.getSpeakContinue().postValue(0);
                        }

                    });
                } else if (!mData.get(position).getExplanationvoice().isEmpty()) {
                    binding.contentLin.setVisibility(View.GONE);
                    binding.PauseBtn.setVisibility(View.GONE);
                    try {
                        ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.StartArrayBroadcast, new UpdateReturn().taskDto());
                    } catch (Exception ignored) {
                    }
                    LogUtil.INSTANCE.i("到点讲解音频路径:" + mData.get(position).getExplanationvoice());
                    MediaPlayerHelper.play(mData.get(position).getExplanationvoice(), "1");
                    MediaPlayerHelper.setOnProgressListener((currentPosition, totalDuration) -> {
                        LogUtil.INSTANCE.i("到点音频播放：currentPosition: " + currentPosition + ",totalDuration: " + totalDuration);
                        if ((totalDuration - currentPosition) <= 150) {
                            try {
                                MediaPlayerHelper.stop();
                                ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.FinishArrayBroadcast, new UpdateReturn().taskDto());
                            } catch (Exception ignored) {
                            }
                            Objects.requireNonNull(viewModel.getCountDownTimer()).startCountDown();
                        }
                    });
                } else if (mData.get(position).getExplanationvoice().isEmpty() && mData.get(position).getExplanationtext().isEmpty()) {
                    binding.PauseBtn.setVisibility(View.GONE);
                    Objects.requireNonNull(viewModel.getCountDownTimer()).startCountDown();
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void processDialog() {
        processClickDialog.show();
        processClickDialog.finishBtn.setOnClickListener(v -> {
            finishTaskDialog.show();
            finishTaskDialog.YesExit.setOnClickListener(v12 -> {
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
                finishTaskDialog.dismiss();
            });
            finishTaskDialog.NoExit.setOnClickListener(v1 -> finishTaskDialog.dismiss());

        });
        processClickDialog.nextBtn.setOnClickListener(v -> {
            //删除讲解队列
//            Universal.Model = "切换下一个点";
            if (Universal.taskQueue != null) {
                Universal.taskQueue.clear();
            }
            beforePage = 0;
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
        //暂停页面
        BaiduTTSHelper.getInstance().pause();
        changingOverDialog.show();
        if (!array) {
            new UpdateReturn().pause();
        }
        ChangePointGridViewAdapter adapter = new ChangePointGridViewAdapter(requireActivity(), viewModel.inForListData(), targetName);
        changingOverDialog.pointGV.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        changingOverDialog.pointGV.setOnItemClickListener((parent, view, position, id) -> {
            if (position == itemTarget) {
                return;
            }
            changingOverDialog.dialog_button.setVisibility(View.VISIBLE);
            changingOverDialog.askTv.setText(Objects.requireNonNull(viewModel.inForListData()).get(position).getName());
            changingOverDialog.Sure.setOnClickListener(v -> {
                if (Universal.taskQueue != null) {
                    Universal.taskQueue.clear();
                }
                BaiduTTSHelper.getInstance().stop();
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
            if (clickCount % 2 != 1) {
                BaiduTTSHelper.getInstance().resume();
            }
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
    public void getFilesAllName(String path,int PicType) {
        try {
            File file = new File(path);
            if (file.isFile()) {
                // This is a file
                List<Advance> fileList = new ArrayList<>();
                if (BaseViewModel.checkIsImageFile(file.getPath())) {
                    fileList.add(new Advance(file.getPath(), "2",PicType)); // image
                } else {
                    fileList.add(new Advance(file.getPath(), "1",1)); // video
                }
                binding.pointImage.setData(fileList);
            } else if (file.isDirectory()) {
                // This is a directory
                File[] files = file.listFiles();
                if (files != null) {
                    List<Advance> fileList = new ArrayList<>();
                    for (File value : files) {
                        if (BaseViewModel.checkIsImageFile(value.getPath())) {
                            fileList.add(new Advance(value.getPath(), "2",PicType)); // image
                        } else {
                            fileList.add(new Advance(value.getPath(), "1",1)); // video
                        }
                    }
                    binding.pointImage.setData(fileList);

                }
            }
        } catch (Exception ignored) {
        }
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
                getFilesAllName(file,PicType);
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
                getFilesAllName(file,PicType);
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