package com.sendi.fooddeliveryrobot;

public class FvadWrapper {
    static {
        System.loadLibrary("fvad");
    }

    private long handle;

    public FvadWrapper() {
        handle = createFvad();
    }

    public void destroy() {
        destroyFvad(handle);
        handle = 0;
    }

    public void reset() {
        reset(handle);
    }

    public int setMode(int mode) {
        return setMode(handle, mode);
    }

    public int setSampleRate(int sampleRate) {
        return setSampleRate(handle, sampleRate);
    }

    public int process(short[] frame) {
        return process(handle, frame, frame.length);
    }

    private static native long createFvad();
    private static native void destroyFvad(long handle);
    private static native void reset(long handle);
    private static native int setMode(long handle, int mode);
    private static native int setSampleRate(long handle, int sampleRate);
    private static native int process(long handle, short[] frame, int length);
}
