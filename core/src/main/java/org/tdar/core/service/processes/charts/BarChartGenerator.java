package org.tdar.core.service.processes.charts;

import java.io.File;
import java.util.Map;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;


@SuppressWarnings("restriction")
public class BarChartGenerator extends AbstractGraphGenerator  {

    private Map<String, Map<String, Number>> data;
    private String xAxisLabel;
    private String yAxisLabel;


    public BarChartGenerator(String title, String xAxis, String yAxis, Map<String,Map<String,Number>> data, int width, int height, String filename) {
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
    public File start(Stage stage) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
//        bc.setTitle("Country Summary");
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        data.keySet().forEach(row ->{
            XYChart.Series series = new XYChart.Series();
            series.setName(row);
            data.get(row).entrySet().forEach(vals -> {
                series.getData().add(new XYChart.Data<String, Number>(vals.getKey(), vals.getValue()));
            });
            bc.getData().add(series);
        });

        bc.setAnimated(false);

        return renderAndExport(stage, bc);
    }


}
