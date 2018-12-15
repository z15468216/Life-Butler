package com.example.user.test_bottom_navigation;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.user.test_bottom_navigation.service.MyService;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class UserFragment extends Fragment {

    OkHttpClient client = new OkHttpClient.Builder().build();
    String name, acc, sensor, group, count, be_before, analyze, before, url, img;
    TextView user_name, user_be_before, user_before, user_analyze;
    ImageButton help;
    SharedPreferences sp;
    ListView listview;
    ImageView image;
    Bundle bundle;
    Bitmap bitmap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        name = Objects.requireNonNull(getArguments()).getString("user_ID");
        acc = getArguments().getString("account");
        sensor = getArguments().getString("sensor_ID");
        group = getArguments().getString("group_ID");
        be_before = getArguments().getString("be_before");
        analyze = getArguments().getString("analyze");
        before = getArguments().getString("before");

        Log.d("UserFragment", name + "," + acc + "," + sensor + "," + group + "," + be_before + "," + analyze + "," + before);

    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_user_fragment, container, false);

        user_name = view.findViewById(R.id.user_name);
        user_be_before = view.findViewById(R.id.be_before_value);
        user_analyze = view.findViewById(R.id.analyze_value);
        user_before = view.findViewById(R.id.before_value);
        listview = view.findViewById(R.id.profile_list);
        image = view.findViewById(R.id.user_img);
        help = view.findViewById(R.id.img_help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog isExit = new AlertDialog.Builder(getContext()).create();
                isExit.setMessage("0 → Excellent 環境安閒舒適\n" + "1 → Very Good 環境寧靜\n" + "2 → Well 環境品質佳\n" +
                        "3 → Good 環境品質良好\n" + "4 → Not Bad 環境品質不錯\n" + "5 → Normal 環境品質普通\n" +"6 → Not Good 環境品質稍差 請注意\n" +
                "7 → Bad 環境品質差，請注意\n" + "8 → Loud 環境品質吵雜，請迴避\n" + "9 → Noisy 環境品質惡劣 請盡速迴避\n");
                isExit.setButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

                isExit.show();
            }
        });

        Thread t1 = new Thread(run);
        t1.start();
        updateIMG();

        String[] item = {"使用者資料", "群組", "感測器", "登出"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.list_view_item, R.id.txtView, item);
        listview.setAdapter(adapter);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        Intent it0 = new Intent();
                        it0.setClass(Objects.requireNonNull(getContext()), UserActivity.class);
                        it0.putExtra("account", acc);
                        it0.putExtra("group_ID", group);
                        it0.putExtra("sensor_ID", sensor);
                        getActivity().startActivityForResult(it0, 0);
                        break;
                    case 1:
                        Intent it1 = new Intent();
                        it1.setClass(Objects.requireNonNull(getContext()), GroupActivity.class);
                        it1.putExtra("account", acc);
                        it1.putExtra("group_ID", group);
                        it1.putExtra("sensor_ID", sensor);
                        getActivity().startActivityForResult(it1, 0);
                        break;
                    case 2:
                        Intent it2 = new Intent();
                        it2.setClass(Objects.requireNonNull(getContext()), SensorActivity.class);
                        it2.putExtra("account", acc);
                        it2.putExtra("group_ID", group);
                        it2.putExtra("sensor_ID", sensor);
                        getActivity().startActivityForResult(it2, 0);
                        break;
                    case 3:
                        AlertDialog isExit = new AlertDialog.Builder(getContext()).create();
                        isExit.setMessage("確定要登出?");
                        isExit.setButton("Yes", quitListener);
                        isExit.setButton2("Cancel", quitListener);
                        isExit.show();
                        break;
                    default:
                        break;
                }
            }
        });
        return view;
    }

    final Runnable run = new Runnable() {
        @Override
        public void run() {
            user_name.setText(name);
            user_be_before.setText(be_before);
            user_analyze.setText(analyze);
            user_before.setText(before);
        }
    };


    // 登出按鈕
    DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    Intent it3 = new Intent();
                    it3.setClass(Objects.requireNonNull(getContext()), LoginActivity.class);
                    SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    startActivity(it3);
                    getActivity().finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    break;
                default:
                    break;
            }
        }
    };

    // Intent 回傳資訊處理
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    //init();
                }
                break;
        }
    }

    public void updateIMG() {
        img = Objects.requireNonNull(getActivity()).getSharedPreferences("UserInfo", Context.MODE_PRIVATE).getString("image", "");
        bitmap = BitmapFactory.decodeFile(img);
        image.setImageBitmap(bitmap);

    }

    @Override
    public void onResume() {
        super.onResume();
        onCreate(null);
    }
}