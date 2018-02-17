package org.tdar.core.service.processes.charts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;

@SuppressWarnings("restriction")
public class TdarBarChart extends AbstractChart {

    private Map<String, Number> data;
    private String xAxisLabel;
    private String yAxisLabel;

    public TdarBarChart(String title, String xAxis, String yAxis, Map<String, Number> data, int width, int height,
            String filename) {
        super();
        this.xAxisLabel = xAxis;
        this.yAxisLabel = yAxis;
        this.data = data;
        setTitle(title);
        setWidth(width);
        setHeight(height);
        setFilename(filename);
        setShowLegend(false);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public File createChart() throws IOException {
        CategoryChart chart = new CategoryChartBuilder().width(getWidth()).height(getHeight()).title(getTitle())
                .xAxisTitle(xAxisLabel).yAxisTitle(yAxisLabel).build();

        chart.getStyler().setPlotGridHorizontalLinesVisible(false);
        chart.getStyler().setPlotGridVerticalLinesVisible(false);
        chart.getStyler().setAxisTicksLineVisible(false);
        chart.getStyler().setYAxisTicksVisible(false);
        chart.getStyler().setYAxisTitleVisible(true);
        chart.getStyler().setAxisTicksMarksVisible(false);
        chart.getStyler().setAxisTickMarkLength(0);
        
//        chart.getStyler().setAxisTicksVisible(false);
        addRowsToChart(data, chart, getTitle());

        return renderAndExport(chart);
    }

    private void addRowsToChart(Map<String, Number> data, CategoryChart chart, String title) {
        List<String> series = new ArrayList<String>();
        List<Number> values = new ArrayList<Number>();

        boolean hasValue = false;
        for (String key : data.keySet()) {
            Number value = data.get(key);
            if (value.intValue() > 0 || hasValue) {
                series.add(key);
                values.add(value);
                hasValue = true;
            }
        }
        chart.addSeries(title, series, values);
    }
}
