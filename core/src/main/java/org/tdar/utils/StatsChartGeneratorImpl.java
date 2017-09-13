package org.tdar.utils;

import java.io.File;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.tdar.core.service.processes.charts.AbstractChart;
import org.tdar.core.service.processes.charts.ChartGenerator;
import org.tdar.core.service.processes.charts.TdarBarChart;
import org.tdar.core.service.processes.charts.TdarPieChart;


@Component
public class StatsChartGeneratorImpl implements StatsChartGenerator {
	public File generateResourcesPieChart(Map<String, Number> data, String filename){
		TdarPieChart chart = new TdarPieChart("", 100, 100, filename, data);
		return runChart(chart);
	}
	
	public File generateTotalViewsBarChart(Map<String, Map<String, Number>>  data, String filename){
		TdarBarChart chart = new TdarBarChart("Total Views", "Views", "", data, 100, 100, filename);
		return runChart(chart);
	}
	
	public File generateTotalDownloadsBarChart(Map<String, Map<String, Number>> data, String filename){
		TdarBarChart chart = new TdarBarChart("Total Downloads", "Downloads", "", data, 100, 100, filename);
		return runChart(chart);
	}
	
	private File runChart(AbstractChart chart){
		 ChartGenerator cg = new ChartGenerator();
		 return cg.execute(chart);
	}
}
