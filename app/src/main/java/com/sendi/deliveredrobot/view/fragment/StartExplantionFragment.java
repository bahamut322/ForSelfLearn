package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.adapter.ChangePointGridViewAdapter;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.databinding.FragmentStartExplantionBinding;
import com.sendi.deliveredrobot.helpers.MP3Player;
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.model.TaskModel;
import com.sendi.deliveredrobot.navigationtask.ConsumptionTask;
import com.sendi.deliveredrobot.navigationtask.LineUpTaskHelp;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.navigationtask.TaskQueues;
import com.sendi.deliveredrobot.ros.constant.MyCountDownTimer;
import com.sendi.deliveredrobot.utils.CenterItemUtils;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.utils.UiUtils;
import com.sendi.deliveredrobot.view.widget.NextTask;
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
    private MutableLiveData<String> pointName = new MutableLiveData<>();
    private ArrayList<MyResultModel> mDatas = new ArrayList<>();
    private MutableLiveData<Boolean> arrayPoint = new MutableLiveData<>();
    private TaskQueues<String> taskQueue;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.cancelMainScope();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        lineUpTaskHelp = LineUpTaskHelp.getInstance();
        controller = Navigation.findNavController(view);
        arrayPoint.postValue(false);
        // 创建任务Consumer
        Consumer<String> taskConsumer = task -> {
            // 执行任务的代码
            BaiduTTSHelper.getInstance().speaks(task, "explantion");
            System.out.println("Task: " + task);
        };
        // 创建TaskQueue实例
        taskQueue = new TaskQueues<>(taskConsumer);
        viewModel = new ViewModelProvider(this).get(StartExplanViewModel.class);
        mDatas = new ArrayList<>();
        mDatas = viewModel.inForListData();
        RobotStatus.INSTANCE.getSpeakContinue().observe(getViewLifecycleOwner(), integer -> {
            if (integer == 1) {
                taskQueue.clear();
                splitString(RobotStatus.INSTANCE.getSpeakNumber().getValue(), 45);
                RobotStatus.INSTANCE.getSpeakContinue().postValue(0);
            }
        });
        viewModel.downTimer();

        init();
        viewModel.mainScope();
        binding.finishBtn.setOnClickListener(v -> {
            //返回
            viewModel.finish();
            BaiduTTSHelper.getInstance().stop();
            //删除讲解队列
            taskQueue.clear();
        });
        binding.nextTaskBtn.setOnClickListener(v -> viewModel.nextTask());

        pointName.observe(getViewLifecycleOwner(), s -> {
            ChangePointGridViewAdapter adapter = new ChangePointGridViewAdapter(getContext(), mDatas, pointName.getValue());
            binding.ChangeFragment.pointGV.setAdapter(adapter);
        });
        View includeLayout = view.findViewById(R.id.ChangeFragment);
        includeLayout.setVisibility(View.GONE);
        includeLayout.setOnClickListener(v -> {
            includeLayout.setVisibility(View.VISIBLE);
            viewModel.pause();
        });
        binding.ChangeFragment.returnImg.setOnClickListener(v -> {
            includeLayout.setVisibility(View.GONE);
            viewModel.resume();
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

    private void init() {
        binding.pointList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.pointList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.pointList.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                centerToTopDistance = binding.pointList.getHeight() / 2;

                int childViewHeight = UiUtils.dip2px(getContext(), 72); //72是当前已知的 Item的高度
//                childViewHalfCount = (recyclerView.getHeight() / childViewHeight + 1) / 2;
                findView();
                initData();

            }
        });
        binding.pointList.postDelayed(() -> scrollToCenter(0), 100L);
    }


    private void initData() {

        RobotStatus.INSTANCE.getTargetName().observe(getViewLifecycleOwner(), s -> {
            Log.d("TAG", "当前讲解点: "+s);
            int i;
            for (i = 0; i < mDatas.size(); i++) {
                if (Objects.equals(mDatas.get(i), s)) {
                    Log.d("TAG", "onClick: " + i);
                    mAdapter.setSelectPosition(i);
                    mAdapter.selectPosition = i;
                    scrollToCenter(i);
                    break;
                }
            }
        });

//        for (int j = 0; j < childViewHalfCount; j++) { //头部的空布局
//            mDatas.add(0, null);
//        }
//        for (int k = 0; k < childViewHalfCount; k++) {  //尾部的空布局
//            mDatas.add(null);
//        }

        }

        private boolean isTouch = false;

        private final List<CenterItemUtils.CenterViewItem> centerViewItems = new ArrayList<>();

        @SuppressLint("ClickableViewAccessibility")
        private void findView () {
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
        private void scrollToCenter ( int position){
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
                if (position == 0) {
                    vh.tvTopLine.setVisibility(View.INVISIBLE);
                }

                if (selectPosition == position) {
                    //当前点显示的文字&图片
                    vh.tv.setTextColor(getResources().getColor(R.color.color_49DCFA));
                    vh.tvDot.setBackgroundResource(R.drawable.lline_dot_normal);
                    binding.pointName.setText(mDatas.get(position).getName());
                    Glide.with(getContext()).load(mDatas.get(position).getTouch_imagefile()).into(binding.pointImage);
                    binding.acceptstationTv.setText(mDatas.get(position).getExplanationtext());
                    binding.nowExplanation.setText(mDatas.get(position).getName());
                    //带光晕的背景图
                    Glide.with(getContext()).load(mDatas.get(position).getBackgroundpic()).into( binding.startImg);
                    //帧动画
                    vh.tvTopLine.setImageResource(R.drawable.anim_login_start_loading);
                    AnimationDrawable animationDrawable = (AnimationDrawable) vh.tvTopLine.getDrawable();
                    animationDrawable.start();
                    //有讲解内容
                    if (!Objects.equals(Objects.requireNonNull(mDatas).get(position).getWalktext(), "")) {
                        splitString(mDatas.get(position).getWalktext(), 45);
                        NextTask.setNextTasK(false);
                        RobotStatus.INSTANCE.getSpeakNumber().postValue(mDatas.get(position).getWalktext());
                        //观察是否结束
//                    RobotStatus.INSTANCE.getSpeakContinue().observe(getViewLifecycleOwner(), (Observer<Integer>) integer -> {
                        RobotStatus.INSTANCE.getReady().observe(getViewLifecycleOwner(), integer1 -> {
                            if (integer1 == 1 && taskQueue.isCompleted()) {
                                RobotStatus.INSTANCE.getSpeakContinue().postValue(0);
                                arrayPoint.postValue(true);
                            }
                        });
                    }
                    //如果有mp3
                    if (!Objects.equals(mDatas.get(position).getWalkvoice(), "")) {
                        MediaPlayerHelper.play(mDatas.get(position).getWalkvoice(), "1");
                        NextTask.setNextTasK(false);
                        MediaPlayerHelper.setOnProgressListener((currentPosition, totalDuration) -> {
                            LogUtil.INSTANCE.i("currentPosition : " + currentPosition + " totalDuration :" + totalDuration);
                            new Handler(Looper.getMainLooper()).post(() ->
                                    RobotStatus.INSTANCE.getReady().observe(getViewLifecycleOwner(), integer -> {
                                        if (currentPosition >= totalDuration && integer == 1) {
                                            LogUtil.INSTANCE.i("到点并且播放完成");
                                            arrayPoint.postValue(true);
                                        }
                                    }));
                        });
                    }
                    //当讲解内容和mp3都为空
                    if(!Objects.equals(Objects.requireNonNull(mDatas).get(position).getWalktext(), "") && !Objects.equals(mDatas.get(position).getWalkvoice(), "")){
                        RobotStatus.INSTANCE.getReady().observe(getViewLifecycleOwner(), integer -> {
                            if (integer == 1) {
                                arrayPoint.postValue(true);
                            }
                        });
                    }
                    //到点讲解内容
                    arrayPoint.observe(getViewLifecycleOwner(), aBoolean -> {
                        if (aBoolean) {
                            arrayToDo(mDatas, position);
                            arrayPoint.postValue(false);
                        }
                    });
                } else {
                    vh.tv.setTextColor(getResources().getColor(R.color.white));
                    vh.tvDot.setBackgroundResource(R.drawable.lline_dot_first);
                }
                vh.tv.setText(mDatas.get(position).getName());
                final int fp = position;
//            scrollToCenter(fp);
                vh.itemView.setOnClickListener(v -> {
                    scrollToCenter(fp);
                    Toast.makeText(getContext(), "点击" + mDatas.get(fp), Toast.LENGTH_SHORT).show();
                });
            }

            private int selectPosition = -1;

            @SuppressLint("NotifyDataSetChanged")
            public void setSelectPosition(int cposition) {
                selectPosition = cposition;
//            notifyItemChanged(cposition);
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

        public List<String> splitString (String input,int length){
            List<String> result = new ArrayList<>();
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
//            ConsumptionTask task = new ConsumptionTask();
                // 添加任务到队列中
                taskQueue.enqueue(result.get(i));
                // 开始执行队列中的任务
                taskQueue.resume();
            }
            return result;
        }


        private void arrayToDo (ArrayList < MyResultModel > mDatas,int position){
            if (!Objects.equals(mDatas.get(position).getExplanationtext(), "")) {
                NextTask.setNextTasK(false);
                LogUtil.INSTANCE.i("到点讲解文字:" + mDatas.get(position).getExplanationtext());
                splitString(mDatas.get(position).getExplanationtext(), 45);
//            RobotStatus.INSTANCE.getSpeakContinue().observe(this, integer -> {
                if (taskQueue.isCompleted()) {
                    viewModel.getCountDownTimer().start();
                    RobotStatus.INSTANCE.getSpeakContinue().postValue(0);
                }
//            });
            } else if (!Objects.equals(mDatas.get(position).getExplanationvoice(), "")) {
                NextTask.setNextTasK(false);
                LogUtil.INSTANCE.i("到点讲解音频路径:" + mDatas.get(position).getExplanationvoice());
                MediaPlayerHelper.play(mDatas.get(position).getExplanationvoice(), "1");
                MediaPlayerHelper.setOnProgressListener((currentPosition, totalDuration) -> {
                    if (currentPosition >= totalDuration) {
                        viewModel.getCountDownTimer().start();
                    }
                });
//           });
            } else if (Objects.equals(mDatas.get(position).getExplanationvoice(), "") && Objects.equals(mDatas.get(position).getExplanationtext(), "")) {
                viewModel.getCountDownTimer().start();
            }
        }

        @Override
        public void onDestroy () {
            super.onDestroy();
        }

    }