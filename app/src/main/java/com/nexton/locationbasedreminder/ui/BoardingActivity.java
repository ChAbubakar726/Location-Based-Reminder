package com.nexton.locationbasedreminder.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.nexton.locationbasedreminder.R;
import com.nexton.locationbasedreminder.adapters.IntroAdapter;
import com.nexton.locationbasedreminder.model.IntroModel;

import java.util.ArrayList;
import java.util.List;

public class BoardingActivity extends AppCompatActivity {

    ViewPager container_vp;
    IntroAdapter introAdapter;
    TabLayout tabIndicator_tabv;
    Button next_btn, skip_btn, getStarted_btn;
    int position;
    Animation getStartedButtonAnimation;
    private static final int UI_OPTIONS = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LOW_PROFILE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boarding);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);

        container_vp = findViewById(R.id.container_vp);
        tabIndicator_tabv = findViewById(R.id.tabIndicator_tabv);
        next_btn = findViewById(R.id.next_btn);
        skip_btn = findViewById(R.id.skip_btn);
        getStarted_btn = findViewById(R.id.getStarted_btn);
        getStartedButtonAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.getstarted_button);

        if (restorePrefData()) {
            Intent homeActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(homeActivity);
            finish();
        }

        List<IntroModel> list = new ArrayList<>();
        list.add(new IntroModel("Welcome to Localarm", "Localarm is a location based reminder app. Just set alarm/reminders all over the map & let Localarm keep track of when you reach your destination so you can sit back, and relax!", R.drawable.app_logo));
        list.add(new IntroModel("Create Alarm Reminder", "Create alarm using location based reminder.", R.drawable.reminders_screen_1));
        list.add(new IntroModel("Add Notes", "Add multiple notes for tasks.", R.drawable.notes_screen_2));
        list.add(new IntroModel("Add Places to Alarm", "Add multiple place to set reminder.", R.drawable.places_screen_3));
        list.add(new IntroModel("Add Places groups", "Add multiple place in one group to set reminder", R.drawable.places_groups_screen_4));
        list.add(new IntroModel("Realtime Map", "Realtime google map to set location.", R.drawable.map_screen_7));
        list.add(new IntroModel("Multi-Lingual", "We have multiple language option so everyone can use without any difficulty.", R.drawable.lang_screen_6));
        list.add(new IntroModel("Settings Screen", "Setting screen with default radius size, night mode and language change option.", R.drawable.lang_screen_6));



        introAdapter = new IntroAdapter(this, list);
        container_vp.setAdapter(introAdapter);

        tabIndicator_tabv.setupWithViewPager(container_vp);

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                position = container_vp.getCurrentItem();
                if (position <= list.size()) {
                    position++;
                    container_vp.setCurrentItem(position);
                }
                if (position == list.size() - 1) {
                    loadLastScreen();
                }
            }
        });

        tabIndicator_tabv.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == list.size() - 1) {
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        getStarted_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent homeActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(homeActivity);
                savePrefsData();
                finish();
            }
        });

        skip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container_vp.setCurrentItem(list.size());
            }
        });

    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        boolean isIntroActivityOpenedBefore = pref.getBoolean("isIntroOpened", false);
        return isIntroActivityOpenedBefore;
    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpened", true);
        editor.commit();
    }

    private void loadLastScreen() {
        next_btn.setVisibility(View.INVISIBLE);
        getStarted_btn.setVisibility(View.VISIBLE);
        skip_btn.setVisibility(View.INVISIBLE);
        tabIndicator_tabv.setVisibility(View.INVISIBLE);
        getStarted_btn.setAnimation(getStartedButtonAnimation);
    }
}