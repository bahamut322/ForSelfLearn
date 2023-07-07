package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.nfc.Tag;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.adapter.ChangePointGridViewAdapter;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.databinding.FragmentStartExplantionBinding;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.AudioMngHelper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private ArrayList<MyResultModel> mDatas = new ArrayList<>();
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
                BaiduTTSHelper.getInstance().speaks(task, "explantion");
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
        mDatas = new ArrayList<>();
        mDatas = viewModel.inForListData();
        RobotStatus.INSTANCE.getSpeakContinue().observe(getViewLifecycleOwner(), integer -> {
            if (integer == 1) {
                Log.d("", "onViewCreated: 讲解打断，重新计算");
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
            binding.acceptstationTv.stopPlay();
            processClickDialog.dismiss();
        });
        binding.nextTaskBtn.setOnClickListener(v -> {
            if (Universal.taskQueue != null) {
                Universal.taskQueue.clear();
            }
            viewModel.getCountDownTimer().pause();
            binding.acceptstationTv.stopPlay();
            viewModel.nextTask(true);
            processClickDialog.dismiss();
        });

        binding.ChangingOver.setOnClickListener(v -> {
            viewModel.getCountDownTimer().pause();
            changeDialog(true);
            pointArray = true;
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
                centerToTopDistance = binding.pointList.getHeight() / 2;
                findView();
            }
        });

        binding.pointList.postDelayed(() -> RobotStatus.INSTANCE.getTargetName().observe(getViewLifecycleOwner(), s -> {
            DialogHelper.loadingDialog.show();
            targetName = s;
            Log.d("TAG", "当前讲解点: " + s);
            // 在这里处理新值，比如更新UI或执行其他逻辑
            for (int i = 0; i < mDatas.size(); i++) {
                if (Objects.equals(mDatas.get(i).getName(), s)) {
                    Log.d("TAG", "onClick: " + i);
                    itemTarget = i;
                    scrollToCenter(i);
                    viewModel.secondScreenModel(i, mDatas);
                    if (Universal.lastValue == null || !Universal.lastValue.equals(s)) { // 检查新值和上一个值是否相同
                        explanGonningSpeak(mDatas, i, true);
                        beforePage = 0;
                        Universal.lastValue = s; // 更新上一个值为新值
                    }
                }
            }

        }), 500);

    }

    private boolean isTouch = false;

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
                        //获取最中间的Item View
                        int centerPositionDiffer = (la - fi) / 2;
                        int centerChildViewPosition = fi + centerPositionDiffer; //获取当前所有条目中中间的一个条目索引
                        centerViewItems.clear();
                        //遍历循环，获取到和中线相差最小的条目索引(精准查找最居中的条目)
                        if (centerChildViewPosition != 0) {
                            for (int i = centerChildViewPosition - 1; i < centerChildViewPosition + 2; i++) {
                                View cView = recyclerView.getLayoutManager().findViewByPosition(i);
                                int viewTop = cView.getTop() + (cView.getHeight() / 2);
                                centerViewItems.add(new CenterItemUtils.CenterViewItem(i, Math.abs(centerToTopDistance - viewTop)));
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

        binding.pointList.setOnTouchListener((view, motionEvent) -> {
            isTouch = true;
            return false;
        });
    }

    private final DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

    /**
     * 移动指定索引到中心处 ， 只可以移动可见区域的内容
     */
    private void scrollToCenter(int position) {
        position = Math.max(position, childViewHalfCount);
        position = Math.min(position, mAdapter.getItemCount() - childViewHalfCount - 1);

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.pointList.getLayoutManager();
        View childView = linearLayoutManager.findViewByPosition(position);
        Log.i("ccb", "滑动后中间View的索引: " + position);
        //把当前View移动到居中位置
        if (childView == null) return;
        int childVhalf = childView.getHeight() / 2;
        int childViewTop = childView.getTop();
        int viewCTop = centerToTopDistance;
        int smoothDistance = childViewTop - viewCTop + childVhalf;
        Log.i("ccb", "\n居中位置距离顶部距离: " + viewCTop
                + "\n当前居中控件距离顶部距离: " + childViewTop
                + "\n当前居中控件的一半高度: " + childVhalf
                + "\n滑动后再次移动距离: " + smoothDistance);
        binding.pointList.smoothScrollBy(0, smoothDistance, decelerateInterpolator);
        mAdapter.setSelectPosition(position);
        explantionNameModel.setName(mDatas.get(position).getName());
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
                LogUtil.INSTANCE.d("当前讲解点开始");
                DialogHelper.loadingDialog.dismiss();
                //当前点显示的文字&图片
                if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type() == 4) {
                    binding.argPic.setVisibility(View.VISIBLE);
                    Glide.with(getContext()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_walkPic()).into(binding.argPic);
                    //touch_walkPic
                } else {
                    vh.tv.setTextColor(getResources().getColor(R.color.color_49DCFA));
                    vh.tvDot.setBackgroundResource(R.drawable.lline_dot_normal);
                    binding.argPic.setVisibility(View.GONE);
                    binding.parentCon.setBackgroundResource(0);
                    binding.goingLin.setVisibility(View.VISIBLE);
                    binding.goingName.setText(mDatas.get(position).getName());
                    binding.parentCon.setClickable(QuerySql.QueryBasic().getWhetherInterrupt());
                    binding.contentCL.setVisibility(View.GONE);

                    MediaPlayerHelper.setOnProgressListener((currentPosition, totalDuration) -> {
                        LogUtil.INSTANCE.i("currentPosition : " + currentPosition + " totalDuration :" + totalDuration);
                        if (array == false) {
                            currentPosition1.postValue(currentPosition);
                            totalDuration1.postValue(totalDuration);
                        }
                    });

                    if (mDatas.get(position).getTouch_imagefile() !=null) {
                        //带光晕的背景图
                        Glide.with(getContext()).load(BaseViewModel.getFilesAllName(mDatas.get(position).getTouch_imagefile()).get(0)).into(binding.startImg);//轮播
                    }
                }
                if (Universal.taskQueue.isTaskQueueCompleted()) {
                    //要执行的任务代码
                    ReportDataHelper.INSTANCE.reportTaskDto(BillManager.INSTANCE.currentBill().currentTask().taskModel(), TaskStageEnum.FinishChannelBroadcast, new UpdateReturn().taskDto());
                    Log.d("TAG", "onBindViewHolder: 任务完成");
                }
                //帧动画
                vh.tvTopLine.setImageResource(R.drawable.anim_login_start_loading);
                AnimationDrawable animationDrawable = (AnimationDrawable) vh.tvTopLine.getDrawable();
                animationDrawable.start();
                if (mDatas.get(position).getWalktext() != null && !mDatas.get(position).getWalktext().isEmpty()) {
                    Log.d("TAG", "途径播报内容：" + mDatas.get(position).getWalktext());
                    RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer1 -> {
                        if (integer1 == 1 && Universal.taskQueue.isCompleted()) {
                            Log.d("TAG", "是否结束");
                            arrayToDo(mDatas, position);

                        }
                    });
                }
                if (mDatas.get(position).getWalkvoice() != null && !mDatas.get(position).getWalkvoice().isEmpty()) {
                    RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer -> {
                        if (integer == 1) {
                            LogUtil.INSTANCE.i("到点");
                            currentPosition1.observe(getViewLifecycleOwner(), integer1 -> {
                                totalDuration1.observe(getViewLifecycleOwner(), integer2 -> {
                                    if (integer1 < integer2 && array == false) {
                                        mHandler.post(() -> {
                                            if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type() == 4) {
                                                binding.argPic.setVisibility(View.VISIBLE);
                                                Glide.with(getContext()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_arrivePic()).into(binding.argPic);
                                            }
                                            binding.parentCon.setClickable(false);
                                            binding.contentLin.setVisibility(View.GONE);
                                            binding.contentCL.setVisibility(View.VISIBLE);
                                            LogUtil.INSTANCE.i("到点并且播放完成1");
                                            binding.parentCon.setBackgroundResource(R.drawable.bg);
                                            binding.statusTv.setText("已到达:");
                                            binding.nowExplanation.setText(mDatas.get(position).getName());
                                            getFilesAllName(mDatas.get(position).getTouch_imagefile());//轮播
                                        });
                                    } else if (integer1 >= integer2 && array == false){
                                        LogUtil.INSTANCE.i("到点并且播放完成");
                                        MediaPlayerHelper.stop();
                                        ReportDataHelper.INSTANCE.reportTaskDto(BillManager.INSTANCE.currentBill().currentTask().taskModel(), TaskStageEnum.FinishChannelBroadcast, new UpdateReturn().taskDto());
                                        arrayToDo(mDatas, position);
                                    }
                                });
                            });
                        }
                    });
                }

                RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer -> {
                    if (integer == 1) {
                        if (mDatas.get(position).getWalktext() == null && mDatas.get(position).getWalkvoice() == null) {
                            Log.d("TAG", "onBindViewHolder: 无内容到达");
                            arrayToDo(mDatas, position);
                        }
                    }
                });

            } else {
                vh.tv.setTextColor(getResources().getColor(R.color.white));
                vh.tvDot.setBackgroundResource(R.drawable.lline_dot_first);
            }
            vh.tv.setText(mDatas.get(position).getName());

        }

        private int selectPosition = -1;

        @SuppressLint("NotifyDataSetChanged")
        public void setSelectPosition(int cposition) {
            selectPosition = cposition;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
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
    }

    private void explanGonningSpeak(ArrayList<MyResultModel> mDatas, int position, boolean tr) {
        array = false;
        Log.d("TAG", "开始途径播报");
        //有讲解内容
        if (mDatas.get(position).getWalktext() != null && !mDatas.get(position).getWalktext().isEmpty()) {
            if (tr) {
                class MyAsyncTask extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected void onPreExecute() {
                        // 在执行后台任务之前执行，通常用于初始化操作
                        Thread thread = new Thread(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                splitString(mDatas.get(position).getWalktext(), Universal.BaiduSpeakLength);
                            }
                        });
                        thread.start();

                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        // 在后台线程中执行耗时操作，例如数据预加载
                        ReportDataHelper.INSTANCE.reportTaskDto(BillManager.INSTANCE.currentBill().currentTask().taskModel(), TaskStageEnum.StartChannelBroadcast, new UpdateReturn().taskDto());

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        RobotStatus.INSTANCE.getSpeakNumber().postValue(mDatas.get(position).getWalktext());
                    }
                }
                MyAsyncTask task = new MyAsyncTask();
                task.execute();
            }
        }
        //如果有mp3
        if (mDatas.get(position).getWalkvoice() != null && !mDatas.get(position).getWalkvoice().isEmpty()) {
            if (tr) {
                if (Universal.taskQueue != null) {
                    Universal.taskQueue.clear();
                }
                ReportDataHelper.INSTANCE.reportTaskDto(BillManager.INSTANCE.currentBill().currentTask().taskModel(), TaskStageEnum.StartChannelBroadcast, new UpdateReturn().taskDto());

                MediaPlayerHelper.play(mDatas.get(position).getWalkvoice(), "1");
            }
        }
    }

    private void arrayToDo(ArrayList<MyResultModel> mDatas, int position) {
        array = true;
        LogUtil.INSTANCE.d("到点讲解：" + Universal.selectMapPoint);
        if (Universal.selectMapPoint) {
            return;
        }
        nextTaskToDo = true;
        currentPosition1.postValue(0);
        totalDuration1.postValue(0);
        requireActivity().runOnUiThread(() -> {
            // 在执行后台任务之前执行，通常用于初始化操作
            binding.parentCon.setClickable(false);
            binding.contentLin.setVisibility(View.VISIBLE);
            binding.contentCL.setVisibility(View.VISIBLE);
            binding.parentCon.setBackgroundResource(R.drawable.bg);
            binding.statusTv.setText("正在讲解:");
            getFilesAllName(mDatas.get(position).getTouch_imagefile());//轮播
            binding.nowExplanation.setText(mDatas.get(position).getName());

            if (QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_type() == 4) {
                binding.argPic.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(QuerySql.queryPointDate(RobotStatus.INSTANCE.getSelectRoutMapItem().getValue()).get(position).getTouch_arrivePic()).into(binding.argPic);
            }
            if (!mDatas.get(position).getExplanationtext().isEmpty()) {
                ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.StartArrayBroadcast, new UpdateReturn().taskDto());
                binding.pointName.setText(mDatas.get(position).getName());
                LogUtil.INSTANCE.i("到点讲解文字:" + mDatas.get(position).getExplanationtext());
            }
            if (!mDatas.get(position).getExplanationtext().isEmpty()) {
                List<String> textEqually = viewModel.splitString(mDatas.get(position).getExplanationtext(), 135);
                //页数
                int page = textEqually.size();
                Log.d("TAG", "内容列表长度: " + page + "当前内容：" + textEqually.get(beforePage));
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
                    } else if (integer > textEqually.get(beforePage).length()) {
                        Universal.progress = 0;
                    }
                    Log.d("TAG", "onPostExecute: " + textEqually.get(beforePage).length());
                    if (integer == textEqually.get(beforePage).length() && TaskQueues.isCompleted() && integer != 0) {
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
                        } else if (beforePage > page - 1 && TaskQueues.isCompleted()) {
                            ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.FinishArrayBroadcast, new UpdateReturn().taskDto());
                            Log.d("TAG", "arrayToDo: 进入倒计时");
                            binding.acceptstationTv.stopPlay();
                            RobotStatus.INSTANCE.getProgress().postValue(0);
                            viewModel.getCountDownTimer().start();
                            if (Universal.taskQueue != null) {
                                Universal.taskQueue.clear();
                            }
                            beforePage = 0;
                            Universal.progress = 0;
                        }
                    }
                });
            } else if (!mDatas.get(position).getExplanationvoice().isEmpty()) {
                ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.StartArrayBroadcast, new UpdateReturn().taskDto());
                LogUtil.INSTANCE.i("到点讲解音频路径:" + mDatas.get(position).getExplanationvoice());
                MediaPlayerHelper.play(mDatas.get(position).getExplanationvoice(), "1");
                MediaPlayerHelper.setOnProgressListener((currentPosition, totalDuration) -> {
                    if (currentPosition >= totalDuration) {
                        viewModel.getCountDownTimer().start();
                        ReportDataHelper.INSTANCE.reportTaskDto(Objects.requireNonNull(Objects.requireNonNull(BillManager.INSTANCE.currentBill()).currentTask()).taskModel(), TaskStageEnum.FinishArrayBroadcast, new UpdateReturn().taskDto());
                    }
                });
            } else if (mDatas.get(position).getExplanationvoice().isEmpty() && mDatas.get(position).getExplanationtext().isEmpty()) {
                viewModel.getCountDownTimer().start();
            }
        });
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
    }
}