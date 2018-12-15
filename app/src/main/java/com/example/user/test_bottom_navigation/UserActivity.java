package com.example.user.test_bottom_navigation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UserActivity extends Activity {

    OkHttpClient client = new OkHttpClient.Builder().build();
    android.support.v7.widget.Toolbar toolbar;
    TextView title;
    ImageView image;
    ListView listview;
    SimpleAdapter simpleAdapter;
    String acc, sensor, group, name, img;
    String[] ListTitle = {"帳號", "暱稱", "緊急聯絡人", "通知訊息", "電子信箱"};
    String[] txt = {"account", "nickname", "tel", "mode", "mail"};
    String[] ListContent = new String[5];
    SharedPreferences sp;
    Uri filePath;
    Bitmap bitmap;
    Boolean first = true;
    private static final String UPLOAD_URL = "http://163.17.135.66/users/imgtest.php";
    private static final int IMAGE_REQUEST_CODE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Intent it = getIntent();
        acc = it.getStringExtra("account");
        sensor = it.getStringExtra("sensor_ID");
        group = it.getStringExtra("group_ID");

        image = findViewById(R.id.profile_photo);
        img = getSharedPreferences("UserInfo", Context.MODE_PRIVATE).getString("image", "");
        bitmap = BitmapFactory.decodeFile(img);
        image.setImageBitmap(bitmap);

        if (first) {
            first = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    _init();
                }
            }).start();
        }

        List<HashMap<String, String>> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("Title", ListTitle[i]);
            hm.put("Content", ListContent[i]);
            list.add(hm);
        }

        String[] from =
                {"Title", "Content"};
        int[] to =
                {R.id.txtTitle, R.id.txtContent};

        simpleAdapter = new SimpleAdapter(getBaseContext(), list, R.layout.list_view_user, from, to);
        listview = findViewById(R.id.group_member_list);
        listview.setAdapter(simpleAdapter);

        image.setOnClickListener(imageOnListener);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        Intent it1 = new Intent();
                        it1.setClass(UserActivity.this, EditName.class);
                        it1.putExtra("account", acc);
                        it1.putExtra("group_ID", group);
                        it1.putExtra("sensor_ID", sensor);
                        startActivityForResult(it1, 1);
                        break;
                    case 2:
                        Intent it2 = new Intent();
                        it2.setClass(UserActivity.this, EditTel.class);
                        it2.putExtra("account", acc);
                        it2.putExtra("group_ID", group);
                        it2.putExtra("sensor_ID", sensor);
                        startActivityForResult(it2, 2);
                        break;
                    case 3:
                        break;
                    case 4:
                        Intent it4 = new Intent();
                        it4.setClass(UserActivity.this, EditMail.class);
                        it4.putExtra("account", acc);
                        it4.putExtra("group_ID", group);
                        it4.putExtra("sensor_ID", sensor);
                        startActivityForResult(it4, 4);
                        break;
                    default:
                        break;
                }
            }
        });

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

    private ImageView.OnClickListener imageOnListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), IMAGE_REQUEST_CODE);

        }
    };

    // 退出 APP 按鈕
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent it = new Intent();
            setResult(RESULT_CANCELED, it);
            finish();
        }
        return false;
    }


    private void _init() {
        RequestBody post = new FormBody.Builder()
                .add("account", acc)
                .build();
        final Request request = new Request.Builder()
                .url("http://163.17.135.66/users/setting.php")
                .post(post)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
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
                            JSONObject json = new JSONObject(retCode);
                            for (int i = 0; i < 5; i++) {
                                ListContent[i] = json.getString(txt[i]);
                            }
                            Log.d("ListContent", Arrays.toString(ListContent));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onResume();
                    }
                });
            }
        });
    }

    // Intent 回傳資訊處理
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    _init();
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    _init();
                }
                break;
            case 4:
                if (resultCode == RESULT_OK) {
                    _init();
                }
                break;
            case 5:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    filePath = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                image.setImageBitmap(bitmap);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onResume();
                                    }
                                });
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    uploadMultipart();
                }
                break;
        }
    }

    public void uploadMultipart() {
        String path = getPath(filePath);
        Log.d("uri", path);
        try {
            String uploadId = UUID.randomUUID().toString();
            new MultipartUploadRequest(this, uploadId, UPLOAD_URL)
                    .addFileToUpload(path, "img") //Adding file
                    .addParameter("account", acc) //Adding text parameter to the request
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
        } catch (Exception exc) {
        }
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        Objects.requireNonNull(cursor).moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        Objects.requireNonNull(cursor).moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        return path;
    }

    @Override
    protected void onResume() {
        super.onResume();
        onCreate(null);
    }
}
