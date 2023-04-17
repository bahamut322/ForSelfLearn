package com.sendi.deliveredrobot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.model.MyResultModel;

import java.util.List;

/**
 * @Author Swn
 * @describe 切换讲解目标点适配器
 * @Data 2023-04-10 16:22
 */
public class MyGridViewAdapter extends BaseAdapter {
    private Context context;
    private List<MyResultModel> data;
    private String name;

    public MyGridViewAdapter(Context context, List<MyResultModel> data ,String name) {
        this.context = context;
        this.data = data;
        this.name = name;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 创建或重用视图
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_changing_point, parent, false);
        }
        // 获取子视图
        Button textView = (Button) convertView.findViewById(R.id.pointName);
        if (data.get(position).getName() == name){
            textView.setBackgroundResource(R.drawable.bg_button_1);
           textView.setEnabled(false);
        }


        // 设置子视图内容
        textView.setText(data.get(position).getName());

        return convertView;
    }
}

