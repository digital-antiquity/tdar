package org.tdar.core.service.processes.charts;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.internal.chartpart.Chart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.TdarConfiguration;

public abstract class AbstractChart {

    private String outputDir = TdarConfiguration.getInstance().getTempDirectory().getAbsolutePath();
    Logger logger = LoggerFactory.getLogger(getClass());

    private int width;
    private int height;
    private String title;
    private String filename;
    private boolean showLegend;

    // Customize Chart
    Color[] sliceColors = TdarConfiguration.getInstance().getBarColors().stream().map(key -> Color.decode(key)).toArray(Color[]::new);

    File renderAndExport(Chart<?, ?> bc) throws IOException {
        render(bc);
        File file = exportChart(bc, Paths.get(getOutputDir() + getFilename()));
        return file;
    }

    public abstract File createChart() throws IOException;

    public void render(Chart<?, ?> chart) {
        chart.setTitle(title);
        chart.getStyler().setSeriesColors(sliceColors);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setLegendBorderColor(Color.WHITE);
        chart.getStyler().setHasAnnotations(true);
        chart.getStyler().setLegendVisible(showLegend);
    }

    public File exportChart(Chart<?, ?> chart, Path path_) throws IOException {
        Path path = path_.normalize();
        String filename = path.toAbsolutePath().toString() + ".png";
        logger.debug("exporting: {}", path.toAbsolutePath());
        File outputFile = new File(filename);
        BitmapEncoder.saveBitmap(chart, new FileOutputStream(outputFile), BitmapFormat.PNG);

        return outputFile;
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

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

}