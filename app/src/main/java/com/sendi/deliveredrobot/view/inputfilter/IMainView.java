package com.sendi.deliveredrobot.view.inputfilter;

import android.view.KeyEvent;

public interface IMainView
{
    boolean onKeyDown(int keyCode, KeyEvent event);

    public void showTipsView();
}
