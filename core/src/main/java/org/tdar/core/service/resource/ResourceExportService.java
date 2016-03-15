package org.tdar.core.service.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.SerializationService;
import org.tdar.utils.PersistableUtils;

@Service
public class ResourceExportService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private SerializationService serializationService;

    @Transactional
    public void export(final Resource... resources) {
//        for (Resource r : resources) {
//            File dir = new File();
//            FileUtils.forceMkdir(type);
//            File dir = new File(type, id.toString());
//            FileUtils.forceMkdir(dir);
//            
//            List<File> files = new ArrayList<>();
//            if (r instanceof InformationResource && ((InformationResource) r).getProject() != Project.NULL) {
//                Project p = setupResourceForExport(((InformationResource) r).getProject());
//                File file = writeToFile(dir, p, "project.xml");
//            }
//            Resource r_ = setupResourceForExport(r);
//
//        }
    }
    
    

    @Transactional(readOnly=true)
    private File writeToFile(File dir, Resource resource,String filename) throws Exception {
      String convertToXML = serializationService.convertToXML(resource);
      File type = new File("target/export/" + resource.getResourceType().name());
      File file = new File(dir, filename);

      FileUtils.writeStringToFile(file, convertToXML);
        return null;
    }



    @Transactional(readOnly = true)
    public <R extends Resource> R setupResourceForExport(final R resource) {
        genericDao.markReadOnly(resource);

        Long id = resource.getId();
        if (id == null) {
            logger.debug("ID NULL: {}", resource);

        }

        for (Keyword kwd : resource.getAllActiveKeywords()) {
            clearId(kwd);
            if (kwd instanceof HierarchicalKeyword) {
                ((HierarchicalKeyword) kwd).setParent(null);
            }
        }

        for (ResourceCreator rc : resource.getResourceCreators()) {
            clearId(rc);
            nullifyCreator(rc.getCreator());
        }

        // remove internal
        resource.getResourceCollections().removeIf(rc -> rc.isInternal());
        resource.getLatitudeLongitudeBoxes().forEach(llb -> clearId(llb));
        resource.getResourceCollections().forEach(rc -> {
            clearId(rc);
            rc.setResourceIds(null);
            rc.getResources().clear();
        });

        resource.getActiveRelatedComparativeCollections().forEach(cc -> clearId(cc));
        resource.getActiveSourceCollections().forEach(cc -> clearId(cc));
        resource.getActiveCoverageDates().forEach(cd -> clearId(cd));
        resource.getResourceNotes().forEach(rn -> clearId(rn));

        resource.getResourceAnnotations().forEach(ra -> {
            clearId(ra);
            clearId(ra.getResourceAnnotationKey());
        });
        logger.debug(id.toString());
        resource.getResourceAnnotations().add(new ResourceAnnotation(new ResourceAnnotationKey("FAIMS ID"), id.toString()));

        if (resource instanceof InformationResource) {
            InformationResource ir = (InformationResource) resource;
            nullifyCreator(ir.getPublisher());
            nullifyCreator(ir.getResourceProviderInstitution());
            nullifyCreator(ir.getCopyrightHolder());

            List<FileProxy> proxies = new ArrayList<>();
            for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                if (irf.isDeleted()) {
                    continue;
                }
                proxies.add(new FileProxy(irf));
            }
            ir.getInformationResourceFiles().clear();
            ir.setFileProxies(proxies);

            if (PersistableUtils.isNotNullOrTransient(ir.getProjectId())) {
                ir.setProject(new Project(ir.getProjectId(), null));
                ir.setMappedDataKeyColumn(null);
            }

            if (resource instanceof Dataset) {
                Dataset dataset = (Dataset) resource;
                dataset.setDataTables(null);
                dataset.setRelationships(null);
            }

            if (resource instanceof CodingSheet) {
                CodingSheet codingSheet = (CodingSheet) resource;
                codingSheet.setCodingRules(null);
                codingSheet.setAssociatedDataTableColumns(null);
                codingSheet.setDefaultOntology(null);
            }

            if (resource instanceof Ontology) {
                ((Ontology) resource).setOntologyNodes(null);
            }

        
        }

        resource.setId(null);
        return resource;
    }

    public void clearId(Persistable p) {
        genericDao.markReadOnly(p);
        p.setId(null);

    }

    private void nullifyCreator(Creator creator) {
        if (creator == null) {
            return;
        }
        clearId(creator);
        if (creator instanceof Person) {
            Person person = (Person) creator;
            if (person.getInstitution() != null) {
                clearId(person.getInstitution());
            }
        }
    }

}
