/**
 * $Id$
 *
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.search.config.TdarSearchAppConfiguration;
import org.tdar.search.service.index.SearchIndexService;
/**
 * http://thegenomefactory.blogspot.com.au/2013/08/minimum-standards-for-bioinformatics.html
 *
 * @author Adam Brin
 */
@Component
public class CommandLineIndexer {

    private static final transient Logger logger = LoggerFactory.getLogger(CommandLineIndexer.class);


    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private GenericService genericService;

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
     */
    public static void main(String[] args) throws IOException {
        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();  
        applicationContext.register(TdarSearchAppConfiguration.class);  
        applicationContext.refresh();  
        applicationContext.start();
        CommandLineIndexer importer = new CommandLineIndexer();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(importer);
        try {
            importer.index();
        } catch (Error exp) {
            logger.error(exp.getMessage(), exp);
            System.exit(9);
        } finally {
            applicationContext.close();
        }
        System.exit(0);
    }

    private void index() {
        TdarUser user = genericService.find(TdarUser.class,TdarConfiguration.getInstance().getAdminUserId());
        searchIndexService.indexAll(user);
        // TODO Auto-generated method stub
        
    }

}
