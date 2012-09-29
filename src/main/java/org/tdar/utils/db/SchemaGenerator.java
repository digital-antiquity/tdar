/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils.db;

/**
 * @author Adam Brin
 *
 */

import java.io.File;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * @author john.thompson
 *         http://jandrewthompson.blogspot.com/2009/10/how-to-generate
 *         -ddl-scripts-from.html
 *         http://www.sleberknight.com/blog/sleberkn/entry/20080623
 */
public class SchemaGenerator {

    private static final String DIALECT = "org.hibernate.dialect.PostgreSQLDialect";
    private Configuration cfg;
    private Logger log = Logger.getLogger(getClass());

    public SchemaGenerator() throws Exception {
        cfg = new Configuration();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(NamedQuery.class));
        String basePackage = "org/tdar/";
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            cfg.addAnnotatedClass(Class.forName(bd.getBeanClassName()));
        }

        cfg.setProperty("hibernate.hbm2ddl.auto", "create");

    }

    /**
     * Method that actually creates the file.
     * 
     * @param dbDialect
     *            to use
     */
    private void generate(String filename) {
        cfg.setProperty("hibernate.dialect", DIALECT);
        log.info("exporting schema to: " + filename);
        SchemaExport export = new SchemaExport(cfg);
        export.setDelimiter(";");
        File f = new File(filename);
        new File(f.getParent()).mkdirs();
        export.setOutputFile(filename);
        export.execute(false, false, false, true);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        SchemaGenerator gen = new SchemaGenerator();
        gen.generate(args[0]);
    }

}
