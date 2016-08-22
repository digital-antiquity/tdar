package org.tdar.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.tdar.core.configuration.SimpleAppConfiguration;
import org.tdar.core.configuration.TdarAppConfiguration;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.ResourceExportService;
import org.tdar.faims.service.FaimsExportService;

@Component
@EnableTransactionManagement
public class FAIMSTransferTool {

    private static String username;

    private static String password;

    @Autowired
    SerializationService serializationService;

    @Autowired
    GenericService genericService;

    @Autowired
    ResourceExportService resourceExportService;

    @Autowired
    private FaimsExportService faimsExportService;

    private Long  accountId = 3L;

    protected static final Logger logger = LoggerFactory.getLogger(FAIMSTransferTool.class);

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    public static void main(String[] args) throws IOException {
        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(SimpleAppConfiguration.class, FAIMSTransferTool.class);
        applicationContext.refresh();
        applicationContext.start();
        username = System.getProperty("username","adam.brin@asu.edu");
        password = System.getProperty("password", "brin");
        FAIMSTransferTool transfer = new FAIMSTransferTool();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(transfer);
        try {
            transfer.exportFAIMS();
        } catch (Error exp) {
            logger.error(exp.getMessage(), exp);
            System.exit(9);
        } finally {
            applicationContext.close();
        }
        System.exit(0);
    }

    public void exportFAIMS() {
        faimsExportService.export(username, password, accountId);
    }

}
