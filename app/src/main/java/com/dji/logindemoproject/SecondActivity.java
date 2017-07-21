package com.dji.logindemoproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }
    @Override
    public void onNewIntent(Intent intent){
        Toast.makeText(getApplicationContext(),"Second Activity onNewIntent is called", Toast.LENGTH_LONG).show();

    }
}
