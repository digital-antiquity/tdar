package org.tdar.core.service.processes;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class BarChartGenerator extends Application {
	final static String austria = "Austria";
	final static String brazil = "Brazil";
	final static String france = "France";
	final static String italy = "Italy";
	final static String usa = "USA";

	@Override
	public void start(Stage stage) {
		// http://docs.oracle.com/javafx/2/charts/css-styles.htm
		stage.setTitle("Bar Chart Sample");
		final CategoryAxis xAxis = new CategoryAxis();
		final NumberAxis yAxis = new NumberAxis();
		final BarChart<String, Number> bc = new BarChart<String, Number>(xAxis, yAxis);
		bc.setTitle("Country Summary");
		xAxis.setLabel("Country");
		yAxis.setLabel("Value");

		XYChart.Series series1 = new XYChart.Series();
		series1.setName("2003");
		List<String> barColors = TdarConfiguration.getInstance().getBarColors();
		XYChart.Data e = new XYChart.Data(austria, 25601.34);
		// e.getNode().setStyle("-fx-pie-color: " + barColors.get(0) + ";");
		series1.getData().add(e);
		series1.getData().add(new XYChart.Data(brazil, 20148.82));
		series1.getData().add(new XYChart.Data(france, 10000));
		series1.getData().add(new XYChart.Data(italy, 35407.15));
		series1.getData().add(new XYChart.Data(usa, 12000));

		XYChart.Series series2 = new XYChart.Series();
		series2.setName("2004");
		// series2.getNode().setStyle("-fx-pie-color: " + barColors.get(1) +
		// ";");
		series2.getData().add(new XYChart.Data(austria, 57401.85));
		series2.getData().add(new XYChart.Data(brazil, 41941.19));
		series2.getData().add(new XYChart.Data(france, 45263.37));
		series2.getData().add(new XYChart.Data(italy, 117320.16));
		series2.getData().add(new XYChart.Data(usa, 14845.27));

		XYChart.Series series3 = new XYChart.Series();
		series3.setName("2005");
		// series3.getNode().setStyle("-fx-pie-color: " + barColors.get(2) +
		// ";");
		series3.getData().add(new XYChart.Data(austria, 45000.65));
		series3.getData().add(new XYChart.Data(brazil, 44835.76));
		series3.getData().add(new XYChart.Data(france, 18722.18));
		series3.getData().add(new XYChart.Data(italy, 17557.31));
		series3.getData().add(new XYChart.Data(usa, 92633.68));
		bc.getData().addAll(series1, series2, series3);
		bc.setAnimated(false);
		for (Node n : bc.lookupAll(".default-color0")) {
			n.setStyle("-fx-bar-fill: " + barColors.get(0) + ";");
		}
		// second bar color
		for (Node n : bc.lookupAll(".default-color1")) {
			n.setStyle("-fx-bar-fill:" + barColors.get(1) + ";");
		}
		for (Node n : bc.lookupAll(".default-color2")) {
			n.setStyle("-fx-bar-fill:" + barColors.get(2) + ";");
		}

		Scene scene = new Scene(bc, 800, 600);
		stage.setScene(scene);
		stage.show();
		final SnapshotParameters snapshotParameters = new SnapshotParameters();

		FileOutputStream baos = null;
		try {
			File file = new File("chart.png");
			snapshotParameters.setFill(Color.TRANSPARENT);
			WritableImage image = bc.snapshot(snapshotParameters, null);
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
		} catch (Exception e1) {
			throw new TdarRecoverableRuntimeException("", e1);
		} finally {
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
