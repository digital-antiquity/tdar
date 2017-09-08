package org.tdar.core.service.processes.charts;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.tdar.core.service.processes.charts.PieChartGenerator;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

@Ignore
public class PieChartTest {
    // from https://stackoverflow.com/a/18980655/667818

    @Test
    @SuppressWarnings("restriction")
    public void testPie() throws Exception {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                new JFXPanel(); // Initializes the JavaFx Platform
                Platform.setImplicitExit(false);
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        Stage stage = new Stage();
                        stage.show();
                        Map<String, Number> data = new HashMap<String, Number>();
                        data.put("a", 50);
                        data.put("b", 50);
                        data.put("c", 150);
                        PieChartGenerator sample = new PieChartGenerator("test", 1000, 1000, "testPie", data);
                        sample.start(stage);
                    }
                });
            }
        });
        thread.start();// Initialize the thread
        Thread.sleep(100000); // Time to use the app, with out this, the thread
                             // will be killed before you can tell.
    }


    @Test
    @SuppressWarnings("restriction")
    public void testBar() throws Exception {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                new JFXPanel(); // Initializes the JavaFx Platform
                Platform.setImplicitExit(false);
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        Stage stage = new Stage();
                        stage.show();
                        Map<String, Map<String, Number>> data = new HashMap<>();
                        Map<String,Number> r1 = new HashMap<>();
                        r1.put("a", 50);
                        r1.put("b", 50);
                        r1.put("c", 150);

                        data.put("2001", r1 );
                        Map<String,Number> r2 = new HashMap<>();
                        r2.put("a", 150);
                        r2.put("b", 150);
                        r2.put("c", 350);

                        data.put("2002", r2 );
                        Map<String,Number> r3 = new HashMap<>();
                        r3.put("a", 75);
                        r3.put("b", 50);
                        r3.put("c", 20);

                        data.put("2003", r3 );
                        BarChartGenerator sample = new BarChartGenerator("test", "time", "more time", data, 1000,1000, "testBar");

                        sample.start(stage);
                    }
                });
            }
        });
        thread.start();// Initialize the thread
        Thread.sleep(100000); // Time to use the app, with out this, the thread
                             // will be killed before you can tell.
    }

}
