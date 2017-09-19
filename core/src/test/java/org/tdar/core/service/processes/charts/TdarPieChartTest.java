package org.tdar.core.service.processes.charts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//@Ignore
public class TdarPieChartTest {
    // from https://stackoverflow.com/a/18980655/667818
    Logger logger = LoggerFactory.getLogger(getClass());

    // Customize Chart
    Color[] sliceColors = new Color[] { new Color(235,215,144),
    new Color(214,184,75),
    new Color(195,170,114),
    new Color(160,157,91),
    new Color(144,157,91),
    new Color(220,118,18),
    new Color(189,50,0),
    new Color(102,0,0)};

    @Test
    @SuppressWarnings("restriction")
    public void testPie() throws Exception {
        setup();

        Map<String, Number> data = new HashMap<String, Number>();
        data.put("a", 50);
        data.put("b", 50);
        data.put("c", 150);
//        TdarPieChart sample = new TdarPieChart("test", 1000, 1000, "testPie", data);
//        JavaFxChartRunnable wrapper = new JavaFxChartRunnable();
//        wrapper.setGraph(sample);
        logger.debug("starting to create chart");
        
        

        // Create Chart
        PieChart chart = new PieChartBuilder().width(800).height(600).title(getClass().getSimpleName()).build();
     
        chart.getStyler().setSeriesColors(sliceColors);
     
        // Series
        chart.addSeries("Gold", 24);
        chart.addSeries("Silver", 21);
        chart.addSeries("Platinum", 39);
        chart.addSeries("Copper", 17);
        chart.addSeries("Zinc", 40);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setLegendBorderColor(Color.WHITE);
        BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapFormat.PNG);
//        VectorGraphicsEncoder.saveVectorGraphic(chart, "./Sample_Chart", VectorGraphicsFormat.EPS);

    }

    @Test
    @SuppressWarnings("restriction")
    public void testBar() throws Exception {
        setup();

        Map<String, Map<String, Number>> data = new HashMap<>();
        Map<String, Number> r1 = new HashMap<>();
        r1.put("a", 50);
        r1.put("b", 50);
        r1.put("c", 150);

        data.put("2001", r1);
        Map<String, Number> r2 = new HashMap<>();
        r1.put("", 0);
        r1.put("", 0);
        r1.put("", 0);
        r2.put("d", 150);
        r2.put("e", 150);
        r2.put("f", 350);

        data.put("2002", r2);
        Map<String, Number> r3 = new HashMap<>();
        r1.put("", 0);
        r1.put("", 0);
        r1.put("", 0);
        r1.put("", 0);
        r1.put("", 0);
        r3.put("g", 75);
        r3.put("h", 50);
        r3.put("i", 20);

        data.put("2003", r3);
//        TdarBarChart sample = new TdarBarChart("test", "time", "more time", data, 1000, 1000, "testBar");
//        ChartGenerator cg = new ChartGenerator();
//        cg.execute(sample);

        

        // Create Chart
        CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title("Score Histogram").xAxisTitle("Score").yAxisTitle("Number").build();

        // Customize Chart
        chart.getStyler().setLegendPosition(LegendPosition.InsideNW);
        chart.getStyler().setHasAnnotations(true);
     
        // Series					xx values										yy values
        //chart.addSeries("test 1", Arrays.asList(new String[] { "A","B","C","D","E" }), Arrays.asList(new Integer[] { 4, 5, 9, 6, 5 }));
        //chart.addSeries("test 2", Arrays.asList(new String[] { "A","B","C","D","E" }), Arrays.asList(new Integer[] { 4, 5, 9, 6, 5 }));

        
        for(String key : data.keySet()){
        	Map<String, Number> row = data.get(key);
        	
        	List<String> xValues = new ArrayList<String>();
        	List<Number> yValues = new ArrayList<Number>();
        	
        	for(String subkey : row.keySet()){
        		xValues.add(subkey);
        		yValues.add(row.get(subkey));
        	}
        	
        	
        	chart.addSeries(key, xValues, yValues);
        	
        }
        
        BitmapEncoder.saveBitmap(chart, "./Sample_Chart2", BitmapFormat.PNG);


    }

    private void setup() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("javafx.macosx.embedded", "true");
    }

}
