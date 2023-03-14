package com.sendi.deliveredrobot.view.fragment;

import android.graphics.Bitmap;

import com.sendi.deliveredrobot.view.fragment.AgeAndGender;
import com.sendi.deliveredrobot.view.fragment.FaceDetector;

import java.util.ArrayList;


public class FaceModule {

    private FaceDetector faceDetector;
    private AgeAndGender ageAndGender;

    private FaceRecognizer faceRecognizer;


    public FaceModule(String faceDetectorPath, String ageAndGenderPath, String faceRecognizermodelPath) {
        try {
            this.faceDetector = new FaceDetector(faceDetectorPath);
            this.ageAndGender = new AgeAndGender(ageAndGenderPath);
            this.faceRecognizer = new FaceRecognizer(faceRecognizermodelPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Info> preidct(Bitmap mBitmap, float xScal, float yScal, boolean ageAndGenderFlag, boolean faceRecognitionFlag, float [][] features) throws Exception {
        ArrayList<Info> infoArrayList;

        infoArrayList = faceDetector.predict(mBitmap);
//        System.out.println("#################################################");
        long T0 = System.currentTimeMillis();
        if (ageAndGenderFlag & (!faceRecognitionFlag)){
            for (int i=0; i < infoArrayList.size(); i++) {
                Info info = infoArrayList.get(i);
                int x = (int) (info.getRect().left * xScal);
                int y = (int) (info.getRect().top * yScal);
                int w = (int) (info.getRect().right * xScal - x);
                int h = (int) (info.getRect().bottom * yScal - y);

                Bitmap faceROI = Bitmap.createBitmap(mBitmap, x, y, w, h);

                AgeGender AG = ageAndGender.predict(faceROI);

                info.setAge(AG.getAge());
                info.setGender(AG.getGender());
            }
        }else if((!ageAndGenderFlag) & faceRecognitionFlag){
            for (int i=0; i < infoArrayList.size(); i++) {
                Info info = infoArrayList.get(i);
                int x = (int) (info.getRect().left * xScal);
                int y = (int) (info.getRect().top * yScal);
                int w = (int) (info.getRect().right * xScal - x);
                int h = (int) (info.getRect().bottom * yScal - y);

                Bitmap faceROI = Bitmap.createBitmap(mBitmap, x, y, w, h);

                faceRecognizer.predict(faceROI, features, info);
            }
        }else if (ageAndGenderFlag & ageAndGenderFlag) {
            for (int i = 0; i < infoArrayList.size(); i++) {
                Info info = infoArrayList.get(i);
                int x = (int) (info.getRect().left * xScal);
                int y = (int) (info.getRect().top * yScal);
                int w = (int) (info.getRect().right * xScal - x);
                int h = (int) (info.getRect().bottom * yScal - y);

                Bitmap faceROI = Bitmap.createBitmap(mBitmap, x, y, w, h);

                AgeGender AG = ageAndGender.predict(faceROI);
                info.setAge(AG.getAge());
                info.setGender(AG.getGender());

                faceRecognizer.predict(faceROI, features, info);
            }
        }

//        System.out.println("age gender rec:"+(System.currentTimeMillis()-T0));
        return infoArrayList;
    }

}
