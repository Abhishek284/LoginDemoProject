package com.dji.logindemoproject;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.base.DJIDiagnostics;
import dji.sdk.remotecontroller.RemoteController;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;

import static android.R.attr.key;


public class MainActivity extends AppCompatActivity implements DJIDiagnostics.DiagnosticsInformationCallback, KeyManagerHelper.KeyCallback{
    private static final String TAG = MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private static BaseComponent mComponent;
    private Handler mHandler;
    private Button login_button,logout_button,setListeners;
    private CheckBox c1_click,c2_click,c1_double_click,c2_double_click, c1_long_click,c2_long_click,both_c1_c2_click;
    private Handler handler= new Handler();
//    AsyncCall asyncCall = new AsyncCall();
    private boolean isRemoteConnected=false;
    RemoteController remoteController ;
    private CheckBox checkBox;
    private ProgressBar progressBar;

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
                        KeyManagerHelper keyManagerHelper = new KeyManagerHelper(MainActivity.this, getApplicationContext());
                        keyManagerHelper.addKeyListeners();
                        progressBar.setVisibility(View.GONE);


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

            handler.post(checkAircraftConnection);
            notifyStatusChange();
        }
    };



    private BaseProduct.BaseProductListener mDJIBaseProductListener = new BaseProduct.BaseProductListener() {
        @Override
        public void onComponentChange(BaseProduct.ComponentKey key, BaseComponent oldComponent, BaseComponent newComponent) {
            if(newComponent != null) {
                newComponent.setComponentListener(mDJIComponentListener);

            }

//            Toast.makeText(getApplicationContext(), key+" "+oldComponent+" "+newComponent, Toast.LENGTH_LONG).show();


            if(key==BaseProduct.ComponentKey.REMOTE_CONTROLLER){
                Log.d("onComponentChange",key+" "+"Remote controller key");
                Toast.makeText(getApplicationContext(), key+" "+"Remote controller key", Toast.LENGTH_LONG).show();
            }

            handler.post(checkRemoteConnection);

            notifyStatusChange();
        }
        @Override
        public void onConnectivityChange(boolean isConnected) {
//            Log.d("onPConnectChange",isConnected+" "+"Connectivity change inside onConnectivityChange for product listeners");
//            Toast.makeText(getApplicationContext(), isConnected+" "+"Connectivity change inside onConnectivityChange for product listeners", Toast.LENGTH_LONG).show();
            notifyStatusChange();
        }
    };
    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {
        @Override
        public void onConnectivityChange(boolean isConnected) {
//            Log.d("onCompConnectChange",isConnected+" "+"Connectivity change inside ComponentListener device");
//            Toast.makeText(getApplicationContext(), "Connectivity change inside ComponentListener"+" "+isConnected, Toast.LENGTH_LONG).show();
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

    private Runnable checkAircraftConnection = new Runnable() {
        @Override
        public void run() {
            if(mProduct!=null){
                if(mProduct.isConnected()){
                    Toast.makeText(getApplicationContext(), "Aircraft and remote connected", Toast.LENGTH_LONG).show();
                    isRemoteConnected=true;
                    checkBox.setChecked(true);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Remote connected", Toast.LENGTH_LONG).show();
                    isRemoteConnected=true;
                    checkBox.setChecked(true);
                }
            }
        }
    };

    private Runnable checkRemoteConnection = new Runnable() {
        @Override
        public void run() {
            if (isRemoteConnected==true){
                isRemoteConnected=false;
//                Toast.makeText(getApplicationContext(), "Remote disconnected", Toast.LENGTH_LONG).show();
                checkBox.setChecked(false);
            }
            else {
                isRemoteConnected=true;
//                Toast.makeText(getApplicationContext(), "Remote reconnected", Toast.LENGTH_LONG).show();
                checkBox.setChecked(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize DJI SDK Manager
        mHandler = new Handler(Looper.getMainLooper());
//        login_button = (Button) findViewById(R.id.login);
//        logout_button = (Button) findViewById(R.id.logout);
//        setListeners = (Button) findViewById(R.id.set_listeners);
        c1_click= (CheckBox) findViewById(R.id.c1_single_click);
        c2_click= (CheckBox) findViewById(R.id.c2_single_click);
        c1_long_click= (CheckBox) findViewById(R.id.c1_long_click);
        c2_long_click= (CheckBox) findViewById(R.id.c2_long_click);
        c1_double_click= (CheckBox) findViewById(R.id.c1_double_click);
        c2_double_click= (CheckBox) findViewById(R.id.c2_double_click);
        both_c1_c2_click = (CheckBox) findViewById(R.id.c1_c2_both_click);
        checkBox = (CheckBox) findViewById(R.id.remote_checkbox);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        //Need to ask y permission was denied for the two things : mount unmount and system alert
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE,Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN,
                    }
                    , 1);
        }
        else{
            register();
        }







    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int sum = 0;
        String s;
        for (int x : grantResults) {
            sum += x;
        }

                if (sum == 0) {
                    // We can now safely use the API we requested access to
                   register();

                } else {
                    // Permission was denied or request was cancelled
                    int y;
                    for(y=0;y<grantResults.length;y++){
                        if(grantResults[y]==-1){
                            Toast.makeText(getApplicationContext(), "Please grant device permissions for in order to proceed", Toast.LENGTH_LONG).show();                        }
                    }


                }
        }

    public void register(){
        progressBar.setVisibility(View.VISIBLE);
        DJISDKManager.getInstance().registerApp(MainActivity.this, mDJISDKManagerCallback);
    }



    @Override
    public void onUpdate(List<DJIDiagnostics> djiDiagnosticses) {


    }
    @Override
    public void onC1Clicked(){
//        Toast.makeText(getApplicationContext(),"C1 is clicked", Toast.LENGTH_SHORT).show();
        clearAll();
//        c1_click.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        c1_click.setChecked(true);



    }
    @Override
    public void onC1LongClicked(){
//        Toast.makeText(getApplicationContext(),"C1 Long clicked ", Toast.LENGTH_SHORT).show();
        clearAll();
//        c1_long_click.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        c1_long_click.setChecked(true);
    }
    @Override
    public void onC1DoubleClicked(){
//        Toast.makeText(getApplicationContext(),"C1 is double clicked", Toast.LENGTH_SHORT).show();
        clearAll();
//        c1_double_click.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        c1_double_click.setChecked(true);

    }


    @Override
    public void onC2Clicked(){
//        Toast.makeText(getApplicationContext(),"C2 is clicked", Toast.LENGTH_SHORT).show();
        clearAll();
//        c2_click.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        c2_click.setChecked(true);
    }
    @Override
    public void onC2LongClicked(){
//        Toast.makeText(getApplicationContext(),"C2 Long clicked ", Toast.LENGTH_SHORT).show();
        clearAll();
//        c2_long_click.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        c2_long_click.setChecked(true);
    }
    @Override
    public void onC2DoubleClicked(){
//        Toast.makeText(getApplicationContext(),"C2 is double clicked", Toast.LENGTH_SHORT).show();
        clearAll();
//        c2_double_click.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        c2_double_click.setChecked(true);
    }
    @Override
    public void onC1C2BothClicked(){
//        Toast.makeText(getApplicationContext(),"Both C1 and C2 is clicked", Toast.LENGTH_SHORT).show();
        clearAll();
//        both_c1_c2_click.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
    both_c1_c2_click.setChecked(true);
    }

    private void clearAll(){
//        setListeners.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
//        c1_click.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
//        c2_click.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
//        c1_long_click.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
//        c2_long_click.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
//        c1_double_click.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
//        c2_double_click.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
//        both_c1_c2_click.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        c1_click.setChecked(false);
        c2_click.setChecked(false);
        c1_long_click.setChecked(false);
        c2_long_click.setChecked(false);
        c1_double_click.setChecked(false);
        c2_double_click.setChecked(false);
        both_c1_c2_click.setChecked(false);




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

//
//    public class AsyncCall extends AsyncTask<Void,Void,Void>{
//
//
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            return null;
//        }
//    }
}