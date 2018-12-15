package com.example.user.test_bottom_navigation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import static android.Manifest.permission.CALL_PHONE;

public class CustomerDialog extends Activity {

    OkHttpClient client = new OkHttpClient.Builder().build();
    Button btn_confirm;
    TextView text_content;
    TextView text_title;
    String body, acc, Tel;
    Vibrator v;
    Handler handler;
    AudioManager audioManager;
    MediaPlayer mediaPlayer;
    int max;
    long[] pattern = {1000, 3000};
    CountDownTimer cdt_v, cdt_m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_customer_dialog);

        Intent it = getIntent();
        acc = it.getStringExtra("acc");
        body = it.getStringExtra("text");

        btn_confirm = findViewById(R.id.btn_confirm);
        text_title = findViewById(R.id.text_title);
        text_title.setText("警告!");
        text_content = findViewById(R.id.text_content);
        text_content.setText(body);
        Log.d("Body", body);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (v.hasVibrator()) {
                    v.cancel();
                    cdt_v.cancel();
                    handler.removeCallbacks(runnable_customer);
                }
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    cdt_m.cancel();
                    handler.removeCallbacks(runnable_customer);
                }
                Toast.makeText(CustomerDialog.this, "收到!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        max = Objects.requireNonNull(audioManager).getStreamMaxVolume(AudioManager.STREAM_RING);
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mediaPlayer = MediaPlayer.create(CustomerDialog.this, R.raw.the_moon_theme);

        //init_customer();

        handler = new Handler();
        handler.post(runnable_customer);

    }

    final Runnable runnable_customer = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(10000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(pattern, 0);
                cdt_v = new  CountDownTimer(10000, 1000) {
                    @Override
                    public void onTick(long l) {
                        Log.d("countdown_vibrate", String.valueOf(l));
                    }

                    @Override
                    public void onFinish() {
                        v.cancel();
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, max, AudioManager.FLAG_PLAY_SOUND);
                        mediaPlayer.start();
                        Log.d("sound_end", "end");
                        cdt_m = new CountDownTimer(20000, 1000) {
                            @Override
                            public void onTick(long l) {
                                Log.d("countdown_media", String.valueOf(l));
                            }

                            @Override
                            public void onFinish() {
                                mediaPlayer.stop();
                                Log.d("call_tel", Tel);
                                Intent phoneIntent = new Intent(Intent.ACTION_CALL, Uri.parse(Tel));
                                if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                    try {
                                        startActivity(phoneIntent);
                                        finish();
                                    } catch (android.content.ActivityNotFoundException ignored) {

                                    }
                                }
                            }
                        }.start();
                    }
                }.start();
            }
        }
    };

    private void init_customer() {
        RequestBody post = new FormBody.Builder()
                .add("account", acc)
                .build();

        // 建立連線請求，寫入網址 URL 和方法 POST
        final Request request = new Request.Builder()
                .url("http://163.17.135.66/users/get_tel.php")
                .post(post)
                .build();

        // 請求的回覆
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Get_Tel_Failure", e.toString());
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
                            Tel = json.getString("Tel");
                            Log.d("tel_init", Tel);
                        } catch (Exception e) {
                            Log.d("log", e.toString());
                            e.printStackTrace();
                        }
                        Log.d("retCode", retCode);
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        init_customer();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable_customer);
        super.onDestroy();
    }
}
