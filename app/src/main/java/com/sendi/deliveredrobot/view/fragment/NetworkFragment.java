package com.sendi.deliveredrobot.view.fragment;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.utils.CollectionUtils;
import com.sendi.deliveredrobot.utils.WifiUtils;
import com.sendi.deliveredrobot.view.bean.WiFiSettingAdapter;
import com.sendi.deliveredrobot.view.bean.WifiBean;
import com.sendi.deliveredrobot.view.broadcastreceiver.WifiBroadcastReceiver;
import com.sendi.deliveredrobot.view.contants.WifiCipherType;
import com.sendi.deliveredrobot.view.contants.WifiConnectType;
import com.sendi.deliveredrobot.view.widget.WifiLinkDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author swn
 * 网络设置
 */
public class NetworkFragment extends Fragment {

    View view = null;
    // 权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;
    // 两个权限需要动态申请
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    ViewHolder mViewHolder;
    private boolean mHasPermission; // 是否通过权限
    public WiFiSettingAdapter mWiFiSettingAdapter;
    public List<WifiBean> mRealWifiList = new ArrayList<>();
    private WifiBroadcastReceiver mWifiReceiver;
    public int mConnectType = 0; // 1：连接成功 2：正在连接（如果wifi热点列表发生变需要该字段）
    private Timer timer; // 用于轮询获取wifi列表
    private TimerTask timerTask; // 用于轮询获取wifi列表


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_network, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        com.thanosfisherman.wifiutils.WifiUtils.enableLog(true);
        mViewHolder = new ViewHolder(NetworkFragment.this);
        mWiFiSettingAdapter = new WiFiSettingAdapter(getActivity(), mRealWifiList);
        mViewHolder.rvWifi.setLayoutManager(new LinearLayoutManager(getActivity()));
        mViewHolder.rvWifi.setAdapter(mWiFiSettingAdapter);
        init();
        initListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 注册广播
        mWifiReceiver = new WifiBroadcastReceiver(NetworkFragment.this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); // 监听wifi是开关变化的状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION); // 监听wifi连接状态广播,是否连接了一个有效路由
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION); // 监听wifi列表变化（开启一个热点或者关闭一个热点）
        getActivity().registerReceiver(mWifiReceiver, filter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mWifiReceiver);
        this.mCompositeDisposable.clear();
        super.onPause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllGranted = true;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                hasAllGranted = false;
                break;
            }
        }
        if (hasAllGranted) {
            // 权限请求成功
            init();
        }
    }

    /**
     * 判断权限是否通过，如果通过就初始化
     */
    private void init() {
        mHasPermission = checkPermission();
        if (!mHasPermission && WifiUtils.isOpenWifi()) {
            // 未获取权限，申请权限
            requestPermission();
        } else if (mHasPermission && WifiUtils.isOpenWifi()) {
            // 已经获取权限
            initData();
//            startTimeTask();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "WIFI处于关闭状态", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), NEEDED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    /**
     * 加载
     */
    private void initData() {
        if (WifiUtils.isOpenWifi()) {
            mViewHolder.cbWifiSwitch.setChecked(true);
        } else {
            mViewHolder.cbWifiSwitch.setChecked(false);
        }
        if (WifiUtils.isOpenWifi() && mHasPermission) {
            queryWifiList();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "WIFI处于关闭状态或权限获取失败", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * 初始化事件
     */
    @SuppressLint("WrongConstant")
    private void initListener() {
        mWiFiSettingAdapter.setOnItemClickListener((view, position, o) -> {
            WifiBean wifiBean = mRealWifiList.get(position);
            if (wifiBean.getState().equals(WifiConnectType.WIFI_STATE_UNCONNECT) || wifiBean.getState().equals(WifiConnectType.WIFI_STATE_CONNECT)) {
                String capabilities = mRealWifiList.get(position).getCapabilities();
                if (WifiUtils.getWifiCipher(capabilities).equals(WifiCipherType.WIFICIPHER_NOPASS)) {
                    // 无需密码，直接连接
                    WifiConfiguration tempConfig = WifiUtils.isExsits(wifiBean.getWifiName());
                    if (tempConfig == null) {
                        WifiConfiguration exsits = WifiUtils.createWifiConfig(wifiBean.getWifiName(), null, WifiCipherType.WIFICIPHER_NOPASS);
                        WifiUtils.addNetWork(exsits);
                    } else {
                        WifiUtils.addNetWork(tempConfig);
                    }
                } else {
                    // 需要密码，弹出输入密码dialog
                    WifiLinkDialog linkDialog = new WifiLinkDialog(getActivity(), R.style.simpleDialogStyle, mRealWifiList.get(position).getWifiName(), mRealWifiList.get(position).getCapabilities());
                    if (!linkDialog.isShowing()) {
                        linkDialog.show();
                    }
                }
            }
        });
        mViewHolder.cbWifiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 打开wifi
                com.thanosfisherman.wifiutils.WifiUtils.withContext(getActivity().getApplicationContext()).enableWifi(isSuccess -> {
                    // 显示view
//                    mViewHolder.llWiFi.setVisibility(View.VISIBLE);
                    queryWifiList();
                });
            } else {
                // 关闭wifi
                com.thanosfisherman.wifiutils.WifiUtils.withContext(getActivity().getApplicationContext()).disableWifi();
                // 隐藏view
//                mViewHolder.llWiFi.setVisibility(View.GONE);
                // 清除数据
                mRealWifiList.clear();
                mWiFiSettingAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 查询wifi列表
     */
    public void queryWifiList() {
        com.thanosfisherman.wifiutils.WifiUtils.withContext(getActivity().getApplicationContext()).scanWifi(scanResults ->
                Observable.create((ObservableOnSubscribe<List<WifiBean>>) emitter -> {
                            if (!emitter.isDisposed()) {
                                List<WifiBean> wifiBeans = sortScaResult(scanResults);
                                WifiInfo connectedWifiInfo = WifiUtils.getConnectedWifiInfo();
                                if (connectedWifiInfo != null) {
                                    wifiBeans = wifiListSet(wifiBeans, connectedWifiInfo.getSSID(), mConnectType);
                                }
                                emitter.onNext(wifiBeans);
                                emitter.onComplete();
                            }
                        }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<WifiBean>>() {

                            @Override
                            public void onSubscribe(Disposable d) {
                                mCompositeDisposable.add(d);
                            }

                            @Override
                            public void onNext(List<WifiBean> s) {
                                mWiFiSettingAdapter.setData(s);
                                mWiFiSettingAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onComplete() {

                            }
                        })).start();
    }

    /**
     * 获取wifi列表然后将bean转成自己定义的WifiBean
     */
    private List<WifiBean> sortScaResult(List<ScanResult> list) {
        List<ScanResult> scanResults = WifiUtils.noSameName(list);
        List<WifiBean> realWifiList = new ArrayList<>();
        if (!CollectionUtils.isNullOrEmpty(scanResults)) {
            for (int i = 0; i < scanResults.size(); i++) {
                WifiBean wifiBean = new WifiBean();
                wifiBean.setWifiName(scanResults.get(i).SSID); // wifi名字
                wifiBean.setState(WifiConnectType.WIFI_STATE_UNCONNECT); // 只要获取都假设设置成未连接，真正的状态都通过广播来确定
                wifiBean.setCapabilities(scanResults.get(i).capabilities); // 加密方式
                wifiBean.setLevel(WifiUtils.getLevel(scanResults.get(i).level));
                realWifiList.add(wifiBean);

                // 根据信号等级排序
                Collections.sort(realWifiList);
            }
        }
        return realWifiList;
    }

    /**
     * 轮询获取wifi列表
     */
    private void startTimeTask() {
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    WifiUtils.scanStart();
                    queryWifiList();
                }
            };
        }
        timer.schedule(timerTask, 0, 2 * 1000);
    }

    public void wifiListSetView(String wifiName, int type) {
        com.thanosfisherman.wifiutils.WifiUtils.withContext(getActivity().getApplicationContext()).scanWifi(scanResults ->
                Observable.create((ObservableOnSubscribe<List<WifiBean>>) emitter -> {
                            if (!emitter.isDisposed()) {
                                List<WifiBean> wifiBeans = sortScaResult(scanResults);
                                wifiBeans = wifiListSet(wifiBeans, wifiName, type);
                                emitter.onNext(wifiBeans);
                                emitter.onComplete();
                            }
                        }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<WifiBean>>() {

                            @Override
                            public void onSubscribe(Disposable d) {
                                mCompositeDisposable.add(d);
                            }

                            @Override
                            public void onNext(List<WifiBean> s) {
                                mWiFiSettingAdapter.setData(s);
                                mWiFiSettingAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onComplete() {

                            }
                        })).start();
    }

    /**
     * 将"已连接"或者"正在连接"的wifi热点放置在第一个位置
     *
     * @param wifiName wifiName
     * @param type     连接状态
     */
    private List<WifiBean> wifiListSet(List<WifiBean> realWifiList, String wifiName, int type) {
        int index = -1;
        WifiBean wifiInfo = new WifiBean();
        if (CollectionUtils.isNullOrEmpty(realWifiList)) {
            return null;
        }
        for (int i = 0; i < realWifiList.size(); i++) {
            realWifiList.get(i).setState(WifiConnectType.WIFI_STATE_UNCONNECT);
        }
        // 根据信号强度排序
        Collections.sort(realWifiList);
        // 拿到相同的wifiName
        for (int i = 0; i < realWifiList.size(); i++) {
            WifiBean wifiBean = realWifiList.get(i);
            if (index == -1 && ("\"" + wifiBean.getWifiName() + "\"").equals(wifiName)) {
                index = i;
                wifiInfo.setLevel(wifiBean.getLevel());
                wifiInfo.setWifiName(wifiBean.getWifiName());
                wifiInfo.setCapabilities(wifiBean.getCapabilities());
                if (type == 1) {
                    wifiInfo.setState(WifiConnectType.WIFI_STATE_CONNECT);
                } else {
                    wifiInfo.setState(WifiConnectType.WIFI_STATE_ON_CONNECTING);
                }
            }
        }
        // 迁移到第一个位置
        if (index != -1) {
            realWifiList.remove(index);
            realWifiList.add(0, wifiInfo);
        }
        return realWifiList;
    }

    /**
     * @return 检查是否已经授予权限
     */
    private boolean checkPermission() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }



    public static class ViewHolder {

        public CheckBox cbWifiSwitch;
        public RecyclerView rvWifi;
        public LinearLayout llWiFi;

        public ViewHolder(NetworkFragment rootView) {
            this.cbWifiSwitch = (CheckBox) rootView.view.findViewById(R.id.cb_wifi_switch);
            this.rvWifi = (RecyclerView) rootView.view.findViewById(R.id.rv_wifi);
        }

    }

}