package org.tdar.core.service.processes.charts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class TdarBarChart extends AbstractChart {

	private Map<String, Number> data;
	private String xAxisLabel;
	private String yAxisLabel;

	public TdarBarChart(String title, String xAxis, String yAxis, Map<String, Number> data, int width, int height,
			String filename) {
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
	public File createChart() throws IOException {
		CategoryChart chart = new CategoryChartBuilder().width(getWidth()).height(getHeight()).title(getTitle())
				.xAxisTitle(xAxisLabel).yAxisTitle(yAxisLabel).build();

		chart.getStyler().setPlotGridHorizontalLinesVisible(false);
		chart.getStyler().setPlotGridVerticalLinesVisible(false);

		addRowsToChart(data, chart);

		return renderAndExport(chart);
	}

	private void addRowsToChart(Map<String, Number> data, CategoryChart chart) {
/*		for (String key : data.keySet()) {
			List<String> series = new ArrayList<String>();
			List<Number> values = new ArrayList<Number>();

			for (int j = 0; j < data.size(); j++) {
				series.add(j, "");
			}
			for (int j = 0; j < data.size(); j++) {
				values.add(j, 0);
			}

			series.set(i, key);
			values.set(i, data.get(key));
			i++;
		}*/
		List<String> series = new ArrayList<String>();
		List<Number> values = new ArrayList<Number>();
		
		for (String key : data.keySet()) {
			series.add(key);
			values.add(data.get(key));

		}
		chart.addSeries("Series Name", series, values);
		
		/*for(int i = 0; i< values.size();i++){
			List<Number> seriesValue = new ArrayList<Number>();
			for (int j = 0; j < values.size(); j++) {
				seriesValue.add(j, 0);
			}
			seriesValue.set(i, values.get(i));	
			chart.addSeries(series.get(i), series, seriesValue);
		}*/
	}
}
