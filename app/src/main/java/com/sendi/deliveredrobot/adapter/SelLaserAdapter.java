package com.sendi.deliveredrobot.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.adapter.base.i.BaseRecyclerViewAdapter;
import com.sendi.deliveredrobot.holder.SelLaserHolder;
import com.sendi.deliveredrobot.model.AllMapRelationshipModel;

import org.jetbrains.annotations.NotNull;

/**
 * @author lsz
 * @describe 选择激光图
 * @date 2021/9/9
 */
public class SelLaserAdapter extends BaseRecyclerViewAdapter<AllMapRelationshipModel, SelLaserHolder> {


    public SelLaserAdapter() {

    }

    @NotNull
    @Override
    public SelLaserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelLaserHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sel_laser_list, parent,false));
    }

}
