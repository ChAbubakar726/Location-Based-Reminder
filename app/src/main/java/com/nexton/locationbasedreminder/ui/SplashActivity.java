package com.nexton.locationbasedreminder.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexton.locationbasedreminder.R;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    String isNightMode;
    String whichLang;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.colorWhite));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        isNightMode = sharedPreferences.getString("nightMode","Disabled");
        whichLang = sharedPreferences.getString("language","English");

        if(isNightMode.equals("Enabled")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if(whichLang.equals("English")){
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }else {
            Locale locale = new Locale("ur");
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i;
                i = new Intent(getApplicationContext(),BoardingActivity.class);
                startActivity(i);
                finish();
            }
        },2000);
    }
}