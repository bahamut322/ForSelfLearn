package com.sendi.deliveredrobot.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Matrix;


import com.sendi.deliveredrobot.mnn.MNNForwardType;
import com.sendi.deliveredrobot.mnn.MNNImageProcess;
import com.sendi.deliveredrobot.mnn.MNNNetInstance;

import java.io.File;


public class AgeAndGender {
    private MNNNetInstance mNetInstance;
    private MNNNetInstance.Session mSession;
    private MNNNetInstance.Session.Tensor mInputTensor;
    private final MNNImageProcess.Config dataConfig;
    private Matrix imgData;

    /**
     * @param modelPath model path
     */
    public AgeAndGender(String modelPath) throws Exception {
        dataConfig = new MNNImageProcess.Config();
        dataConfig.mean = new float[] {128.0f, 128.0f, 128.0f};
        dataConfig.normal = new float[] {0.0078125f, 0.0078125f, 0.0078125f};
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


    // prediction
    public AgeGender predict(Bitmap bmp) throws Exception {
        String gender = "boy";
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, 64, 64, true);


        imgData.reset();
//        imgData.postScale(64 / (float) bmp.getWidth(), 64 / (float) bmp.getHeight());
        imgData.invert(imgData);
        MNNImageProcess.convertBitmap(resizedBitmap, mInputTensor, dataConfig, imgData);

        try {
            mSession.run();
        } catch (Exception e) {
            throw new Exception("predict image fail! log:" + e);
        }

        float[] age = mSession.getOutput("age").getFloatData();
        float[] genderArray = mSession.getOutput("gender").getFloatData();
        if (genderArray[0] > genderArray[1]) {
            gender = "gril";
        }

        AgeGender AG = new AgeGender((int) age[0], gender);

        return AG;
    }

    public void release() {
        if (mNetInstance != null) {
            mNetInstance.release();
            mNetInstance = null;
        }
    }
}
