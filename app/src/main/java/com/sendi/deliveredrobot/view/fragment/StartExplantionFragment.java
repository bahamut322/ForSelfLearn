package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
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
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.adapter.ChangePointGridViewAdapter;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.databinding.FragmentStartExplantionBinding;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper;
import com.sendi.deliveredrobot.model.ExplantionNameModel;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.navigationtask.BillManager;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.navigationtask.TaskQueues;
import com.sendi.deliveredrobot.service.UpdateReturn;
import com.sendi.deliveredrobot.utils.CenterItemUtils;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.view.widget.ChangingOverDialog;

import com.sendi.deliveredrobot.view.widget.ProcessClickDialog;
import com.sendi.deliveredrobot.view.widget.Stat;
import com.sendi.deliveredrobot.viewmodel.StartExplanViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author swn
 * @describe 开始智能讲解
 */
public class StartExplantionFragment extends Fragment {

    FragmentStartExplantionBinding binding;
    NavController controller;
    private MAdapter mAdapter;
    private int centerToTopDistance; //RecyclerView高度的一半 ,也就是控件中间位置到顶部的距离 ，
    private final int childViewHalfCount = 0; //当前RecyclerView一半最多可以存在几个Item
    private StartExplanViewModel viewModel;
    private ArrayList<MyResultModel> mDatas = new ArrayList<>();
    private TaskQueues<String> taskQueue;
    private ProcessClickDialog processClickDialog;
    private ChangingOverDialog changingOverDialog;
    private int beforePage;
    boolean nextTaskToDo = true;

    String targetName = "";

    private ExplantionNameModel explantionNameModel;
    private int itemTarget;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        LogUtil.INSTANCE.d("onAttach页面准备进入");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.INSTANCE.d("onCreate页面进入");
        explantionNameModel = new ExplantionNameModel();
        viewModel = new ViewModelProvider(this).get(StartExplanViewModel.class);
        Consumer<String> taskConsumer = task -> {
            // 执行任务的代码
            if (BuildConfig.IS_SPEAK) {
                BaiduTTSHelper.getInstance().speaks(task, "explantion");
            }
            System.out.println("Task: " + task);
        };
        // 创建TaskQueue实例
        taskQueue = new TaskQueues<>(taskConsumer);
        // 创建任务Consumer
        mDatas = new ArrayList<>();
        mDatas = viewModel.inForListData();
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
//                taskQueue.pause();
                MediaPlayerHelper.pause();
                BaiduTTSHelper.getInstance().pause();
            } else if (Stat.getFlage() == 3) {
                //继续
//                taskQueue.resume();
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
        controller = Navigation.findNavController(view);
        nextTaskToDo = true;
        LogUtil.INSTANCE.d("onViewCreated进入讲解页面");
        status();
        RobotStatus.INSTANCE.getSpeakContinue().observe(getViewLifecycleOwner(), integer -> {
            if (integer == 1) {
                Log.d("", "onViewCreated: 讲解打断，重新计算");
                taskQueue.clear();
                BaiduTTSHelper.getInstance().stop();
                splitString(RobotStatus.INSTANCE.getSpeakNumber().getValue(), 45);
                RobotStatus.INSTANCE.getSpeakContinue().postValue(0);
            }
        });
        viewModel.downTimer();
        processClickDialog = new ProcessClickDialog(getContext());
        changingOverDialog = new ChangingOverDialog(getContext());
        init();
        viewModel.mainScope();
        binding.finishBtn.setOnClickListener(v -> {
            //返回
            BaiduTTSHelper.getInstance().stop();
            Universal.taskNum = 0;
            Universal.progress = 0;
            //删除讲解队列
            taskQueue.clear();
            viewModel.finishTask();
            binding.acceptstationTv.stopPlay();
            processClickDialog.dismiss();
        });
        binding.nextTaskBtn.setOnClickListener(v -> {
            BaiduTTSHelper.getInstance().stop();
            binding.acceptstationTv.stopPlay();
            taskQueue.clear();
            viewModel.nextTask(true);
            processClickDialog.dismiss();
        });

        binding.ChangingOver.setOnClickListener(v -> {
            changeDialog(true);
        });
        binding.parentCon.setOnClickListener(v -> {
            processDialog();
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start_explantion, container, false);
        binding = DataBindingUtil.bind(view);
        return view;
    }

    @SuppressLint("SuspiciousIndentation")
    private void init() {
        binding.pointList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.pointList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.pointList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                centerToTopDistance = binding.pointList.getHeight() / 2;
                findView();
            }
        });

        binding.pointList.postDelayed(() -> RobotStatus.INSTANCE.getTargetName().observe(getViewLifecycleOwner(), s -> {
            targetName = s;
            Log.d("TAG", "当前讲解点: " + s);
            // 在这里处理新值，比如更新UI或执行其他逻辑
            for (int i = 0; i < mDatas.size(); i++) {
                if (Objects.equals(mDatas.get(i).getName(), s)) {
                    Log.d("TAG", "onClick: " + i);
                    itemTarget = i;
                    scrollToCenter(i);
                    explanGonningSpeak(mDatas, i,false);
                    viewModel.secondScreenModel(i, mDatas);
                    if (Universal.lastValue == null || !Universal.lastValue.equals(s)) { // 检查新值和上一个值是否相同
                        explanGonningSpeak(mDatas, i,true);
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
        position = position < childViewHalfCount ? childViewHalfCount : position;
        position = position < mAdapter.getItemCount() - childViewHalfCount - 1 ? position : mAdapter.getItemCount() - childViewHalfCount - 1;

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
            if (selectPosition == position) {
                LogUtil.INSTANCE.d("当前讲解点开始");
                //当前点显示的文字&图片
                vh.tv.setTextColor(getResources().getColor(R.color.color_49DCFA));
                vh.tvDot.setBackgroundResource(R.drawable.lline_dot_normal);
                binding.parentCon.setBackgroundResource(0);
                binding.goingLin.setVisibility(View.VISIBLE);
                binding.goingName.setText(mDatas.get(position).getName());
                if (QuerySql.QueryBasic().getWhetherInterrupt()) {
                    binding.parentCon.setClickable(true);
                } else {
                    binding.parentCon.setClickable(false);
                }
                binding.contentCL.setVisibility(View.GONE);
                //带光晕的背景图
                Glide.with(getContext()).load(mDatas.get(position).getTouch_imagefile()).into(binding.startImg);
                //帧动画
                vh.tvTopLine.setImageResource(R.drawable.anim_login_start_loading);
                AnimationDrawable animationDrawable = (AnimationDrawable) vh.tvTopLine.getDrawable();
                animationDrawable.start();

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
//        taskQueue.clear();
        int startIndex = 0;
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
            taskQueue.enqueue(result.get(i));
            // 开始执行队列中的任务
            taskQueue.resume();
        }
        return result;
    }

    private void explanGonningSpeak(ArrayList<MyResultModel> mDatas, int position ,boolean tr) {
        Log.d("TAG", "开始途径播报");
        //有讲解内容
        if (!Objects.equals(Objects.requireNonNull(mDatas).get(position).getWalktext(), "")) {
            if (tr) {
                splitString(mDatas.get(position).getWalktext(), 45);
                RobotStatus.INSTANCE.getSpeakNumber().postValue(mDatas.get(position).getWalktext());
            }
            //观察是否结束
            RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer1 -> {
                Log.d("TAG", "explanGonningSpeak: 是否结束");
                if (integer1 == 1 && taskQueue.isCompleted()) {
                    arrayToDo(mDatas, position);
                }
            });
        }
        //如果有mp3
        if (!Objects.equals(mDatas.get(position).getWalkvoice(), null)) {
//                    Universal.Model = "讲解";
            if (tr) {
                MediaPlayerHelper.play(mDatas.get(position).getWalkvoice(), "1");
            }
            MediaPlayerHelper.setOnProgressListener((currentPosition, totalDuration) -> {
                LogUtil.INSTANCE.i("currentPosition : " + currentPosition + " totalDuration :" + totalDuration);
                new Handler(Looper.getMainLooper()).post(() ->
                        RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer -> {
                            if (currentPosition >= totalDuration && integer == 1) {
                                LogUtil.INSTANCE.i("到点并且播放完成");
                                arrayToDo(mDatas, position);
                            }
                        }));
            });
        }
        //当讲解内容和mp3都为空
        if (!Objects.equals(Objects.requireNonNull(mDatas).get(position).getWalktext(), "") && !Objects.equals(mDatas.get(position).getWalkvoice(), null)) {
//                    Universal.Model = "讲解";
            RobotStatus.INSTANCE.getArrayPointExplan().observe(getViewLifecycleOwner(), integer -> {
                if (integer == 1) {
                    arrayToDo(mDatas, position);
                }
            });
        }
    }

    private void arrayToDo(ArrayList<MyResultModel> mDatas, int position) {
        if (Universal.selectMapPoint) {
            return;
        }
        nextTaskToDo = true;
        if (BillManager.INSTANCE.billList().size() == 1) {
            binding.nextTaskBtn.setVisibility(View.GONE);
        }
        binding.parentCon.setClickable(false);
        binding.contentCL.setVisibility(View.VISIBLE);
        binding.parentCon.setBackgroundResource(R.drawable.bg);
        binding.statusTv.setText("正在讲解:");

        binding.nowExplanation.setText(mDatas.get(position).getName());
        Glide.with(getContext()).load(mDatas.get(position).getTouch_imagefile()).into(binding.pointImage);
        if (!Objects.equals(mDatas.get(position).getExplanationtext(), "")) {
            binding.pointName.setText(mDatas.get(position).getName());
//            Universal.Model = "讲解";
            LogUtil.INSTANCE.i("到点讲解文字:" + mDatas.get(position).getExplanationtext());
            List<String> textEqually = viewModel.splitString(mDatas.get(position).getExplanationtext(), 130);
            //页数
            int page = textEqually.size();
            //显示第一页
            beforePage = 0;
            binding.acceptstationTv.setText(textEqually.get(beforePage));
            //将第一页的内容再次等分成BaiduTTS可以朗读的范围
            Universal.taskNum = splitString(textEqually.get(beforePage), 45).size();
            RobotStatus.INSTANCE.getProgress().observe(getViewLifecycleOwner(), integer -> {
                Log.d("TAG", "当前进度: " + integer);
                if (nextTaskToDo) {
                    binding.acceptstationTv.startPlayLine(
                            integer,
                            textEqually.get(beforePage).length(),
                            ((long) QuerySql.QueryBasic().getSpeechSpeed() * textEqually.get(beforePage).length()) * 1000);
                }
                if (beforePage < page && integer == textEqually.get(beforePage).length() && taskQueue.isCompleted()) {
                    nextTaskToDo = false;
                    RobotStatus.INSTANCE.getProgress().postValue(0);
//                    taskQueue.clear();
                    Universal.progress = 0;
                    Universal.taskNum = 0;
                    BaiduTTSHelper.getInstance().stop();
                    beforePage++;
                    if (beforePage < page) {
                        binding.acceptstationTv.stopPlay();
                        LogUtil.INSTANCE.d("当前在多少页: " + beforePage);
                        binding.acceptstationTv.setText(textEqually.get(beforePage));
//                        taskQueue.clear();
                        Universal.taskNum = splitString(textEqually.get(beforePage), 45).size();
                        nextTaskToDo = true;
                    }
                } else if (beforePage >= page) {
                    viewModel.getCountDownTimer().start();
                    taskQueue.clear();
                }
            });

        } else if (!Objects.equals(mDatas.get(position).getExplanationvoice(), "")) {
//            Universal.Model = "讲解";
            LogUtil.INSTANCE.i("到点讲解音频路径:" + mDatas.get(position).getExplanationvoice());
            MediaPlayerHelper.play(mDatas.get(position).getExplanationvoice(), "1");
            MediaPlayerHelper.setOnProgressListener((currentPosition, totalDuration) -> {
                if (currentPosition >= totalDuration) {
                    viewModel.getCountDownTimer().start();
                }
            });
//           });
        } else if (Objects.equals(mDatas.get(position).getExplanationvoice(), null) && Objects.equals(mDatas.get(position).getExplanationtext(), "")) {
            viewModel.getCountDownTimer().start();
        }
    }

    private void processDialog() {
        processClickDialog.show();
        processClickDialog.finishBtn.setOnClickListener(v -> {
            //返回
            BaiduTTSHelper.getInstance().stop();
            Universal.taskNum = 0;
            Universal.progress = 0;
            //删除讲解队列
            taskQueue.clear();
            viewModel.finishTask();
            binding.acceptstationTv.stopPlay();
            processClickDialog.dismiss();
        });
        processClickDialog.nextBtn.setOnClickListener(v -> {
            //删除讲解队列
//            Universal.Model = "切换下一个点";
            BaiduTTSHelper.getInstance().stop();
            taskQueue.clear();
            binding.acceptstationTv.stopPlay();
            viewModel.nextTask(false);
            processClickDialog.dismiss();
        });
        processClickDialog.otherBtn.setOnClickListener(v -> {
            processClickDialog.dismiss();
            changeDialog(false);
        });
    }

    private void changeDialog(boolean array) {
        changingOverDialog.show();
        if (!array) {
            new UpdateReturn().pause();
        }
//        Log.d("TAG", "当前讲解点: " + targetName);
        ChangePointGridViewAdapter adapter = new ChangePointGridViewAdapter(getContext(), viewModel.inForListData(), targetName);
        changingOverDialog.pointGV.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        changingOverDialog.pointGV.setOnItemClickListener((parent, view, position, id) -> {
            if (position == itemTarget) {
                return;
            }
            LogUtil.INSTANCE.i("点击了列表Item");
            changingOverDialog.dialog_button.setVisibility(View.VISIBLE);
            changingOverDialog.askTv.setText(viewModel.inForListData().get(position).getName());
            changingOverDialog.Sure.setOnClickListener(v -> {
                Universal.taskNum = 0;
                Universal.progress = 0;
                BaiduTTSHelper.getInstance().stop();
                binding.acceptstationTv.stopPlay();
                changingOverDialog.dismiss();
                viewModel.recombine(viewModel.inForListData().get(position).getName());
            });
            changingOverDialog.No.setOnClickListener(v -> changingOverDialog.dialog_button.setVisibility(View.GONE));
        });
        changingOverDialog.returnImg.setOnClickListener(v1 -> {
            changingOverDialog.dismiss();
            if (!array) {
                new UpdateReturn().resume();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}