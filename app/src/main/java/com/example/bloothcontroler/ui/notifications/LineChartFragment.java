package com.example.bloothcontroler.ui.notifications;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.base.LazyFragment;
import com.example.bloothcontroler.service.DataMessage;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Arrays;
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
    private int index;
    private LineChart lineChart;
    private Button button;
    private TextView tvPmax;
    private TextView tvDiff;
    public static LineChartFragment getInstance(String title,int index) {
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("index",index);
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
            index = getArguments().getInt("index", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this).get(LineChartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_linechart, container, false);
        lineChart = root.findViewById(R.id.line_chart);
        button = root.findViewById(R.id.button);
        tvPmax = root.findViewById(R.id.tvPmax);
        tvDiff = root.findViewById(R.id.tvDiff);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                addEntry(0, (float) (Math.random() * 10));
//                addEntry(1, (float) ((Math.random()) * 10 + 10));
//                addEntry(2, (float)((Math.random()) * 10 + 10));
//                addEntry(3, (float) ((Math.random()) * 10 + 10));
//                notifyData();
                addTestData();
            }
        });
        viewModel.getText().observe(this, new Observer<DataMessage>() {
            @Override
            public void onChanged(@Nullable DataMessage s) {
                handleMessage(s);
            }
        });
        initChart();
        addLineDataSet();
        return root;
    }

    private void handleMessage(DataMessage message) {
        if (null != message) {
            if (message.what == index) {
                    int[] data = message.getData();
                    tvPmax.setText(getString(R.string.pmax_str,viewModel.getValue(data[0])));
                    tvDiff.setText(getString(R.string.difference,viewModel.getValue(data[1])+ "%") );
                    setPVData(data);
            }
        }
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
//        leftAxis.setAxisMaximum(5f);
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
//        xAxis.setAxisMaximum(110f);
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
//        int color;
//        switch (index){
//            case 0:
//                color = Color.RED;
//                break;
//            case 1:
//                color = Color.YELLOW;
//                break;
//            case 2:
//                color = Color.BLUE;
//                break;
//            case 3:
//                color = Color.GREEN;
//                break;
//            default:
//                color = Color.RED;
//        }
        mLineData.addDataSet(createLineDataSet(title, Color.RED));
//        mLineData.addDataSet(createLineDataSet("two", Color.YELLOW));
//        mLineData.addDataSet(createLineDataSet("three", Color.BLUE));
//        mLineData.addDataSet(createLineDataSet("four", Color.GREEN));
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
//        LineData data = lineChart.getData();
//        if (data != null) {
//            ILineDataSet set = data.getDataSetByIndex(index);
//            if (set != null) {
//                data.addEntry(new Entry(set.getEntryCount(), y), index);
//            }
//        }
    }

    private void addTestData(){
        LineData data = lineChart.getData();
        List<Entry> entries = new ArrayList<>();
        int datasize = 128;
        float[] xs = new float[datasize];
        List<Float> ys = new ArrayList<>();
        if (data != null) {
            for (int i = 0;i < datasize;i++){
                xs[i] = ((float) (Math.random() * 110));
                ys.add((float) (Math.random() * 5));
            }
            Arrays.sort(xs);
            for (int i = 0;i < datasize ;i++){
                entries.add(new Entry(xs[i],ys.get(i)));
            }
            LineDataSet lineDataSet = new LineDataSet(entries, title);
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSet.setFillColor(Color.RED);
            lineDataSet.setColor(Color.RED);
            lineDataSet.setFillAlpha(50);
            lineDataSet.setLineWidth(1.5f);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawValues(true);
            data.getDataSets().set(0,lineDataSet);
            notifyData();
        }
    }

    private void setPVData(int[] pvData){
        LineData data = lineChart.getData();
        if (data != null) {
            if (pvData.length >= 4){
                List<Entry> entries = new ArrayList<>();
//                int datasize = 128;
                int datasize = (pvData.length - 2) / 2;
                int[] xs = new int[datasize];
                int[] ys = new int[datasize];
                System.arraycopy(pvData, 2, xs, 0, datasize);
                System.arraycopy(pvData, 2 + datasize, ys, 0, datasize);

                for (int i = 0;i < datasize ;i++){
                    for (int j = i + 1 ; j < datasize ; j ++){
                        if (xs[j] < xs[i]){
                            int c = xs[j];
                            xs[j] = xs[i];
                            xs[i] = c;

                            int cy = ys[j];
                            ys[j] = ys[i];
                            ys[i] = cy;
                        }
                    }
                }

                for (int i = 0;i < datasize ;i++){
                    entries.add(new Entry((float) viewModel.getValue(xs[i]),(float)viewModel.getValue(ys[i])));
                }

                LineDataSet lineDataSet = new LineDataSet(entries, title);
                lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                lineDataSet.setFillColor(Color.RED);
                lineDataSet.setColor(Color.RED);
                lineDataSet.setFillAlpha(50);
                lineDataSet.setLineWidth(1.5f);
                lineDataSet.setDrawCircles(false);
                lineDataSet.setDrawValues(true);
                data.getDataSets().set(0,lineDataSet);
                notifyData();
            }

        }
    }

    private void notifyData() {
        LineData data = lineChart.getData();
        data.notifyDataChanged();
        // let the chart know it's data has changed
        lineChart.notifyDataSetChanged();
        //x轴显示最大个数
        lineChart.setVisibleXRangeMaximum(128);
        // move to the latest entry
        lineChart.moveViewToAnimated(data.getEntryCount(), 0, YAxis.AxisDependency.RIGHT, 0);
    }
}
