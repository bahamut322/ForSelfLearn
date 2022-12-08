package com.sendi.deliveredrobot.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;


import com.sendi.deliveredrobot.mnn.MNNForwardType;
import com.sendi.deliveredrobot.mnn.MNNImageProcess;
import com.sendi.deliveredrobot.mnn.MNNNetInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


public class FaceDetector {
    private static Float maxPro;
    private static int maxIndex;
    private MNNNetInstance mNetInstance;
    private MNNNetInstance.Session mSession;
    private MNNNetInstance.Session.Tensor mInputTensor;
    private final MNNImageProcess.Config dataConfig;
    private Matrix imgData;


    /**
     * @param modelPath model path
     */
    public FaceDetector(String modelPath) throws Exception {
        dataConfig = new MNNImageProcess.Config();
        dataConfig.mean = new float[]{128.0f, 128.0f, 128.0f};
        dataConfig.normal = new float[]{0.0078125f, 0.0078125f, 0.0078125f};
        dataConfig.dest = MNNImageProcess.Format.RGBA;
        imgData = new Matrix();

        File file = new File(modelPath);
        if (!file.exists()) {
            throw new Exception("model file is not exists!");
        }
        try {
            mNetInstance = MNNNetInstance.createFromFile(modelPath);
            MNNNetInstance.Config config = new MNNNetInstance.Config();
            config.numThread = Utils.NUM_THREADS;
            config.forwardType = MNNForwardType.FORWARD_CPU.type;
            mSession = mNetInstance.createSession(config);
            mInputTensor = mSession.getInput(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("load model fail!");
        }
    }



    static float IOU(Rect a, Rect b) {
        float areaA = (a.right - a.left) * (a.bottom - a.top);
        if (areaA <= 0.0) return 0.0f;

        float areaB = (b.right - b.left) * (b.bottom - b.top);
        if (areaB <= 0.0) return 0.0f;

        float intersectionMinX = Math.max(a.left, b.left);
        float intersectionMinY = Math.max(a.top, b.top);
        float intersectionMaxX = Math.min(a.right, b.right);
        float intersectionMaxY = Math.min(a.bottom, b.bottom);
        float intersectionArea = Math.max(intersectionMaxY - intersectionMinY, 0) *
                Math.max(intersectionMaxX - intersectionMinX, 0);
        return intersectionArea / (areaA + areaB - intersectionArea);
    }


    static ArrayList<Info> nonMaxSuppression(ArrayList<Result> boxes, int limit, float threshold) {
        ArrayList<Info> select = new ArrayList<>();
        // Do an argsort on the confidence scores, from high to low.
        Collections.sort(boxes,
                new Comparator<Result>() {
                    @Override
                    public int compare(Result o1, Result o2) {
                        return o1.getScore().compareTo(o2.getScore());
                    }
                });

        boolean[] active = new boolean[boxes.size()];
        Arrays.fill(active, true);
        int numActive = active.length;

        // The algorithm is simple: Start with the box that has the highest score.
        // Remove any remaining boxes that overlap it more than the given threshold
        // amount. If there are any boxes left (i.e. these did not overlap with any
        // previous boxes), then repeat this procedure, until no more boxes remain
        // or the limit has been reached.
        boolean done = false;
        for (int i=0; i<boxes.size() && !done; i++) {
            if (active[i]) {
                Result boxA = boxes.get(i);
                select.add(new Info(boxA.getRect(), boxA.getMaskState()));
                if (select.size() >= limit) break;

                for (int j=i+1; j<boxes.size(); j++) {
                    if (active[j]) {
                        Result boxB = boxes.get(j);
                        if (IOU(boxA.getRect(), boxB.getRect()) > threshold) {
                            active[j] = false;
                            numActive -= 1;
                            if (numActive <= 0) {
                                done = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return select;
    }


    static ArrayList<Info> outputsToNMSPredictions(float[] output) {
        ArrayList<Result> results = new ArrayList<>();
        for (int i = 0; i < Utils.mOutputRow; i++) {
            if (output[i*Utils.mOutputColumn +4] > Utils.mThreshold) {
                float x_center = output[i*Utils.mOutputColumn];
                float y_center = output[i*Utils.mOutputColumn +1];
                float w = output[i*Utils.mOutputColumn +2];
                float h = output[i*Utils.mOutputColumn +3];

                int left = (int) (Math.max(x_center - w / 2.0, 0));
                int top = (int) (Math.max(y_center - h / 2.0, 0));
                int right = (int) (Math.min(x_center + w / 2.0, Utils.inputWidth));
                int bottom = (int) (Math.min(y_center + h / 2.0, Utils.inputHeight));

                if (output[i*Utils.mOutputColumn +5] > output[i*Utils.mOutputColumn +6]){
                    if (output[i*Utils.mOutputColumn +5] > output[i*Utils.mOutputColumn +7]){
                        maxIndex = 0;
                        maxPro = output[i*Utils.mOutputColumn +4] * output[i*Utils.mOutputColumn +5];
                    } else{
                        maxIndex = 2;
                        maxPro = output[i*Utils.mOutputColumn +4] * output[i*Utils.mOutputColumn +7];
                    }
                } else{
                    if (output[i*Utils.mOutputColumn +6] < output[i*Utils.mOutputColumn +7]){
                        maxIndex = 2;
                        maxPro = output[i*Utils.mOutputColumn +4] * output[i*Utils.mOutputColumn +7];
                    } else{
                        maxIndex = 1;
                        maxPro = output[i*Utils.mOutputColumn +4] * output[i*Utils.mOutputColumn +6];
                    }
                }

                Rect rect = new Rect(left, top, right, bottom);
                Result result = new Result(maxIndex, maxPro, rect);
                results.add(result);
            }
        }
        return nonMaxSuppression(results, Utils.mNmsLimit, Utils.mIoUThreshold);
    }


    // prediction
    public ArrayList<Info> predict(Bitmap bmp) throws Exception {
        ArrayList<Info> infoArrayList;
        imgData.reset();
        imgData.postScale(Utils.inputWidth / (float) bmp.getWidth(), Utils.inputHeight / (float) bmp.getHeight());
        imgData.invert(imgData);
        MNNImageProcess.convertBitmap(bmp, mInputTensor, dataConfig, imgData);

        try {
            mSession.run();
        } catch (Exception e) {
            throw new Exception("predict image fail! log:" + e);
        }
        float[] output = mSession.getOutput("output").getFloatData();
//        float[] boxes = mSession.getOutput("boxes").getFloatData();

        infoArrayList = outputsToNMSPredictions(output);

        return infoArrayList;
    }

    public void release() {
        if (mNetInstance != null) {
            mNetInstance.release();
            mNetInstance = null;
        }
    }
}
