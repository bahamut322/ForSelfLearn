package com.iflytek.vtncaetest.recorder;



import com.iflytek.vtncaetest.engine.EngineConstants;
/**
 * 初始化不同类型的录音机
 */
public class RecorderFactory  {

    public static AudioRecorder getRecorder(){
        //初始化录音
        if(EngineConstants.recorderType==0||EngineConstants.recorderType==3){
            return SystemRecorder.getInstance(); //单麦系统录音
        }else if(EngineConstants.recorderType==1){
            return new SingleAlsaRecorder(); // 多麦，1声卡录音
        }else if(EngineConstants.recorderType==2){
            return new DoubleAlsaRecorder(); // 多麦，2声卡录音
        }
        return null;
    }
}
