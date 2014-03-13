/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.ControlledKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.core.service.workflow.ActionMessageErrorListener;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.Pair;

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
    private ReflectionService reflectionService;
    @Autowired
    private GenericKeywordService genericKeywordService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private AuthenticationAndAuthorizationService authenticationAndAuthorizationService;
    @Autowired
    private GenericService genericService;
    @Autowired
    private InformationResourceService informationResourceService;

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
    public <R extends Resource> R bringObjectOntoSession(R incoming, Person authorizedUser) throws Exception {
        return bringObjectOntoSession(incoming, authorizedUser, null, null);
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
    public <R extends Resource> R bringObjectOntoSession(R incoming_, Person authorizedUser, Collection<FileProxy> proxies, Long projectId)
            throws APIException, IOException {
        R incomingResource = incoming_;
        boolean created = true;
        // If the object already has a tDAR ID
        created = reconcileIncomingObjectWithExisting(authorizedUser, incomingResource, created);

        if (Persistable.Base.isNotNullOrTransient(projectId) && incoming_ instanceof InformationResource) {
            ((InformationResource) incoming_).setProject(genericService.find(Project.class, projectId));
        }

        validateInvalidImportFields(incomingResource);

        
        reconcilePersistableChildBeans(authorizedUser, incomingResource);
        logger.debug("comparing before/after merge:: before:{}", System.identityHashCode(authorizedUser));
        Person blessedAuthorizedUser = genericService.merge(authorizedUser);
        logger.debug("comparing before/after merge:: before:{}        after:{}", System.identityHashCode(authorizedUser),
                System.identityHashCode(blessedAuthorizedUser));
        incomingResource.markUpdated(blessedAuthorizedUser);
        // genericService.detachFromSession(authorizedUser);
        incomingResource = genericService.merge(incomingResource);

        processFiles(authorizedUser, proxies, incomingResource);

        incomingResource.setCreated(created);
        genericService.saveOrUpdate(incomingResource);
        return incomingResource;
    }

    /**
     * Iterate through the @link FileProxy objects and Files and import them setting metadata and permissions as needed.
     * @param authorizedUser
     * @param proxies
     * @param incomingResource
     * @throws APIException
     * @throws IOException
     */
    private <R extends Resource> void processFiles(Person authorizedUser, Collection<FileProxy> proxies, R incomingResource) throws APIException, IOException {
        Set<String> extensionsForType = fileAnalyzer.getExtensionsForType(ResourceType.fromClass(incomingResource.getClass()));
        if (CollectionUtils.isNotEmpty(proxies)) {
            for (FileProxy proxy : proxies) {
                String ext = FilenameUtils.getExtension(proxy.getFilename()).toLowerCase();
                if (!extensionsForType.contains(ext))
                    throw new APIException("importService.invalid_file_type", Arrays.asList(ext, StringUtils.join(extensionsForType, ", ")), StatusCode.FORBIDDEN);
            }

            ActionMessageErrorListener listener = new ActionMessageErrorListener();
            informationResourceService.importFileProxiesAndProcessThroughWorkflow((InformationResource) incomingResource, authorizedUser, null, listener,
                    new ArrayList<FileProxy>(proxies));

            if (listener.hasActionErrors()) {
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
    private <R extends Resource> boolean reconcileIncomingObjectWithExisting(Person authorizedUser, R incomingResource, boolean created) throws APIException {
        if (Persistable.Base.isNotTransient(incomingResource)) {
            @SuppressWarnings("unchecked")
            R existing = (R) genericService.find(incomingResource.getClass(), incomingResource.getId());

            if (existing == null) {
                throw new APIException(MessageHelper.getMessage("importService.object_not_found"), StatusCode.NOT_FOUND);
            }

            if (!incomingResource.getResourceType().equals(existing.getResourceType())) {
                throw new APIException(MessageHelper.getMessage("importService.different_types"), StatusCode.FORBIDDEN);
            }

            // check if the user can modify the record
            if (!authenticationAndAuthorizationService.canEditResource(authorizedUser, existing)) {
                throw new APIException(MessageHelper.getMessage("error.permission_denied"), StatusCode.UNAUTHORIZED);
            }

            incomingResource.copyImmutableFieldsFrom(existing);
            // FIXME: could be trouble: the next line implicitly detaches the submitter we just copied to incomingResource
            genericService.detachFromSession(existing);
            created = false;
        }
        return created;
    }

    /**
     * Find all of the @link Persistable children and look them up in the database, using the entries that have ids or equivalents in tDAR before using the ones that are attached to the XML.
     * 
     * @param authorizedUser
     * @param incomingResource
     * @throws APIException
     */
    private <R extends Resource> void reconcilePersistableChildBeans(Person authorizedUser, R incomingResource) throws APIException {
        // for every field that has a "persistable" or a collection of them...
        List<Pair<Field, Class<? extends Persistable>>> testReflection = reflectionService.findAllPersistableFields(incomingResource.getClass());
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
                Collection<Persistable> contents = (Collection<Persistable>) content;
                Iterator<Persistable> iterator = contents.iterator();
                while (iterator.hasNext()) {
                    Persistable p = iterator.next();
                    toAdd.add(processIncoming(p, incomingResource, authorizedUser));
                }
                contents.clear();
                if (toAdd.size() > 0) {
                    logger.info("{} adding ({})", contents, toAdd);
                }
                contents.addAll(toAdd);
            } else if (Persistable.class.isAssignableFrom(content.getClass())) {
                reflectionService.callFieldSetter(incomingResource, pair.getFirst(), processIncoming((Persistable) content, incomingResource, authorizedUser));
            }
        }
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

        if (CollectionUtils.isNotEmpty(incomingResource.getBookmarks())) {
            throw new APIException(MessageHelper.getMessage("importService.bookmark_not_supported"), StatusCode.UNKNOWN_ERROR);
        }

        if (incomingResource instanceof CodingSheet) {
            CodingSheet codingSheet = (CodingSheet) incomingResource;
            if (CollectionUtils.isNotEmpty(codingSheet.getMappedValues()) || 
                    CollectionUtils.isNotEmpty(codingSheet.getAssociatedDataTableColumns()) ||
                    CollectionUtils.isNotEmpty(codingSheet.getCodingRules()) ||
                    Persistable.Base.isNotNullOrTransient(codingSheet.getDefaultOntology())
                    ) {
                throw new APIException(MessageHelper.getMessage("importService.coding_sheet_mappings_not_supported"), StatusCode.UNKNOWN_ERROR);
            }
        }

        if (incomingResource instanceof Ontology) {
            Ontology ontology = (Ontology) incomingResource;
            if (CollectionUtils.isNotEmpty(ontology.getOntologyNodes())) {
                throw new APIException(MessageHelper.getMessage("importService.ontology_node_not_supported"), StatusCode.UNKNOWN_ERROR);
            }
        }

        if (incomingResource instanceof Project) {
            Project project = (Project) incomingResource;
            if (CollectionUtils.isNotEmpty(project.getCachedInformationResources())) {
                throw new APIException(MessageHelper.getMessage("importService.cached_data_not_supported"), StatusCode.UNKNOWN_ERROR);
            }
        }

        if (incomingResource instanceof InformationResource) {
            InformationResource informationResource = (InformationResource) incomingResource;

            if (CollectionUtils.isNotEmpty(informationResource.getRelatedDatasetData().keySet())) {
                throw new APIException(MessageHelper.getMessage("importService.related_dataset_not_supported"), StatusCode.UNKNOWN_ERROR);
            }
            
            if (Persistable.Base.isNotNullOrTransient(informationResource.getMappedDataKeyColumn())) {
                throw new APIException(MessageHelper.getMessage("importService.related_dataset_not_supported"), StatusCode.UNKNOWN_ERROR);
            }
        }
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
    private <P extends Persistable, R extends Resource> P processIncoming(P property, R resource, Person authenticatedUser) throws APIException {
        P toReturn = property;

        // if we're not transient, find by id...
        if (Persistable.Base.isNotNullOrTransient((Persistable) property)) {
            // if (property instanceof HasResource<?> && toReturn instanceof Validatable && ((Validatable)toReturn).isValidForController()) {
            // if (property instanceof ResourceCreator) {
            // entityService.findOrSaveResourceCreator((ResourceCreator) property);
            // ((ResourceCreator) property).isValidForResource(resource);
            // }
            //
            // return toReturn;
            // } else {
            toReturn = (P) findById(property.getClass(), property.getId());
            // }
        }
        else // otherwise, reconcile appropriately
        {
            if (property instanceof Keyword) {
                Class<? extends Keyword> kwdCls = (Class<? extends Keyword>) property.getClass();
                if (property instanceof ControlledKeyword) {
                    Keyword findByLabel = (Keyword) genericKeywordService.findByLabel(kwdCls, ((Keyword) property).getLabel());
                    if (findByLabel == null) {
                        throw new APIException("importService.unsupported_keyword", Arrays.asList(property.getClass().getSimpleName()),
                                StatusCode.FORBIDDEN);
                    }
                } else {
                    toReturn = (P) genericKeywordService.findOrCreateByLabel(kwdCls, ((Keyword) property).getLabel());
                }
            }
            if (property instanceof ResourceCreator) {
                entityService.findOrSaveResourceCreator((ResourceCreator) property);
                ((ResourceCreator) property).isValidForResource(resource);
            }

            if (property instanceof ResourceCollection) {
                ResourceCollection collection = (ResourceCollection) property;
                resourceCollectionService.addResourceCollectionToResource(resource, resource.getResourceCollections(), authenticatedUser, true, ErrorHandling.VALIDATE_WITH_EXCEPTION, collection);;
            }

            if (property instanceof ResourceAnnotation) {
                ResourceAnnotationKey incomingKey = ((ResourceAnnotation) property).getResourceAnnotationKey();
                ResourceAnnotationKey resolvedKey = genericService.findByExample(ResourceAnnotationKey.class, incomingKey, FindOptions.FIND_FIRST_OR_CREATE)
                        .get(0);
                ((ResourceAnnotation) property).setResourceAnnotationKey(resolvedKey);
            }

            if (property instanceof Validatable) {
                if (!((Validatable) property).isValidForController()) {
                    if (property instanceof Project) {
                        toReturn = (P) Project.NULL;
                    } else if (property instanceof Creator && ((Creator) property).hasNoPersistableValues()) {
                        toReturn = null;
                    } else {
                        throw new APIException("importService.object_invalid", Arrays.asList(property.getClass(), property), StatusCode.FORBIDDEN);
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * Wrapper around {@link GenericService.findById(Class c,Object o)} that errors if it doesn't exist.
     * 
     * @param second
     * @param id
     * @return
     */
    private <H extends Persistable> H findById(Class<H> second, Long id) {
        logger.info("{} {}", second, id);
        H h = genericService.find(second, id);
        if (h == null) {
            throw new TdarRecoverableRuntimeException("error.object_does_not_exist");
        }
        return h;
    }

}
