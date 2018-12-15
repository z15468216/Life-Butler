package com.example.user.test_bottom_navigation;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChartFragment extends Fragment {

    OkHttpClient client = new OkHttpClient.Builder().build();
    String name, acc, sensor, group, count, be_before, analyze, before, cate;
    TextView calendar;
    Button btn_draw;
    String date = "null";
    BarChart mChart;
    Paint p;
    int i = 0;
    float barWidth, barSpace, value;
    ArrayList<BarEntry> yVal = new ArrayList<>();
    DatePickerDialog datePickerDialog;

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

    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chart_fragment, container, false);

        Spinner spinner = view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(spinnerOnListener);
        String[] item = {"Decibel", "CO", "CH4", "Smoke"};
        calendar = view.findViewById(R.id.calendar);
        calendar.setOnClickListener(dateOnListener);
        btn_draw = view.findViewById(R.id.chart_start);
        btn_draw.setOnClickListener(drawOnListener);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.spinner_item, R.id.spinner_txt, item);
        spinner.setAdapter(adapter);

        barWidth = 0.3f;
        barSpace = 0f;

        mChart = view.findViewById(R.id.chart);
        mChart.getDescription().setEnabled(false); // 描述 「不顯示」
        mChart.setPinchZoom(false);
        mChart.setDrawBarShadow(false);  // 顯示長條圖陰影
        mChart.setDrawGridBackground(false);  // 顯示網格
        mChart.setScaleEnabled(false);  //设置是否可以缩放
        mChart.getAxisLeft().setStartAtZero(true); // deprecated
        mChart.setNoDataText("尚未選擇內容");
        mChart.setNoDataTextColor(Color.BLACK);

        // 初始畫面 "No data set available" 字體大小
        p = mChart.getPaint(Chart.PAINT_INFO);
        if (p != null) {
            p.setTextSize(45);
        }

        // X軸欄位內容
        final ArrayList<String> xAxisLabel = new ArrayList<>();
        xAxisLabel.add("0:00");
        xAxisLabel.add("3:00");
        xAxisLabel.add("6:00");
        xAxisLabel.add("9:00");
        xAxisLabel.add("12:00");
        xAxisLabel.add("15:00");
        xAxisLabel.add("18:00");
        xAxisLabel.add("21:00");

        // X軸樣式設定
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 置底
        xAxis.setDrawGridLines(false); //不會至網格
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xAxisLabel.get((int) value);
            }
        });

        mChart.animateY(2500); // Y軸動畫時間
        mChart.getAxisRight().setEnabled(false);
        mChart.getLegend().setEnabled(false);
        //mChart.invalidate(); // 通知圖表有變更

        return view;
    }

    // 選擇日期 Calendar
    private TextView.OnClickListener dateOnListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            datePickerDialog = new DatePickerDialog(
                    Objects.requireNonNull(getActivity()), android.R.style.Theme_Holo_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    calendar.setText(i2 + "-" + (i1 + 1) + "-" + i);
                    date = calendar.getText().toString();
                }
            }, mYear, mMonth, mDay);
            // 透明背景
            Objects.requireNonNull(datePickerDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        }
    };


    // 選擇欲查詢的數據類別 ( DB、CO、CH4、Smoke )
    private Spinner.OnItemSelectedListener spinnerOnListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            switch (adapterView.getItemAtPosition(i).toString()) {
                case "Decibel":
                    cate = "db";
                    break;
                case "CH4":
                    cate = "CH4";
                    break;
                case "CO":
                    cate = "CO";
                    break;
                case "Smoke":
                    cate = "smoke";
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    // 繪圖按鈕  如果沒選擇日期會跳出提視窗
    private Button.OnClickListener drawOnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!date.equals("null")) {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i != 0) {
                            resetChart();
                            init();
                        } else {
                            init();
                            i = 1;
                        }


                    }
                });
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle("Error!")
                        .setMessage("Please choose a date.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            }
        }
    };

    // 清空舊圖表
    private void resetChart() {
        mChart.fitScreen();
        yVal.clear();
        mChart.clearValues();
        mChart.clear();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    // 連線取得圖表資料
    private void init() {
        RequestBody post = new FormBody.Builder()
                .add("sensor_ID", sensor)
                .add("date", date)
                .add("cate", cate)
                .build();
        Log.d("post", sensor + " " + date + " " + cate);
        final Request request = new Request.Builder()
                .url("http://163.17.135.66/sensor/chart.php")
                .post(post)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("chart_data_get_failure", e.toString());
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String retCode;
                retCode = Objects.requireNonNull(response.body()).string();
                Log.d("retCode", retCode);
                try {
                    JSONArray ja = new JSONArray(retCode);
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject obj = ja.getJSONObject(i);
                        value = (float) obj.getDouble("data");
                        yVal.add(new BarEntry(i, value));
                        BarDataSet set = new BarDataSet(yVal, "Average Data");
                        set.setColors(ColorTemplate.VORDIPLOM_COLORS);
                        set.setDrawValues(true);
                        set.setValueTextSize(15f);

                        BarData data = new BarData(set);
                        mChart.setData(data);
                        mChart.setFitBars(true);
                    }
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mChart.invalidate();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
