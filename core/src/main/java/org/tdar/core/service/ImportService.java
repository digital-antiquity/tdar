/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.OneToMany;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Sequenceable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.ControlledKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.SuggestedKeyword;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnRelationship;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.search.geosearch.GeoSearchService;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.Pair;
import org.tdar.utils.PersistableUtils;

/**
 * The service that handles the import of info from the API into tDAR... Ideally this should be merged wih the BulkUploadService
 * 
 * @author Adam Brin
 * 
 */
@Transactional
@Service
public class ImportService {

    public static final String UNDERSCORE = "_";
    public static final String _NEW_ID = "_NEW_ID_";
    public static final String COPY = " (Copy)";
    @Autowired
    private FileAnalyzer fileAnalyzer;
    @Autowired
    private ReflectionService reflectionService;
    @Autowired
    private GeoSearchService geoSearchService;
    @Autowired
    private GenericKeywordService genericKeywordService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private AuthorizationService authenticationAndAuthorizationService;
    @Autowired
    private GenericService genericService;
    @Autowired
    private InformationResourceService informationResourceService;
    @Autowired
    private SerializationService serializationService;

    private transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @see #bringObjectOntoSession(Resource, Person, Collection, Long)
     * 
     * @param incoming
     * @param authorizedUser
     * @return
     * @throws APIException
     * @throws Exception
     */
    public <R extends Resource> R bringObjectOntoSession(R incoming, TdarUser authorizedUser, boolean validate) throws Exception {
        return bringObjectOntoSession(incoming, authorizedUser, null, null, validate);
    }

    /**
     * Bring a @link Resource onto the session by checking that it exists, validating that the user has the rights to, avoiding collisions, copying the
     * immutable fields from the existing object, and reconcilling all of the child objects by looking them up in the database.
     * 
     * @param incoming_
     * @param authorizedUser
     * @param proxies
     * @param projectId
     * @return
     * @throws APIException
     * @throws IOException
     */
    @Transactional
    public <R extends Resource> R bringObjectOntoSession(R incoming_, TdarUser authorizedUser, Collection<FileProxy> proxies, Long projectId, boolean validate)
            throws APIException, IOException {
        R incomingResource = incoming_;
        boolean created = true;
        // If the object already has a tDAR ID
        created = reconcileIncomingObjectWithExisting(authorizedUser, incomingResource, created);

        if (PersistableUtils.isNotNullOrTransient(projectId) && (incoming_ instanceof InformationResource)) {
            ((InformationResource) incoming_).setProject(genericService.find(Project.class, projectId));
        }

        if (validate) {
            validateInvalidImportFields(incomingResource);
        }
        TdarUser blessedAuthorizedUser = genericService.merge(authorizedUser);
        incomingResource.markUpdated(blessedAuthorizedUser);

        reconcilePersistableChildBeans(blessedAuthorizedUser, incomingResource);
//        reflectionService.walkObject(incomingResource);
        logger.debug("comparing before/after merge:: before:{}", System.identityHashCode(blessedAuthorizedUser));
        incomingResource = genericService.merge(incomingResource);
        if (incomingResource instanceof InformationResource) {
            ((InformationResource) incomingResource).setDate(((InformationResource) incomingResource).getDate());
        }

        processFiles(blessedAuthorizedUser, proxies, incomingResource);
        geoSearchService.processManagedGeographicKeywords(incomingResource, incomingResource.getLatitudeLongitudeBoxes());
        
        incomingResource.setCreated(created);
        genericService.saveOrUpdate(incomingResource);
        return incomingResource;
    }

    public <R extends InformationResource> R processFileProxies(R incoming_, Collection<FileProxy> proxies, TdarUser authorizedUser) throws APIException,
            IOException {
        for (InformationResourceFile file : incoming_.getInformationResourceFiles()) {
            genericService.markWritableOnExistingSession(file);
        }
        processFiles(authorizedUser, proxies, incoming_);
        incoming_.markUpdated(authorizedUser);
        genericService.saveOrUpdate(incoming_);
        genericService.saveOrUpdate(incoming_.getInformationResourceFiles());
        return incoming_;

    }

    /**
     * Iterate through the @link FileProxy objects and Files and import them setting metadata and permissions as needed.
     * 
     * @param authorizedUser
     * @param proxies
     * @param incomingResource
     * @throws APIException
     * @throws IOException
     */
    private <R extends Resource> void processFiles(TdarUser authorizedUser, Collection<FileProxy> proxies, R incomingResource) throws APIException, IOException {
        Set<String> extensionsForType = fileAnalyzer.getExtensionsForType(ResourceType.fromClass(incomingResource.getClass()));
        if (CollectionUtils.isNotEmpty(proxies)) {
            for (FileProxy proxy : proxies) {
                String ext = FilenameUtils.getExtension(proxy.getFilename()).toLowerCase();
                if (!extensionsForType.contains(ext)) {
                    throw new APIException("importService.invalid_file_type", Arrays.asList(ext, StringUtils.join(extensionsForType, ", ")),
                            StatusCode.FORBIDDEN);
                }
            }

            ErrorTransferObject listener = informationResourceService.importFileProxiesAndProcessThroughWorkflow((InformationResource) incomingResource,
                    authorizedUser, null,
                    new ArrayList<FileProxy>(proxies));

            if (CollectionUtils.isNotEmpty(listener.getActionErrors())) {
                throw new APIException(listener.toString(), StatusCode.UNKNOWN_ERROR);
            }
        }
    }

    /**
     * Look up the existing object within tDAR and reconcile it with the incomming one. Sets immutable fields on incoming entry based on the existing values.
     * 
     * @param authorizedUser
     * @param incomingResource
     * @param created
     * @return
     * @throws APIException
     */
    private <R extends Resource> boolean reconcileIncomingObjectWithExisting(TdarUser authorizedUser, R incomingResource, boolean created_) throws APIException {
        boolean created = created_;
        if (PersistableUtils.isNotTransient(incomingResource)) {
            @SuppressWarnings("unchecked")
            R existing = (R) genericService.find(incomingResource.getClass(), incomingResource.getId());

            if (existing == null) {
                throw new APIException(MessageHelper.getMessage("importService.object_not_found"), StatusCode.NOT_FOUND);
            }

            if (!incomingResource.getResourceType().equals(existing.getResourceType())) {
                throw new APIException(MessageHelper.getMessage("importService.different_types"), StatusCode.FORBIDDEN);
            }

            // check if the user can modify the record
            if (!authenticationAndAuthorizationService.canEditResource(authorizedUser, existing, GeneralPermissions.MODIFY_RECORD)) {
                throw new APIException(MessageHelper.getMessage("error.permission_denied"), StatusCode.UNAUTHORIZED);
            }
            if (incomingResource instanceof InformationResource) {
                // when we bring an object onto the session,
                ((InformationResource) incomingResource).getInformationResourceFiles().clear();
            }
            incomingResource.copyImmutableFieldsFrom(existing);
            // FIXME: could be trouble: the next line implicitly detaches the submitter we just copied to incomingResource
            genericService.detachFromSession(existing);
            created = false;
        }
        return created;
    }

    /**
     * Find all of the @link Persistable children and look them up in the database, using the entries that have ids or equivalents in tDAR before using the ones
     * that are attached to the XML.
     * 
     * @param authorizedUser
     * @param incomingResource
     * @return
     * @throws APIException
     */
    @Transactional(readOnly = false)
    public <R extends Persistable> R reconcilePersistableChildBeans(final TdarUser authorizedUser, final R incomingResource) throws APIException {
        // for every field that has a "persistable" or a collection of them...
        List<Pair<Field, Class<? extends Persistable>>> testReflection = reflectionService.findAllPersistableFields(incomingResource.getClass());
        // Map<String, Persistable> knownObjects = new HashMap<>();
        // knownObjects.put(makeKey(authorizedUser), authorizedUser);
        for (Pair<Field, Class<? extends Persistable>> pair : testReflection) {
            logger.trace("{}", pair);
            Object content = reflectionService.callFieldGetter(incomingResource, pair.getFirst());
            if (content == null) {
                continue;
            }
            logger.trace("{}, {}", content, pair.getFirst());
            if (Collection.class.isAssignableFrom(content.getClass())) {
                List<Persistable> toAdd = new ArrayList<Persistable>();
                @SuppressWarnings("unchecked")
                Collection<Persistable> originalList = (Collection<Persistable>) content;
                Collection<Persistable> contents = new ArrayList<Persistable>(originalList);
                // using a separate collection to avoid concurrent modification of bi-directional double-lists
                Iterator<Persistable> iterator = contents.iterator();
                originalList.clear();
                boolean isResourceCollection = false;
                int count = 0;
                while (iterator.hasNext()) {
                    Persistable p = iterator.next();
                    if (p instanceof ResourceCollection) {
                        isResourceCollection = true;
                    }
                    Persistable result = processIncoming(p, incomingResource, authorizedUser);
                    if (result instanceof Sequenceable) {
                        ((Sequenceable<?>) result).setSequenceNumber(count);
                    }
                    count++;
                    toAdd.add(result);
                }
                if (isResourceCollection) {
                    continue;
                }
                if (toAdd.size() > 0) {
                    logger.info("{} adding ({})", contents, toAdd);
                }
                originalList.addAll(toAdd);
            } else if (Persistable.class.isAssignableFrom(content.getClass())) {
                logger.trace("setter: {}", pair.getFirst());
                reflectionService.callFieldSetter(incomingResource, pair.getFirst(), processIncoming((Persistable) content, incomingResource, authorizedUser));
            }
        }
        return incomingResource;
    }

    /**
     * There are a bunch of fields on the XML schema that are not supported, throw errors if they're encounted.
     * 
     * @param incomingResource
     * @throws APIException
     */
    private <R extends Resource> void validateInvalidImportFields(R incomingResource) throws APIException {
        if (incomingResource instanceof Dataset) {
            Dataset dataset = (Dataset) incomingResource;
            if (CollectionUtils.isNotEmpty(dataset.getDataTables())) {
                throw new APIException(MessageHelper.getMessage("importService.dataset_not_supported"), StatusCode.UNKNOWN_ERROR);
            }
        }

        if (incomingResource instanceof CodingSheet) {
            CodingSheet codingSheet = (CodingSheet) incomingResource;
            if (CollectionUtils.isNotEmpty(codingSheet.getMappedValues()) ||
                    CollectionUtils.isNotEmpty(codingSheet.getAssociatedDataTableColumns()) ||
                    CollectionUtils.isNotEmpty(codingSheet.getCodingRules()) ||
                    PersistableUtils.isNotNullOrTransient(codingSheet.getDefaultOntology())) {
                throw new APIException(MessageHelper.getMessage("importService.coding_sheet_mappings_not_supported"), StatusCode.UNKNOWN_ERROR);
            }
        }

        if (incomingResource instanceof Ontology) {
            Ontology ontology = (Ontology) incomingResource;
            if (CollectionUtils.isNotEmpty(ontology.getOntologyNodes())) {
                throw new APIException(MessageHelper.getMessage("importService.ontology_node_not_supported"), StatusCode.UNKNOWN_ERROR);
            }
        }

        if (incomingResource instanceof InformationResource) {
            InformationResource informationResource = (InformationResource) incomingResource;

            if (PersistableUtils.isNotNullOrTransient(informationResource.getMappedDataKeyColumn())) {
                throw new APIException(MessageHelper.getMessage("importService.related_dataset_not_supported"), StatusCode.UNKNOWN_ERROR);
            }
        }
    }

    /**
     * Takes a record and round-trips it to XML to allow us to manipulate it and clone it with the session
     * 
     * @param resource
     * @param user
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = false)
    public <R extends Resource> R cloneResource(R resource, TdarUser user) throws Exception {
        boolean canEditResource = true;
        if (!authenticationAndAuthorizationService.canEdit(user, resource)) {
            canEditResource = false;
        }

        // serialize to XML -- gets the new copy of resource off the session, so we can reset IDs as needed
        // Long oldId = resource.getId();
        
        String xml = serializationService.convertToXML(resource);
        @SuppressWarnings("unchecked")
        R rec = (R) serializationService.parseXml(new StringReader(xml));
        rec.setId(null);
        rec.setTitle(rec.getTitle() + COPY);
        rec.setDateCreated(new Date());
        InformationResource informationResource = null;

        if (rec instanceof InformationResource) {
            InformationResource originalIr = (InformationResource) resource;
            informationResource = (InformationResource) rec;
            informationResource.setExternalId(null);
            // reset project if user doesn't have rights to it
            if (originalIr.getProject() != Project.NULL) {
                if (authenticationAndAuthorizationService.canEdit(user, originalIr.getProject())) {
                    informationResource.setProject(originalIr.getProject());
                }
            }

            // remove files
            informationResource.getInformationResourceFiles().clear();
        }

        // obfuscate LatLong and clear collections if no permissions to resource
        ResourceCollection irc = rec.getInternalResourceCollection();

        if (irc != null) {
            for (AuthorizedUser au : irc.getAuthorizedUsers()) {
                au.setId(null);
            }
            irc.setId(null);
            irc.getResources().clear();
            irc.getResources().add(rec);
        }

        if (!canEditResource) {
            for (LatitudeLongitudeBox latLong : rec.getLatitudeLongitudeBoxes()) {
                latLong.obfuscate();
            }
            rec.getResourceCollections().clear();
            if (informationResource != null) {
                informationResource.setProject(Project.NULL);
            }
            irc = null;
        } else {
            // if user does have rights; clone the collections, but reset the Internal ResourceCollection
            for (ResourceCollection rc : rec.getResourceCollections()) {
                rc.getResources().add(rec);
            }
        }
        genericService.detachFromSession(rec);
        if (irc != null) {
            genericService.detachFromSession(irc);
            irc.setId(null);
        }

        // reset one-to-many IDs so that new versions are generated for this resource and not the orignal clone
        resetOneToManyPersistableIds(rec);

        rec.getResourceRevisionLog().clear();
        rec = bringObjectOntoSession(rec, user, false);
        ResourceRevisionLog rrl = new ResourceRevisionLog(String.format("Cloned Resource from id: %s", resource.getId()), rec, user, RevisionLogType.CREATE);
        genericService.saveOrUpdate(rrl);
        if (rec instanceof Dataset) {
            Dataset dataset = (Dataset) rec;
            for (DataTable dt : dataset.getDataTables()) {
                String name = dt.getName();
                int index1 = name.indexOf(UNDERSCORE);
                int index2 = name.indexOf(UNDERSCORE, index1 + 1);
                name = name.substring(0, index1) + "_0_" + name.substring(index2 + 1);
                dt.setName(name);
            }
            genericService.saveOrUpdate(dataset.getDataTables());
        }
        rec.getResourceRevisionLog().add(rrl);
        rec.setStatus(Status.DRAFT);
        rec.markUpdated(user);
        rec.setSubmitter(user);
        rec.setUploader(user);
        genericService.saveOrUpdate(rec);
        return rec;
    }

    @SuppressWarnings("unchecked")
    public <R extends Resource> void resetOneToManyPersistableIds(R rec) {
        List<Field> findAnnotatedFieldsOfClass = ReflectionService.findAnnotatedFieldsOfClass(rec.getClass(), OneToMany.class);
        for (Field fld : findAnnotatedFieldsOfClass) {
            Collection<Persistable> actual = (Collection<Persistable>) reflectionService.callFieldGetter(rec, fld);
            Collection<Persistable> values = new ArrayList<>(actual);
            actual.clear();
            for (Persistable value : values) {
                value.setId(null);
                actual.add(value);
                if (value instanceof CodingRule) {
                    CodingRule rule = (CodingRule)value;
                    CodingSheet sheet = (CodingSheet)rec;
                    rule.setCodingSheet(sheet);
                }
                if (value instanceof OntologyNode) {
                    OntologyNode rule = (OntologyNode)value;
                    Ontology sheet = (Ontology)rec;
                    rule.setOntology(sheet);
                }
                if (value instanceof DataTable) {
                    DataTable dataTable = (DataTable) value;
                    Dataset dataset = (Dataset) rec;
                    dataTable.setDataset(dataset);
                    List<DataTableColumn> adt = dataTable.getDataTableColumns();
                    List<DataTableColumn> vals = new ArrayList<>(adt);
                    adt.clear();
                    for (DataTableColumn dtc : vals) {
                        resetIdAndAdd(adt, dtc);
                        dtc.setDataTable(dataTable);
                    }
                    Set<DataTableRelationship> relationships = dataset.getRelationships();
                    Collection<DataTableRelationship> vals_ = new ArrayList<>(relationships);
                    relationships.clear();
                    for (DataTableRelationship dtr : vals_) {
                        resetIdAndAdd(relationships, dtr);
                        Set<DataTableColumnRelationship> crs = dtr.getColumnRelationships();
                        Collection<DataTableColumnRelationship> cr_ = new ArrayList<>(crs);
                        crs.clear();
                        for (DataTableColumnRelationship r : cr_) {
                            resetIdAndAdd(crs, r);
                        }
                    }

                }
            }
        }
    }

    private <R extends Persistable> void resetIdAndAdd(Collection<R> crs, R r) {
        r.setId(null);
        crs.add(r);
    }

    /**
     * Takes a POJO property that's off the session and returns a managed instance of that property and handling
     * special casing and validation as needed.
     * 
     * @param property
     * @param resource
     * @param authenticatedUser
     * @return
     * @throws APIException
     */
    @SuppressWarnings("unchecked")
    public <P extends Persistable, R extends Persistable> P processIncoming(P property, R resource, TdarUser authenticatedUser) throws APIException {
        // if we're not transient, find by id...
        if (PersistableUtils.isNotNullOrTransient(property)) {
            Class<? extends Persistable> cls = property.getClass();
            Long id = property.getId();
//            property = null;
            P toReturn = (P) findById(cls, id);
            if (toReturn instanceof ResourceCollection && resource instanceof Resource) {
                ResourceCollection collection = (ResourceCollection) toReturn;
                // making sure that the collection's creators and other things are on the sessions properly too
                resetOwnerOnSession(collection);
                collection.getResources().add((Resource) resource);
                ((Resource) resource).getResourceCollections().add(collection);
            }
            if (toReturn instanceof Person) {
                Institution inst = ((Person) toReturn).getInstitution();
                if (PersistableUtils.isNotNullOrTransient(inst) && !genericService.sessionContains(inst)) {
                    ((Person) toReturn).setInstitution(findById(Institution.class, inst.getId()));
                }
            }
            if (toReturn instanceof TdarUser) {
                Institution inst = ((TdarUser) toReturn).getProxyInstitution();
                if (PersistableUtils.isNotNullOrTransient(inst) && !genericService.sessionContains(inst)) {
                    ((TdarUser) toReturn).setProxyInstitution(findById(Institution.class, inst.getId()));
                }
            }
            return toReturn;
        }

        P toReturn = property;

        if (property instanceof Keyword) {
            Class<? extends Keyword> kwdCls = (Class<? extends Keyword>) property.getClass();
//            logger.debug(":::> {} ({} [{}])", property, kwdCls, property instanceof ControlledKeyword);
            if (property instanceof ControlledKeyword && !(property instanceof SuggestedKeyword)) {
                Keyword findByLabel = genericKeywordService.findByLabel(kwdCls, ((Keyword) property).getLabel());
                if (findByLabel == null) {
                    throw new APIException("importService.unsupported_keyword", Arrays.asList(property.getClass().getSimpleName()),
                            StatusCode.FORBIDDEN);
                }
                toReturn = (P)findByLabel;
            } else {
                toReturn = (P) genericKeywordService.findOrCreateByLabel(kwdCls, ((Keyword) property).getLabel());
            }
        }
        if (property instanceof ResourceCreator) {
            ResourceCreator creator = (ResourceCreator) property;
            entityService.findOrSaveResourceCreator(creator);
            creator.isValidForResource((Resource) resource);
        }

        if (property instanceof Creator) {
            Creator<?> creator = (Creator<?>) property;
            toReturn = (P) entityService.findOrSaveCreator(creator);
            logger.debug("findOrSaveCreator:{}", creator);
        }

        if (property instanceof ResourceCollection && resource instanceof Resource) {
            ResourceCollection collection = (ResourceCollection) property;
            collection = reconcilePersistableChildBeans(authenticatedUser, collection);
            resourceCollectionService.addResourceCollectionToResource((Resource) resource, ((Resource) resource).getResourceCollections(),
                    authenticatedUser, true,
                    ErrorHandling.VALIDATE_WITH_EXCEPTION, collection);
            toReturn = null;
        }

        if (property instanceof ResourceAnnotation) {
            ResourceAnnotationKey incomingKey = ((ResourceAnnotation) property).getResourceAnnotationKey();
            ResourceAnnotationKey resolvedKey = genericService.findByExample(ResourceAnnotationKey.class, incomingKey, FindOptions.FIND_FIRST_OR_CREATE)
                    .get(0);
            ((ResourceAnnotation) property).setResourceAnnotationKey(resolvedKey);
        }

        if (property instanceof Validatable && !(property instanceof Resource)) {
            if (!((Validatable) property).isValidForController()) {
                if (property instanceof Project) {
                    toReturn = (P) Project.NULL;
                } else if ((property instanceof Creator) && ((Creator<?>) property).hasNoPersistableValues()) {
                    toReturn = null;
                } else if ((property instanceof ResourceCollection) && ((ResourceCollection) property).isInternal()) {
                    toReturn = property;
                } else {
                    throw new APIException("importService.object_invalid", Arrays.asList(property.getClass(), property), StatusCode.FORBIDDEN);
                }
            }
        }
        return toReturn;
    }

    private void resetOwnerOnSession(ResourceCollection collection) {
        collection.setOwner(entityService.findOrSaveCreator(collection.getOwner()));
        collection.setUpdater(entityService.findOrSaveCreator(collection.getUpdater()));
    }

    /**
     * Wrapper around {@link GenericService.findById(Class c,Object o)} that errors if it doesn't exist.
     * 
     * @param second
     * @param id
     * @return
     */
    private <H extends Persistable> H findById(Class<H> second, Long id) {
        logger.trace("{} {}", second, id);
        H h = genericService.find(second, id);
        if (h == null) {
            logger.error("object does not exist: {} ({})", second, id);
            throw new TdarRecoverableRuntimeException("error.object_does_not_exist");
        }
        return h;
    }

}
