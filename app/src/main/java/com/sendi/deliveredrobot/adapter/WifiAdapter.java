package com.sendi.deliveredrobot.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hacknife.wifimanager.IWifi;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.adapter.base.i.BaseRecyclerViewAdapter;
import com.sendi.deliveredrobot.holder.WifiViewHolder;

import org.jetbrains.annotations.NotNull;

/**
 * author  : hacknife
 * e-mail  : 4884280@qq.com
 * github  : http://github.com/hacknife
 * project : MVVM
 */
public class WifiAdapter extends BaseRecyclerViewAdapter<IWifi, WifiViewHolder> {
    public WifiAdapter() {
    }

    @NotNull
    @Override
    public WifiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WifiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi_list, parent,false));
    }

}
