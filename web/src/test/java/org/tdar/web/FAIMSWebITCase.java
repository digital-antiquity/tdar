package org.tdar.web;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.ResourceExportService;
import org.tdar.utils.APIClient;
import org.tdar.utils.Pair;
import org.tdar.utils.TestConfiguration;
import org.tdar.utils.jaxb.JaxbResultContainer;

public class FAIMSWebITCase extends AbstractIntegrationTestCase {

    @Autowired
    SerializationService serializationService;

    @Autowired
    ResourceExportService resourceExportService;
    
    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();

 
    @Test
    @Rollback(true)
    public void testFAIMS() {
        genericService.markReadOnly();
        APIClient client = new APIClient(CONFIG.getBaseSecureUrl());
        try {
            client.apiLogin(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());
        } catch (Exception e) {
            logger.error("error logging in", e);
        }
        for (Long id : genericService.findAllIds(Project.class)) {
            Resource resource = genericService.find(Project.class, id);
            if (resource.getStatus() != Status.ACTIVE && resource.getStatus() != Status.DRAFT) {
                continue;
            }
            String output = export(resource);
            try {
                client.uploadRecord(output, null, 8L    );
            } catch (IOException e) {
                logger.error("error uploading",e);
            }
            genericService.clearCurrentSession();
        }
        logger.debug("done projects");
        for (Long id : genericService.findAllIds(InformationResource.class)) {
            InformationResource resource = genericService.find(InformationResource.class, id);
            if (resource.getStatus() != Status.ACTIVE && resource.getStatus() != Status.DRAFT) {
                continue;
            }
            String output =  export(resource);
            
            genericService.clearCurrentSession();
        }
    }

    private String export(Resource resource) {
        Long id = resource.getId();
        // we mess with IDs, so just in case
        Resource r = resourceExportService.setupResourceForExport(resource);

        try {
            String convertToXML = serializationService.convertToXML(r);
            genericService.detachFromSession(resource);
            r= null;
            resource.setId(id);
            File type = new File("target/export/" + resource.getResourceType().name());
            FileUtils.forceMkdir(type);
            File dir = new File(type, id.toString());
            FileUtils.forceMkdir(dir);

            File file = new File(dir, "record.xml");

            FileUtils.writeStringToFile(file, convertToXML);
            return convertToXML;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        
    }

}
