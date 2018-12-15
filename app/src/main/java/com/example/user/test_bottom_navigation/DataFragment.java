package com.example.user.test_bottom_navigation;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DataFragment extends Fragment {

    OkHttpClient client = new OkHttpClient.Builder().build();
    String name, acc, sensor, group, count, be_before, analyze, before, result;
    TextView db, CO, CH4, Smoke;
    Switch fan;
    Handler handler;
    int value_dB, value_Smoke, co, ch4;
    float value_CO, value_CH4;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_data_fragment, container, false);

        name = Objects.requireNonNull(getArguments()).getString("user_ID");
        acc = getArguments().getString("account");
        sensor = getArguments().getString("sensor_ID");
        group = getArguments().getString("group_ID");
        be_before = getArguments().getString("be_before");
        analyze = getArguments().getString("analyze");
        before = getArguments().getString("before");
        Log.d("Bundle Get", name + " + " + acc + " + " + sensor + " + " + group + " + " + count);

        db = view.findViewById(R.id.count_db);
        CO = view.findViewById(R.id.count_CO);
        CH4 = view.findViewById(R.id.count_CH4);
        Smoke = view.findViewById(R.id.count_smoke);
        fan = view.findViewById(R.id.fan);
        fan.setOnCheckedChangeListener(fanOnListener);

        handler = new Handler();
        handler.post(runnable_data);

        return view;
    }


    // 控制風扇
    private Switch.OnCheckedChangeListener fanOnListener = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            // 判斷當下風扇開關狀態
            if (b) {
                result = "on";
            } else {
                result = "off";
            }
            RequestBody post = new FormBody.Builder()
                    .add("sensor_ID", sensor)
                    .add("status", result)
                    .build();
            final Request request = new Request.Builder()
                    .url("http://163.17.135.66/sensor/conFan.php")
                    .post(post)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //失敗的情況 (一般是網路或伺服器的問題)
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    final String Code;
                    Code = Objects.requireNonNull(response.body()).string();
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json = new JSONObject(Code);
                                String result = json.getString("result");
                                Log.d("fan_control_result", result);
                                if (result.equals("on")) {
                                    Log.d("Code", Code);
                                    Toast.makeText(getActivity(), "開啟", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "關閉", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    };


    // 背景更新資料 & 畫面
    final Runnable runnable_data = new Runnable() {
        @Override
        public void run() {
            RequestBody post = new FormBody.Builder()
                    .add("sensor_ID", sensor)
                    .build();
            // 建立連線請求，寫入網址URL和方法POST
            final Request request = new Request.Builder()
                    .url("http://163.17.135.66/sensor/searchData.php")
                    .post(post)
                    .build();
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d("real_time_data_failure", e.toString());
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    final String retCode;
                    retCode = Objects.requireNonNull(response.body()).string();
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("retCode", retCode);
                                for (int i = 0; i < 5; i++) {
                                    JSONObject json = new JSONObject(retCode);
                                    if (json.getString("DB").equals("null")) {
                                        db.setText("0");
                                        handler.removeCallbacks(runnable_data);
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                new AlertDialog.Builder(getContext())
                                                        .setTitle("警告!")
                                                        .setMessage("必須先綁定設備")
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                            }
                                                        })
                                                        .show();
                                            }
                                        });
                                        break;
                                    }
                                    switch (json.names().getString(i)) {
                                        case "DB":
                                            db.setText(String.valueOf(json.optString("DB")));
                                            value_dB = Integer.parseInt(json.optString("DB"));
                                            if (value_dB >= 75) {
                                                db.setTextColor(Color.parseColor("#ff0000"));
                                            } else {
                                                db.setTextColor(Color.parseColor("#339933"));
                                            }
                                            break;
                                        case "CH4":
                                            value_CH4 = Float.valueOf(json.getString("CH4"));
                                            ch4 = Math.round(value_CH4);
                                            CH4.setText(String.valueOf(ch4));
                                            if (ch4 >= 200) {
                                                CH4.setTextColor(Color.parseColor("#ff0000"));
                                            } else {
                                                CH4.setTextColor(Color.parseColor("#339933"));
                                            }
                                            break;
                                        case "CO":
                                            value_CO = Float.valueOf(json.getString("CO"));
                                            co = Math.round(value_CO);
                                            CO.setText(String.valueOf(co));
                                            if (co >= 20) {
                                                CO.setTextColor(Color.parseColor("#ff0000"));
                                            } else {
                                                CO.setTextColor(Color.parseColor("#339933"));
                                            }
                                            break;
                                        case "smoke":
                                            value_Smoke = Integer.parseInt(json.optString("smoke"));
                                            Smoke.setText(String.valueOf(json.optString("smoke")));
                                            if (value_Smoke >= 10000) {
                                                Smoke.setTextColor(Color.parseColor("#ff0000"));
                                            } else {
                                                Smoke.setTextColor(Color.parseColor("#339933"));
                                            }
                                            break;
                                        case "status":
                                            result = json.getString("status");
                                            break;
                                    }
                                    //Log.d("Data_JSON", value_dB + "," + co + "," + ch4  + ","  + value_Smoke);
                                    if (co >= 20 || ch4 >= 200 || value_Smoke >= 10000) {
                                        fan.setChecked(true);
                                        fan.setClickable(false);
                                    } else {
                                        fan.setClickable(true);
                                    }
                                }
                            } catch (Exception e) {
                                Log.d("log", e.toString());
                                e.printStackTrace();
                            }

                        }
                    });
                }
            });
            handler.postDelayed(this, 1000);
        }
    };


    // 離開畫面後 取消即時資訊的更新
    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable_data);
        super.onDestroy();
    }

}
