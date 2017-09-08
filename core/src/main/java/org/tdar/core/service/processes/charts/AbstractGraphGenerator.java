package org.tdar.core.service.processes.charts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.Chart;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public abstract class AbstractGraphGenerator  {
    Logger logger = LoggerFactory.getLogger(getClass());

    private int width;
    private int height;
    private String title;
    private String filename;

    File renderAndExport(Stage stage, Chart bc) {
        render(stage, bc);
        File file  = exportChart(bc, Paths.get("./target/" + getFilename() + ".png"));
        stage.close();
        return file;
    }

    public void render(Stage stage, Chart chart) {
        Scene scene = new Scene(chart, width, height);
        stage.setTitle(title);
        chart.setTitle(title);
        scene.getStylesheets().add("tdar-charts.css");

        stage.setScene(scene);
        stage.show();
    }

    public File exportChart(Chart chart, Path path_) {
        Path path = path_.normalize();
        String filename = path.getFileName().toString();
        String ext = filename.substring(filename.indexOf(".") + 1);
        logger.debug("exporting: {}\t type:{}", path.toAbsolutePath(), ext);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage image = chart.snapshot(params, null);
        try {
            File file = path.toFile();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), ext, file);
            return file;
        } catch (IOException e) {
            logger.error("Could not save file: {}", e.getMessage());
        }
        return null;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}