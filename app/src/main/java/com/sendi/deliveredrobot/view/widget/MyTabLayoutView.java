package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sos0707
 * @desc 顶部 tablayout
 * @date 2020/11/11 9:09
 **/
public class MyTabLayoutView extends LinearLayout {

    private String[] ssTabName;
    private HorizontalScrollView hsclvTabStage;
    private LinearLayout llayoutTabStage;
    private Context con;
    private int screenWidth;
    private float textSelSize = 15f;
    private float textUnSelSize = 15f;
    private OnTabChangeListener onTabChangeListener;
    private int curSelTab = 0;

    private Rect rect;
    LayoutParams params;

    public MyTabLayoutView(Context context) {
        super(context);
        init(context);
    }

    public MyTabLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyTabLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        con = context;
        //获取到控件
        //加载布局文件，与setContentView()效果一样
        LayoutInflater.from(context).inflate(R.layout.view_my_tab_layout, this);
        hsclvTabStage = findViewById(R.id.hsclv_tab);
        llayoutTabStage = findViewById(R.id.llayout_tab_stage);
        rect = new Rect();
        this.getWindowVisibleDisplayFrame(rect);
        params = new
                LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }

    public void cleanTab() {
        llayoutTabStage.removeAllViews();
    }

    public int getCurSelTab() {
        return curSelTab;
    }

    public void initTab(String[] ss) {
        ssTabName = ss;
        screenWidth = rect.width();
        llayoutTabStage.removeAllViews();

        for (int i = 0; i < ss.length; i++) {
            View view = LayoutInflater.from(con).
                    inflate(R.layout.view_business_query_type, null);
//            TextView tvTypeName = view.findViewById(R.id.tv_type_name);
//            TextView tvTypeNameLine = view.findViewById(R.id.tv_type_name_line);
//            tvTypeName.setText( ss[i] );
//            tvTypeNameLine.setText( ss[i] );
            ((TextView) (view.findViewById(R.id.tv_type_name))).setText(ss[i]);
            ((TextView) (view.findViewById(R.id.tv_type_name_line))).setText(ss[i]);

            view.setId(i);
            int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    selTabStage(finalI);
                    if (onTabChangeListener != null) {
                        onTabChangeListener.onTabChange(finalI);
                        curSelTab = finalI;
                    }
                }
            });

//            LayoutParams params = new
//                    LayoutParams(LayoutParams.WRAP_CONTENT,
//                    LayoutParams.WRAP_CONTENT);

            llayoutTabStage.addView(view, params);
        }
        selTabStage(curSelTab);
        if (onTabChangeListener != null) {
            onTabChangeListener.onTabChange(curSelTab);
        }
    }

    public void initTab(List<String> ss) {
        ssTabName = (String[]) ss.toArray();
//        ssTabName = ss;
        screenWidth = rect.width();
        llayoutTabStage.removeAllViews();

        for (int i = 0; i < ss.size(); i++) {
            View view = LayoutInflater.from(con).
                    inflate(R.layout.view_business_query_type, null);
            ((TextView) (view.findViewById(R.id.tv_type_name))).setText(ss.get(i));
            ((TextView) (view.findViewById(R.id.tv_type_name_line))).setText(ss.get(i));

            view.setId(i);
            int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    selTabStage(finalI);
                    if (onTabChangeListener != null) {
                        onTabChangeListener.onTabChange(finalI);
                        curSelTab = finalI;
                    }
                }
            });
            llayoutTabStage.addView(view, params);
        }
        selTabStage(0);
        if (onTabChangeListener != null) {
            onTabChangeListener.onTabChange(0);
        }
    }

    public void initTab(String ss[], int idx) {
        ssTabName = ss;
        Rect rect = new Rect();
        this.getWindowVisibleDisplayFrame(rect);
        screenWidth = rect.width();
        llayoutTabStage.removeAllViews();

        selTabStage(idx);

        List<TextView> tvTypeNameList = new ArrayList<>();
        List<TextView> tvTypeLineList = new ArrayList<>();

        for (int i = 0; i < ss.length; i++) {

            View view = LayoutInflater.from(con).
                    inflate(R.layout.view_business_query_type, null);
            TextView tvTypeName = view.findViewById(R.id.tv_type_name);
            if (i == idx) {
                tvTypeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
                tvTypeName.setTextColor(Color.WHITE);
            } else {
                tvTypeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                tvTypeName.setTextColor(Color.parseColor("#A0BAEF"));
            }
            TextView tvTypeNameLine = view.findViewById(R.id.tv_type_name_line);

            tvTypeNameList.add(tvTypeName);
            tvTypeLineList.add(tvTypeNameLine);

            tvTypeName.setText(ss[i]);
            tvTypeNameLine.setText(ss[i]);

            view.setId(i);
            int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    selTabStage(finalI);

                    for (int i = 0; i < tvTypeNameList.size(); i++) {
                        tvTypeNameList.get(i).setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                        tvTypeNameList.get(i).setTextColor(Color.parseColor("#A0BAEF"));
                        if (i == finalI) {
                            tvTypeNameList.get(i).setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
                            tvTypeNameList.get(i).setTextColor(Color.WHITE);
                        }
                    }

                    if (onTabChangeListener != null) {
                        onTabChangeListener.onTabChange(finalI);
                        curSelTab = finalI;
                    }
                }
            });

            LayoutParams params = new
                    LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);

            llayoutTabStage.addView(view, params);
        }
        selTabStage(idx);
        if (onTabChangeListener != null) {
            onTabChangeListener.onTabChange(idx);
        }
    }

    TextPaint paint;
    int left;
    int width;
    int len;

    public void selTabStage(int idx) {
        if (llayoutTabStage.getChildCount() == 0 || llayoutTabStage.getChildCount() <= idx) {
            return;
        }
        for (int i = 0; i < llayoutTabStage.getChildCount(); i++) {
            View view = llayoutTabStage.getChildAt(i);
            TextView tvTypeName = view.findViewById(R.id.tv_type_name);
//            tvTypeName.setTextSize( textUnSelSize );
            TextPaint paint = tvTypeName.getPaint();
            paint.setFakeBoldText(false);
            view.setSelected(false);
        }
        View view = llayoutTabStage.getChildAt(idx);
        TextView tvTypeName = view.findViewById(R.id.tv_type_name);
//        tvTypeName.setTextSize( textSelSize );
        paint = tvTypeName.getPaint();
        paint.setFakeBoldText(true);
        view.setSelected(true);
        left = view.getLeft();
        width = view.getMeasuredWidth();
        len = left + width / 2 - baseViewWidth / 2;
        LogUtil.INSTANCE.d("selTabStage len=" + len + " left=" + left + " width=" + width + " screenWidth=" + baseViewWidth);
        hsclvTabStage.smoothScrollTo(len, 0); //滑动ScroollView
    }

    public void selTabStage(String tabName) {
        int idx = -1;
        for (int i = 0; i < ssTabName.length; i++) {
            if (ssTabName[i].equals(tabName)) {
                idx = i;
                break;
            }
        }
        LogUtil.INSTANCE.d("tabname=" + tabName + idx);
        if (idx == -1) {
            return;
        }
        if (llayoutTabStage.getChildCount() == 0 || llayoutTabStage.getChildCount() <= idx) {
            return;
        }
        for (int i = 0; i < llayoutTabStage.getChildCount(); i++) {
            View view = llayoutTabStage.getChildAt(i);
            TextView tvTypeName = view.findViewById(R.id.tv_type_name);
            TextPaint paint = tvTypeName.getPaint();
            paint.setFakeBoldText(false);
            view.setSelected(false);
        }
        View view = llayoutTabStage.getChildAt(idx);
        TextView tvTypeName = view.findViewById(R.id.tv_type_name);
        paint = tvTypeName.getPaint();
        paint.setFakeBoldText(true);
        view.setSelected(true);
        left = view.getLeft();
        width = view.getMeasuredWidth();
        len = left + width / 2 - baseViewWidth / 2;
        LogUtil.INSTANCE.d("selTabStage len=" + len + " left=" + left + " width=" + width + " screenWidth=" + baseViewWidth);
        hsclvTabStage.smoothScrollTo(len, 0); //滑动ScroollView
        if (onTabChangeListener != null) {
            onTabChangeListener.onTabChange(idx);
            curSelTab = idx;
        }
    }

    private int baseViewWidth;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        baseViewWidth = MeasureSpec.getSize(widthMeasureSpec);
    }

    public void setOnTabChangeListener(OnTabChangeListener listener) {
        onTabChangeListener = listener;
    }

    private int spToPx(int sp) {
        float scaledDensity = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (scaledDensity * sp + 0.5f * (sp >= 0 ? 1 : -1));
    }

    public interface OnTabChangeListener {
        void onTabChange(int position);
    }

}
