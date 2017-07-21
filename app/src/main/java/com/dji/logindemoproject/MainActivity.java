package com.dji.logindemoproject;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Callable;

import dji.common.error.DJIError;
import dji.common.error.DJIGeoError;
import dji.common.error.DJIMissionError;
import dji.common.error.DJISDKError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.midware.data.config.P3.Ccode;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.base.DJIDiagnostics;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;
import dji.thirdparty.rx.Observable;
import dji.thirdparty.rx.schedulers.Schedulers;

import static dji.midware.data.forbid.DJIFlyForbidController.FlyforbidDataSourceType.DJI;


public class MainActivity extends AppCompatActivity implements DJIDiagnostics.DiagnosticsInformationCallback, KeyManagerHelper.KeyCallback{
    private static final String TAG = MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private Handler mHandler;
    private Button login_button,logout_button,setListeners;
    private Handler handler= new Handler();
    AsyncCall asyncCall = new AsyncCall();

    private DJISDKManager.SDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {
        @Override
        public void onRegister(DJIError error) {
            Log.d(TAG, error == null ? "success" : error.getDescription());
            if(error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Register Success", Toast.LENGTH_LONG).show();


                    }
                });
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "register sdk failed, check if network is available", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            Log.e("TAG", error.toString());
        }
        @Override
        public void onProductChange(BaseProduct oldProduct, BaseProduct newProduct) {
            mProduct = newProduct;
            if(mProduct != null) {
                mProduct.setBaseProductListener(mDJIBaseProductListener);
            }
            notifyStatusChange();
        }
    };
    private BaseProduct.BaseProductListener mDJIBaseProductListener = new BaseProduct.BaseProductListener() {
        @Override
        public void onComponentChange(BaseProduct.ComponentKey key, BaseComponent oldComponent, BaseComponent newComponent) {
            if(newComponent != null) {
                newComponent.setComponentListener(mDJIComponentListener);
            }
            notifyStatusChange();
        }
        @Override
        public void onConnectivityChange(boolean isConnected) {
            notifyStatusChange();
        }
    };
    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {
        @Override
        public void onConnectivityChange(boolean isConnected) {
            notifyStatusChange();
        }
    };
    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize DJI SDK Manager
        mHandler = new Handler(Looper.getMainLooper());
        login_button = (Button) findViewById(R.id.login);
        logout_button = (Button) findViewById(R.id.logout);
        setListeners = (Button) findViewById(R.id.set_listeners);

        asyncCall.execute();



        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              loginAccount();

            }
        });
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutAccount();

            }
        });
        setListeners.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTheKeyListeners();
            }
        });




    }

    private void addTheKeyListeners() {
        KeyManagerHelper keyManagerHelper = new KeyManagerHelper(MainActivity.this, getApplicationContext());
        keyManagerHelper.addKeyListeners();
    }

    @Override
    public void onUpdate(List<DJIDiagnostics> djiDiagnosticses) {


    }
    @Override
    public void onC1Clicked(){
        Toast.makeText(getApplicationContext(),"C1 is clicked", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onC1LongClicked(){
        Toast.makeText(getApplicationContext(),"C1 Long clicked ", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onC1DoubleClicked(){
        Toast.makeText(getApplicationContext(),"C1 is double clicked", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onC2Clicked(){
        Toast.makeText(getApplicationContext(),"C2 is clicked", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onC2LongClicked(){
        Toast.makeText(getApplicationContext(),"C2 Long clicked ", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onC2DoubleClicked(){
        Toast.makeText(getApplicationContext(),"C2 is double clicked", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onC1C2BothClicked(){
        Toast.makeText(getApplicationContext(),"Both C1 and C2 is clicked", Toast.LENGTH_SHORT).show();
    }



    public void onResume(){
        super.onResume();
//        DJIDiagnostics djiDiagnostics = new DJIDiagnostics();
//        Toast.makeText(getApplicationContext(), djiDiagnostics.getCode()+" "+djiDiagnostics.getReason()+" "+djiDiagnostics.getSolution(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        //Toast.makeText(getApplicationContext(),"Main Activity onNewIntent is called", Toast.LENGTH_LONG).show();

    }

    private void  startsecond(){
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }
    private void loginAccount(){
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Toast.makeText(getApplicationContext(), "User Login is Successfull", Toast.LENGTH_SHORT).show();


                    }
                    @Override
                    public void onFailure(DJIError error) {
                        Toast.makeText(getApplicationContext(), "Login ERROR", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logoutAccount(){
        UserAccountManager.getInstance().logoutOfDJIUserAccount(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (null == error) {
                    Toast.makeText(getApplicationContext(), "Logged out ", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to log out", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public class AsyncCall extends AsyncTask<Void,Void,Void>{



        @Override
        protected Void doInBackground(Void... voids) {
            DJISDKManager.getInstance().registerApp(MainActivity.this, mDJISDKManagerCallback);
            return null;
        }
    }
}