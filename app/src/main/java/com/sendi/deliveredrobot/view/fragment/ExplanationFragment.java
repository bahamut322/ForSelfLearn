package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.databinding.ExplanationItemBinding;
import com.sendi.deliveredrobot.databinding.FragmentExplanationBinding;
import com.sendi.deliveredrobot.entity.FunctionSkip;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.DialogHelper;
import com.sendi.deliveredrobot.helpers.SpeakHelper;
import com.sendi.deliveredrobot.model.RouteMapList;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.utils.CenterItemUtils;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.utils.UiUtils;
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author swn
 * @describe 智能讲解
 */
public class ExplanationFragment extends Fragment {

    FragmentExplanationBinding binding;
    private explantionAdapter mAdapter;
    private int centerToLiftDistance; //RecyclerView款度的一半 ,也就是控件中间位置到左部的距离 ，
    private int childViewHalfCount = 0; //当前RecyclerView一半最多可以存在几个Item
    NavController controller;

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
        controller = Navigation.findNavController(requireView());
        binding.tvExplanationName.setText(QuerySql.QueryExplainConfig().getSlogan());
        //返回主页面
        binding.llReturn.setOnClickListener(v -> controller.navigate(R.id.action_explanationFragment_to_homeFragment));

        binding.imageViewSetting.setOnClickListener(v -> {
            fromeSettingDialog.show();
            RobotStatus.INSTANCE.getPassWordToSetting().observe(getViewLifecycleOwner(), it -> {
                if (Boolean.TRUE.equals(RobotStatus.INSTANCE.getPassWordToSetting().getValue())) {
                    try {
                        controller.navigate(R.id.action_explanationFragment_to_settingHomeFragment);
                    }catch (Exception ignored){}
                    fromeSettingDialog.dismiss();
                    RobotStatus.INSTANCE.getPassWordToSetting().postValue(false);
                }
            });
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
        //回到主页面的时候初始化一下选择讲解点的值
        RobotStatus.INSTANCE.getSelectRoutMapItem().postValue(-1);
        RobotStatus.INSTANCE.getPointItem().postValue(-1);
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
        }catch (Exception e){
            Log.d("TAG", "列表为空: "+e);
        }
    }

    private List<RouteMapList> mDatas;

    private void initData() {
        if (mDatas == null)
            //查询当前设置的总图下所有的路线
            mDatas = QuerySql.queryRoute(Universal.MapName);
        for (int j = 0; j < childViewHalfCount; j++) { //头部的空布局
            mDatas.add(0, null);
        }
        for (int k = 0; k < childViewHalfCount; k++) {  //尾部的空布局
            mDatas.add(null);
        }
    }
    private boolean isTouch = false;

    private List<CenterItemUtils.CenterViewItem> centerViewItems = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    private void findView() {
        mAdapter = new explantionAdapter();
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
        position = position < childViewHalfCount ? childViewHalfCount : position;
        position = position < mAdapter.getItemCount() - childViewHalfCount - 1 ? position : mAdapter.getItemCount() - childViewHalfCount - 1;

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.explainRv.getLayoutManager();
        View childView = linearLayoutManager.findViewByPosition(position);
        Log.i("ccb", "000000: " + position);
        //把当前View移动到居中位置
        if (childView == null) return;
        int childVhalf = childView.getWidth() / 2;
        int childViewLeft = childView.getLeft();
        int viewCTop = centerToLiftDistance;
        int smoothDistance = childViewLeft - viewCTop + childVhalf;
        Log.i("ccb", "\n居中位置距离左部距离: " + viewCTop
                + "\n当前居中控件距离左部距离: " + childViewLeft
                + "\n当前居中控件的一半高度: " + childVhalf
                + "\n滑动后再次移动距离: " + smoothDistance);
        binding.explainRv.smoothScrollBy(smoothDistance, 0, decelerateInterpolator);
        mAdapter.setSelectPosition(position);

        LogUtil.INSTANCE.d("当前选中:" + mDatas.get(position));
    }


    /**
     * 列表适配器
     */
    class explantionAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(getContext()).inflate(R.layout.explanation_item, parent, false));
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            VH vh = (VH) holder;
            //第一次进入的时候，不改变第一个的宽高
            if (position == 0) {
                vh.tv.setTextColor(getResources().getColor(R.color.white));
                vh.tv.setTextSize(32);
                vh.imgBottom.setImageDrawable(getContext().getResources().getDrawable(R.drawable.img_explanation_bottom));
                setViewSize(vh.imgBottom, 240, 4);
                vh.imgStart.setVisibility(View.VISIBLE);
                vh.imgEnd.setVisibility(View.VISIBLE);
                vh.textNameImg.setVisibility(View.VISIBLE);
                setViewSize(vh.bottomImg, 320, 56);
                vh.bottomImg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.select_explanation_bottom));
            } else {
                //居中item的布局样式
                if (selectPosition == position) {
                    vh.tv.setTextColor(getResources().getColor(R.color.white));
                    vh.tv.setTextSize(32);
                    setViewSize(vh.view, 320, 448);
//                    setViewSize(vh.imageView, 288, 334);
                    vh.view.setSelected(true);
                    setViewSize(vh.imgBottom, 240, 4);
                    setViewSize(vh.bottomImg, 320, 56);
                    vh.imgBottom.setImageDrawable(getContext().getResources().getDrawable(R.drawable.img_explanation_bottom));
                    vh.imgStart.setVisibility(View.VISIBLE);
                    vh.imgEnd.setVisibility(View.VISIBLE);
                    vh.textNameImg.setVisibility(View.VISIBLE);
                    vh.bottomImg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.select_explanation_bottom));
                } else {
                    vh.tv.setTextColor(getResources().getColor(R.color.Awhite));
                    vh.view.setSelected(false);
                    vh.tv.setTextSize(28);
                    setViewSize(vh.view, 290, 407);
                    setViewSize(vh.imageView, 258, 320);
                    setViewSize(vh.bottomImg, 290, 48);
                    vh.imgBottom.setImageDrawable(getContext().getResources().getDrawable(R.drawable.img_explanation_bottom_false));
                    setViewSize(vh.imgBottom, 220, 4);
                    vh.imgStart.setVisibility(View.GONE);
                    vh.imgEnd.setVisibility(View.GONE);
                    vh.textNameImg.setVisibility(View.GONE);
                    vh.bottomImg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.un_select_explan_bottom));

                }
            }
            if (mDatas.get(position) == null) {
                vh.itemView.setVisibility(View.INVISIBLE);
            } else {
                vh.itemView.setVisibility(View.VISIBLE);
                vh.tv.setText(mDatas.get(position).getRouteName());
                Glide.with(getContext()).load(mDatas.get(position).getBackGroundPic()).into(vh.imageView);
//                imageFile(vh.imageView, new File(mDatas.get(position).getBackGroundPic()));
            }
            final int fp = position;
            //item点击
            vh.view.setOnClickListener(v -> {
                Universal.lastValue = null;
                if (!RobotStatus.INSTANCE.getBatteryStateNumber().getValue()) {
                    Toast.makeText(getContext(), "请先对接充电桩", Toast.LENGTH_SHORT).show();
                    DialogHelper.briefingDialog.show();
                } else {
                    scrollToCenter(fp);
                    RobotStatus.INSTANCE.getSelectRoutMapItem().postValue(mDatas.get(position).getId());
                    Log.d("TAG", "onBindViewHolder: "+mDatas.get(position).getId());
                    controller.navigate(R.id.action_explanationFragment_to_CatalogueExplantionFragment);
                    SpeakHelper.INSTANCE.speak(QuerySql.QueryExplainConfig().getPointListText());
                }
            });
        }

        private int selectPosition = -1;

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

            public TextView tv;
            public View view;
            public ImageView imageView, imgBottom, imgStart, imgEnd, textNameImg,bottomImg;

            public VH(@NonNull View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.tv);
                view = itemView.findViewById(R.id.view);
                imageView = itemView.findViewById(R.id.imageView);
                imgBottom = itemView.findViewById(R.id.imgBottom);
                imgStart = itemView.findViewById(R.id.imgStart);
                imgEnd = itemView.findViewById(R.id.imgEnd);
                textNameImg = itemView.findViewById(R.id.name_bg_img);
                bottomImg = itemView.findViewById(R.id.bottomImg);
            }
        }
    }

    /**
     * 设置控件大小
     *
     * @param view   控件
     * @param width  宽度，单位：像素
     * @param height 高度，单位：像素
     */
    public static void setViewSize(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }


    /**
     * 异步加载图片
     *
     * @param imageView 图片控件
     * @param imageFile 图片路径
     */
    @SuppressLint("StaticFieldLeak")
    private void imageFile(ImageView imageView, File imageFile) {
        new AsyncTask<File, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(File... files) {
                File imageFile = files[0];
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = 4;
                bmOptions.inPurgeable = true;
                return BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        }.execute(imageFile);
    }
}