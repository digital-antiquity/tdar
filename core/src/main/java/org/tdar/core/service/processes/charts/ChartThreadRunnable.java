package org.tdar.core.service.processes.charts;

import java.io.File;

import javafx.stage.Stage;

/**
 * Creates the actual stage and graph
 * @author abrin
 *
 */
public class ChartThreadRunnable implements Runnable {
    private AbstractChart chart;
    private File outputFile;

    @Override
    public void run() {
        Stage stage = new Stage();
        stage.show();
        setOutputFile(chart.createChart(stage));
    }

    public AbstractChart getChart() {
        return chart;
    }

    public void setChart(AbstractChart chart) {
        this.chart = chart;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}