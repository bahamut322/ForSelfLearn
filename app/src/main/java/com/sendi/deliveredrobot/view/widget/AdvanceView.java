package com.sendi.deliveredrobot.view.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.ViewPager;

import com.sendi.deliveredrobot.entity.Universal;

import java.util.ArrayList;
import java.util.List;

public class AdvanceView extends RelativeLayout {
    private ViewPager viewPager;
    private List<View> views = new ArrayList<>();
    private AdvancePagerAdapter adapter;

    public AdvanceView(Context context) {
        super(context);
        initView();
    }

    public AdvanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AdvanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void clear() {
        viewPager.invalidate();
    }


    public void initView() {
        viewPager = new ViewPager(getContext());
        adapter = new AdvancePagerAdapter(getContext(), viewPager);
        viewPager.setAdapter(adapter);
        addView(viewPager, new LayoutParams(-1, -1));
    }

    public void setData(List<Advance> advances) {
        clearData();
        adapter.setData(advances);
    }
    public void clearData() {
        if (views != null) {
            views.clear();
        }
    }
    public void mediaStop(){
        adapter.mediaStop();
    }
    public void mediaRestart(){
        adapter.mediaRestart();
    }
    public void setPause(){
        adapter.setPause();
    }
    public void setResume(){
        adapter.setResume();
    }
}