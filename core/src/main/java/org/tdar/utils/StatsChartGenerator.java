package org.tdar.utils;

import java.io.File;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.tdar.core.service.processes.charts.AbstractChart;
import org.tdar.core.service.processes.charts.ChartGenerator;
import org.tdar.core.service.processes.charts.TdarBarChart;
import org.tdar.core.service.processes.charts.TdarPieChart;


public interface StatsChartGenerator {
	public File generateResourcesPieChart(Map<String, Number> data, String filename);
	
	public File generateTotalViewsBarChart(Map<String, Map<String, Number>>  data, String filename);
	
	public File generateTotalDownloadsBarChart(Map<String, Map<String, Number>> data, String filename);
}
