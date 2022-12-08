package jni;

import android.util.Log;

public class Usbjni {
    public static native int usb3803_mode_setting(int i);

    public static native int usb3803_read_parameter(int i);

    static {
        try {
            System.loadLibrary("usb3803_hub");
        } catch (UnsatisfiedLinkError e) {
            Log.e("hcz", "Couldn't load lib:   - " + e.getMessage());
        }
    }
}
