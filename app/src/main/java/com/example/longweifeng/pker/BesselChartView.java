package com.example.longweifeng.pker;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.support.v4.view.ViewCompat;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.Scroller;

import com.example.longweifeng.pker.BesselCalculator;
import com.example.longweifeng.pker.BesselChart.ChartListener;
import com.example.longweifeng.pker.ChartData.Label;

/**
 * 贝塞尔曲线图
 *
 * @author longweifeng
 * @date :2017年7月4日
 */
class BesselChartView extends View {
    /**
     * 通用画笔
     */
    private Paint paint;
    /**
     * 曲线的路径，用于绘制曲线
     */
    private Path curvePath;
    /**
     * 曲线图绘制的计算信息
     */
    private BesselCalculator calculator;
    /**
     * 曲线图的样式
     */
    private ChartStyle style;
    /**
     * 曲线图的数据
     */
    private ChartData data;
    /**
     * 手势解析
     */
    private GestureDetector detector;
    /**
     * 是否绘制全部贝塞尔结点
     */
    private boolean drawBesselPoint;
    /**
     * 滚动计算器
     */
    private Scroller scroller;
    /**
     * 曲线图事件监听器
     */
    private ChartListener chartListener;

    public BesselChartView(Context context, ChartData data, ChartStyle style, BesselCalculator calculator) {
        super(context);
        this.calculator = calculator;
        this.data = data;
        this.style = style;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.curvePath = new Path();
        this.drawBesselPoint = false;
        this.scroller = new Scroller(context);

        this.detector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
            float lastScrollX = 0f;

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (Math.abs(distanceX / distanceY) > 1) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    BesselChartView.this.calculator.move(distanceX);
                    ViewCompat.postInvalidateOnAnimation(BesselChartView.this);
                    if (e1.getX() != lastScrollX) {
                        lastScrollX = e1.getX();
                        if (chartListener != null)
                            chartListener.onMove();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                scroller.fling((int) BesselChartView.this.calculator.getTranslateX(), 0, (int) velocityX, 0, -3 * getWidth(), 0, 0, 0);
                ViewCompat.postInvalidateOnAnimation(BesselChartView.this);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                scroller.forceFinished(true);
                ViewCompat.postInvalidateOnAnimation(BesselChartView.this);
                return true;
            }
        });
    }

    public void animateScrollToEnd(int duration) {
        scroller.startScroll(0, 0, -calculator.xAxisWidth / 2, 0, duration);
    }

    public void setChartListener(ChartListener chartListener) {
        this.chartListener = chartListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            calculator.moveTo(scroller.getCurrX());
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (data.getSeriesList().size() == 0)
            return;
        calculator.ensureTranslation();
        canvas.translate(calculator.getTranslateX(), 0);
        drawGrid(canvas);
        drawLines(canvas);
        drawCurveAndPoints(canvas);
        drawMarker(canvas);
        drawHorLabels(canvas);

    }

    /**
     * 绘制曲线图中的房源
     */
    private void drawMarker(Canvas canvas) {
        Marker marker = data.getMarker();
        if (marker != null) {
            paint.setAlpha(255);
            canvas.drawBitmap(marker.getBitmap(), null, marker.updateRect(marker.getPoint().x, marker.getPoint().y, marker.getWidth(), marker.getHeight()),
                    paint);
        }
    }

    /**
     * 绘制网格线
     */
    private void drawGrid(Canvas canvas) {
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(style.getGridColor());
        paint.setAlpha(80);
        List<Label> yLabels = data.getYLabels();
        // 先绘制两条水平线
        float coordinateY = yLabels.get(0).y;
        coordinateY = yLabels.get(yLabels.size() - 1).y;
        //canvas.drawLine(0, coordinateY, calculator.xAxisWidth, coordinateY, paint);
        for (int i = 1; i < 7; i++) {
            canvas.drawLine(0, 0 - calculator.yAxisHeight / 7 * i + coordinateY, calculator.xAxisWidth, 0 - calculator.yAxisHeight / 7 * i + coordinateY, paint);
        }
        canvas.drawLine(0, coordinateY, calculator.xAxisWidth, coordinateY, paint);
        // 再绘制竖直线
        int index = 0;
        for (Point point : calculator.gridPoints) {
            index++;
            if (index % 4 == 0) {
                if (point != null && point.willDrawing && point.valueY >= 0)
                    canvas.drawLine(point.x, 40, point.x, calculator.yAxisHeight - 20, paint);
            }
        }
       /* for (int i = 0; i < 24; i++) {
            //竖线起点
            float x = calculator.xAxisWidth + calculator.xAxisWidth;
            canvas.drawLine(x, 40, x, calculator.yAxisHeight - 20, paint);
        }*/

    }

    /**
     * 绘制设定直线线
     */
    private void drawLines(Canvas canvas) {
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLUE);
        Series series = data.getSeriesList().get(0);
        List<Point> points = series.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (i % 2 != 0 && i > 0) {
                canvas.drawLine(points.get(i).x, points.get(i).y, points.get(i - 1).x, points.get(i).y, paint);
            }
        }


    }

    /**
     * 绘制曲线和结点
     */
    private void drawCurveAndPoints(Canvas canvas) {
        paint.setStrokeWidth(5);
        //for (Series series : data.getSeriesList()) {
        //第二个item绘制图表和
        Series series = data.getSeriesList().get(1);
        paint.setColor(series.getColor());
        curvePath.reset();
        List<Point> list = series.getBesselPoints();
        for (int i = 0; i < list.size(); i = i + 3) {
            if (i == 0) {
                curvePath.moveTo(list.get(i).x, list.get(i).y);
            } else {
                curvePath.cubicTo(list.get(i - 2).x, list.get(i - 2).y, list.get(i - 1).x, list.get(i - 1).y, list.get(i).x, list.get(i).y);
            }
        }
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(curvePath, paint);// 绘制光滑曲线
        paint.setStyle(Paint.Style.FILL);
        for (Point point : series.getPoints()) {
            if (!point.willDrawing) {
                canvas.drawCircle(point.x, point.y, 5, paint);
                paint.setAlpha(80);
                canvas.drawCircle(point.x, point.y, 10, paint);
                paint.setAlpha(255);
            }
        }// 绘制结点
        if (drawBesselPoint) {
            for (Point point : list) {
                if (!series.getPoints().contains(point)) {
                    paint.setColor(Color.BLUE);
                    paint.setAlpha(255);
                    canvas.drawCircle(point.x, point.y, 5, paint);
                }
            }// 绘制贝塞尔控制结点
        }
        // }
    }

    /**
     * 绘制横轴
     */
    private void drawHorLabels(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);
        paint.setColor(style.getHorizontalLabelTextColor());
        paint.setTextSize(style.getHorizontalLabelTextSize());
        paint.setTextAlign(Align.CENTER);
        float endCoordinateX = calculator.xAxisWidth;
        float coordinateY = getHeight() - calculator.xAxisHeight;
        // canvas.drawLine(0, coordinateY, endCoordinateX, coordinateY, paint);
        for (Label label : data.getXLabels()) {
            // 绘制橫坐标文本
            canvas.drawText(label.text, label.x, label.y,
                    paint);
        }
    }

    public void updateSize() {
        LayoutParams lp = getLayoutParams();
        lp.height = calculator.height;
        lp.width = calculator.xAxisWidth;
        setLayoutParams(lp);
    }

    public void setDrawBesselPoint(boolean drawBesselPoint) {
        this.drawBesselPoint = drawBesselPoint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //  setMeasuredDimension(2*widthMeasureSpec,heightMeasureSpec);
    }
}
