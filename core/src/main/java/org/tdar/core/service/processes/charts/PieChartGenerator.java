package org.tdar.core.service.processes.charts;

import java.io.File;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class PieChartGenerator extends AbstractGraphGenerator {

    private Map<String, Number> data;

    public PieChartGenerator(String title, int width, int height, String filename, Map<String,Number> data) {
        setTitle(title);
        setWidth(width);
        setHeight(height);
        setFilename(filename);
        this.data = data;
    }
    
    public File start(Stage stage) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        data.entrySet().forEach(row -> {
            pieChartData.add(new PieChart.Data(row.getKey(), row.getValue().doubleValue()));
        });
        final PieChart chart = new PieChart(pieChartData);

        return renderAndExport(stage, chart);
    }

}