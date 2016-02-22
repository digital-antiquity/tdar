/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils;

/**
 * @author Adam Brin
 *
 */

import java.io.File;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaExport.Type;
import org.hibernate.tool.hbm2ddl.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final transient Logger log = LoggerFactory.getLogger(getClass());

    public static final String HBM_FILENAME = "hibernate.hbm.xml";

    public SchemaGenerator() throws Exception {
        cfg = new Configuration();

        // allow for xml-based mappings, if present.
        if (getClass().getClassLoader().getResource(HBM_FILENAME) != null) {
            cfg.addResource("hibernate.hbm.xml");
        }

        // now add annotated class mappings
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(NamedQuery.class));
        scanner.addExcludeFilter(new AnnotationTypeFilter(Subselect.class));
        scanner.addExcludeFilter(new AnnotationTypeFilter(Immutable.class));
        String basePackage = "org/tdar/";
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            Class<?> forName = Class.forName(bd.getBeanClassName());
            cfg.addAnnotatedClass(forName);
        }

        cfg.setProperty("hibernate.hbm2ddl.auto", "create");
        cfg.setProperty("hibernate.dialect", DIALECT);

    }

    /**
     * Method that actually creates the file.
     * 
     * @param dbDialect
     *            to use
     */
    private void generate(String filename) {
        log.info("exporting schema to: " + filename);
        
//        SchemaExport export = new SchemaExport();
//        export.
//        export.setDelimiter(";");
        File f = new File(filename);
        new File(f.getParent()).mkdirs();
  //      export.setOutputFile(filename);
//        export.execute(Target.SCRIPT, Type.CREATE);
        log.error("DISABLED w/breakage in Hiberate 5.1");
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        SchemaGenerator gen = new SchemaGenerator();
        gen.generate(args[0]);
    }

}
