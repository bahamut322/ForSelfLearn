package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import com.infisense.iruvc.utils.SynchronizedBitmap;

public class CameraView extends TextureView {
    private String TAG="CameraView";
    private Bitmap bitmap;
    private SynchronizedBitmap syncimage;
    private Runnable runnable;
    private Thread cameraThread;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setSyncimage(SynchronizedBitmap syncimage) {
        this.syncimage = syncimage;
    }

    public CameraView(Context context) {
        this(context, null, 0);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        runnable = () -> {
            Canvas canvas = null;
            while (!cameraThread.isInterrupted()) {
//old
//                    synchronized (syncimage.viewLock) {
//                        if (syncimage.valid == false) {
//                            try {
//                                syncimage.viewLock.wait();
//                            } catch (InterruptedException e) {
//                                cameraThread.interrupt();
//                                Log.e(TAG, "lock.wait(): catch an interrupted exception");
//                            }
//                        }
//                        if (syncimage.valid == true) {
//                            canvas = lockCanvas();
//                            if (canvas == null)
//                                continue;

                        //p2
                        /*Matrix matrix = new Matrix();
                        matrix.setRotate(90);
                        Bitmap newBM = Bitmap.create·(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        */
//                            Bitmap mScaledBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
//                            canvas.drawBitmap(mScaledBitmap, 0, 0, null);
//
//                            Paint paint = new Paint();  //画笔
//                            paint.setStrokeWidth(2);  //设置线宽。单位为像素
//                            paint.setAntiAlias(true); //抗锯齿
//                            paint.setColor(Color.WHITE);  //画笔颜色
//
//                            int cross_len = 20;
//                            canvas.drawLine(getWidth() / 2 - cross_len, getHeight() / 2,
//                                    getWidth() / 2 + cross_len, getHeight() / 2, paint);
//                            canvas.drawLine(getWidth() / 2, getHeight() / 2 - cross_len,
//                                    getWidth() / 2, getHeight() / 2 + cross_len, paint);
//                            unlockCanvasAndPost(canvas);
//                            syncimage.valid = false;
//                        }
//                    }



                        canvas = lockCanvas();
                        if (canvas == null)
                            continue;

                        //p2
                        /*Matrix matrix = new Matrix();
                        matrix.setRotate(90);
                        Bitmap newBM = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        */



                        try {
                            if(bitmap != null){
                                Bitmap mScaledBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
                                canvas.drawBitmap(mScaledBitmap, 0, 0, null);
                                mScaledBitmap.recycle();
                            }

                            unlockCanvasAndPost(canvas);
                        }catch (Exception e){
                            e.printStackTrace();
                            unlockCanvasAndPost(canvas);
                        }




                    }

                try {
                    cameraThread.sleep(20);
                } catch (InterruptedException e) {
                    Log.d(TAG, "sleep crash");
                    e.printStackTrace();
                    cameraThread.interrupt();

                }

            Log.w(TAG, "DisplayThread exit:");



        };


    }


    public boolean isRun = true;
    public void start() {
        isRun = true;
        cameraThread=new Thread(runnable);
        cameraThread.start();
    }

    public void stop() {
        isRun = false;
        cameraThread.interrupt();
        try {
            cameraThread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
