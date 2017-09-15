package org.tdar.core.service.processes.charts;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChartGenerator {
    Logger logger = LoggerFactory.getLogger(getClass());

    public File execute(AbstractChart sample) {
        JavaFxChartRunnable wrapper = new JavaFxChartRunnable();
        wrapper.setGraph(sample);
        logger.debug("starting to create chart");
        Thread thread = new Thread(wrapper);
        thread.start();
        while (wrapper.getResult() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("{}",e,e);
            }
        }
        return wrapper.getResult();
        
    }

}
