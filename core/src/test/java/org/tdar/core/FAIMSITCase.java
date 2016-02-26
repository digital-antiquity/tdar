package org.tdar.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.service.SerializationService;

public class FAIMSITCase extends AbstractIntegrationTestCase {

    @Autowired
    SerializationService serializationService;

    @Test
    @Rollback
    public void testFAIMS() {
        for (Resource resource : genericService.findAll(Resource.class)) {
            if (resource.getStatus() != Status.ACTIVE && resource.getStatus() != Status.DRAFT) {
                continue;
            }
            Long id = resource.getId();

            for (Keyword kwd : resource.getAllActiveKeywords()) {
                kwd.setId(null);
                if (kwd instanceof HierarchicalKeyword) {
                    ((HierarchicalKeyword) kwd).setParent(null);
                }
            }

            for (ResourceCreator rc : resource.getResourceCreators()) {
                rc.setId(null);
                nullifyCreator(rc.getCreator());
            }

            
            
            // remove internal
            resource.getResourceCollections().removeIf(rc -> rc.isInternal());
            resource.getResourceCollections().forEach(rc -> {
                rc.setId(null);
                rc.getResourceIds().clear();
                rc.getResources().clear();
            });

            resource.getResourceNotes().forEach(rn -> rn.setId(null));
            
            resource.getActiveRelatedComparativeCollections().forEach(cc -> cc.setId(null));
            resource.getActiveSourceCollections().forEach(cc -> cc.setId(null));
            resource.getActiveCoverageDates().forEach(cd -> cd.setId(null));
            resource.getResourceNotes().forEach(rn -> rn.setId(null));

            resource.getResourceAnnotations().forEach(ra -> { 
                ra.setId(null);
                ra.getResourceAnnotationKey().setId(null);
            });
            resource.getResourceAnnotations().add(new ResourceAnnotation(new ResourceAnnotationKey("FAIMS ID"), id.toString()));

            if (resource instanceof InformationResource) {
                InformationResource ir = (InformationResource) resource;
                nullifyCreator(ir.getPublisher());
                nullifyCreator(ir.getResourceProviderInstitution());
                nullifyCreator(ir.getCopyrightHolder());

                List<FileProxy> proxies = new ArrayList<>();
                for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                    proxies.add(new FileProxy(irf));
                }
                ir.getInformationResourceFiles().clear();
                ir.setFileProxies(proxies);
                ir.setProject(null);
            }

            
            resource.setId(null);
            
            try {
                String convertToXML = serializationService.convertToXML(resource);
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

    private void nullifyCreator(Creator creator) {
        if (creator == null) {
            return;
        }
        creator.setId(null);
        if (creator instanceof Person) {
            Person person = (Person) creator;
            if (person.getInstitution() != null) {
                person.getInstitution().setId(null);
            }
        }
    }
}
