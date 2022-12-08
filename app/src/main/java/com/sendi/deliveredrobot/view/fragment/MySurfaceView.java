package com.sendi.deliveredrobot.view.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.utils.ImgByteDealFunction;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @author Cui
 * 自定义SurfaceView
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas = null;
    private Paint paint = null;
    Info info;
    ArrayList<Info> infoArrayList = null;
    private Paint pianTV = null;
    private Paint paintTV1 = null;

    public MySurfaceView(Context context) {
        super(context);
        initView();

    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#0000FF"));
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(20);
        //返回SurfaceHolder，提供对该SurfaceView的基础表面的访问和控制
        this.mSurfaceHolder = getHolder();
        //注册回调方法
        this.mSurfaceHolder.addCallback(this);
        //画布透明处理
        this.setZOrderOnTop(true);
        this.mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Surface创建时触发
        try {
            if (info.getRect() != null && info != null) {
                new Thread(this).start();
            }
        } catch (Exception e) {
            e.getMessage();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Surface改变时触发
        width = 640;
        height = 480;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface销毁时触发
    }

    @Override
    public void run() {
        Drawing();
    }

    // 绘制人脸矩形框
    private void Drawing() {
        mCanvas = mSurfaceHolder.lockCanvas();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(paint);
        //判断画布是否存在 人脸数据是否为空
        if (null != mCanvas && info != null && info.getRect() != null) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mCanvas.drawPaint(paint);
            //创建一个for循环，更具infoArrayList的size去绘制人脸框
            for (int i = 0; i < infoArrayList.size(); i++) {
//                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC_OVER);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
//                //绘制矩形(稍后删除，以便于检测活体)
//                mCanvas.drawRect(infoArrayList.get(i).getRect().left*9/4,
//                        infoArrayList.get(i).getRect().top*9/4,
//                        infoArrayList.get(i).getRect().right*9/4,
//                        infoArrayList.get(i).getRect().bottom*9/4, paint);
                /**
                 * 已知关系：
                 * rect.right：矩形对象的右边缘，即右上顶点对应的x坐标（左上顶点的横坐标+矩形对象的长）。
                 * rect.left：矩形对象的左边缘，即左上顶点对应的x坐标。
                 * rect.top：矩形对象的上边缘，即左上顶点对应的y坐标。
                 * rect.bottom:矩形对象的下边缘，即左下顶点对应的y坐标（左上顶点的纵坐标+矩形对象的高）。
                 * rect.x：矩形对象的下边缘，即左下顶点对应的y坐标（左上顶点的纵坐标+矩形对象的高）。
                 * rect.y：矩形对象的上边缘，即左上顶点对应的y坐标。
                 * 推出关系：
                 * rect.x = rect.left
                 * rect.y = rect.top
                 * rect.dx = rect.width
                 * rect dy = rect.height
                 * 求RGB在红外上对应的人脸公式：
                 * 获取红外指定区域的温度，设RGB图片中的(x,y)，对应着的红外照片(u,v)，那么u=(x - 128)*0.5，v = (y-83)*0.5,同时width 和 heigh都要乘以0.5
                 */
                //将换算之后的位置传给红外
                try {
                    ImgByteDealFunction.getMaxTemplate((infoArrayList.get(i).getRect().left*9/4 - 128)/2,
                            (infoArrayList.get(i).getRect().top*9/4 - 83) / 2,
                            (infoArrayList.get(i).getRect().width()*9/4) / 2,
                            (infoArrayList.get(i).getRect().height()*9/4) / 2);
                }catch (Exception e){}
                System.out.println("打印温度" + ImgByteDealFunction.Temperature);
                //将folat数据只显示小数点后两位
                DecimalFormat decimalFormat=new DecimalFormat(".00");
                String pri=decimalFormat.format(ImgByteDealFunction.Temperature);
                if(ImgByteDealFunction.Temperature != 0){
                    //绘制矩形
                    mCanvas.drawRect(infoArrayList.get(i).getRect().left*9/4,
                            infoArrayList.get(i).getRect().top*9/4,
                            infoArrayList.get(i).getRect().right*9/4,
                            infoArrayList.get(i).getRect().bottom*9/4, paint);
                    pianTV = new Paint();
                    pianTV.setColor(Color.parseColor("#FF6079"));
                    pianTV.setStrokeWidth(2);
                    pianTV.setStyle(Paint.Style.STROKE);
                    pianTV.setTextSize(20);

                    paintTV1 = new Paint();
                    paintTV1.setColor(Color.parseColor("#FF3454"));
                    paintTV1.setStrokeWidth(2);
                    paintTV1.setStyle(Paint.Style.STROKE);
                    paintTV1.setTextSize(20);
                switch (infoArrayList.get(i).getMaskState()){
                    case 0:
                        mCanvas.drawRect(infoArrayList.get(i).getRect().left*9/4,
                                infoArrayList.get(i).getRect().top*9/4,
                                infoArrayList.get(i).getRect().right*9/4,
                                infoArrayList.get(i).getRect().bottom*9/4,pianTV);
                        mCanvas.drawRect(infoArrayList.get(i).getRect().left*9/4,
                                infoArrayList.get(i).getRect().top*9/4,
                                infoArrayList.get(i).getRect().right*9/4,
                                infoArrayList.get(i).getRect().bottom*9/4, paintTV1);
                        if (ImgByteDealFunction.Temperature> Universal.TemperatureMax){
                            mCanvas.drawText(pri, infoArrayList.get(i).getRect().left*9/4, infoArrayList.get(i).getRect().top*9/4-30, paintTV1);
                        }else {
                            mCanvas.drawText(pri, infoArrayList.get(i).getRect().left*9/4,infoArrayList.get(i).getRect().top*9/4-30, paint);
                        }
                        mCanvas.drawText("未佩戴口罩", infoArrayList.get(i).getRect().left*9/4, infoArrayList.get(i).getRect().top*9/4-5, pianTV);
                        break;
                    case 1:
                        mCanvas.drawRect(infoArrayList.get(i).getRect().left*9/4,
                                infoArrayList.get(i).getRect().top*9/4,
                                infoArrayList.get(i).getRect().right*9/4,
                                infoArrayList.get(i).getRect().bottom*9/4, pianTV);
                        mCanvas.drawRect(infoArrayList.get(i).getRect().left*9/4,
                                infoArrayList.get(i).getRect().top*9/4,
                                infoArrayList.get(i).getRect().right*9/4,
                                infoArrayList.get(i).getRect().bottom*9/4, paintTV1);
                        if (ImgByteDealFunction.Temperature> Universal.TemperatureMax){
                            mCanvas.drawText(pri, infoArrayList.get(i).getRect().left*9/4, infoArrayList.get(i).getRect().top*9/4-30, paintTV1);
                        }else {
                            mCanvas.drawText(pri, infoArrayList.get(i).getRect().left*9/4,infoArrayList.get(i).getRect().top*9/4-30, paint);
                        }
                        mCanvas.drawText("佩戴口罩不规范", infoArrayList.get(i).getRect().left*9/4, infoArrayList.get(i).getRect().top*9/4-5, pianTV);
                        break;
                    case  2:
                        if (ImgByteDealFunction.Temperature> Universal.TemperatureMax){
                            mCanvas.drawRect(infoArrayList.get(i).getRect().left*9/4,
                                    infoArrayList.get(i).getRect().top*9/4,
                                    infoArrayList.get(i).getRect().right*9/4,
                                    infoArrayList.get(i).getRect().bottom*9/4, pianTV);
                            mCanvas.drawRect(infoArrayList.get(i).getRect().left*9/4,
                                    infoArrayList.get(i).getRect().top*9/4,
                                    infoArrayList.get(i).getRect().right*9/4,
                                    infoArrayList.get(i).getRect().bottom*9/4, paintTV1);
                            mCanvas.drawText(pri, infoArrayList.get(i).getRect().left*9/4, infoArrayList.get(i).getRect().top*9/4-5, paintTV1);
                        }else {
                            mCanvas.drawText(pri, infoArrayList.get(i).getRect().left*9/4, infoArrayList.get(i).getRect().top*9/4-5, paint);
                        }
                        break;
                    default:
                        return;
                }
            }

        }
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);

            }else if (info == null){
            mCanvas.drawColor(0,PorterDuff.Mode.CLEAR);
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }

        }

}

