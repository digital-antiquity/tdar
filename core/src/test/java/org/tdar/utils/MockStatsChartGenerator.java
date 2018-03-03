package org.tdar.utils;

import java.io.File;
import java.util.Map;

public class MockStatsChartGenerator implements StatsChartGenerator {
	public File generateResourcesPieChart(Map<String, Number> data, String filename){
		return new File("src/test/resources/charts/pie-sample.png");
	}
	
	public File generateTotalViewsBarChart(Map<String, Number>  data, String filename){
		return new File("src/test/resources/charts/bar-sample.png");
	}
	
	public File generateTotalDownloadsBarChart(Map<String, Number> data, String filename){
		return new File("src/test/resources/charts/bar-sample2.png");
	}
}
