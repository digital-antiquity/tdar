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
import org.tdar.core.bean.resource.InformationResource;
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
import org.tdar.core.service.workflow.ActionMessageErrorListener;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.data.FileProxy;
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

    public static final String INVALID_FILE_TYPE = "invalid file type %s for resource type -- acceptable: %s";
    @Autowired
    private FileAnalyzer fileAnalyzer;
    @Autowired
    private ReflectionService reflectionService;
    @Autowired
    private GenericKeywordService genericKeywordService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private AuthenticationAndAuthorizationService authenticationAndAuthorizationService;
    @Autowired
    private GenericService genericService;
    @Autowired
    private InformationResourceService informationResourceService;

    public transient Logger logger = LoggerFactory.getLogger(getClass());

    public <R extends Resource> R bringObjectOntoSession(R incoming, Person authorizedUser) throws APIException, Exception {
        return bringObjectOntoSession(incoming, authorizedUser, null, null);
    }

    public <R extends Resource> R bringObjectOntoSession(R incoming_, Person authorizedUser, Collection<FileProxy> proxies, Long projectId)
            throws APIException, Exception {
        R incomingResource = incoming_;
        boolean created = true;
        if (Persistable.Base.isNotTransient(incomingResource)) {
            @SuppressWarnings("unchecked")
            R existing = (R) genericService.find(incomingResource.getClass(), incomingResource.getId());

            if (existing == null) {
                throw new APIException("Resource not found", StatusCode.NOT_FOUND);
            }

            if (!incomingResource.getResourceType().equals(existing.getResourceType())) {
                throw new APIException("incoming and existing resource types are different", StatusCode.FORBIDDEN);
            }

            // check if the user can modify the record
            if (!authenticationAndAuthorizationService.canEditResource(authorizedUser, existing)) {
                throw new APIException("Permission Denied", StatusCode.UNAUTHORIZED);
            }

            incomingResource.copyImmutableFieldsFrom(existing);
            genericService.detachFromSession(existing);
            created = false;
        }

        if (Persistable.Base.isNotNullOrTransient(projectId) && incoming_ instanceof InformationResource) {
            ((InformationResource) incoming_).setProject(genericService.find(Project.class, projectId));
        }

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
                    toAdd.add(processIncoming(p, incomingResource));
                }
                contents.clear();
                if (toAdd.size() > 0) {
                    logger.info("{} adding ({})", contents, toAdd);
                }
                contents.addAll(toAdd);
            } else if (Persistable.class.isAssignableFrom(content.getClass())) {
                reflectionService.callFieldSetter(incomingResource, pair.getFirst(), processIncoming((Persistable) content, incomingResource));
            }
        }

        incomingResource.markUpdated(authorizedUser);
        incomingResource = genericService.merge(incomingResource);

        Set<String> extensionsForType = fileAnalyzer.getExtensionsForType(ResourceType.fromClass(incomingResource.getClass()));
        if (CollectionUtils.isNotEmpty(proxies)) {
            for (FileProxy proxy : proxies) {
                String ext = FilenameUtils.getExtension(proxy.getFilename()).toLowerCase();
                if (!extensionsForType.contains(ext))
                    throw new APIException(String.format(INVALID_FILE_TYPE, ext, StringUtils.join(extensionsForType, ", ")), StatusCode.FORBIDDEN);
            }
            
            ActionMessageErrorListener listener = new ActionMessageErrorListener();
            informationResourceService.importFileProxiesAndProcessThroughWorkflow((InformationResource) incomingResource, authorizedUser, null, listener,
                    new ArrayList<FileProxy>(proxies));
            
            if (listener.hasActionErrors()) {
                throw new APIException(listener.toString(), StatusCode.UNKNOWN_ERROR);
            }
        }

        incomingResource.setCreated(created);
        genericService.saveOrUpdate(incomingResource);
        return incomingResource;
    }

    /*
     * Takes a POJO property that's off the session and returns a managed instance of that property and handling
     * special casing and validation as needed.
     */
    @SuppressWarnings("unchecked")
    private <P extends Persistable, R extends Resource> P processIncoming(P property, R resource) throws APIException {
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
                        throw new APIException("using unsupported controlled keyword (" + property.getClass().getSimpleName() + ")", StatusCode.FORBIDDEN);
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
                throw new APIException("new resource collections are not supported", StatusCode.FORBIDDEN);
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
                        throw new APIException(String.format("Object (%s: %s) is invalid", property.getClass(), property), StatusCode.FORBIDDEN);
                    }
                }
            }
        }
        return toReturn;
    }

    private <H extends Persistable> H findById(Class<H> second, Long id) {
        logger.info("{} {}", second, id);
        H h = genericService.find(second, id);
        if (h == null) {
            throw new TdarRecoverableRuntimeException("object was null");
        }
        return h;
    }

}
