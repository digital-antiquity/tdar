package org.tdar.utils;

import java.io.File;
import java.util.Map;

public interface StatsChartGenerator {
	public File generateResourcesPieChart(Map<String, Number> data, String filename);
	
	public File generateTotalViewsBarChart(Map<String, Number>  data, String filename);
	
	public File generateTotalDownloadsBarChart(Map<String, Number> totalDownloadsData, String filename);
}
