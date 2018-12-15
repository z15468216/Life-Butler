package com.example.user.test_bottom_navigation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.anwarshahriar.calligrapher.Calligrapher;


public class WelcomeActivity extends Activity {

    private ViewPager viewPager;
    private LinearLayout layoutDot;
    private int[] layouts;
    private Button btn_next, btn_skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (!isFirstTimeStartApp()) {
            _startMainActivity();
            finish();
        }

        setStatusBarTransparent();
        setContentView(R.layout.activity_welcome);

        viewPager = findViewById(R.id.view_pager);
        layoutDot = findViewById(R.id.dotLayout);
        btn_next = findViewById(R.id.btn_next);
        btn_skip = findViewById(R.id.btn_skip);

        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _startMainActivity();
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPage = viewPager.getCurrentItem() + 1 ;
                if (currentPage < layouts.length) {
                    viewPager.setCurrentItem(currentPage);
                } else {
                    startMainActivity();
                }

            }
        });

        layouts = new int[]{R.layout.slide_1, R.layout.slide_2, R.layout.slide_3};
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(layouts, getApplicationContext());
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onPageSelected(int position) {
                if (position == layouts.length-1) {
                    btn_next.setText("註冊");
                    btn_skip.setVisibility(View.VISIBLE);
                } else {
                    btn_next.setText("下一步");
                    btn_skip.setVisibility(View.VISIBLE);
                }
                setDotStatus(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setDotStatus(0);

    }

    // 判斷是否為第一次開啟 APP
    private boolean isFirstTimeStartApp() {
        SharedPreferences ref = getSharedPreferences("IntroSliderAPP", Context.MODE_PRIVATE);
        return ref.getBoolean("FirstTimeStartFlag", true);
    }

    // 設定是否第一次開啟的判斷
    private void setFirstTimesStartStatus(boolean str) {
        SharedPreferences ref = getSharedPreferences("IntroSliderAPP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putBoolean("FirstTimeStartFlag", str);
        editor.apply();
    }

    // 進度條設定
    private void setDotStatus(int page) {
        layoutDot.removeAllViews();
        TextView[] dotstv = new TextView[layouts.length];
        for (int i = 0; i < dotstv.length; i++) {
            dotstv[i] = new TextView(this);
            dotstv[i].setText(Html.fromHtml("&#8226;"));
            dotstv[i].setTextSize(35);
            dotstv[i].setTextColor(Color.parseColor("#a9b4bb"));
            layoutDot.addView(dotstv[i]);
        }
        if (dotstv.length > 0) {
            dotstv[page].setTextColor(Color.parseColor("#ffffff"));
        }
    }

    // 如果是第一次 跳至註冊畫面
    private void startMainActivity() {
        setFirstTimesStartStatus(false);
        startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
        finish();
    }

    // 如果不是第一次 跳至登入畫面
    private void _startMainActivity() {
        setFirstTimesStartStatus(false);
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        finish();
    }

    // 前導頁畫面設定 ( 全螢幕、 狀態列、 顏色)
    @SuppressLint("ObsoleteSdkInt")
    private void setStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

}
