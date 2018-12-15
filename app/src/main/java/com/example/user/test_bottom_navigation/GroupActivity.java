package com.example.user.test_bottom_navigation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupActivity extends Activity {

    OkHttpClient client = new OkHttpClient.Builder().build();
    android.support.v7.widget.Toolbar toolbar;
    TextView title, group_title, group_name;
    String acc, sensor, group;
    Button create, join;
    ListView list;
    List<String> items = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Intent it = getIntent();
        acc = it.getStringExtra("account");
        sensor = it.getStringExtra("sensor_ID");
        group = it.getStringExtra("group_ID");

        list = findViewById(R.id.group_member_list);
        list.setVisibility(View.GONE);
        group_title = findViewById(R.id.group_txt);
        group_name = findViewById(R.id.group_name);
        group_name.setVisibility(View.GONE);
        create = findViewById(R.id.group_create);
        create.setOnClickListener(createOnListener);
        join = findViewById(R.id.group_join);
        join.setOnClickListener(joinOnListener);

        // 判斷是否已綁定群組
        if (!group.equals("null")) {
            create.setVisibility(View.GONE);
            join.setVisibility(View.GONE);
            group_title.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            group_name.setVisibility(View.VISIBLE);
            init();
        }

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

    // 創建新群組
    private Button.OnClickListener createOnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            @SuppressLint("InflateParams") final View item = LayoutInflater.from(GroupActivity.this).inflate(R.layout.item_layout, null);
            new AlertDialog.Builder(GroupActivity.this)
                    .setTitle("請輸入群組名稱")
                    .setView(item)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final EditText editText = item.findViewById(R.id.edt_sensor);
                            final String name = editText.getText().toString();
                            if (TextUtils.isEmpty(name)) {
                                editText.setText("");
                                Toast.makeText(getApplicationContext(), "不可空白", Toast.LENGTH_SHORT).show();
                            } else {
                                RequestBody post = new FormBody.Builder()
                                        .add("cate", "create")
                                        .add("account", acc)
                                        .add("group_ID", editText.getText().toString())
                                        .build();
                                // 建立連線請求，寫入網址URL和方法POST
                                final Request request = new Request.Builder()
                                        .url("http://163.17.135.66/users/addFamily.php")
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
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (result.equals("create success")) {
                                                        Intent it = new Intent();
                                                        it.putExtra("group_ID", group);
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
                    })
                    .show();
        }
    };

    // 加入舊有群組
    private Button.OnClickListener joinOnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            @SuppressLint("InflateParams") final View item = LayoutInflater.from(GroupActivity.this).inflate(R.layout.item_layout, null);
            new AlertDialog.Builder(GroupActivity.this)
                    .setTitle("請輸入群組名稱")
                    .setView(item)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final EditText editText = item.findViewById(R.id.edt_sensor);
                            final String name = editText.getText().toString();
                            if (TextUtils.isEmpty(name)) {
                                editText.setText("");
                                Toast.makeText(getApplicationContext(), "請重新輸入", Toast.LENGTH_SHORT).show();
                            } else {
                                RequestBody post = new FormBody.Builder()
                                        .add("cate", "join")
                                        .add("account", acc)
                                        .add("group_ID", editText.getText().toString())
                                        .build();
                                // 建立連線請求，寫入網址URL和方法POST
                                final Request request = new Request.Builder()
                                        .url("http://163.17.135.66/users/addFamily.php")
                                        .post(post)
                                        .build();
                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        //Log.d("join_group_failure", e.toString());
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
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (result.equals("join success")) {
                                                        Intent it = new Intent();
                                                        it.putExtra("group_ID", group);
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
                    }).show();
        }
    };

    // 如果已綁定群組就連線獲得群組資訊
    private void init() {
        RequestBody post = new FormBody.Builder()
                .add("account", acc)
                .build();
        // 建立連線請求，寫入網址URL和方法POST
        final Request request = new Request.Builder()
                .url("http://163.17.135.66/users/group.php")
                .post(post)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("group_get_failure", e.toString());
                call.cancel();
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String retCode;
                retCode = Objects.requireNonNull(response.body()).string();
                //Log.d("retCode Get", retCode);
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        try {
                            JSONObject json = new JSONObject(retCode);
                            group_name.setText(group);
                            JSONArray array = json.getJSONArray("group_member");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                items.add(jsonObject.getString("name"));
                            }
                            //Log.d("items", String.valueOf(items));
                            adapter = new ArrayAdapter<>(getApplicationContext(),
                                    R.layout.list_view_item_group, R.id.txtView);
                            adapter.addAll(items);
                            adapter.notifyDataSetChanged();
                            list.setAdapter(adapter);
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

