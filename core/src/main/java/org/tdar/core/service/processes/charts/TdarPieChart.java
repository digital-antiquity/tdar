package org.tdar.core.service.processes.charts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;

public class TdarPieChart extends AbstractChart {

    private Map<String, Number> data;

    public TdarPieChart(String title, int width, int height, String filename, Map<String,Number> data) {
        setTitle(title);
        setWidth(width);
        setHeight(height);
        setFilename(filename);
        this.data = data;
    }
    
    @Override
    public File createChart() throws IOException {
        PieChart chart = new PieChartBuilder().width(getWidth()).height(getHeight()).title(getTitle()).build();
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setLegendBorderColor(Color.WHITE);

        data.entrySet().forEach(row -> {
            chart.addSeries(row.getKey(), row.getValue().doubleValue());
        });

        return renderAndExport(chart);
    }

}