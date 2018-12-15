package com.example.user.test_bottom_navigation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import me.anwarshahriar.calligrapher.Calligrapher;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends Activity {

    private EditText edt_acc, edt_pw, edt_pw_check, edt_nickname, edt_mail;
    private Button btn_register;
    String s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
/*
        Calligrapher calligrapher = new Calligrapher(this);
        calligrapher.setFont(this, "fonts/font_type01.ttf", true);
*/

        edt_acc = findViewById(R.id.edt_register_acc);
        edt_pw = findViewById(R.id.edt_register_password);
        edt_pw_check = findViewById(R.id.edt_register_password2);
        edt_nickname = findViewById(R.id.edt_register_nickname);
        edt_mail = findViewById(R.id.edt_register_mail);
        btn_register = findViewById(R.id.btn_create);
        btn_register.setOnClickListener(registerListener);

    }

    // 退出 APP
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

    // 註冊按鈕
    private Button.OnClickListener registerListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btn_register) { // 判斷密碼是否相同
                if ((edt_pw.getText().toString()).equals(edt_pw_check.getText().toString())) {
                    Register_link();
                } else {
                    edt_pw.setText("");
                    edt_pw_check.setText("");
                    Toast.makeText(RegisterActivity.this, "輸入密碼不相同，請重新輸入。", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    // 註冊連線取得回覆
    private void Register_link() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        // 創建 post 表單
        RequestBody post = new FormBody.Builder()
                .add("account", edt_acc.getText().toString())
                .add("password", edt_pw.getText().toString())
                //.add("pw_check", edt_pw_check.getText().toString())
                .add("name", edt_nickname.getText().toString())
                .add("email", edt_mail.getText().toString())
                .build();
        // 建立連線請求，寫入網址URL和方法POST
        final Request request = new Request.Builder()
                .url("http://163.17.135.66/users/signup.php")
                .post(post)
                .build();
        //Log.d("register_request", "request success");
        // 請求的回覆
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("register_failure", e.toString());
                //失敗的情況 (一般是網路或伺服器的問題)
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) {
                final String[] retCode = new String[1];
                //UI线程运行
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Log.d("register_response", "response ing");
                            retCode[0] = Objects.requireNonNull(response.body()).string();
                            // Log.d("register_response", "response success");
                            // 分析出資料
                            //  JSONObject jsonObject = new JSONObject(String.valueOf(MainActivity.this.s));
                            //  retCode = jsonObject.getInt("success");
                        } catch (Exception e) {
                            Log.d("log", e.toString());
                            e.printStackTrace();
                        }

                        Log.d("retCode", retCode[0]);
                        switch (retCode[0]) {
                            case "success":
                                Toast.makeText(RegisterActivity.this, "註冊成功", Toast.LENGTH_SHORT).show();
                                Timer timer = new Timer();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        Intent it = new Intent();
                                        it.setClass(RegisterActivity.this, LoginActivity.class);
                                        startActivity(it);
                                        finish();
                                    }
                                };
                                timer.schedule(task, 2000);
                                break;
                            case "repeat_account":
                                Toast.makeText(RegisterActivity.this, "帳號已經被註冊過", Toast.LENGTH_SHORT).show();
                                break;
                            case "repeat_mail":
                                Toast.makeText(RegisterActivity.this, "電子信箱已被註冊過", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(RegisterActivity.this, "發生未知錯誤，請檢查網路連線", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            }
        });
    }
}
