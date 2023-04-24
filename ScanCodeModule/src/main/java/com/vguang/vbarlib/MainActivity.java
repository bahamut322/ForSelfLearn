//package com.vguang.vbarlib;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.os.Bundle;
//import android.text.method.ScrollingMovementMethod;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;
//
//import com.sun.jna.ptr.IntByReference;
//import com.vguang.Vbar;
//
//public class MainActivity extends Activity {
//
//    private TextView decodeStr;
//    private Button opendev, lignton, lightoff, close, decodestart, closedev;
//
//    boolean state = false;
//    boolean devicestate = false;
//    IntByReference device;
//    Vbar b = new Vbar();
//    Thread t;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        decodeStr = (TextView) findViewById(R.id.decodeStr);
//        decodeStr.setMovementMethod(ScrollingMovementMethod.getInstance());
//
////        lignton = (Button) findViewById(R.id.lighton);
////        lignton.setOnClickListener(new OnClickListener() {
////
////            @Override
////            public void onClick(View v) {
////                b.vbarLight(true);
////
////            }
////        });
//
//        close = (Button) findViewById(R.id.close);
//        close.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                b.closeDev();
//                System.exit(0);
//            }
//        });
//        closedev = (Button) findViewById(R.id.closedev);
//        closedev.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                t.interrupt();
//                b.vbarLight(false);
//                devicestate = false;
////                t.resume();
//                b.closeDev();
////                lignton.setEnabled(true);
////                lightoff.setEnabled(true);
//
//            }
//        });
////        lightoff = (Button) findViewById(R.id.lightoff);
////        lightoff.setOnClickListener(new OnClickListener() {
////
////            @Override
////            public void onClick(View v) {
////                b.vbarLight(false);
////
////            }
////        });
//        opendev = (Button) findViewById(R.id.openDev);
//        opendev.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                state = b.vbarOpen();
//                if (state) {
//                    AlertDialog.Builder builders = new AlertDialog.Builder(MainActivity.this);
//                    builders.setTitle("设备连接状态");
//                    builders.setMessage("连接成功");
//                    builders.setPositiveButton("确认", null);
//                    builders.show();
//                    Log.v("######################", "open success");
////                    lignton.setEnabled(true);
////                    lightoff.setEnabled(true);
//                    b.vbarLight(true);
//                    t = new Thread() {
//                        @Override
//                        public void run() {
//                            // TODO Auto-generated method stub
//                            super.run();
//                            while (true) {
//                                final String str = b.getResultsingle();
//                                if (str != null) {
//                                    runOnUiThread(new Runnable() {
//                                        public void run() {
//                                            {
//                                                decodeStr.setText(str + "\r\n");
//                                            }
//                                        }
//                                    });
//                                }
//                                try {
//                                    Thread.sleep(1);
//                                } catch (InterruptedException e) {
//                                    // TODO Auto-generated catch block
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    };
//                    t.start();
////                    if (devicestate) {
////                        lignton.setEnabled(false);
////                        lightoff.setEnabled(false);
////                    }
//                } else {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                    builder.setTitle("设备连接状态");
//                    builder.setMessage("连接失败");
//                    builder.setPositiveButton("确认", null);
//                    builder.show();
//                    devicestate = false;
//                    Log.v("#####################", "open fail");
//                }
//            }
//        });
////        decodestart = (Button) findViewById(R.id.begindecode);
////        decodestart.setOnClickListener(new OnClickListener() {
////
////            @Override
////            public void onClick(View v) {
////
////
////            }
////        });
//    }
//
//    public void refreshAlarmView(TextView textView, String msg) {
//        textView.setText(msg);
//        int offset = textView.getLineCount() * textView.getLineHeight();
//        if (offset > (textView.getHeight() - textView.getLineHeight() - 20)) {
//            textView.scrollTo(0, offset - textView.getHeight() + textView.getLineHeight() + 20);
//        }
//    }
//
//
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//}
