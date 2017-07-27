package com.dji.logindemoproject;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;

import dji.common.remotecontroller.HardwareState;
import dji.keysdk.DJIKey;
import dji.keysdk.KeyManager;
import dji.keysdk.RemoteControllerKey;
import dji.keysdk.callback.KeyListener;

/**
 * Created by abhishek on 20/07/17.
 import android.content.Context;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.provider.Settings;
 import android.support.annotation.Nullable;
 import android.view.ContextMenu;
 import android.view.View;
 import android.widget.Toast;

 import dji.common.remotecontroller.ConnectToMasterResult;
 import dji.common.remotecontroller.GPSData;
 import dji.common.remotecontroller.HardwareState;
 import dji.keysdk.DJIKey;
 import dji.keysdk.KeyManager;
 import dji.keysdk.RemoteControllerKey;
 import dji.keysdk.callback.KeyListener;

 /**
 * Created by saurabh.saxena on 5/18/17
 */
public class KeyManagerHelper  {

    private Handler handler = new Handler();
//    private Handler handlerC1SingleClick = new Handler();
//    private Handler handlerC2LongClick = new Handler();
//    private Handler handlerC2SingleClick = new Handler();
    private boolean isHandlerC1SingleClickCallbackSet = false, isC1DoubleClickSuccess=false, isHandlerC2SingleClickCallbackSet = false, isC2DoubleClickSuccess=false;

    private KeyCallback callback;
    private long c1PressedTime,c2PressedTime;
    private Context context;

    public KeyManagerHelper(KeyCallback callback, Context context) {
        this.context=context;
        this.callback = callback;
    }

    private KeyListener c1KeyListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object o, @Nullable final Object newValue) {

            boolean clicked = newValue != null && ((HardwareState.Button) newValue).isClicked();
            if (clicked && !isHandlerC1SingleClickCallbackSet) {



                    c1PressedTime = System.currentTimeMillis();
                handler.postDelayed(c1LongClickRunnable, 1000);
                if(Math.abs(c1PressedTime-c2PressedTime)<60){
                    c1PressedTime=-1;
                    c2PressedTime=-1;
                    handler.post(c1c2BothClickRunnable);
                    handler.removeCallbacks(c1LongClickRunnable);
                    handler.removeCallbacks(c2LongClickRunnable);

                }
                if(isHandlerC2SingleClickCallbackSet){
                    handler.removeCallbacks(c2ClickRunnable);
                    //the click of the c2 button can also be discarded if needed

                    handler.post(c2ClickRunnable);

                }

                }


            else if ((System.currentTimeMillis()-c1PressedTime)<1000){
                if(isHandlerC1SingleClickCallbackSet){
                    handler.removeCallbacks(c1ClickRunnable);
                    isHandlerC1SingleClickCallbackSet=false;
                    //handler.removeCallbacks(c1LongClickRunnable);
                    handler.post(c1DoubleClickRunnable);
                    isC1DoubleClickSuccess=true;

                }else if(isC1DoubleClickSuccess) {
                    c1PressedTime=-1;
                    isC1DoubleClickSuccess=false;
                }
                else {
                    handler.removeCallbacks(c1LongClickRunnable);
                    isHandlerC1SingleClickCallbackSet=true;
                    handler.postDelayed(c1ClickRunnable, 500);
                }
            }
            else if((System.currentTimeMillis()-c1PressedTime)>1000){
                c1PressedTime=-1;
            }

        }


    };

    private Runnable c1c2BothClickRunnable = new Runnable(){
        @Override
        public void run() {
            callback.onC1C2BothClicked();

        }
    };

    private Runnable c1DoubleClickRunnable = new Runnable(){
        @Override
        public void run() {
            callback.onC1DoubleClicked();

        }
    };

    private Runnable c1ClickRunnable = new Runnable(){
        @Override
        public void run() {
            isHandlerC1SingleClickCallbackSet=false;
            handler.removeCallbacks(c1LongClickRunnable);
            callback.onC1Clicked();
            c1PressedTime=-1;

        }
    };
    private Runnable c1LongClickRunnable = new Runnable(){
        @Override
        public void run() {
            callback.onC1LongClicked();

        }
    };

    private KeyListener c2KeyListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object o, @Nullable final Object newValue) {
            boolean clicked = newValue != null && ((HardwareState.Button) newValue).isClicked();
            if (clicked && !isHandlerC2SingleClickCallbackSet) {



                c2PressedTime = System.currentTimeMillis();
                handler.postDelayed(c2LongClickRunnable, 1000);
                if(Math.abs(c1PressedTime-c2PressedTime)<60){
                    c1PressedTime=-1;
                    c2PressedTime=-1;
                    handler.post(c1c2BothClickRunnable);
                    handler.removeCallbacks(c2LongClickRunnable);
                    handler.removeCallbacks(c1LongClickRunnable);

                }
                if(isHandlerC1SingleClickCallbackSet){
                    handler.removeCallbacks(c1ClickRunnable);
                    //the click of the c1 button can also be discarded if needed
                    handler.post(c1ClickRunnable);
                }

            }


            else if ((System.currentTimeMillis()-c2PressedTime)<1000){
                if(isHandlerC2SingleClickCallbackSet){
                    handler.removeCallbacks(c2ClickRunnable);
                    isHandlerC2SingleClickCallbackSet=false;
                   // handler.removeCallbacks(c2LongClickRunnable);
                    handler.post(c2DoubleClickRunnable);
                    isC2DoubleClickSuccess=true;

                }else if(isC2DoubleClickSuccess) {
                    c2PressedTime=-1;
                    isC2DoubleClickSuccess=false;
                }
                else {
                    handler.removeCallbacks(c2LongClickRunnable);
                    isHandlerC2SingleClickCallbackSet=true;
                    handler.postDelayed(c2ClickRunnable, 500);

                }
            }
            else if((System.currentTimeMillis()-c2PressedTime)>1000){
                c2PressedTime=-1;
            }
        }
    };

    private Runnable c2DoubleClickRunnable = new Runnable(){
        @Override
        public void run() {
            callback.onC2DoubleClicked();

        }
    };
    private Runnable c2ClickRunnable = new Runnable(){
        @Override
        public void run() {
            callback.onC2Clicked();
            handler.removeCallbacks(c2LongClickRunnable);
            isHandlerC2SingleClickCallbackSet=false;
            c2PressedTime=-1;
        }
    };


    private Runnable c2LongClickRunnable = new Runnable(){
        @Override
        public void run() {
            callback.onC2LongClicked();

        }
    };


    public void addKeyListeners() {
//        Toast.makeText(context,"Adding Key Listerners"+" "+KeyManager.getInstance(), Toast.LENGTH_SHORT).show();
        if (KeyManager.getInstance() != null) {
//            Toast.makeText(context,"Inside if condition", Toast.LENGTH_SHORT).show();
            removeKeyListeners();
            DJIKey keyC1 = RemoteControllerKey.create(RemoteControllerKey.CUSTOM_BUTTON_1);
            KeyManager.getInstance().addListener(keyC1, c1KeyListener);

            KeyManager.getInstance().addListener(RemoteControllerKey.create(RemoteControllerKey.CUSTOM_BUTTON_2), c2KeyListener);
        }
    }

    public void removeKeyListeners() {
        if (KeyManager.getInstance() != null) {
            KeyManager.getInstance().removeListener(c1KeyListener);
            KeyManager.getInstance().removeListener(c2KeyListener);
        }
    }

    public static interface KeyCallback {

        void onC1Clicked();

        void onC2Clicked();

        void onC1LongClicked();

        void onC2LongClicked();

        void onC1DoubleClicked();

        void onC2DoubleClicked();

        void onC1C2BothClicked();
    }
}