package com.sendi.deliveredrobot.camera;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;


import com.infisense.iruvc.usb.DeviceFilter;
import com.infisense.iruvc.usb.IFrameCallback;
import com.infisense.iruvc.usb.USBMonitor;
import com.infisense.iruvc.usb.UVCCamera;
import com.infisense.iruvc.utils.SynchronizedBitmap;
import com.sendi.deliveredrobot.utils.ImgByteDealFunction;

import java.util.HashMap;
import java.util.List;

public class IRUVC {
    private final IFrameCallback iFrameCallback;
    private final Context context;
    public UVCCamera uvcCamera;
    public USBMonitor mUSBMonitor;
    private int cameraWidth;
    private int cameraHeight;
    private byte[] image;
    private byte[] temperature;
    private SynchronizedBitmap syncimage;
    public int pid=0x5840;
    public boolean valid = false;
    private static final String TAG = "IRUVC";
    private boolean isRequest=false;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private Handler handler;
    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    public boolean rotate=false;

    public void setImage(byte[] image) {
        this.image=image;
    }

    public void setTemperature(byte[] temperature) {
        this.temperature=temperature;
    }

    public IRUVC(int cameraHeight, int cameraWidth, Context context, SynchronizedBitmap syncimage) {
        this.cameraHeight=cameraHeight;
        this.cameraWidth=cameraWidth;
        this.context = context;
        this.syncimage=syncimage;
        init(cameraHeight, cameraWidth,context );
        mUSBMonitor = new USBMonitor(context, new USBMonitor.OnDeviceConnectListener() {

            // called by checking usb device
            // do request device permission
            @Override
            public void onAttach(UsbDevice device) {
                Log.w(TAG, "USBMonitor"+"onAttach"+device.getProductId());
              //  Toast.makeText(context, device.getProductId(), Toast.LENGTH_SHORT).show();
               if(pid!=0 &&device.getProductId()==pid) {
                    if (uvcCamera == null || !uvcCamera.getOpenStatus()) {
                        Log.w(TAG, "USBMonitor"+"onAttach requestPermission" + pid);
                        isDeviceConnected(context,pid);
                        if(!isRequest){
                            isRequest = true;
                            mUSBMonitor.requestPermission(device);
                        }
                    }
                }
            }

            // called by taking out usb device
            // do close camera
            @Override
            public void onDettach(UsbDevice device) {
                Log.w(TAG, "onDettach");

                if(pid!=0 &&device!=null&&device.getProductId()==pid) {

                    if (isRequest) {
                        isRequest = false;
                        stop();
                    }
                }


            }

            // called by connect to usb camera
            // do open camera,start previewing
            @Override
            public void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                Log.w(TAG, "onConnect");
                if(pid!=0 &&device.getProductId()!=pid)return;
                if(createNew) {
                    open(ctrlBlock);
                    start();
                }
            }
            // called by disconnect to usb camera
            // do nothing
            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                Log.w(TAG, "onDisconnect");
            }

            @Override
            public void onCancel(UsbDevice device) {
            }
        });
        iFrameCallback = frame -> {
            if(isRequest){
                Log.w(TAG, "onFrame");
                ImgByteDealFunction.setImgBytes(frame);

            }



        };

    }

    public void init(int cameraHeight, int cameraWidth,Context context ) {
        Log.w(TAG, "init");
        uvcCamera = new UVCCamera(cameraWidth, cameraHeight,context );
        uvcCamera.create();


    }

    public void registerUSB() {
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
    }

    public void unregisterUSB() {
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
        }
    }
    public List<UsbDevice> getUsbDeviceList() {
        List<DeviceFilter> deviceFilters = DeviceFilter
                .getDeviceFilters(context, com.infisense.iruvc.libusbcamera.R.xml.device_filter);
        if (mUSBMonitor == null || deviceFilters == null )
//            throw new NullPointerException("mUSBMonitor ="+mUSBMonitor+"deviceFilters=;"+deviceFilters);
            return null;
        // matching all of filter devices
        return mUSBMonitor.getDeviceList(deviceFilters);
    }
    public void requestPermission(int index) {
        List<UsbDevice> devList = getUsbDeviceList();
        if (devList == null || devList.size() == 0) {
            return;
        }
        int count = devList.size();
        if (index >= count)
            new IllegalArgumentException("index illegal,should be < devList.size()");
        if (mUSBMonitor != null) {
            mUSBMonitor.requestPermission(getUsbDeviceList().get(index));
        }
    }
    public void open(USBMonitor.UsbControlBlock ctrlBlock)
    {
        if(ctrlBlock.getProductId()==0x3901)
            if(syncimage!=null)syncimage.type=1;
        if (uvcCamera == null) {
            init(cameraHeight, cameraWidth,context );
        }
        uvcCamera.open(ctrlBlock);
    }
    public void start() {
        Log.w(TAG, "start");
        uvcCamera.setOpenStatus(true);
        uvcCamera.setFrameCallback(iFrameCallback);
        //uvcCamera.setgetframemode(uvcCamera.GET_FRAME_ASYNC);
        //default sync mode for some devices  Lost-Packet
        uvcCamera.startPreview();

    }

    public void isDeviceConnected(Context context, int productId) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbManager != null) {
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            for (UsbDevice device : deviceList.values()) {
                if (device.getProductId() == productId) {
                    return;
                }
            }
        }
    }


    public void stop()
    {
        Log.w(TAG, "stop");
        if (uvcCamera != null) {
            if(uvcCamera.getOpenStatus())
                uvcCamera.stopPreview();
            final UVCCamera camera;
            camera = uvcCamera;
            uvcCamera = null;
            SystemClock.sleep(200);
            camera.destroy();
        }
    }

}

