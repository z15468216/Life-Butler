package com.example.user.test_bottom_navigation;

import android.app.Activity;
import android.content.Intent;
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

public class ForgetPasswordActivity extends Activity {

    private EditText edt_forget_nickname, edt_forget_acc, edt_forget_mail;
    private Button btn_send;
    String s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        edt_forget_nickname = findViewById(R.id.edt_forget_nickname);
        edt_forget_acc = findViewById(R.id.edt_forget_acc);
        edt_forget_mail = findViewById(R.id.edt_forget_mail);
        btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(sendListener);
    }

    // 返回登入畫面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent it = new Intent();
            it.setClass(ForgetPasswordActivity.this, LoginActivity.class);
            startActivity(it);
            this.finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 取得輸入內容
    private Button.OnClickListener sendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btn_send) {
                SendMail();
            }
        }
    };

    private void SendMail() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        // 創建 post 表單
        RequestBody post = new FormBody.Builder()
                .add("name", edt_forget_nickname.getText().toString())
                .add("account", edt_forget_acc.getText().toString())
                .add("email", edt_forget_mail.getText().toString())
                .build();
        // 建立連線請求，寫入網址URL和方法POST
        final Request request = new Request.Builder()
                .url("http://163.17.135.66/users/maiToYou.php")
                .post(post)
                .build();
        //Log.d("forgot_request","request success");
        // 請求的回覆
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //Log.d("forgot_failure",e.toString());
                //失敗的情況 (一般是網路或伺服器的問題)
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                final String retCode;
                retCode = Objects.requireNonNull(response.body()).string();
                //UI线程运行
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("retCode",retCode);
                        // 取出回覆的值判斷要顯示的內容
                        switch (retCode) {
                            case "success":
                                Toast.makeText(ForgetPasswordActivity.this, "驗證成功，請查收郵件。", Toast.LENGTH_SHORT).show();
                                Timer timer = new Timer();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        Intent it = new Intent();
                                        it.setClass(ForgetPasswordActivity.this, LoginActivity.class);
                                        startActivity(it);
                                        finish();
                                    }
                                };
                                timer.schedule(task, 2000);
                                break;
                            case "false":
                                edt_forget_nickname.setText("");
                                edt_forget_acc.setText("");
                                edt_forget_mail.setText("");
                                Toast.makeText(ForgetPasswordActivity.this, "驗證錯誤，請重新輸入", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(ForgetPasswordActivity.this, "發生錯誤，請確認網路狀況", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            }
        });
    }
}
