package com.dji.logindemoproject;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.widget.Toast;

import dji.common.remotecontroller.HardwareState;
import dji.keysdk.DJIKey;
import dji.keysdk.KeyManager;
import dji.keysdk.RemoteControllerKey;
import dji.keysdk.callback.KeyListener;

import static dji.midware.data.model.P3.DataCameraVirtualKey.KEY.S1;

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

    private Handler handlerC1LongClick = new Handler();
    private Handler handlerC1SingleClick = new Handler();
    private Handler handlerC2LongClick = new Handler();
    private Handler handlerC2SingleClick = new Handler();
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
                handlerC1LongClick.postDelayed(c1LongClickRunnable, 1500);
                if(Math.abs(c1PressedTime-c2PressedTime)<200){
                    c1PressedTime=-1;
                    c2PressedTime=-1;
                    callback.onC1C2BothClicked();
                    handlerC1LongClick.removeCallbacks(c1LongClickRunnable);
                    handlerC2LongClick.removeCallbacks(c2LongClickRunnable);

                }

                }


            else if ((System.currentTimeMillis()-c1PressedTime)<1500){
                if(isHandlerC1SingleClickCallbackSet){
                    handlerC1SingleClick.removeCallbacks(c1ClickRunnable);
                    isHandlerC1SingleClickCallbackSet=false;
                    handlerC1LongClick.removeCallbacks(c1LongClickRunnable);
                    callback.onC1DoubleClicked();
                    isC1DoubleClickSuccess=true;

                }else if(isC1DoubleClickSuccess) {
                    c1PressedTime=-1;
                    isC1DoubleClickSuccess=false;
                }
                else {

                    isHandlerC1SingleClickCallbackSet=true;
                    handlerC1SingleClick.postDelayed(c1ClickRunnable, 750);
                }
            }
            else if((System.currentTimeMillis()-c1PressedTime)>1500){
                c1PressedTime=-1;
            }

        }


    };

    private Runnable c1ClickRunnable = new Runnable(){
        @Override
        public void run() {
            isHandlerC1SingleClickCallbackSet=false;
            handlerC1LongClick.removeCallbacks(c1LongClickRunnable);
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
                handlerC2LongClick.postDelayed(c2LongClickRunnable, 1500);
                if(Math.abs(c1PressedTime-c2PressedTime)<200){
                    c1PressedTime=-1;
                    c2PressedTime=-1;
                    callback.onC1C2BothClicked();
                    handlerC2LongClick.removeCallbacks(c2LongClickRunnable);
                    handlerC1LongClick.removeCallbacks(c1LongClickRunnable);

                }
            }


            else if ((System.currentTimeMillis()-c2PressedTime)<1500){
                if(isHandlerC2SingleClickCallbackSet){
                    handlerC2SingleClick.removeCallbacks(c2ClickRunnable);
                    isHandlerC2SingleClickCallbackSet=false;
                    handlerC2LongClick.removeCallbacks(c2LongClickRunnable);
                    callback.onC2DoubleClicked();
                    isC2DoubleClickSuccess=true;

                }else if(isC2DoubleClickSuccess) {
                    c2PressedTime=-1;
                    isC2DoubleClickSuccess=false;
                }
                else {

                    isHandlerC2SingleClickCallbackSet=true;
                    handlerC2SingleClick.postDelayed(c2ClickRunnable, 600);

                }
            }
            else if((System.currentTimeMillis()-c2PressedTime)>1500){
                c2PressedTime=-1;
            }
        }
    };

    private Runnable c2ClickRunnable = new Runnable(){
        @Override
        public void run() {
            callback.onC2Clicked();
            handlerC2LongClick.removeCallbacks(c2LongClickRunnable);
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
        Toast.makeText(context,"Adding Key Listerners"+" "+KeyManager.getInstance(), Toast.LENGTH_SHORT).show();
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