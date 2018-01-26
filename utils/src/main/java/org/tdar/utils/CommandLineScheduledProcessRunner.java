/**
 * $Id$
 *
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils;

import java.io.IOException;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;
import org.tdar.core.service.processes.AbstractScheduledProcess;
import org.tdar.search.config.TdarSearchAppConfiguration;
/**
 * http://thegenomefactory.blogspot.com.au/2013/08/minimum-standards-for-bioinformatics.html
 *
 * @author Adam Brin
 */
@Component
public class CommandLineScheduledProcessRunner {

    private static final transient Logger logger = LoggerFactory.getLogger(CommandLineScheduledProcessRunner.class);


    @Autowired
    private ScheduledProcessService scheduledProcessService;

    private boolean force;
    private Class<? extends AbstractScheduledProcess> cls;
    private AbstractScheduledProcess bean;


    private int startId;


    /**
     * The exit codes have the following meaning:
     * <ul>
     * <li>-1 : there was a problem encountered in the parsing of the arguments
     * <li>0 : no issues were encountered and the run completed successfully
     * <li>any number > 0 : the number of files that the tool was not able to import successfully.
     * </ul>
     *
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();  
        applicationContext.register(TdarSearchAppConfiguration.class);  
        applicationContext.refresh();  
        applicationContext.start();
        CommandLineScheduledProcessRunner importer = new CommandLineScheduledProcessRunner();
        importer.setCls((Class<? extends AbstractScheduledProcess>) Class.forName(args[0]));
        importer.setBean(applicationContext.getBean(importer.getCls()));
        if (args[1] != null) {
            if (NumberUtils.isDigits(args[1])) {
                importer.startId = Integer.parseInt(args[1]);
            } else if (BooleanUtils.toBoolean(args[1])) {
                importer.setForce(BooleanUtils.toBoolean(args[1]));
            }
        }
        if (args[2] != null) {
            importer.setForce(BooleanUtils.toBoolean(args[1]));
        }
        applicationContext.getAutowireCapableBeanFactory().autowireBean(importer);
        try {
            importer.run();
        } catch (Error exp) {
            logger.error(exp.getMessage(), exp);
            System.exit(9);
        } finally {
            applicationContext.close();
        }
        System.exit(0);
    }

    @SuppressWarnings("rawtypes")
    private void run() {
        
        if (!isForce() && scheduledProcessService.hasRun(getBean().getDisplayName())) {
            logger.debug("{} has already run", getBean().getDisplayName());
            System.exit(0);
        }
        
        if (getBean() instanceof AbstractScheduledBatchProcess && startId > -1) {
            ((AbstractScheduledBatchProcess) getBean()).setProcessStartId(startId);
        }
        getBean().execute();
        
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public Class<? extends AbstractScheduledProcess> getCls() {
        return cls;
    }

    public void setCls(Class<? extends AbstractScheduledProcess> cls) {
        this.cls = cls;
    }

    public AbstractScheduledProcess getBean() {
        return bean;
    }

    public void setBean(AbstractScheduledProcess bean) {
        this.bean = bean;
    }

}
