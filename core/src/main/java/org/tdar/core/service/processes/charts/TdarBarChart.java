package org.tdar.core.service.processes.charts;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;

@SuppressWarnings("restriction")
public class TdarBarChart extends AbstractChart {

    private Map<String, Map<String, Number>> data;
    private String xAxisLabel;
    private String yAxisLabel;

    public TdarBarChart(String title, String xAxis, String yAxis, Map<String, Map<String, Number>> data, int width, int height, String filename) {
        super();
        this.xAxisLabel = xAxis; //
        this.yAxisLabel = yAxis;
        this.data = data;
        setTitle(title);
        setWidth(width);
        setHeight(height);
        setFilename(filename);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public File createChart() throws IOException {
        CategoryChart chart = new CategoryChartBuilder().width(getWidth()).height(getHeight()).title(getTitle()).xAxisTitle(xAxisLabel).yAxisTitle(yAxisLabel)
                .build();

        chart.getStyler().setPlotGridHorizontalLinesVisible(false);
        chart.getStyler().setPlotGridVerticalLinesVisible(false);

        Map<String, Number> r1 = new HashMap<>();
        r1.put("a", 50);
        r1.put("b", 50);
        r1.put("c", 150);

        data.put("2001", r1);
        Map<String, Number> r2 = new HashMap<>();
        r2.put("a", 150);
        r2.put("b", 150);
        r2.put("c", 350);

        data.put("2002", r2);
        data.keySet().forEach(row -> {
            data.get(row).entrySet().forEach(vals -> {
                // series.getData().add(new XYChart.Data<String, Number>(vals.getKey(), vals.getValue()));
                chart.addSeries("test 1", Arrays.asList(new Integer[] { 0, 1, 2, 3, 4 }), Arrays.asList(new Integer[] { 4, 5, 9, 6, 5 }));

            });
        });

        return renderAndExport(chart);
    }

}
