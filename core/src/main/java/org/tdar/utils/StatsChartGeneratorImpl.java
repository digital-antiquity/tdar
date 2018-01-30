package org.tdar.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.service.processes.charts.AbstractChart;
import org.tdar.core.service.processes.charts.TdarBarChart;
import org.tdar.core.service.processes.charts.TdarPieChart;


@Component
public class StatsChartGeneratorImpl implements StatsChartGenerator {
    Logger logger = LoggerFactory.getLogger(getClass());

	public File generateResourcesPieChart(Map<String, Number> data, String filename){
		TdarPieChart chart = new TdarPieChart("", 300, 300, filename, data);
		return runChart(chart);
	}
	
	public File generateTotalViewsBarChart(Map<String, Number>  data, String filename){
		TdarBarChart chart = new TdarBarChart("Total Views", "Views", "", data, 500, 500, filename);
		return runChart(chart);
	}
	
	public File generateTotalDownloadsBarChart(Map<String, Number> data, String filename){
		TdarBarChart chart = new TdarBarChart("Total Downloads", "Downloads", "", data, 500, 500, filename);
		return runChart(chart);
	}
	
	private File runChart(AbstractChart chart){
		try {
			return chart.createChart();
		} catch (IOException e) {
			logger.debug("Couldn't create chart image: {} {}",e,e);
			return null;
		}
	}
}
