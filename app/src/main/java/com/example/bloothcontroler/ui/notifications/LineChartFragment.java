package com.example.bloothcontroler.ui.notifications;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.base.LazyFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : WangHao
 * @date : 2020.4.15
 * @desc :
 * @version:
 */
public class LineChartFragment extends LazyFragment {

    private LineChartViewModel viewModel;

    private String title;
    private LineChart lineChart;
    private Button button;

    public static LineChartFragment getInstance(String title) {
        Bundle args = new Bundle();
        args.putString("title", title);
        LineChartFragment fragment = new LineChartFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void visibleToUser() {

    }

    @Override
    public void lazyLoad() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString("title", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this).get(LineChartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_linechart, container, false);
        lineChart = root.findViewById(R.id.line_chart);
        button = root.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEntry(0, (float) (Math.random() * 10));
                addEntry(1, (float) ((Math.random()) * 10 + 10));
                addEntry(2, (float)((Math.random()) * 10 + 10));
                addEntry(3, (float) ((Math.random()) * 10 + 10));
                notifyData();
            }
        });
        initChart();
        addLineDataSet();
        return root;
    }


    private void initChart() {
        //设置描述文本不显示
        lineChart.getDescription().setEnabled(false);
        // 设置图表的背景颜色
        lineChart.setBackgroundColor(Color.rgb(255, 255, 255));
        //設置图表无数据文本
        lineChart.setNoDataText("暂无数据");
        lineChart.setNoDataTextColor(R.color.base_black);
        //设置可以触摸
        lineChart.setTouchEnabled(true);
        //设置可拖拽
        lineChart.setDragEnabled(true);
        // 不可以缩放
        lineChart.setScaleEnabled(true);
        //设置图表网格背景
        lineChart.setDrawGridBackground(false);
        //设置多点触控
        lineChart.setPinchZoom(true);
        //x轴显示最大个数
//        lineChart.setVisibleXRangeMaximum(10);
        //显示边界
        lineChart.setDrawBorders(false);

        // 图表左边的y坐标轴线
        YAxis leftAxis = lineChart.getAxisLeft();
        //保证Y轴从0开始，不然会上移一点
        leftAxis.setAxisMinimum(0f);
        //设置图表左边的y轴禁用
        leftAxis.setEnabled(true);

        // 图表右边的y坐标轴线
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setAxisMinimum(0f);
        //设置图表右边的y轴禁用
        rightAxis.setEnabled(false);

        //设置x轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.parseColor("#333333"));
        xAxis.setTextSize(11f);
        xAxis.setAxisMinimum(0f);
        //是否绘制轴线
        xAxis.setDrawAxisLine(false);
        //设置x轴上每个点对应的线
        xAxis.setDrawGridLines(false);
        //绘制标签  指x轴上的对应数值
        xAxis.setDrawLabels(true);
        //设置x轴的显示位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //禁止放大后x轴标签重绘
        xAxis.setGranularity(1f);
        //自定义x轴值
//        xAxis.setValueFormatter(LineXiavf0);
        //图表将避免第一个和最后一个标签条目被减掉在图表或屏幕的边缘
        xAxis.setAvoidFirstLastClipping(true);

        // 图表的注解(只有当数据集存在时候才生效)
        Legend l = lineChart.getLegend();
        // 线性，也可是圆
        l.setForm(Legend.LegendForm.LINE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
    }

    /**
     * 为LineChart增加LineDataSet
     */
    private void addLineDataSet() {
        LineData mLineData = new LineData();
        mLineData.addDataSet(createLineDataSet("one", Color.RED));
        mLineData.addDataSet(createLineDataSet("two", Color.YELLOW));
        mLineData.addDataSet(createLineDataSet("three", Color.BLUE));
        mLineData.addDataSet(createLineDataSet("four", Color.GREEN));
        lineChart.setData(mLineData);
    }

    /**
     * 创建一条Line
     *
     * @param name      名称
     * @param lineColor 线条颜色
     * @return
     */
    private ILineDataSet createLineDataSet(String name, int lineColor) {
        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setFillColor(lineColor);
        set.setColor(lineColor);
        set.setFillAlpha(50);
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawValues(true);
        return set;
    }
    /**
     * 添加点
     * @param index LineData 集合下标
     * @param y y坐标
     */
    private void addEntry(int index, float y) {
        LineData data = lineChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(index);
            if (set != null) {
                data.addEntry(new Entry(set.getEntryCount(), y), index);
            }
        }
    }

    private void notifyData() {
        LineData data = lineChart.getData();
        data.notifyDataChanged();
        // let the chart know it's data has changed
        lineChart.notifyDataSetChanged();
        //x轴显示最大个数
        lineChart.setVisibleXRangeMaximum(10);
        // move to the latest entry
        lineChart.moveViewToAnimated(data.getEntryCount(), 0, YAxis.AxisDependency.RIGHT, 0);
    }
}
