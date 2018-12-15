package com.example.user.test_bottom_navigation;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
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

public class EditName extends Activity {

    OkHttpClient client = new OkHttpClient.Builder().build();
    android.support.v7.widget.Toolbar toolbar;
    TextView title;
    String name, acc, group, sensor;
    EditText edit_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        Intent it = getIntent();
        acc = it.getStringExtra("account");
        sensor = it.getStringExtra("sensor_ID");
        group = it.getStringExtra("group_ID");

        edit_name = findViewById(R.id.input_edit_name);
        Button  edit_name_btn = findViewById(R.id.edit_name_btn);
        edit_name_btn.setOnClickListener(editNameOnClickListener);

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

    private Button.OnClickListener editNameOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            name = edit_name.getText().toString();
            if (name.isEmpty()) {
                edit_name.setText("");
                Toast.makeText(getApplicationContext(), "輸入部分不可空白", Toast.LENGTH_SHORT).show();
            } else {
                init(name);
            }
        }
    };

    private void init(final String New_name) {
        RequestBody post = new FormBody.Builder()
                .add("account", acc)
                .add("name", New_name)
                .build();
        // 建立連線請求，寫入網址URL和方法POST
        final Request request = new Request.Builder()
                .url("http://163.17.135.66/users/mod_nickname.php")
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
                try {
                    JSONObject json = new JSONObject(callback);
                    result = json.getString("result");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result.equals("success")) {
                                Intent it = new Intent();
                                it.putExtra("nickname", New_name);
                                setResult(RESULT_OK, it);
                                finish();
                                Toast.makeText(getApplicationContext(), "成功", Toast.LENGTH_SHORT).show();
                            } else {
                                edit_name.setText("");
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
