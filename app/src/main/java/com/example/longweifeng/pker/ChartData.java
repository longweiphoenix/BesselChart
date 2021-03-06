
package com.example.longweifeng.pker;

import java.util.ArrayList;
import java.util.List;

/**
 * 曲线图的数据以及相关配置信息
 * 
 * @author tomkeyzhang（qitongzhang@anjuke.com）
 * @date :2014年4月17日
 */
public class ChartData {
    private Marker marker;
    private List<Series> seriesList;
    private List<Label> xLabels;
    private List<Label> yLabels;
    private List<Title> titles;
    private int maxValueY;
    private int minValueY;
    private int maxPointsCount;
    private LabelTransform labelTransform;
    private  static  final int  step = 5;
    /** 纵坐标显示文本的数量 */
    private int yLabelCount;

    /** 使用哪一个series的横坐标来显示横坐标文本 */
    private int xLabelUsageSeries;

    ChartData() {
        xLabels = new ArrayList<Label>();
        yLabels = new ArrayList<Label>();
        titles=new ArrayList<Title>();
        seriesList = new ArrayList<Series>();
        labelTransform = new LabelTransform() {
            @Override
            public String verticalTransform(int valueY) {
                return String.valueOf(valueY);
            }

            @Override
            public String horizontalTransform(int valueX) {
                return String.valueOf(valueX);
            }

            @Override
            public boolean labelDrawing(int valueX) {
                return true;
            }
        };
        yLabelCount = 7;// 默认纵轴显示6个文本
        xLabelUsageSeries = 0;// 默认横轴使用第一个序列来显示文本
    }

    /** 设置数据序列 */
    public void setSeriesList(List<Series> seriesList) {
        this.seriesList.clear();
        if (seriesList != null && seriesList.size() > 0) {
            this.seriesList.addAll(seriesList);
            if (this.seriesList.size() <= xLabelUsageSeries)
                throw new IllegalArgumentException("xLabelUsageSeries should greater than seriesList.size()");
            resetXLabels();
            resetYLabels();
            titles.clear();

            for(Series series:seriesList){
                titles.add(series.getTitle());
                if (series.getPoints().size() > maxPointsCount)
                    maxPointsCount = series.getPoints().size();
            }
        }
    }

    /** 重新生成X坐标轴文本 */
    private void resetXLabels() {
        xLabels.clear();
        for (Point point : seriesList.get(xLabelUsageSeries).getPoints()) {
            if (labelTransform.labelDrawing(point.valueX))
                xLabels.add(new Label(point.valueX, labelTransform
                        .horizontalTransform(point.valueX)));
        }
    }

    /** 重新生成Y坐标轴文本 */
    private void resetYLabels() {
        maxValueY = 0;
        minValueY = Integer.MAX_VALUE;
        for (Series series : seriesList) {
            for (Point point : series.getPoints()) {
                if (point.valueY > maxValueY)
                    maxValueY = point.valueY;
                if (point.valueY>0&&point.valueY < minValueY)
                    minValueY = point.valueY;
            }
        }
        maxValueY = 30;
        minValueY = 10;
        int mStep = (maxValueY - minValueY) / (yLabelCount - 1);
        yLabels.clear();
        minValueY = minValueY - mStep;
        maxValueY = maxValueY + mStep;
        //mStep 纵轴的间距
        mStep = (maxValueY - minValueY) / (yLabelCount - 1);
        //纵轴的比列
        mStep = (mStep/5+1)*5;
        minValueY=minValueY/5*5;
        maxValueY= (maxValueY/5+1)*5;
        int value =0;
        for (int i = 0; i < yLabelCount; i++) {
            value=minValueY+mStep*i;
            yLabels.add(0,
                    new Label(value, labelTransform.verticalTransform(value)));
        }
        maxValueY=value;
        Log.d("step="+mStep);
        Log.d("step minValueY="+minValueY);
        Log.d("step maxValueY="+maxValueY);
    }
    public void setLabelTransform(LabelTransform labelTransform) {
        this.labelTransform = labelTransform;
    }
    public List<Series> getSeriesList() {
        return seriesList;
    }

    public LabelTransform getLabelTransform() {
        return labelTransform;
    }

    public List<Label> getXLabels() {
        return xLabels;
    }

    public List<Label> getYLabels() {
        return yLabels;
    }
    public List<Title> getTitles() {
        return titles;
    }
    public int getMaxValueY() {
        return maxValueY;
    }

    public int getMinValueY() {
        return minValueY;
    }

    public int getyLabelCount() {
        return yLabelCount;
    }

    public void setyLabelCount(int yLabelCount) {
        this.yLabelCount = yLabelCount;
    }

    public int getxLabelUsageSeries() {
        return xLabelUsageSeries;
    }

    public void setxLabelUsageSeries(int xLabelUsageSeries) {
        this.xLabelUsageSeries = xLabelUsageSeries;
    }
    public void setMarker(Marker marker) {
        titles.add(marker);
        this.marker = marker;
    }
    public Marker getMarker() {
        return marker;
    }

    public int getMaxPointsCount() {
        return maxPointsCount;
    }
    public interface LabelTransform {
        /** 纵坐标显示的文本 */
        String verticalTransform(int valueY);

        /** 横坐标显示的文本 */
        String horizontalTransform(int valueX);

        /** 是否显示指定位置的横坐标文本 */
        boolean labelDrawing(int valueX);

    }
    class Label {
        /**文本对应的坐标X*/
        public float x;
        /**文本对应的坐标Y*/
        public float y;
        /** 文本对应的绘制坐标Y */
        public float drawingY;
        /**文本对应的实际数值*/
        public int value;
        /**文本*/
        public String text;
        public Label(int value, String text) {
            this.value = value;
            this.text = text;
        }
    }
}
