/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.SupportsResource;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.sensory.SensoryDataImage;
import org.tdar.core.bean.resource.sensory.SensoryDataScan;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.data.FileProxy;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The service that handles the import of info from the API into tDAR... Ideally this should be merged wih the BulkUploadService
 * 
 * @author Adam Brin
 * 
 */
@Transactional
@Service
public class ImportService {

    @Autowired
    private FileAnalyzer fileAnalyzer;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private AuthenticationAndAuthorizationService authenticationAndAuthorizationService;
    @Autowired
    private GenericService genericService;
    @Autowired
    private InformationResourceService informationResourceService;
    @Autowired
    private ProjectService projectService;

    public transient Logger logger = LoggerFactory.getLogger(getClass());
    private XStream xs;

    public XStream configureXStream() throws ClassNotFoundException {
        if (xs == null) {
            xs = new XStream();
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(XStreamAlias.class));
            String basePackage = "org/tdar/";
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                xs.processAnnotations((Class.forName(bd.getBeanClassName())));
            }

            xs.autodetectAnnotations(true);
        }
        return xs;

    }

    public Resource loadXMLFile(InputStream fileio, Person p, List<FileProxy> fileProxies) throws ClassNotFoundException,
            IOException, APIException {
        return loadXMLFile(fileio, p, fileProxies, null);
    }

    /**
     * @param filename
     * @param object
     * @param l
     * @return
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     */
    @Transactional
    public Resource loadXMLFile(File filename, Long personId, boolean persistRecord) throws FileNotFoundException, ClassNotFoundException, APIException {
        Person p = new Person();
        p.setName("NADB User");
        p.setId(personId);
        return loadXMLFile(new FileInputStream(filename), p, (Long) null);
    }

    public Resource loadXMLFile(InputStream fileio, Person p, List<FileProxy> proxies, Long overrideProjectId) throws ClassNotFoundException,
            IOException, APIException {
        Resource incomingResource = loadXMLFile(fileio, p, overrideProjectId);
        Set<String> extensionsForType = fileAnalyzer.getExtensionsForType(ResourceType.fromClass(incomingResource.getClass()));
        if (incomingResource instanceof InformationResource) {
            for (FileProxy proxy : proxies) {
                String ext = FilenameUtils.getExtension(proxy.getFilename()).toLowerCase();
                if (!extensionsForType.contains(ext))
                    throw new APIException("invalid file type " + ext + " for resource type -- acceptable:"
                            + StringUtils.join(extensionsForType, ", "), StatusCode.FORBIDDEN);
                informationResourceService.processFileProxy((InformationResource) incomingResource, proxy);
            }
            genericService.saveOrUpdate(incomingResource);
        }

        return incomingResource;
    }

    @SuppressWarnings("deprecation")
    @Transactional
    public Resource loadXMLFile(InputStream inputStream, Person person, Long projectOverrideId) throws ClassNotFoundException, FileNotFoundException,
            APIException {
        boolean created = true;
        XStream xs = configureXStream();
        Resource xstreamResource = (Resource) xs.fromXML(inputStream);
        Long projectId = null;
        if (xstreamResource instanceof InformationResource) {
            if (projectOverrideId != null) {
                projectId = projectOverrideId;
            } else {
                projectId = ((InformationResource) xstreamResource).getProjectId(); // we'll need this later for associating resource to project
            }
        }

        initializeBasicMetadata(person, xstreamResource);

        logger.info("{} UPDATING record {} ", person, xstreamResource.getId());
        created = populateFromExistingResource(person, xstreamResource);

        logger.debug("can edit record");
        if (CollectionUtils.isNotEmpty(xstreamResource.getResourceCreators())) {
            for (ResourceCreator resourceCreator : xstreamResource.getResourceCreators()) {
                entityService.findOrSaveResourceCreator(resourceCreator);
            }
        }

        saveSharedChildMetadata(xstreamResource, person);

        if (xstreamResource instanceof SupportsResource) {
            CategoryVariable categoryVariable = ((SupportsResource) xstreamResource).getCategoryVariable();
            ((SupportsResource) xstreamResource).setCategoryVariable(genericService.findByExample(CategoryVariable.class, categoryVariable,
                    FindOptions.FIND_FIRST).get(0));
        }

        xstreamResource = genericService.merge(xstreamResource);

        if (xstreamResource instanceof SensoryData) {
            saveCustomChildren((SensoryData) xstreamResource);
        }

        if (xstreamResource instanceof InformationResource) {
            InformationResource ir = (InformationResource) xstreamResource;
            ir.setProjectId(projectId);
            resolveProject(ir);
        }

        resourceService.saveOrUpdate(xstreamResource);

        resourceService.saveRecordToFilestore(xstreamResource);
        String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]", xstreamResource.getResourceType().getLabel(),
                person, xstreamResource.getId(), StringUtils.left(xstreamResource.getTitle(), 100));
        resourceService.logResourceModification(xstreamResource, person, logMessage);

        xstreamResource.setCreated(created);
        return xstreamResource;
    }

    /**
     * @param xstreamResource
     * @throws APIException
     */
    private void saveSharedChildMetadata(Resource xstreamResource, Person user) throws APIException {
        List<String> ignoreProperties = new ArrayList<String>(Arrays.asList("approved", "selectable"));

        // fixme if we have to do this for more ... we should make this nice and generic
        resolveManyToMany(MaterialKeyword.class, xstreamResource.getMaterialKeywords(), ignoreProperties, false);
        resolveManyToMany(InvestigationType.class, xstreamResource.getInvestigationTypes(), ignoreProperties, false);

        resolveManyToMany(OtherKeyword.class, xstreamResource.getOtherKeywords(), ignoreProperties, true);
        resolveManyToMany(TemporalKeyword.class, xstreamResource.getTemporalKeywords(), ignoreProperties, true);
        resolveManyToMany(CultureKeyword.class, xstreamResource.getCultureKeywords(), ignoreProperties, true);
        resolveManyToMany(SiteNameKeyword.class, xstreamResource.getSiteNameKeywords(), ignoreProperties, true);
        resolveManyToMany(SiteTypeKeyword.class, xstreamResource.getSiteTypeKeywords(), ignoreProperties, true);

        // FIXME: ADD SUPPORT FOR SENSORY DATA

        ignoreProperties.add("level");
        resolveManyToMany(GeographicKeyword.class, xstreamResource.getGeographicKeywords(), ignoreProperties, true);

        for (ResourceAnnotation annotation : xstreamResource.getResourceAnnotations()) {
            annotation.setResourceAnnotationKey(genericService.findByExample(ResourceAnnotationKey.class, annotation.getResourceAnnotationKey(),
                    FindOptions.FIND_FIRST_OR_CREATE).get(0));
        }

        // xstreamResource = genericService.merge(xstreamResource);
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, xstreamResource.getResourceCreators(),
                xstreamResource.getResourceCreators(), ResourceCreator.class);
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, xstreamResource.getSourceCollections(),
                xstreamResource.getSourceCollections(), SourceCollection.class);
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, xstreamResource.getRelatedComparativeCollections(),
                xstreamResource.getRelatedComparativeCollections(), RelatedComparativeCollection.class);

        // overloading this method in order to reuse it and take advantage of the validation
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, xstreamResource.getResourceNotes(),
                xstreamResource.getResourceNotes(), ResourceNote.class);
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, xstreamResource.getCoverageDates(),
                xstreamResource.getCoverageDates(), CoverageDate.class);
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, xstreamResource.getLatitudeLongitudeBoxes(),
                xstreamResource.getLatitudeLongitudeBoxes(), LatitudeLongitudeBox.class);
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, xstreamResource.getResourceAnnotations(),
                xstreamResource.getResourceAnnotations(), ResourceAnnotation.class);

        resourceCollectionService.saveSharedResourceCollections(xstreamResource, xstreamResource.getResourceCollections(),
                xstreamResource.getResourceCollections(),
                user, false, ErrorHandling.VALIDATE_WITH_EXCEPTION);

    }

    private void saveCustomChildren(SensoryData sensoryData) {
        resourceService.saveHasResources(sensoryData, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, sensoryData.getSensoryDataScans(),
                sensoryData.getSensoryDataScans(), SensoryDataScan.class);
        resourceService.saveHasResources(sensoryData, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, sensoryData.getSensoryDataImages(),
                sensoryData.getSensoryDataImages(), SensoryDataImage.class);
    }

    /**
     * @param person
     * @param xstreamResource
     */
    private void initializeBasicMetadata(Person person, Resource xstreamResource) {
        xstreamResource.setStatus(Status.ACTIVE);
        xstreamResource.markUpdated(person);
    }

    private boolean populateFromExistingResource(Person person, Resource xstreamResource) throws APIException {
        Long id = xstreamResource.getId();
        boolean created = true;
        // grab the ID and check whether it's valid
        if (id != null && id > 0) {
            logger.info("{} UPDATING record {} ", person, xstreamResource.getId());
            Resource oldRecord = resourceService.find(id);
            // there was an id and the record wasn't found
            if (oldRecord == null) {
                throw new APIException("Resource not found", StatusCode.NOT_FOUND);
            }

            if (!xstreamResource.getResourceType().equals(oldRecord.getResourceType())) {
                throw new APIException("incoming and existing resource types are different", StatusCode.FORBIDDEN);
            }

            // check if the user can modify the record
            if (!authenticationAndAuthorizationService.canEditResource(person, oldRecord)) {
                throw new APIException("Permission Denied", StatusCode.UNAUTHORIZED);
            }
            logger.debug("updating existing record " + xstreamResource.getId());
            // remove old keywords ... and carry over values
            xstreamResource.setDateCreated(oldRecord.getDateCreated());
            xstreamResource.setSubmitter(oldRecord.getSubmitter());

            xstreamResource.getResourceCollections().addAll(oldRecord.getResourceCollections());
            // it is probably not necessary to clear the collections of from the orig. resource, but uncomment if I'm wrong
            // oldRecord.getResourceCollections().clear();

            if (oldRecord instanceof InformationResource) {
                Set<InformationResourceFile> files = new HashSet<InformationResourceFile>(((InformationResource) oldRecord).getInformationResourceFiles());
                // ((InformationResource) oldRecord).getInformationResourceFiles().clear();
                ((InformationResource) xstreamResource).getInformationResourceFiles().addAll(files);
            }

            genericService.detachFromSession(oldRecord);
            oldRecord = null;
            created = false;
        } else {
            logger.info("{} CREATING new record  ", person);
        }
        return created;
    }

    public <G> void resolveManyToMany(Class<G> incomingClass, Collection<G> incomingCollection, List<String> ignoreProperties, boolean create)
            throws APIException {
        if (CollectionUtils.isEmpty(incomingCollection)) {
            return;
        }

        // if just creating, then simple call (otherwise, dealing with validation)
        if (create) {
            Set<G> findByExamples = genericService.findByExamples(incomingClass, incomingCollection, ignoreProperties, FindOptions.FIND_FIRST_OR_CREATE);
            incomingCollection.clear();
            incomingCollection.addAll(findByExamples);
        } else {
            List<G> toPersist = new ArrayList<G>();
            for (G incoming : incomingCollection) {
                List<G> found = genericService.findByExample(incomingClass, incoming, ignoreProperties, FindOptions.FIND_FIRST);
                if (CollectionUtils.isEmpty(found)) {
                    throw new APIException(incomingClass.getSimpleName() + " " + incoming + " is not valid", StatusCode.FORBIDDEN);
                } else {
                    toPersist.add(found.get(0));
                }
            }
            incomingCollection.clear();
            incomingCollection.addAll(toPersist);

        }
    }

    // if the informationResource specifies a project id, look it up and make necessary assignments
    private void resolveProject(InformationResource informationResource) throws APIException {
        logger.trace("resolving project...");
        if (informationResource.getProjectId() == null || informationResource.getProject() == Project.NULL)
            return;
        Project project = projectService.find(informationResource.getProjectId());
        if (project == null) {
            throw new APIException("Project not found:" + informationResource.getProjectId(), StatusCode.NOT_FOUND);
        }
        // project.getInformationResources().add(informationResource);
        informationResource.setProject(project);
        logger.trace("resolved {} to project: {}", informationResource, project);
    }
}
