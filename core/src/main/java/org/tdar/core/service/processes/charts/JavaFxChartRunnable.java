package org.tdar.core.service.processes.charts;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

/**
 * Create the "Runanble/Thread" that JavaFX runs on
 * @author abrin
 *
 */
public class JavaFxChartRunnable implements Runnable {

    Logger logger = LoggerFactory.getLogger(getClass());

    private AbstractChart graph;
    private File result;

    @Override
    public void run() {
        Platform.setImplicitExit(false);
        // prevent segfault... quite possibly brittle
        Platform.isFxApplicationThread();
        new JFXPanel(); // Initializes the JavaFx Platform
        ChartThreadRunnable chartRunner = new ChartThreadRunnable();
        chartRunner.setChart(graph);
        Platform.runLater(chartRunner);
        // would make sense to add a timeout
        while (chartRunner.getOutputFile() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("{}",e,e);
            }
        }
        result = chartRunner.getOutputFile();

    }

    public AbstractChart getGraph() {
        return graph;
    }

    public void setGraph(AbstractChart graph) {
        this.graph = graph;
    }

    public File getResult() {
        return result;
    }

    public void setResult(File result) {
        this.result = result;
    }

}
