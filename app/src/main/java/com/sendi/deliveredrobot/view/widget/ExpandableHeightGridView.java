package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * @Author Swn
 * @Data 2023/11/17
 * @describe 自定义居中GridView
 */
public class ExpandableHeightGridView extends GridView {

    private int maxHeight = Integer.MAX_VALUE; // 默认最大高度为无限大

    public ExpandableHeightGridView(Context context) {
        super(context);
    }

    public ExpandableHeightGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableHeightGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算GridView的高度
        int expandSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        if (getMeasuredHeight() > maxHeight) {
            params.height = maxHeight;
        } else {
            params.height = getMeasuredHeight();
        }
        setLayoutParams(params);
    }
}
