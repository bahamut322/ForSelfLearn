package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.databinding.FragmentStartExplantionBinding;
import com.sendi.deliveredrobot.navigationtask.ConsumptionTask;
import com.sendi.deliveredrobot.navigationtask.LineUpTaskHelp;
import com.sendi.deliveredrobot.utils.CenterItemUtils;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.utils.UiUtils;
import com.sendi.deliveredrobot.viewmodel.StartExplanViewModel;

import java.util.ArrayList;
import java.util.List;

import kotlinx.coroutines.CoroutineScope;

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
    private LineUpTaskHelp lineUpTaskHelp;
    int index = 0;


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
        lineUpTaskHelp = LineUpTaskHelp.getInstance();
        controller = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(StartExplanViewModel.class);
        viewModel.videoAudio();
        init();
        viewModel.mainScope();
        binding.finishBtn.setOnClickListener(v -> {
            //返回
            viewModel.finish();
            //删除讲解队列
            if(lineUpTaskHelp.checkTask()){
                lineUpTaskHelp.deletePlanNoAll("speakNumber");
            }
        });
        binding.nextTaskBtn.setOnClickListener(v-> viewModel.nextTask());


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

//                int childViewHeight = UiUtils.dip2px(getContext(), 72); //72是当前已知的 Item的高度
//                childViewHalfCount = (recyclerView.getHeight() / childViewHeight + 1) / 2;
                initData();
                findView();


            }
        });
        binding.pointList.postDelayed(() -> scrollToCenter(0), 100L);
    }


    private void initData() {
        viewModel.infoList();

        for (int j = 0; j < childViewHalfCount; j++) { //头部的空布局
            viewModel.getMDatas().add(0, null);
        }
        for (int k = 0; k < childViewHalfCount; k++) {  //尾部的空布局
            viewModel.getMDatas().add(null);
        }

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
//                    int fi = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
//                    int la = linearLayoutManager.findLastCompletelyVisibleItemPosition();
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
     *
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
                vh.tv.setTextColor(getResources().getColor(R.color.color_49DCFA));
                vh.tvDot.setBackgroundResource(R.drawable.lline_dot_normal);
                binding.pointName.setText(viewModel.getMDatas().get(position).getPointName().getPointName());
                Glide.with(getContext()).load(viewModel.getMDatas().get(position).getPointImage()).into(binding.pointImage);
                binding.acceptstationTv.setText(viewModel.getMDatas().get(position).getAcceptStation());
                binding.nowExplanation.setText("当前讲解：" + viewModel.getMDatas().get(position).getPointName().getPointName());
                //讲解
                getLength(viewModel.getMDatas().get(position).getPointName().getSpeakString());
            } else {
                vh.tv.setTextColor(getResources().getColor(R.color.white));
                vh.tvDot.setBackgroundResource(R.drawable.lline_dot_first);
            }
            vh.tv.setText(viewModel.getMDatas().get(position).getPointName().getPointName());
            final int fp = position;
            vh.itemView.setOnClickListener(v -> {
                scrollToCenter(fp);
                Toast.makeText(getContext(), "点击" + viewModel.getMDatas().get(fp), Toast.LENGTH_SHORT).show();
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
            return viewModel.getMDatas().size();
        }

        class VH extends RecyclerView.ViewHolder {

            public TextView tv, tvTopLine, tvDot;

            public VH(@NonNull View itemView) {
                super(itemView);
                // 第一行头的竖线不显示
                tvTopLine = itemView.findViewById(R.id.tvTopLine);
                tv = itemView.findViewById(R.id.tv);
                tvDot = itemView.findViewById(R.id.tvDot);
            }
        }
    }

    //更具字符串长度添加换行符
    public void getLength(String string) {
        //记录一共有多少位字符
        int valueLength = 0;
        //定义一个StringBuffer存储数据
        StringBuilder stringBuffer= new StringBuilder();
        int remainNum = 0;
        String remainText = null;
        //剩余字数
        int remainSize = (string.length()) - (string.length() % 45);
        if (string.length()%45 == 0){
            LogUtil.INSTANCE.d("讲解字数为45倍数不需要过多操作");
        }else {
            remainText = string.substring(remainSize); //截取

        }
        for (int i = 0; i < string.length(); i++) {
            // 获取一个字符
            String temp = string.substring(i, i + 1);
            // 判断是否为中文字符
            valueLength +=1;
            remainNum +=1;
            //每个数据放入StringBuffer中
            stringBuffer.append(temp);
            //如果长度为5，开始换行
            if (valueLength >= 45){
                ConsumptionTask task = new ConsumptionTask();
                task.explantinSpeaking = stringBuffer.toString(); // 队列数据
                task.planNo = "speakNumber"; // 将数据分组，可以不进行设置
                lineUpTaskHelp.addTask(task); // 添加到排队列表中去， 如果还有任务没完成，
                stringBuffer = new StringBuilder();
                //清空valueLength
                valueLength = 0;
            }
            //将剩余的字添加到队列中
            if (remainNum == remainSize){
                ConsumptionTask task = new ConsumptionTask();
                task.explantinSpeaking = remainText; // 队列数据
                task.planNo = "speakNumber"; // 将数据分组，可以不进行设置
                lineUpTaskHelp.addTask(task); // 添加到排队列表中去， 如果还有任务没完成，
                remainNum = 0;
            }
            initListener();
        }
    }

    private void initListener(){
        lineUpTaskHelp.setOnTaskListener(new LineUpTaskHelp.OnTaskListener() {
            @Override
            public void exNextTask(ConsumptionTask task) {
                // 所有任务，会列队调用exNextTask。在这里编写你的任务执行过程
                exTask(task);
            }

            @Override
            public void noTask() {
                Log.e("Post","所有任务执行完成");
            }
        });
    }

    public void exTask(final ConsumptionTask task){
        new Thread(){
            @Override
            public void run() {
                super.run();
                //子线程执行队列任务
                BaiduTTSHelper.getInstance().speak(task.explantinSpeaking);
                Log.e("Post","开始执行任务" + task.explantinSpeaking);
                // 检查列队
                lineUpTaskHelp.exOk(task);
            }
        }.start();
    }


}