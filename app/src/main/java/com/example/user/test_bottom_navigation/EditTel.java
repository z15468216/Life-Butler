package com.example.user.test_bottom_navigation;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
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

public class EditTel extends Activity {

    OkHttpClient client = new OkHttpClient.Builder().build();
    android.support.v7.widget.Toolbar toolbar;
    TextView title;
    String tel, acc, group, sensor;
    EditText edit_tel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tel);

        Intent it = getIntent();
        acc = it.getStringExtra("account");
        sensor = it.getStringExtra("sensor_ID");
        group = it.getStringExtra("group_ID");

        edit_tel = findViewById(R.id.input_edit_tel);
        Button btn_tel = findViewById(R.id.edit_tel_btn);
        btn_tel.setOnClickListener(telOnClickListener);

        toolbar = findViewById(R.id.toolbar);
        title = toolbar.findViewById(R.id.toolbar_title);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private Button.OnClickListener telOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            tel = edit_tel.getText().toString();
            if (tel.isEmpty()) {
                edit_tel.setText("");
                Toast.makeText(getApplicationContext(), "輸入部分不可空白", Toast.LENGTH_SHORT).show();
            } else {
                init(tel);
            }
        }
    };

    private void init(final String New_name) {
        RequestBody post = new FormBody.Builder()
                .add("account", acc)
                .add("tel", tel)
                .build();
        // 建立連線請求，寫入網址URL和方法POST
        final Request request = new Request.Builder()
                .url("http://163.17.135.66/users/contact.php")
                .post(post)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //Log.d("create_group__failure", e.toString());
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String callback, result;
                callback = Objects.requireNonNull(response.body()).string();
                //Log.d("result", callback);
                try {
                    JSONObject json = new JSONObject(callback);
                    result = json.getString("result");
                    Log.d("edit_tel", result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result.equals("success")) {
                                Intent it = new Intent();
                                it.putExtra("tel", tel);
                                setResult(RESULT_OK, it);
                                finish();
                                Toast.makeText(getApplicationContext(), "成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "請重新輸入", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
