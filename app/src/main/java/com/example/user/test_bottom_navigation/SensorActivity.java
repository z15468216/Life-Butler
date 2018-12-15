package com.example.user.test_bottom_navigation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SensorActivity extends Activity {
    OkHttpClient client = new OkHttpClient.Builder().build();
    android.support.v7.widget.Toolbar toolbar;
    TextView title, sensor_edt, sensor_info, sensor_name;
    String result, acc, sensor, group;
    Button binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        Intent it = getIntent();
        acc = it.getStringExtra("account");
        sensor = it.getStringExtra("sensor_ID");
        group = it.getStringExtra("group_ID");
        result = acc;

        binding = findViewById(R.id.sensor_btn);
        binding.setOnClickListener(bindOnListener);
        sensor_edt = findViewById(R.id.sensor_edt);
        sensor_info = findViewById(R.id.sensor_info);
        sensor_info.setVisibility(View.GONE);
        sensor_name = findViewById(R.id.sensor_name);
        sensor_name.setVisibility(View.GONE);

        // 判斷是否已綁定感測器
        if (!sensor.equals("null")) {
            sensor_info.setVisibility(View.VISIBLE);
            sensor_name.setVisibility(View.VISIBLE);
            sensor_name.setText(sensor);
            sensor_info.setText("1. 偵測廚房的分貝\n" +
                    "2. 偵測瓦斯濃度\n" +
                    "3. 可以操控風扇開關\n" +
                    "4. 如果瓦斯濃度達危險範圍，風扇會自動開啟\n" +
                    "5. 如果瓦斯濃度達危險範圍，風扇會鎖定於開啟狀態\n" +
                    "6. 請注意每天的數值變化量");
            binding.setVisibility(View.GONE);
            sensor_edt.setVisibility(View.GONE);
        }

        toolbar = findViewById(R.id.toolbar);
        title = toolbar.findViewById(R.id.toolbar_title);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });

    }

    // 綁定感測器按鈕
    private Button.OnClickListener bindOnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            // 判斷是否已加入群組
            if (group.equals("null")) {
                new AlertDialog.Builder(SensorActivity.this)
                        .setTitle("錯誤!")
                        .setMessage("綁定設備前，需要先加入或創立群組")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            } else {
                @SuppressLint("InflateParams") final View item = LayoutInflater.from(SensorActivity.this).inflate(R.layout.item_layout, null);
                new AlertDialog.Builder(SensorActivity.this)
                        .setTitle("請輸入設備編號")
                        .setView(item)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText editText = item.findViewById(R.id.edt_sensor);
                                String name = editText.getText().toString();
                                if (TextUtils.isEmpty(name)) {
                                    editText.setText("");
                                    Toast.makeText(getApplicationContext(), "請重新輸入", Toast.LENGTH_SHORT).show();
                                } else {
                                    RequestBody post = new FormBody.Builder()
                                            .add("group_ID", group)
                                            .add("sensor_ID", editText.getText().toString())
                                            .build();
                                    // 建立連線請求，寫入網址URL和方法POST
                                    final Request request = new Request.Builder()
                                            .url("http://163.17.135.66/sensor/addsensor.php")
                                            .post(post)
                                            .build();
                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                            Log.d("binding_failure", e.toString());
                                            call.cancel();
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                            final String callback_out;
                                            callback_out = Objects.requireNonNull(response.body()).string();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        final String result_out;
                                                        JSONObject json = new JSONObject(callback_out);
                                                        result_out = json.getString("result");
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (result_out.equals("success")) {
                                                                    Toast.makeText(getApplicationContext(), "綁定成功", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(getApplicationContext(), "發生錯誤，請重新輸入", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        Log.d("log", e.toString());
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }).show();
            }
        }
    };
}


