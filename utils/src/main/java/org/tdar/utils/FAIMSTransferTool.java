package org.tdar.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarAppConfiguration;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.FaimsExportService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.ResourceExportService;

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

    private Long  accountId = 216L;

    protected static final Logger logger = LoggerFactory.getLogger(FAIMSTransferTool.class);

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    public static void main(String[] args) throws IOException {
        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(TdarAppConfiguration.class, FAIMSTransferTool.class);
        applicationContext.refresh();
        applicationContext.start();
        username = "adam.brin@asu.edu";
        password = "brin";
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
