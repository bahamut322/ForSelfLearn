package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * @author swn
 * @describe 仿密码输入EditText
 */
public class WiseEditText extends AppCompatEditText {


    private OnKeyListener keyListener;

    public WiseEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WiseEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WiseEditText(Context context) {
        super(context);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new MyInputConnection(super.onCreateInputConnection(outAttrs),
                true);
    }

    private class MyInputConnection extends InputConnectionWrapper {

        public MyInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (keyListener != null) {
                keyListener.onKey(WiseEditText.this,event.getKeyCode(),event);
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // magic:在最新的Android系统中，deleteSurroundingText（1，0）将被调用作为退格
            if (beforeLength == 1 && afterLength == 0) {
                // 删除键监听
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }

    }

    //设置监听回调
    public void setSoftKeyListener(OnKeyListener listener){
        keyListener = listener;
    }

}
