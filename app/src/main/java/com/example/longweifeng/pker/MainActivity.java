package com.example.longweifeng.pker;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements BesselChart.ChartListener {
    BesselChart besselChart;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        besselChart = (BesselChart) findViewById(R.id.besselChart);
        besselChart.setSmoothness(0.4f);
        besselChart.setChartListener(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getSeriesList(true);
                besselChart.setSmoothness(0.33f);
            }
        }, 1000);
    }

    /**
     *
     * @param title 标题
     * @param color 颜色
     * @param willDrawing 是否画线
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    private Series getLinesPoints(String title, int color, boolean willDrawing,int[] startTime, int[] endTime,int temp) {
        List<Point> points = new ArrayList<>();
        Random random = new Random();
        for(int i = 0;i< 6;i++) {
            int tempe = random.nextInt(30)+5;
            points.add(new Point(startTime[i]*2,tempe,true));
            points.add(new Point(endTime[i]*2,tempe,true));
        }
        return new Series(title,color,points);
    }
    private Series getBesselPoints() {
        List<Point> points = new ArrayList<>();
        return null;
    }
    private Series getRandomSeries(String title, int color, boolean willDrawing) {
        List<Point> points = new ArrayList<Point>();
        Random random = new Random();
        if (willDrawing) {
            for (int i = 0; i < 96; i++) {
                points.add(new Point(i + 1, random.nextInt(30)+5, true));
            }
        }
        for (Point point : points) {
            Log.d("getRandomSeries valueY=" + point.valueY);
        }
        return new Series(title, color, points);
    }

    private void getSeriesList(boolean willDrawing) {
        List<Series> series = new ArrayList<>();
        //series.add(getLinesPoints("设定温度",Color.BLUE,willDrawing,new int[]{4,8,12,16,20,26},new int[]{7,11,15,19,25,35},0));
        series.add(getRandomSeries("室温", Color.GREEN, willDrawing));

        if (willDrawing) {
            besselChart.getData().setLabelTransform(new ChartData.LabelTransform() {

                @Override
                public String verticalTransform(int valueY) {
                    return String.valueOf(valueY);
                }

                @Override
                public String horizontalTransform(int valueX) {
                    if (valueX % 4 == 0) {
                        int hour = valueX / 4;
                        String h = String.valueOf(hour);
                        if (hour < 10) {
                            return "0" + h + ":00";
                        }
                        return h + ":00";
                    }
                    return "";
                }

                @Override
                public boolean labelDrawing(int valueX) {
                    return true;
                }
            });
        }
        besselChart.getData().setSeriesList(series);
        besselChart.refresh(true);
    }

    @Override
    public void onMove() {
        Log.d("onMove");
    }

}
