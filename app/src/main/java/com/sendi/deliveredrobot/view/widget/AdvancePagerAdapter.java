package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.sendi.deliveredrobot.entity.Universal;

import java.util.ArrayList;
import java.util.List;

public class AdvancePagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private final Context context;
    private ViewPager viewPager;
    private List<Advance> datas;
    private List<View> list = new ArrayList<>();
    private int current = 0;
    public static int time = Universal.picPlayTime * 1000;
    private boolean pause;
    private Thread thread;
    private int lastPosition = -1;

    public AdvancePagerAdapter(Context context, ViewPager viewPager) {
        this.context = context;
        this.viewPager = viewPager;
    }


    public void setData(List<Advance> advances) {
        if (advances.size() == 0) return;
        this.datas = advances;
        list.clear();
        if (advances != null) {
            addView(advances.get(advances.size() - 1));
            if (advances.size() > 1) { //多于1个要循环
                for (Advance d : advances) { //中间的N个（index:1~N）
                    addView(d);
                }
                addView(advances.get(0));
            }
        }
        viewPager.addOnPageChangeListener(this);
        notifyDataSetChanged();
        //在外层，将mViewPager初始位置设置为1即可
        if (advances.size() > 1) { //多于1个，才循环并开启定时器
            viewPager.setCurrentItem(1);
            startTimer();
        }
        if (advances.get(0).type.equals("1")) {//第一个是视频不播放这边优化了一下
            ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).setVideo(mediaPlayer -> {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            });
        }

    }

    private void addView(Advance advance) {
        if (advance.type.equals("1")) {
            AdvanceVideoView videoView = new AdvanceVideoView(context);
            videoView.setImage(advance.path);
            list.add(videoView);
        } else {
            AdvanceImageView imageView = new AdvanceImageView(context);
            imageView.setImage(advance.path);
            list.add(imageView);
        }
    }

    private void startTimer() {
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
            thread = null;
        }
        thread = new Thread(() -> {
            while (thread != null && !thread.isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    if (!pause && !(list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView))
                        current += 1000;
                    if (current >= time) {
                        viewPager.post(() -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true));
                        current = 0;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(list.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = list.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    //
//    // 实现ViewPager.OnPageChangeListener接口
    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
        // 什么都不干
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // 由于viewpager的预加载机制onPageSelected这里面加载videoview 放的跟玩一样  等操作完成后再播放videoview就香了  很丝滑
        Log.d("", "");
        if (state == 0) {
            if (list.size() > 1) { //多于1，才会循环跳转
                if (lastPosition != -1 && lastPosition != viewPager.getCurrentItem() && list.get(lastPosition) instanceof AdvanceVideoView) {
                    ((AdvanceVideoView) list.get(lastPosition)).setPause();
                }
                if (viewPager.getCurrentItem() < 1) { //首位之前，跳转到末尾（N）
                    int position = datas.size(); //注意这里是mList，而不是mViews
                    viewPager.setCurrentItem(position, false);
                } else if (viewPager.getCurrentItem() > datas.size()) { //末位之后，跳转到首位（1）
                    viewPager.setCurrentItem(1, false); //false:不显示跳转过程的动画
                }
                current = 0;//换页重新计算时间
                if (list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView) {
                    ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).setVideo(mediaPlayer -> {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                    });
                }
                lastPosition = viewPager.getCurrentItem();
            }
        }
    }

    public void setPause() {
        pause = true;
        if (list.size() > 0 && list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView) {
            ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).setPause();
            Log.e("调用暂停", " pause");
        }
    }

    public void setResume() {
        pause = false;
        if (list.size() > 0 && list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView) {
            ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).setRestart();
            Log.e("调用start", " start");
        }
    }
}
