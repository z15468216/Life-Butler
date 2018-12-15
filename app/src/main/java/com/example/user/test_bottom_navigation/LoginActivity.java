package com.example.user.test_bottom_navigation;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

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

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


public class LoginActivity extends Activity {

    private static final int REQUEST_CONTACTS = 1;
    private AutoCompleteTextView auto_acc, auto_pw;
    private CheckBox remember;
    Button btn_login;
    String s, result, name, get_acc, get_pw, token;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //未取得權限，向使用者要求允許權限
            ActivityCompat.requestPermissions(this,
                    new String[]{READ_CONTACTS, CALL_PHONE, READ_EXTERNAL_STORAGE},
                    REQUEST_CONTACTS);
        } else {
            //已有權限，可進行檔案存取
            readContacts();
        }

        auto_acc = findViewById(R.id.auto_acc);
        auto_pw = findViewById(R.id.auto_pw);
        remember = findViewById(R.id.checkbox_rem);
        remember.setOnCheckedChangeListener(remOnListener);
        TextView txt_forget = findViewById(R.id.txt_forget);
        txt_forget.setOnClickListener(forgetOnListener);
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(loginOnListener);

        //先從設定檔 UserInfo 分別抓出 account 和 password 兩個字串來看使用者是否曾經設置過
        sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        get_acc =  sp.getString("acc", "");
        get_pw = sp.getString("pass", "");
        auto_acc.setText(sp.getString("acc", get_acc));
        auto_pw.setText(sp.getString("pass", get_pw));
        token = getSharedPreferences("UserInfo", Context.MODE_PRIVATE).getString("token", "");
        Log.d("token", token);
        Log.d("get_acc", get_acc);

        if ( !get_acc.isEmpty() ) {
            Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
            intent.putExtra("account", get_acc);
            startActivity(intent);
            finish();
        }
    }

    // "記住我" 勾選按鈕
    private CheckBox.OnCheckedChangeListener remOnListener = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("acc", auto_acc.getText().toString());
                edit.putString("pass", auto_pw.getText().toString());
                edit.apply();
            }
            if (!isChecked) {
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("acc", "");
                edit.putString("pass", "");
                edit.apply();
            }
        }
    };

    private Button.OnClickListener loginOnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            btn_login.setClickable(false);
            if (remember.isChecked()) // 判斷使用者是否有勾選 "記得我"
            {
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("acc", auto_acc.getText().toString());
                edit.putString("pass", auto_pw.getText().toString());
                edit.apply();
            }
            login();
        }
    };

    // 忘記密碼
    private TextView.OnClickListener forgetOnListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent it_forget = new Intent();
            it_forget.setClass(LoginActivity.this, ForgetPasswordActivity.class);
            startActivity(it_forget);
            finish();
        }
    };

    // 登入連線取得回覆
    private void login() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        final String account = auto_acc.getText().toString().trim().toLowerCase();
        // 創建 post 表單
        RequestBody post = new FormBody.Builder()
                .add("account", auto_acc.getText().toString().trim().toLowerCase())
                .add("password", auto_pw.getText().toString().trim().toLowerCase())
                .add("token", token)
                .build();
        Log.d("post", auto_acc.getText().toString().trim().toLowerCase() + "," + auto_pw.getText().toString().trim().toLowerCase() + "," + token);
        // 建立連線請求，寫入網址 URL 和方法 POST
        final Request request = new Request.Builder()
                .url("http://163.17.135.66/users/signin.php")
                .post(post)
                .build();
        //Log.d("Register_Request", "Request Success");
        // 請求的回覆
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //Log.d("Register_Failure", e.toString());
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
                            //Log.d("Register_Response", "Response_ing....");
                            JSONObject json = new JSONObject(retCode);
                            result = json.getString("result");
                            switch (result) {
                                case "success": {
                                    Toast.makeText(LoginActivity.this, "登入成功", Toast.LENGTH_SHORT).show();
                                    Timer timer = new Timer();
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            Intent it = new Intent();
                                            it.setClass(LoginActivity.this, HomePageActivity.class);
                                            it.putExtra("account", account);
                                            startActivity(it);
                                            finish();
                                        }
                                    };
                                    timer.schedule(task, 1000);
                                    break;
                                }
                                case "account_error": {
                                    auto_pw.setText("");
                                    Toast.makeText(LoginActivity.this, "無此使用者帳號", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                case "password_error": {
                                    auto_pw.setText("");
                                    Toast.makeText(LoginActivity.this, "輸入錯誤，請重新輸入", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                case "login_error": {
                                    auto_pw.setText("");
                                    Toast.makeText(LoginActivity.this, "發生未知錯誤，請重新輸入", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                default: {
                                    Timer timer = new Timer();
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            auto_pw.setText("");
                                            Toast.makeText(LoginActivity.this, "發生未知錯誤，請檢查網路連線", Toast.LENGTH_SHORT).show();
                                        }
                                    };
                                    timer.schedule(task, 7000);
                                    timer.cancel();
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            //Log.d("log", e.toString());
                            e.printStackTrace();
                        }
                        //Log.d("retCode", retCode);
                    }
                });
            }
        });
    }

    private void readContacts() {
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

}
