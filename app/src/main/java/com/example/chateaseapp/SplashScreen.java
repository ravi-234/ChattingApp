package com.example.chateaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;



public class SplashScreen extends AppCompatActivity {

     private ImageView mainlogo,sublogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         getSupportActionBar().hide();
         setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(SplashScreen.this,signin.class);
                startActivity(intent);
                finish();
            }
        }, 3000);

    }
}