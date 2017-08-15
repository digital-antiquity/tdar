package org.tdar.core.service.processes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


@SuppressWarnings("restriction")
public class BarChartGenerator extends Application  {
    final static String austria = "Austria";
    final static String brazil = "Brazil";
    final static String france = "France";
    final static String italy = "Italy";
    final static String usa = "USA";

    private Properties props = new Properties();
    private Logger logger  = LoggerFactory.getLogger(getClass());
    private String title;
    private Map<String, Map<String, Number>> data;
    private String xAxisLabel;
    private String yAxisLabel;
    private int width;
    private String filename;
    private int height;


    public BarChartGenerator(String title, String xAxis, String yAxis, Map<String,Map<String,Number>> data, int width, int height, String filename) {
        super();
        this.title = title; // "Bar Chart Sample"
        this.xAxisLabel = xAxis; // 
        this.yAxisLabel = yAxis;
        this.data = data;
        this.width = width;
        this.height = height;
        this.filename = filename;

        try {
            props.load(getClass().getClassLoader().getResourceAsStream("tdar.properties"));
        } catch (IOException | NullPointerException e) {
            logger.error("could not find properties file");
        }
    }

    public List<String> getBarColors() {
        String prop = props.getProperty("chart.colors.bars", "red,orange,yellow,breen,blue,purple,vermilion,amber,chartreuse,teal,violet,magenta");
        List<String> colors = Arrays.asList(prop.split(","));
        return colors;
    }

    @Override
    public void start(Stage stage) {
        // http://docs.oracle.com/javafx/2/charts/css-styles.htm
        stage.setTitle(title);
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
//        bc.setTitle("Country Summary");
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);
        List<String> barColors = getBarColors();

        XYChart.Series series = new XYChart.Series();
        data.keySet().forEach(row ->{
            series.setName(row);
            data.get(row).entrySet().forEach(vals -> {
                series.getData().add(new XYChart.Data<String, Number>(vals.getKey(), vals.getValue()));
            });
            bc.getData().add(series);
        });

        bc.setAnimated(false);

        Scene scene = new Scene(bc, width, height);
        // FIXME:  For the record, I have no idea why javafx is able to find this file w/o an explicit path.  But it does.
        scene.getStylesheets().add("tdar-charts.css");

        stage.setScene(scene);
        stage.show();
        exportChart(bc, Paths.get("./target/"+filename+".png"));
        stage.close();

    }

    public void exportChart(BarChart<?, ?> chart, Path path) {
        path = path.normalize();
        String filename = path.getFileName().toString();
        String ext = filename.substring(filename.indexOf(".") + 1);
        logger.debug("exporting: {}\t type:{}", path.toAbsolutePath(), ext);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage image = chart.snapshot(params, null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), ext, path.toFile());
        } catch (IOException e) {
            logger.error("Could not save file: {}", e.getMessage());
        }
    }


    public void exportXml(Path path) {

    }

    
    public static void main(String[] args){
        Application.launch(BarChartGenerator.class, args);
        System.exit(0);
    }
}
