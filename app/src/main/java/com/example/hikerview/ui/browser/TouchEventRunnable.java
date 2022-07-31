package com.example.hikerview.ui.browser;

import android.app.Instrumentation;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 作者：By 15968
 * 日期：On 2020/3/6
 * 时间：At 21:57
 */

public class TouchEventRunnable implements Runnable {
    private static final String TAG = "TouchEventRunnable";
    private int x;
    private int y;

    private boolean isLongPress;

    public TouchEventRunnable(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public TouchEventRunnable(int x, int y, boolean isLongPress) {
        this.x = x;
        this.y = y;
        this.isLongPress = isLongPress;
    }

    @Override
    public void run() {
        if (isLongPress) {
            longClickOnScreen(x, y);
        } else {
            onClick();
        }
    }


    private void longClickOnScreen(int x, int y) {
        Log.d(TAG, "longClickOnScreen: x, y, " + x + ", " + y);
        try {
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis();
            MotionEvent eventDown = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
            eventDown.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            MotionEvent eventMove = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y + 1, 0);
            eventMove.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            MotionEvent eventUp = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
            eventUp.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            Instrumentation instrumentation2 = new Instrumentation();
            instrumentation2.sendPointerSync(eventDown);
            instrumentation2.sendPointerSync(eventMove);
            try {
                Thread.sleep(650);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            instrumentation2.sendPointerSync(eventUp);
        } catch (NullPointerException e) {
            Log.e(TAG, "longClickOnScreen: ", e);
        }
    }

    private void invokeInjectInputEvent(MotionEvent event) {
        Class cl = InputManager.class;
        try {
            Method method = cl.getMethod("getInstance");
            Object result = method.invoke(cl);
            InputManager im = (InputManager) result;
            method = cl.getMethod("injectInputEvent", InputEvent.class, int.class);
            method.invoke(im, event, 0);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    private void onClick() {

        Instrumentation inst = new Instrumentation();
        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, x, y, 0));
        inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP, x, y, 0));
        //Log.v("LOG", " x = " + x + " y = " + y);

    }
}
