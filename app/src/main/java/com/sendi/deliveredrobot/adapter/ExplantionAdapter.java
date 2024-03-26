package com.sendi.deliveredrobot.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.model.RouteMapList;

import java.util.List;

/**
 * 列表适配器
 */
public class ExplantionAdapter extends RecyclerView.Adapter {
    private List<RouteMapList> datas;
    private Context context;

    private OnItemClickListener listener;
    private int colorWhite;

    private int colorAWhite;
    private Drawable imgExplanationBottom;
    private Drawable imgExplanationBottomFalse;
    private Drawable unSelectExplainBottom;
    private Drawable selectExplainBottom;

    public ExplantionAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        initResources(context);
    }

    public void setData(List<RouteMapList> data){
        this.datas = data;
    }

    private void initResources(Context context) {
        colorWhite = ContextCompat.getColor(context,R.color.white);
        imgExplanationBottom = ContextCompat.getDrawable(context,R.drawable.img_explanation_bottom);
        imgExplanationBottomFalse = ContextCompat.getDrawable(context,R.drawable.img_explanation_bottom_false);
        unSelectExplainBottom = ContextCompat.getDrawable(context,R.drawable.un_select_explan_bottom);
        selectExplainBottom = ContextCompat.getDrawable(context,R.drawable.select_explanation_bottom);
        colorAWhite = ContextCompat.getColor(context,R.color.Awhite);
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.explanation_item, parent, false));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VH vh = (VH) holder;
        //第一次进入的时候，不改变第一个的宽高
        if (position == 0) {
            vh.tv.setTextColor(colorWhite);
            vh.tv.setTextSize(32);
            vh.imgBottom.setImageDrawable(imgExplanationBottom);
            setViewSize(vh.imgBottom, 240, 4);
            vh.imgStart.setVisibility(View.VISIBLE);
            vh.imgEnd.setVisibility(View.VISIBLE);
            vh.textNameImg.setVisibility(View.VISIBLE);
            setViewSize(vh.bottomImg, 320, 56);
            vh.bottomImg.setImageDrawable(selectExplainBottom);
        } else {
            //居中item的布局样式
            if (selectPosition == position) {
                vh.tv.setTextColor(ContextCompat.getColor(context, R.color.white));
                vh.tv.setTextSize(32);
                setViewSize(vh.view, 320, 448);
//                    setViewSize(vh.imageView, 288, 334);
                vh.view.setSelected(true);
                setViewSize(vh.imgBottom, 240, 4);
                setViewSize(vh.bottomImg, 320, 56);
                vh.imgBottom.setImageDrawable(imgExplanationBottom);
                vh.imgStart.setVisibility(View.VISIBLE);
                vh.imgEnd.setVisibility(View.VISIBLE);
                vh.textNameImg.setVisibility(View.VISIBLE);
                vh.bottomImg.setImageDrawable(selectExplainBottom);
            } else {
                vh.tv.setTextColor(colorAWhite);
                vh.view.setSelected(false);
                vh.tv.setTextSize(28);
                setViewSize(vh.view, 290, 407);
                setViewSize(vh.imageView, 258, 320);
                setViewSize(vh.bottomImg, 290, 48);
                vh.imgBottom.setImageDrawable(imgExplanationBottomFalse);
                setViewSize(vh.imgBottom, 220, 4);
                vh.imgStart.setVisibility(View.GONE);
                vh.imgEnd.setVisibility(View.GONE);
                vh.textNameImg.setVisibility(View.GONE);
                vh.bottomImg.setImageDrawable(unSelectExplainBottom);

            }
        }
        if (datas.get(position) == null) {
            vh.itemView.setVisibility(View.INVISIBLE);
        } else {
            vh.itemView.setVisibility(View.VISIBLE);
            vh.tv.setText(datas.get(position).getRouteName());
            Glide.with(context).load(datas.get(position).getBackGroundPic()).into(vh.imageView);
//                imageFile(vh.imageView, new File(mDatas.get(position).getBackGroundPic()));
        }
        //item点击
        vh.view.setOnClickListener(v -> {
            listener.onItemClick(position, datas.get(position));
        });
    }

    private int selectPosition = -1;

    public void setSelectPosition(int cposition) {
        selectPosition = cposition;
//            notifyItemChanged(cposition);
        notifyDataSetChanged();
    }

    /**
     * 设置控件大小
     *
     * @param view   控件
     * @param width  宽度，单位：像素
     * @param height 高度，单位：像素
     */
    public void setViewSize(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class VH extends RecyclerView.ViewHolder {

        public TextView tv;
        public View view;
        public ImageView imageView, imgBottom, imgStart, imgEnd, textNameImg, bottomImg;

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

    public interface OnItemClickListener {
        void onItemClick(int position, RouteMapList routeMap);
    }
}
