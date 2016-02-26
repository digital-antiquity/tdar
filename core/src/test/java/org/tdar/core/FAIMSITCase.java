package org.tdar.core;

import java.io.File;

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

public class FAIMSITCase extends AbstractIntegrationTestCase {

    @Autowired
    SerializationService serializationService;

    @Autowired
    ResourceExportService resourceExportService;
 
    @Test
    @Rollback(true)
    public void testFAIMS() {
        genericService.markReadOnly();
        for (Long id : genericService.findAllIds(Project.class)) {
            Resource resource = genericService.find(Project.class, id);
            if (resource.getStatus() != Status.ACTIVE && resource.getStatus() != Status.DRAFT) {
                continue;
            }
            export(resource);
            genericService.clearCurrentSession();
        }
        logger.debug("done projects");
        for (Long id : genericService.findAllIds(InformationResource.class)) {
            InformationResource resource = genericService.find(InformationResource.class, id);
            if (resource.getStatus() != Status.ACTIVE && resource.getStatus() != Status.DRAFT) {
                continue;
            }
            export(resource);
            genericService.clearCurrentSession();
        }
    }

    private void export(Resource resource) {
        Long id = resource.getId();
        // we mess with IDs, so just in case
        Resource r = resourceExportService.setupResourceForExport(resource);

        try {
            String convertToXML = serializationService.convertToXML(r);
            genericService.detachFromSession(resource);
            r= null;
            resource.setId(id);
            File type = new File(resource.getResourceType().name());
            FileUtils.forceMkdir(type);
            File dir = new File(type, id.toString());
            FileUtils.forceMkdir(dir);

            File file = new File(dir, "record.xml");

            FileUtils.writeStringToFile(file, convertToXML);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

}
