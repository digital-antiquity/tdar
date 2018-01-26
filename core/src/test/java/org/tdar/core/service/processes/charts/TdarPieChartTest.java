package org.tdar.core.service.processes.charts;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.processes.charts.TdarPieChart;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

@Ignore
public class TdarPieChartTest {
    // from https://stackoverflow.com/a/18980655/667818
    Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    @SuppressWarnings("restriction")
    public void testPie() throws Exception {
        Map<String, Number> data = new HashMap<String, Number>();
        data.put("a", 50);
        data.put("b", 50);
        data.put("c", 150);
        TdarPieChart sample = new TdarPieChart("test", 1000, 1000, "testPie", data);
        JavaFxChartRunnable wrapper = new JavaFxChartRunnable();
        wrapper.setGraph(sample);
        logger.debug("starting to create chart");
        ChartGenerator cg = new ChartGenerator();
        cg.execute(sample);
    }

    @Test
    @SuppressWarnings("restriction")
    public void testBar() throws Exception {
        Map<String, Map<String, Number>> data = new HashMap<>();
        Map<String, Number> r1 = new HashMap<>();
        r1.put("a", 50);
        r1.put("b", 50);
        r1.put("c", 150);

        data.put("2001", r1);
        Map<String, Number> r2 = new HashMap<>();
        r2.put("a", 150);
        r2.put("b", 150);
        r2.put("c", 350);

        data.put("2002", r2);
        Map<String, Number> r3 = new HashMap<>();
        r3.put("a", 75);
        r3.put("b", 50);
        r3.put("c", 20);

        data.put("2003", r3);
        TdarBarChart sample = new TdarBarChart("test", "time", "more time", data, 1000, 1000, "testBar");
        ChartGenerator cg = new ChartGenerator();
        cg.execute(sample);
        

    }

}
