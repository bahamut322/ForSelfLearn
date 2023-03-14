package com.sendi.deliveredrobot.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.sendi.deliveredrobot.mnn.MNNForwardType;
import com.sendi.deliveredrobot.mnn.MNNImageProcess;
import com.sendi.deliveredrobot.mnn.MNNNetInstance;

import java.io.File;
import java.util.ArrayList;

public class FaceRecognizer {
    private MNNNetInstance mNetInstance;
    private MNNNetInstance.Session mSession;
    private MNNNetInstance.Session.Tensor mInputTensor;
    private final MNNImageProcess.Config dataConfig;
    private Matrix imgData;

    /**
     * @param modelPath model path
     */
    public FaceRecognizer(String modelPath) throws Exception {
        dataConfig = new MNNImageProcess.Config();
        dataConfig.mean = new float[] {128.0f, 128.0f, 128.0f};
        dataConfig.normal = new float[] {0.0078125f, 0.0078125f, 0.0078125f};
        dataConfig.dest = MNNImageProcess.Format.GRAY;
        imgData = new Matrix();

        File file = new File(modelPath);
        if (!file.exists()) {
            throw new Exception("model file is not exists!");
        }
        try {
            mNetInstance = MNNNetInstance.createFromFile(modelPath);
            MNNNetInstance.Config config = new MNNNetInstance.Config();
            config.numThread = Utils.NUM_THREADS;
            config.forwardType = MNNForwardType.FORWARD_VULKAN.type;
            mSession = mNetInstance.createSession(config);
            mInputTensor = mSession.getInput(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("load model fail!");
        }
    }

    // prediction
    public void predict(Bitmap bmp, float[][] features, Info info) throws Exception {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, 128, 128, true);
        float[] confs = new float[features.length];

        imgData.reset();
//        imgData.postScale(64 / (float) bmp.getWidth(), 64 / (float) bmp.getHeight());
        imgData.invert(imgData);
        MNNImageProcess.convertBitmap(resizedBitmap, mInputTensor, dataConfig, imgData);

        try {
            mSession.run();
        } catch (Exception e) {
            throw new Exception("predict image fail! log:" + e);
        }

        float[] output = mSession.getOutput(null).getFloatData();
        info.setFeature(output);

        long T0 = System.currentTimeMillis();
        float outputHat = 0;
        float outputHat2 = 0;
        for (float ele : output) {
            outputHat2 += ele * ele;
        }
        outputHat = (float) Math.sqrt(outputHat2);

        for (int i=0; i<features.length; i++) {
            float dot = 0;
            float featureHat2 = 0;
            float[] feature = features[i];
            for (int j = 0; j < feature.length; j++) {
                dot += output[j] * feature[j];
                featureHat2 += feature[j] * feature[j];
            }
            float cosSim = dot /(float) (outputHat * Math.sqrt(featureHat2) + 1e-10);
            float cosSimNorm = (float) (0.5 + 0.5 * cosSim);
            confs[i] = cosSimNorm;
            System.out.println("one rec end...");
        }
        info.setConfList(confs);
//        return info;
        long T1 = System.currentTimeMillis();
        System.out.println("feat compare: " + (T1-T0));
    }

    public void release() {
        if (mNetInstance != null) {
            mNetInstance.release();
            mNetInstance = null;
        }
    }
}
