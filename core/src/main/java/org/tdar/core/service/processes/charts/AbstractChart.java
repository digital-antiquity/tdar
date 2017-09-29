package org.tdar.core.service.processes.charts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.internal.chartpart.Chart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;

@SuppressWarnings("restriction")
public abstract class AbstractChart  {
    
    private String outputDir = TdarConfiguration.getInstance().getTempDirectory().getAbsolutePath();
    Logger logger = LoggerFactory.getLogger(getClass());

    private int width;
    private int height;
    private String title;
    private String filename;

    // Customize Chart
    Color[] sliceColors = new Color[] { new Color(235,215,144),
    new Color(214,184,75),
    new Color(195,170,114),
    new Color(160,157,91),
    new Color(144,157,91),
    new Color(220,118,18),
    new Color(189,50,0),
    new Color(102,0,0)};
    
    File renderAndExport(Chart bc) throws IOException {
        render(bc);
        File file  = exportChart(bc, Paths.get(getOutputDir() + getFilename()));
        return file;
    }

    public abstract File createChart() throws IOException;

    public void render(Chart chart) {
        chart.setTitle(title);
        chart.getStyler().setSeriesColors(sliceColors);

        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setLegendBorderColor(Color.WHITE);
    
    }

    public File exportChart(Chart chart, Path path_) throws IOException {
        Path path = path_.normalize();
        String filename = path.getFileName().toString();
        logger.debug("exporting: {}\t type:{}", path.toAbsolutePath());
        //FIXME: NOT SURE THIS PATH IS RIGHT
        BitmapEncoder.saveBitmap(chart, filename, BitmapFormat.PNG);
        return new File(path.toFile(), filename + ".png");
//      VectorGraphicsEncoder.saveVectorGraphic(chart, "./Sample_Chart", VectorGraphicsFormat.EPS);
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
}