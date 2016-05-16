package com.gookkis.monitoringlistrik;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "Monitor Listrik";
    OkHttpClient client = new OkHttpClient();
    String urlPush = "http://agnosthings.com/c2939ee6-1865-11e6-8001-005056805279/feed?push=kwh=";
    String url = "http://agnosthings.com/c2939ee6-1865-11e6-8001-005056805279/channel/last/feed/347/";
    BarChart barChart;
    LineChart lineChart;

    RadioButton rbTimeHour;
    RadioButton rbTimeDay;
    RadioButton rbTimeWeek;

    RadioButton rbPlotTypeLine;
    RadioButton rbPlotTypeBar;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barChart = (BarChart) findViewById(R.id.bar_chart);
        lineChart = (LineChart) findViewById(R.id.line_chart);

        progressBar = (ProgressBar) findViewById(R.id.marker_progress);
        progressBar.setVisibility(View.VISIBLE);

        rbTimeHour = (RadioButton) findViewById(R.id.rbPlotTimeHour);
        rbTimeDay = (RadioButton) findViewById(R.id.rbPlotTimeDay);
        rbTimeWeek = (RadioButton) findViewById(R.id.rbPlotTimeWeek);

        rbTimeHour.setOnClickListener(new OnTimeIntervalRadioButtonListener());
        rbTimeDay.setOnClickListener(new OnTimeIntervalRadioButtonListener());
        rbTimeWeek.setOnClickListener(new OnTimeIntervalRadioButtonListener());

        rbPlotTypeLine = (RadioButton) findViewById(R.id.rbPlotTypeLine);
        rbPlotTypeBar = (RadioButton) findViewById(R.id.rbPlotTypeBar);

        rbPlotTypeLine.setOnClickListener(new OnTimeIntervalRadioButtonListener());
        rbPlotTypeBar.setOnClickListener(new OnTimeIntervalRadioButtonListener());

        new GetChartAsyncTask().execute();

        //sendPushRandom();

    }

    private void sendPushRandom() {
        Random rand = new Random();
        for (int i = 0; i < 50; i++) {
            try {
                int n = rand.nextInt(30) + 10;
                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                String res = run(urlPush + n);
                Log.d(TAG, "sendPushRandom: " + res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    private class OnTimeIntervalRadioButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new GetChartAsyncTask().execute();
        }
    }


    String timeInterval, time;

    private class GetChartAsyncTask extends AsyncTask<Void, Void, float[]> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected float[] doInBackground(Void... arg) {
            float[] values = null;
            // Chosen time interval
            if (rbTimeHour.isChecked()) {
                timeInterval = "24";
                time = "hour";
                try {
                    String respon = run(url + timeInterval);
                    Gson gson = new Gson();
                    MonitorModel monitorModel = gson.fromJson(respon, MonitorModel.class);
                    values = new float[monitorModel.getCValue().size()];
                    for (int i = 0; i < monitorModel.getCValue().size(); i++) {
                        String[] kwh = monitorModel.getCValue().get(i).split(",");
                        //Log.d(TAG, "onParse: " + kwh[1]);
                        values[i] = Float.parseFloat(kwh[1].replace("%", ""));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (rbTimeDay.isChecked()) {
                timeInterval = "168"; //24x7
                time = "day";
                try {
                    String respon = run(url + timeInterval);
                    Gson gson = new Gson();
                    MonitorModel monitorModel = gson.fromJson(respon, MonitorModel.class);

                    float[] all = new float[monitorModel.getCValue().size()];
                    for (int i = 0; i < monitorModel.getCValue().size(); i++) {
                        String[] kwh = monitorModel.getCValue().get(i).split(",");
                        all[i] = Float.parseFloat(kwh[1].replace("%", ""));
                    }

                    float[] part1 = new float[all.length / 7];
                    float[] part2 = new float[all.length / 7];
                    float[] part3 = new float[all.length / 7];
                    float[] part4 = new float[all.length / 7];
                    float[] part5 = new float[all.length / 7];
                    float[] part6 = new float[all.length / 7];
                    float[] part7 = new float[all.length / 7];

                    System.arraycopy(all, 0, part1, 0, part1.length);
                    System.arraycopy(all, part1.length, part2, 0, part2.length);
                    System.arraycopy(all, part2.length, part3, 0, part3.length);
                    System.arraycopy(all, part3.length, part4, 0, part4.length);
                    System.arraycopy(all, part4.length, part5, 0, part5.length);
                    System.arraycopy(all, part5.length, part6, 0, part6.length);
                    System.arraycopy(all, part6.length, part7, 0, part7.length);

                    values = new float[7];
                    float n1 = 0;
                    for (int i = 0; i < part1.length; i++) {
                        n1 += part1[i];
                        values[0] = n1;
                    }

                    float n2 = 0;
                    for (int i = 0; i < part2.length; i++) {
                        n2 += part2[i];
                        values[1] = n1;
                    }

                    float n3 = 0;
                    for (int i = 0; i < part3.length; i++) {
                        n3 += part3[i];
                        values[2] = n3;
                    }
                    float n4 = 0;
                    for (int i = 0; i < part4.length; i++) {
                        n4 += part4[i];
                        values[3] = n4;
                    }
                    float n5 = 0;
                    for (int i = 0; i < part5.length; i++) {
                        n5 += part1[i];
                        values[4] = n5;
                    }
                    float n6 = 0;
                    for (int i = 0; i < part6.length; i++) {
                        n6 += part6[i];
                        values[5] = n6;
                    }
                    float n7 = 0;
                    for (int i = 0; i < part7.length; i++) {
                        n7 += part1[i];
                        values[6] = n7;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (rbTimeWeek.isChecked()) {
                timeInterval = "672"; //24x7X4
                time = "week";
                try {
                    String respon = run(url + timeInterval);
                    Gson gson = new Gson();
                    MonitorModel monitorModel = gson.fromJson(respon, MonitorModel.class);

                    float[] all = new float[monitorModel.getCValue().size()];
                    for (int i = 0; i < monitorModel.getCValue().size(); i++) {
                        String[] kwh = monitorModel.getCValue().get(i).split(",");
                        all[i] = Float.parseFloat(kwh[1].replace("%", ""));
                    }

                    float[] part1 = new float[all.length / 4];
                    float[] part2 = new float[all.length / 4];
                    float[] part3 = new float[all.length / 4];
                    float[] part4 = new float[all.length / 4];

                    System.arraycopy(all, 0, part1, 0, part1.length);
                    System.arraycopy(all, part1.length, part2, 0, part2.length);
                    System.arraycopy(all, part2.length, part3, 0, part3.length);
                    System.arraycopy(all, part3.length, part4, 0, part4.length);

                    values = new float[4];
                    float n1 = 0;
                    for (int i = 0; i < part1.length; i++) {
                        n1 += part1[i];
                        values[0] = n1;
                    }

                    float n2 = 0;
                    for (int i = 0; i < part2.length; i++) {
                        n2 += part2[i];
                        values[1] = n1;
                    }

                    float n3 = 0;
                    for (int i = 0; i < part3.length; i++) {
                        n3 += part3[i];
                        values[2] = n3;
                    }
                    float n4 = 0;
                    for (int i = 0; i < part4.length; i++) {
                        n4 += part4[i];
                        values[3] = n4;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else {
                timeInterval = "24";
                time = "hour";
                try {
                    String respon = run(url + timeInterval);
                    Gson gson = new Gson();
                    MonitorModel monitorModel = gson.fromJson(respon, MonitorModel.class);
                    values = new float[monitorModel.getCValue().size()];
                    for (int i = 0; i < monitorModel.getCValue().size(); i++) {
                        String[] kwh = monitorModel.getCValue().get(i).split(",");
                        //Log.d(TAG, "onParse: " + kwh[1]);
                        values[i] = Float.parseFloat(kwh[1].replace("%", ""));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            return values;
        }

        @Override
        protected void onPostExecute(float[] respon) {
            Log.d(TAG, "onPostExecute: " + respon[0]);

            // Plot type (bar, candle, line)
            if (rbPlotTypeBar.isChecked()) {
                barChart.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.INVISIBLE);
                ArrayList<BarEntry> entries = new ArrayList<>();
                for (int i = 0; i < respon.length; i++) {
                    //String[] kwh = monitorModel.getCValue().get(i).split(",");
                    entries.add(new BarEntry(respon[i], i));
                }

                BarDataSet dataset = new BarDataSet(entries, "# kwh");
                ArrayList<String> labels = new ArrayList<String>();
                for (int i = 0; i < respon.length; i++) {
                    int j = i + 1;
                    labels.add("" + j);
                }

                dataset.setColors(ColorTemplate.VORDIPLOM_COLORS);

                BarData data = new BarData(labels, dataset);
                barChart.setData(data);
                barChart.setDescription("# penggunaan listrik per " + time);
                barChart.animateY(3000);
                barChart.setClickable(true);
                barChart.setDragEnabled(true);
                barChart.setTouchEnabled(true);
            } else if (rbPlotTypeLine.isChecked()) {
                lineChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.INVISIBLE);
                ArrayList<Entry> entries = new ArrayList<>();
                for (int i = 0; i < respon.length; i++) {
//                    String[] kwh = monitorModel.getCValue().get(i).split(",");
                    entries.add(new Entry(respon[i], i));
                }

                LineDataSet dataset = new LineDataSet(entries, "# kwh");
                ArrayList<String> labels = new ArrayList<String>();
                for (int i = 0; i < respon.length; i++) {
                    int j = i + 1;
                    labels.add("" + j);
                }

                dataset.setColor(Color.RED);
                dataset.setLineWidth(2f);

                LineData data = new LineData(labels, dataset);
                lineChart.setData(data);
                lineChart.setDescription("# penggunaan listrik per " + time);
                lineChart.animateY(3000);
                lineChart.setClickable(true);
                lineChart.setDragEnabled(true);
                lineChart.setTouchEnabled(true);
            } else {
                lineChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.INVISIBLE);
                ArrayList<Entry> entries = new ArrayList<>();
                for (int i = 0; i < respon.length; i++) {
//                    String[] kwh = monitorModel.getCValue().get(i).split(",");
                    entries.add(new Entry(respon[i], i));
                }

                LineDataSet dataset = new LineDataSet(entries, "# kwh");
                ArrayList<String> labels = new ArrayList<String>();
                for (int i = 0; i < respon.length; i++) {
                    int j = i + 1;
                    labels.add("" + j);
                }

                dataset.setColor(Color.RED);
                dataset.setLineWidth(2f);

                LineData data = new LineData(labels, dataset);
                lineChart.setData(data);
                lineChart.setDescription("# penggunaan listrik per " + time);
                lineChart.animateY(3000);
                lineChart.setClickable(true);
                lineChart.setDragEnabled(true);
                lineChart.setTouchEnabled(true);
            }

            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
