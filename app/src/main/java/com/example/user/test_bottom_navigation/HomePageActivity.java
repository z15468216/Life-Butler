package com.example.user.test_bottom_navigation;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.example.user.test_bottom_navigation.service.MyService;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.github.mikephil.charting.charts.Chart.LOG_TAG;
import static com.google.android.gms.internal.zzbfq.NULL;

public class HomePageActivity extends FragmentActivity {

    OkHttpClient client = new OkHttpClient.Builder().build();
    Fragment fragment = null;
    String acc, sensor, group, name, be_before, analyze, before, img_url;
    FragmentTransaction fragmentTransaction;
    Bundle bundle;
    SharedPreferences sp;
    BottomNavigationView navigation;
    Bitmap bitmap;
    File f;
    String url;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent it = getIntent();
        acc = it.getStringExtra("account");

        bundle = new Bundle();
        bundle.putString("account", acc);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //navigation.setSelectedItemId(R.id.navigation_user);

        handler = new Handler();
        //handler.post(runnable);
    }

    // 切換 Fragment 畫面
    private void switchFragment(Fragment fragment) {
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.commit();
    }

    // Bottom Navigation
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @SuppressLint("SetTextI18n")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_user:
                    fragment = new UserFragment();
                    fragment.setArguments(bundle);
                    switchFragment(fragment);
                    return true;
                case R.id.navigation_data:
                    fragment = new DataFragment();
                    fragment.setArguments(bundle);
                    switchFragment(fragment);
                    return true;
                case R.id.navigation_charts:
                    fragment = new ChartFragment();
                    fragment.setArguments(bundle);
                    switchFragment(fragment);
                    return true;
                case R.id.navigation_info:
                    fragment = new InfoFragment();
                    switchFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    // 退出 APP 按鈕
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            isExit.setMessage("確定要離開嗎?");
            isExit.setButton("Yes", quitListener);
            isExit.setButton2("Cancel", quitListener);
            isExit.show();
        }
        return false;
    }

    DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    break;
                default:
                    break;
            }
        }
    };

    private void init() {
        RequestBody post = new FormBody.Builder()
                .add("account", acc)
                .build();
        // 建立連線請求，寫入網址URL和方法POST
        final Request request = new Request.Builder()
                .url("http://163.17.135.66/users/userinfo.php")
                .post(post)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String retCode;
                retCode = Objects.requireNonNull(response.body()).string();
                Log.d("retCode Get", retCode);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (int i = 0; i < 7; i++) {
                                JSONObject json = new JSONObject(retCode);
                                switch (json.names().getString(i)) {
                                    case "user_ID":
                                        name = json.getString("user_ID");
                                        bundle.putString("user_ID", name);
                                        break;
                                    case "sensor_ID":  //感測器
                                        sensor = json.getString("sensor_ID");
                                        bundle.putString("sensor_ID", sensor);
                                        break;
                                    case "group_ID":  //群組
                                        group = json.getString("group_ID");
                                        bundle.putString("group_ID", group);
                                        break;
                                    case "be_before":
                                        be_before = json.getString("be_before");
                                        bundle.putString("be_before", be_before);
                                        break;
                                    case "analyze":
                                        analyze = json.getString("analyze");
                                        bundle.putString("analyze", analyze);
                                        break;
                                    case "before":
                                        before = json.getString("before");
                                        bundle.putString("before", before);
                                        break;
                                    case "img":
                                        img_url = json.getString("img");
                                        if (img_url.equals("NULL"))
                                            break;
                                        else {
                                            url = "http://" + img_url;
                                            if (f == null) {
                                                Thread t1 = new Thread(run);
                                                t1.start();
                                            } else {
                                                String img = getSharedPreferences("UserInfo", Context.MODE_PRIVATE).getString("image", "");
                                                bitmap = BitmapFactory.decodeFile(img);
                                                bundle.putParcelable("img", bitmap);
                                            }
                                            Log.d("image_home", String.valueOf(bitmap));
                                        }
                                        break;
                                }
                            }
                        } catch (Exception e) {
                            //Log.d("log", e.toString());
                            e.printStackTrace();
                        }
                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        navigation.setSelectedItemId(R.id.navigation_user);
                    }
                });
            }
        });
    }

    final Runnable run = new Runnable() {
        @Override
        public void run() {
            try {
                InputStream is = (InputStream) new URL(url).getContent();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
                bundle.putParcelable("img", bitmap);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                f = new File(Environment.getExternalStorageDirectory()
                        + File.separator + "test.png");
                f.createNewFile();
                Log.d("image", String.valueOf(f));
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
                sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("image", String.valueOf(f));
                edit.apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    // 返回 Home 畫面時再次取得更新後的使用者資訊
    @Override
    public void onResume() {
        super.onResume();
        Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        };
        timer.schedule(tt, 5000);

        init();

    }

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            RequestBody post = new FormBody.Builder()
                    .add("account", acc)
                    .build();
            // 建立連線請求，寫入網址URL和方法POST
            final Request request = new Request.Builder()
                    .url("http://163.17.135.66/sensor/alarm.php")
                    .post(post)
                    .build();
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d("Alarm_error", e.toString());
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    final String retCode;
                    retCode = Objects.requireNonNull(response.body()).string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("Alarm_retCode", retCode);
                                if (!retCode.equals("0")) {
                                    Intent it = new Intent(HomePageActivity.this, CustomerDialog.class);
                                    it.putExtra("acc", acc);
                                    it.putExtra("text", retCode);
                                    startActivity(it);
                                    handler.removeCallbacks(runnable);
                                }

                            } catch (Exception e) {
                                Log.d("alarm_response_error", e.toString());
                                e.printStackTrace();
                            }

                        }
                    });
                }
            });
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}


