package org.tdar.utils;

import java.io.File;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.tdar.core.service.processes.charts.AbstractChart;
import org.tdar.core.service.processes.charts.ChartGenerator;
import org.tdar.core.service.processes.charts.TdarBarChart;
import org.tdar.core.service.processes.charts.TdarPieChart;


@Component
public class MockStatsChartGenerator implements StatsChartGenerator {
	public File generateResourcesPieChart(Map<String, Number> data, String filename){
		return new File("src/test/resources/charts/pie-sample.png");
	}
	
	public File generateTotalViewsBarChart(Map<String, Map<String, Number>>  data, String filename){
		return new File("src/test/resources/charts/bar-sample.png");
	}
	
	public File generateTotalDownloadsBarChart(Map<String, Map<String, Number>> data, String filename){
		return new File("src/test/resources/charts/bar-sample2.png");
	}
}
