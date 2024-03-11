package com.sendi.deliveredrobot.view.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.entity.Table_Face;
import com.sendi.deliveredrobot.helpers.DialogHelper;
import com.sendi.deliveredrobot.interfaces.FaceDataListener;

import org.litepal.LitePal;

/**
 * @Author Swn
 * @Data 2023/12/12
 * @describe
 */
public class IdentifyDialog extends Dialog {

    public Context context;
    public TextView Tips;
    public EditText editText;
    public String name;
    public Button cancelBtn, submitBtn;
    public LifecycleOwner owner;
    public int anInt = 0;

    public IdentifyDialog(Context context, String name, LifecycleOwner owner) {
        super(context, R.style.Dialog);
        this.context = context;
        this.name = name;
        this.owner = owner;
    }

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_identify);
        Tips = findViewById(R.id.textView63);
        editText = findViewById(R.id.ed);
        submitBtn = findViewById(R.id.submitBtn1);
        cancelBtn = findViewById(R.id.cancelBtn1);
        cancelBtn.setOnClickListener(v -> dismiss());
        Tips.setText(name);
        Bitmap bitmap = BitmapFactory.decodeFile(name);
        submitBtn.setOnClickListener(v -> {
            FaceRecognition.INSTANCE.faceHttp(true, bitmap, owner, false);
            DialogHelper.loadingDialog.show();
            Toast.makeText(context, editText.getText(), Toast.LENGTH_LONG).show();
        });
        FaceDataListener.setOnChangeListener(() -> {
            anInt++;
            if (anInt < 5) {
                FaceRecognition.INSTANCE.faceHttp(true, bitmap, owner, false);
            } else {
                if (FaceDataListener.getFaceModels() != null && FaceDataListener.getFaceModels().size() == 1) {
                    Table_Face faceTips = new Table_Face();
                    String name = String.valueOf(editText.getText());
                    // 检查数据库中是否存在该名称

                    boolean isExist = LitePal.where("name = ?", name).count(Table_Face.class)> 0 ;
                    if (!isExist) {
                        // 如果名称不存在，则可以添加新记录
                        faceTips.setName(name);
                        faceTips.setSexual(FaceDataListener.getFaceModels().get(0).getFeat().toString());
                        faceTips.save();
                    }
                    DialogHelper.loadingDialog.dismiss();
                    Toast.makeText(context, "人脸添加成功", Toast.LENGTH_LONG).show();
                    dismiss();
                } else {
                    DialogHelper.loadingDialog.dismiss();
                    if (FaceDataListener.getFaceModels().size() > 1) {
                        Toast.makeText(context, "图片错误，请上传单个人脸的照片", Toast.LENGTH_LONG).show();
                    }
                    if (FaceDataListener.getFaceModels() == null) {
                        Toast.makeText(context, "未检测到人脸", Toast.LENGTH_LONG).show();
                    }
                    dismiss();
                }
            }
        });
    }


    @Override
    public void dismiss() {
        FaceDataListener.removeOnChangeListener();
        super.dismiss();

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

