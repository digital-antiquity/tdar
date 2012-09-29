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
import java.util.LinkedHashSet;
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
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.FullUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ReadUser;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
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
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.ErrorStatusCode;
import org.tdar.core.service.ResourceService.ErrorHandling;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.geosearch.GeoSearchService;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.Pair;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Adam Brin
 * 
 */
@Transactional
@Service
public class ImportService {
    @Autowired
    GeographicKeywordService geographicKeywordService;
    @Autowired
    OtherKeywordService otherKeywordService;
    @Autowired
    TemporalKeywordService temporalKeywordService;
    @Autowired
    SiteTypeKeywordService siteTypeKeywordService;
    @Autowired
    SiteNameKeywordService siteNameKeywordService;
    @Autowired
    InvestigationTypeService investigationTypeService;
    @Autowired
    MaterialKeywordService materialKeywordService;
    @Autowired
    CultureKeywordService cultureKeywordService;
    @Autowired
    FileAnalyzer fileAnalyzer;
    @Autowired
    ResourceService resourceService;
    @Autowired
    EntityService entityService;
    @Autowired
    GenericService genericService;
    @Autowired
    GeoSearchService geoSearchService;
    @Autowired
    InformationResourceService informationResourceService;
    @Autowired
    GenericDao genericDao;
    @Autowired
    ProjectService projectService;

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

    public Resource loadXMLFile(InputStream fileio, Person p, List<Pair<String, InputStream>> filePairs) throws ClassNotFoundException,
            IOException, APIException {
        return loadXMLFile(fileio, p, filePairs, null);
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

    public Resource loadXMLFile(InputStream fileio, Person p, List<Pair<String, InputStream>> filePairs, Long overrideProjectId) throws ClassNotFoundException,
            IOException, APIException {
        Resource incomingResource = loadXMLFile(fileio, p, overrideProjectId);
        Set<String> extensionsForType = fileAnalyzer.getExtensionsForType(ResourceType.fromClass(incomingResource.getClass()));
        if (incomingResource instanceof InformationResource) {
            for (Pair<String, InputStream> filePair : filePairs) {
                String ext = FilenameUtils.getExtension(filePair.getFirst()).toLowerCase();
                if (!extensionsForType.contains(ext))
                    throw new APIException("invalid file type " + ext + " for resource type -- acceptable:"
                            + StringUtils.join(extensionsForType, ", "), ErrorStatusCode.NOT_ALLOWED);
                FileProxy proxy = new FileProxy(filePair.getFirst(), filePair.getSecond(), VersionType.UPLOADED, FileAction.ADD);
                informationResourceService.processFileProxy((InformationResource) incomingResource, proxy);
            }
            genericService.saveOrUpdate(incomingResource);
        }

        return incomingResource;
    }

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
        created = populateFromExistingResource(xstreamResource, person);

        logger.debug("can edit record");
        for (ResourceCreator resourceCreator : xstreamResource.getResourceCreators()) {
            entityService.findOrSaveResourceCreator(resourceCreator);
        }

        saveSharedChildMetadata(xstreamResource);

        xstreamResource = genericService.merge(xstreamResource);

        // FIXME: this is a hack for child collections that are unique to SensoryData. Extend this to handle other InformationResource types
        if (xstreamResource instanceof SensoryData) {
            saveCustomChildren((SensoryData) xstreamResource);
        }

        // FIXME: also add handling for codingsheet/ontology category variable

        // FIXME: THIS IS A HACK FOR NADB, should remove when NADB IS LOADED
        List<String> fips = new ArrayList<String>();
        for (GeographicKeyword kwd : xstreamResource.getGeographicKeywords()) {
            if (kwd.getLevel() == Level.FIPS_CODE) {
                fips.add(kwd.getLabel().substring(0, kwd.getLabel().indexOf("(")).trim());
            }
        }

        if (fips.size() > 0) {
            if (xstreamResource.getLatitudeLongitudeBoxes() == null) {
                xstreamResource.setLatitudeLongitudeBoxes(new LinkedHashSet<LatitudeLongitudeBox>());
            }
            LatitudeLongitudeBox extractedLatLong = geoSearchService.extractLatLongFromFipsCode(fips.toArray(new String[fips.size()]));
            xstreamResource.getLatitudeLongitudeBoxes().clear();
            if (extractedLatLong != null) {
                xstreamResource.setLatitudeLongitudeBox(extractedLatLong);
                resourceService.processManagedKeywords(xstreamResource, xstreamResource.getLatitudeLongitudeBoxes());
            }
        }

        if (xstreamResource instanceof InformationResource) {
            InformationResource ir = (InformationResource) xstreamResource;
            ir.setProjectId(projectId);
            resolveProject(ir);
        }

        resourceService.saveOrUpdate(xstreamResource);
        xstreamResource.setCreated(created);
        return xstreamResource;
    }

    /**
     * @param person
     * @param xstreamResource
     */
    private void initializeBasicMetadata(Person person, Resource xstreamResource) {
        xstreamResource.setAccessCounter(0L);
        xstreamResource.setStatus(Status.ACTIVE);
        xstreamResource.markUpdated(person);
        xstreamResource.setFullUsers(new HashSet<FullUser>());
        xstreamResource.setReadUsers(new HashSet<ReadUser>());
    }

    // if incomingresource id already exists, populate certain values from existing record into incoming record. Return false if updating, true if creating.
    private boolean populateFromExistingResource(Resource xstreamResource, Person person) throws APIException {
        Long id = xstreamResource.getId();
        boolean created = true;

        if (id != null && id > 0) {
            created = false;
            Resource oldRecord = resourceService.find(id);
            // there was an id and the record wasn't found
            if (oldRecord == null) {
                throw new APIException("Resource not found", ErrorStatusCode.NOT_FOUND);
            }

            // check if the user can modify the record
            if (!entityService.canEditResource(person, oldRecord)) {
                throw new APIException("Permission Denied", ErrorStatusCode.UNAUTHORIZED);
            }
            logger.debug("updating existing record " + xstreamResource.getId());
            // remove old keywords ... and carry over values
            xstreamResource.setAccessCounter(oldRecord.getAccessCounter());
            xstreamResource.setDateRegistered(oldRecord.getDateRegistered());
            xstreamResource.setSubmitter(oldRecord.getSubmitter());

            for (FullUser user : oldRecord.getFullUsers()) {
                // FIXME: something weird here that we have to completely duplicate and cannot just reset Resource and "copy"
                // user.setResource(r);
                // r.getFullUsers().add(user);
                FullUser user_ = new FullUser();
                user_.setResource(xstreamResource);
                user_.setPerson(user.getPerson());
                xstreamResource.getFullUsers().add(user_);
            }
            for (ReadUser user : oldRecord.getReadUsers()) {
                ReadUser user_ = new ReadUser();
                user_.setResource(xstreamResource);
                user_.setPerson(user.getPerson());
                xstreamResource.getReadUsers().add(user_);
            }
            genericService.detachFromSession(oldRecord);
            oldRecord = null;
        }
        return created;
    }

    private void saveSharedChildMetadata(Resource xstreamResource) throws APIException {
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

        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_SKIP_ERRORS, xstreamResource.getResourceCreators(),
                xstreamResource.getResourceCreators(), ResourceCreator.class);

        // overloading this method in order to reuse it and take advantage of the validation
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_SKIP_ERRORS, xstreamResource.getResourceNotes(),
                xstreamResource.getResourceNotes(), ResourceNote.class);
        // FIXME: onetime change for NADB, switch back to reporting errors
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_SKIP_ERRORS, xstreamResource.getCoverageDates(),
                xstreamResource.getCoverageDates(), CoverageDate.class);
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_SKIP_ERRORS, xstreamResource.getLatitudeLongitudeBoxes(),
                xstreamResource.getLatitudeLongitudeBoxes(), LatitudeLongitudeBox.class);
        resourceService.saveHasResources(xstreamResource, false, ErrorHandling.VALIDATE_SKIP_ERRORS, xstreamResource.getResourceAnnotations(),
                xstreamResource.getResourceAnnotations(), ResourceAnnotation.class);
    }

    private void saveCustomChildren(SensoryData sensoryData) {
        resourceService.saveHasResources(sensoryData, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, sensoryData.getSensoryDataScans(),
                sensoryData.getSensoryDataScans(), SensoryDataScan.class);
        resourceService.saveHasResources(sensoryData, false, ErrorHandling.VALIDATE_WITH_EXCEPTION, sensoryData.getSensoryDataImages(),
                sensoryData.getSensoryDataImages(), SensoryDataImage.class);
    }

    public <G> void resolveManyToMany(Class<G> incomingClass, Collection<G> incomingCollection, List<String> ignoreProperties, boolean create)
            throws APIException {
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
                    throw new APIException(incomingClass.getSimpleName() + " " + incoming + " is not valid", ErrorStatusCode.NOT_ALLOWED);
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
        if (informationResource.getProjectId() == null)
            return;
        Project project = projectService.find(informationResource.getProjectId());
        if (project == null) {
            throw new APIException("Project not found:" + informationResource.getProjectId(), ErrorStatusCode.NOT_FOUND);
        }
        project.getInformationResources().add(informationResource);
        informationResource.setProject(project);
        logger.trace("resolved {} to project: {}", informationResource, project);
    }
}
