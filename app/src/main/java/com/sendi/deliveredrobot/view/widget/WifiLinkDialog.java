package com.sendi.deliveredrobot.view.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.sendi.deliveredrobot.R;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;


public class WifiLinkDialog extends Dialog {

    private ViewHolder mViewHolder;
    private String wifiName;
    private String capabilities;
    private Context mContext;


    public WifiLinkDialog(@NonNull Context context, @StyleRes int themeResId, String wifiName, String capabilities) {
        super(context, R.style.Dialog);
        this.wifiName = wifiName;
        this.capabilities = capabilities;
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_wifi_link, null);
        setContentView(view);
        mViewHolder = new ViewHolder(view);
        mViewHolder.tvTitle.setText(wifiName);
        initListener();
    }

    private void initListener() {
        mViewHolder.etValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 一些格式识别
                if ((capabilities.contains("WPA") || capabilities.contains("WPA2") || capabilities.contains("WPS"))) {
                    if (mViewHolder.etValue.getText() == null || mViewHolder.etValue.getText().toString().length() < 8) {
                        mViewHolder.tvOK.setOnClickListener(null);
                        mViewHolder.tvOK.setTextColor(getContext().getResources().getColor(R.color.gray_home));
                    } else {
                        mViewHolder.tvOK.setOnClickListener(new OnOKListener());
                        mViewHolder.tvOK.setTextColor(getContext().getResources().getColor(R.color.blue));
                    }
                } else if (capabilities.contains("WEP")) {
                    if (mViewHolder.etValue.getText() == null || mViewHolder.etValue.getText().toString().length() < 8) {
                        mViewHolder.tvOK.setOnClickListener(null);
                        mViewHolder.tvOK.setTextColor(getContext().getResources().getColor(R.color.gray_home));
                    } else {
                        mViewHolder.tvOK.setOnClickListener(new OnOKListener());
                        mViewHolder.tvOK.setTextColor(getContext().getResources().getColor(R.color.blue));
                    }
                }
            }
        });
        mViewHolder.tvClose.setOnClickListener(v -> dismiss());
    }

    /**
     * 点击事件
     */
    private class OnOKListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            WifiUtils.withContext(mContext)
                    .connectWith(wifiName, mViewHolder.etValue.getText().toString())
                    .setTimeout(40000)
                    .onConnectionResult(new ConnectionSuccessListener() {
                        @Override
                        public void success() {
//                            Toast.makeText(mContext, "SUCCESS!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed(@NonNull ConnectionErrorCode errorCode) {
                            Toast.makeText(mContext, "EPIC FAIL!" + errorCode.toString(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .start();
//            WifiConfiguration tempConfig = WifiUtils.isExsits(wifiName);
//            if (tempConfig == null) {
            // 如果以前没连接过，重新连接
//                WifiConfiguration wifiConfiguration = WifiUtils.createWifiConfig(wifiName, mViewHolder.etValue.getText().toString(), WifiUtils.getWifiCipher(capabilities));
//                WifiUtils.addNetWork(wifiConfiguration);
//            } else {
//                // 直接连接
//                WifiUtils.addNetWork(tempConfig);
//            }
            dismiss();
        }

    }

    public static class ViewHolder {
        public View rootView;
        public TextView tvTitle;
        public ConstraintLayout tvClose;
        public TextView tvOK;
        public EditText etValue;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.tvTitle = rootView.findViewById(R.id.tv_connect_tip);
            this.tvClose = rootView.findViewById(R.id.imgv_close);
            this.tvOK = rootView.findViewById(R.id.btn_connect);
            this.etValue = rootView.findViewById(R.id.edt_pwd);
        }

    }

    /**
     * 调用弹窗全屏显示方法
     */
    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);//设置全屏
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    }

    /**
     * 弹窗弹出时全屏显示
     * @param view
     */
    private void fullScreenImmersive(View view) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(uiOptions);
    }
}
