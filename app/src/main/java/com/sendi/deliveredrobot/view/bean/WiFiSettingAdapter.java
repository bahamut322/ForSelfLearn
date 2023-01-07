package com.sendi.deliveredrobot.view.bean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.utils.WifiUtils;
import com.sendi.deliveredrobot.view.contants.WifiCipherType;
import com.sendi.deliveredrobot.view.contants.WifiConnectType;

import java.util.List;


public class WiFiSettingAdapter extends RecyclerView.Adapter<WiFiSettingAdapter.ViewHolder> {

    private Context mContext;
    private List<WifiBean> resultList;
    private onItemClickListener onItemClickListener;

    public void setOnItemClickListener(WiFiSettingAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public WiFiSettingAdapter(Context mContext, List<WifiBean> resultList) {
        this.mContext = mContext;
        this.resultList = resultList;
    }

    public void setData(List<WifiBean> data) {
        resultList.clear();
        resultList.addAll(data);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_wifi_setting, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "WrongConstant"})
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final WifiBean bean = resultList.get(position);
        holder.tvSsid.setText(bean.getWifiName() + "(" + bean.getState() + ")");

        // 可以传递给adapter的数据都是经过处理的，已连接或者正在连接状态的wifi都是处于集合中的首位，所以可以写出如下判断
        if (position == 0 && (WifiConnectType.WIFI_STATE_ON_CONNECTING.equals(bean.getState()) || WifiConnectType.WIFI_STATE_CONNECT.equals(bean.getState()))) {
            holder.tvSsid.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.tvState.setTextColor(mContext.getResources().getColor(R.color.white));
            switch (bean.getLevel()) {
                case 1:
                    holder.imgWifi.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wifi_3));
                    holder.imagWifiLock.setVisibility(View.GONE);
                    break;
                case 2:
                    holder.imgWifi.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wifi_2));
                    holder.imagWifiLock.setVisibility(View.GONE);
                    break;
                case 3:
                case 4:
                    holder.imgWifi.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wifi_1));
                    holder.imagWifiLock.setVisibility(View.GONE);
                    break;
            }

        } else {
            holder.tvSsid.setTextColor(mContext.getResources().getColor(R.color.status_text));
            holder.tvState.setTextColor(mContext.getResources().getColor(R.color.status_text));
            switch (bean.getLevel()) {
                case 1:
                    holder.imgWifi.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wifi_3));
                    holder.imagWifiLock.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    holder.imgWifi.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wifi_2));
                    holder.imagWifiLock.setVisibility(View.VISIBLE);
                    break;
                case 3:
                case 4:
                    holder.imgWifi.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wifi_1));
                    holder.imagWifiLock.setVisibility(View.VISIBLE);
                    break;
            }
        }

        // 是否需要密码才能连接
        if (WifiUtils.getWifiCipher(bean.getCapabilities()).equals(WifiCipherType.WIFICIPHER_NOPASS)) {
            //不需要
            switch (bean.getLevel()) {
                case 1:
                    holder.imgWifi.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wifi_3));
                    holder.imagWifiLock.setVisibility(View.GONE);
                    break;
                case 2:
                    holder.imgWifi.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wifi_2));
                    holder.imagWifiLock.setVisibility(View.GONE);
                    break;
                case 3:
                case 4:
                    holder.imgWifi.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wifi_1));
                    holder.imagWifiLock.setVisibility(View.GONE);
                    break;
            }
        }



        holder.rootView.setOnClickListener(view -> onItemClickListener.onItemClick(view, position, bean));
    }

    /**
     * 替换所有数据源
     */
    public void replaceAll(List<WifiBean> datas) {
        if (resultList.size() > 0) {
            resultList.clear();
        }
        resultList.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }


    public interface onItemClickListener {
        void onItemClick(View view, int position, Object o);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        public TextView tvSsid;
        public TextView tvState;
        public ImageView imgWifi;
        public ImageView imagWifiLock;


        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.tvSsid = rootView.findViewById(R.id.tv_ssid);
            this.tvState = rootView.findViewById(R.id.tv_state);
            this.imgWifi = rootView.findViewById(R.id.img_wifi);
            this.imagWifiLock = rootView.findViewById(R.id.imag_wifi_lock);
        }

    }
}
