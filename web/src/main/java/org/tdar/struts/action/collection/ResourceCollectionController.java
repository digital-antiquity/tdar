package org.tdar.struts.action.collection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.CollectionSaveObject;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.DataTableResourceDisplay;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespaces(value={@Namespace("/collection")})
public class ResourceCollectionController extends AbstractPersistableController<ResourceCollection> implements DataTableResourceDisplay {

    private static final long serialVersionUID = 1169442990022630650L;

    /**
     * Threshold that defines a "big" collection (based on imperieal evidence by highly-trained tDAR staff). This number
     * refers to the combined count of authorized users +the count of resources associated with a collection. Big
     * collections may adversely affect save/load times as well as cause rendering problems on the client, and so the
     * system may choose to mitigate these effects (somehow)
     */
    public static final int BIG_COLLECTION_CHILDREN_COUNT = 3_000;

    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    
    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient AuthorizationService authorizationService;
    

    private static final String RIGHTS = "rights";
    private List<ResourceCollection> allResourceCollections = new LinkedList<>();

    private List<Long> selectedResourceIds = new ArrayList<>();
    private List<Resource> fullUserProjects;
    private List<ResourceCollection> collections = new LinkedList<>();

    private Long viewCount = 0L;
    private ArrayList<ResourceType> selectedResourceTypes = new ArrayList<ResourceType>();

    private List<Project> allSubmittedProjects;
    private File file;
    private String fileContentType;
    private String fileFileName;
    private String ownerProperName;
    private TdarUser owner;

    private String parentCollectionName;
    private String alternateParentCollectionName;
    private Long parentId;
    private Long alternateParentId;
    private ResourceCollection parentCollection;
    private ResourceCollection alternateParentCollection;


    @Override
    public boolean authorize() {
        if (isNullOrNew()) {
            return true;
        }
        return authorizationService.canEditCollection(getAuthenticatedUser(), getPersistable());
    }

    
    
    private List<Long> toRemove = new ArrayList<>();
    private List<Long> toAdd = new ArrayList<>();

    public List<Long> getToRemove() {
        return toRemove;
    }


    public void setToRemove(List<Long> toRemove) {
        this.toRemove = toRemove;
    }


    public List<Long> getToAdd() {
        return toAdd;
    }


    public void setToAdd(List<Long> toAdd) {
        this.toAdd = toAdd;
    }


    /**
     * Returns a list of all resource collections that can act as candidate parents for the current resource collection.
     * 
     * @return
     */
    public List<ResourceCollection> getCandidateParentResourceCollections() {
        List<ResourceCollection> publicResourceCollections = resourceCollectionService.findPotentialParentCollections(getAuthenticatedUser(),
                getPersistable());
        return publicResourceCollections;
    }


    @Override
    protected String save(ResourceCollection persistable) {
        // FIXME: may need some potential check for recursive loops here to prevent self-referential parent-child loops
        // FIXME: if persistable's parent is different from current parent; then need to reindex all of the children as well

        CollectionSaveObject cso = new CollectionSaveObject(persistable, getAuthenticatedUser(), getStartTime());
        cso.setParent(getParentCollection());
        cso.setAlternateParent(getAlternateParentCollection());
        cso.setParentId(getParentId());
        cso.setAlternateParentId(getAlternateParentId());
        cso.setShouldSave(shouldSaveResource());
        cso.setFileProxy(generateFileProxy(getFileFileName(), getFile()));
        cso.setToAdd(getToAdd());
        cso.setToRemove(getToRemove());
        resourceCollectionService.saveCollectionForController(cso);
        setSaveSuccessPath(getPersistable().getUrlNamespace());
        
        return SUCCESS;
    }


    public ResourceCollection getResourceCollection() {
        if (getPersistable() == null) {
            setPersistable(new ResourceCollection());
        }
        return getPersistable();
    }

    public void setResourceCollection(ResourceCollection rc) {
        setPersistable(rc);
    }

    @Override
    public Class<ResourceCollection> getPersistableClass() {
        return ResourceCollection.class;
    }

    /**
     * A hint to the view-layer that this resource collection is "big". The view-layer may choose to gracefully degrade the presentation to save on bandwidth
     * and/or
     * client resources.
     * 
     * @return
     */
    public boolean isBigCollection() {
        return (getPersistable().getManagedResources().size()) > BIG_COLLECTION_CHILDREN_COUNT;
    }
    
    
    

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();

        parentCollection = prepareParent(parentId, parentCollectionName);
        alternateParentCollection = prepareParent(alternateParentId, alternateParentCollectionName);
        
        setupOwnerField();
        if (PersistableUtils.isNotNullOrTransient(getOwner())) {
            TdarUser uploader = getGenericService().find(TdarUser.class, getOwner().getId());
            getPersistable().setOwner(uploader);
        }

        if(getParentCollection() != null) {
            parentId = getParentCollection().getId();
        }
        if(getAlternateParentCollection() != null) {
            alternateParentId =  getAlternateParentCollection().getId();
        }

        if (PersistableUtils.isNotNullOrTransient(parentId) && PersistableUtils.isNullOrTransient((Persistable)getParentCollection())) {
            addActionError(getText("collectionController.type_mismatch"));
        }

    }
    
    

    private ResourceCollection prepareParent(Long pid, String parentName) {
        ResourceCollection parentC = null;
        if(PersistableUtils.isNotNullOrTransient(pid)) {
            parentC = getGenericService().find(getPersistableClass(), pid);
            getLogger().debug("lookup parent collection by id:{}  result:{}", pid, parentC);
        }
        else if(StringUtils.isNotBlank(parentName)) {
            parentC = resourceCollectionService.findCollectionsWithName(getAuthenticatedUser(), parentName);
            getLogger().debug("lookup parent collection by name:{}  results:{}", parentName, parentC);
        }
        return parentC;
    }
    
    @Override
    public void validate() {
        super.validate();
        parentId = evaluteParent(parentId, getPersistable().getParent(), parentCollection);
        alternateParentId = evaluteParent(getAlternateParentId(), getPersistable().getAlternateParent(), alternateParentCollection);

    }


    private Long evaluteParent(Long _pid, ResourceCollection _currentParent, ResourceCollection _incomingParent) {
        Long _parentId = _pid;
        if(PersistableUtils.isNotNullOrTransient(_incomingParent)) {
            _parentId = _incomingParent.getId();
        }
        
        // FIXME: this section smells like validation.  Consider overriding validate() and moving it there.
        if (PersistableUtils.isNotNullOrTransient(_incomingParent) && PersistableUtils.isNotNullOrTransient(_currentParent)
                && (_incomingParent.getParentIds().contains(_incomingParent.getId()) || getPersistable().getId().equals(_incomingParent.getId()))) {
            addActionError(getText("collectionController.cannot_set_self_parent"));
        }
        return _parentId;
    }
    @Override
    public void indexPersistable() {
        /*
         * if we want to be really "aggressive" we only need to do this if
         * (a) permissions change
         * (b) visibility changes
         */
        if (isAsync()) {
            searchIndexService.indexAllResourcesInCollectionSubTreeAsync(getPersistable());
        } else {
            searchIndexService.indexAllResourcesInCollectionSubTree(getPersistable());
        }
    }



    public List<SortOption> getSortOptions() {
        return SortOption.getOptionsForResourceCollectionPage();
    }

    public List<DisplayOrientation> getResultsOrientations() {
        List<DisplayOrientation> options = Arrays.asList(DisplayOrientation.values());
        return options;
    }

    @Override
    public List<SortOption> getResourceDatatableSortOptions() {
        return SortOption.getOptionsForContext(Resource.class);
    }

    @Override
    public String loadEditMetadata() throws TdarActionException {
        setOwner(getPersistable().getOwner());
        setupOwnerField();
        prepareDataTableSection();
        setParentId(getPersistable().getParentId());
        if (PersistableUtils.isNotNullOrTransient(getParentId())) {
            parentCollectionName = getPersistable().getParent().getName();
        }
        setAlternateParentId(getPersistable().getAlternateParentId());
        if (PersistableUtils.isNotNullOrTransient(getAlternateParentId())) {
            alternateParentCollectionName = getPersistable().getAlternateParent().getName();
        }
        return SUCCESS;
    }

    @Override
    public String loadAddMetadata() {
        if (PersistableUtils.isNotNullOrTransient(parentId)) {
            ResourceCollection parent = getGenericService().find(ResourceCollection.class,parentId);
            if (parent != null) {
                parentCollectionName =  parent.getName();
            }
        }
        prepareDataTableSection();
        setupOwnerField();
        return SUCCESS;
    }

    @Override
    @SkipValidation
    @Action(value = EDIT, results = {
            @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = INPUT, location = ADD, type = TDAR_REDIRECT)
    })
    public String edit() throws TdarActionException {
        String result = super.edit();
        return result;
    }
    
    

    @Action(value = SAVE,
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TdarActionSupport.REDIRECT, location = SAVE_SUCCESS_PATH),
                    @Result(name = SUCCESS_ASYNC, location = "view-async.ftl"),
                    @Result(name = INPUT, location = "edit.ftl"),
                    @Result(name = RIGHTS, type = TdarActionSupport.REDIRECT,  location = "rights?id=${id}")
            })
    @WriteableSession
    @PostOnly
    @HttpsOnly
    @Override
    /**
     * FIXME: appears to only override the INPUT result type compared to AbstractPersistableController's declaration,
     * see if it's possible to do this with less duplicatiousness
     * 
     * @see org.tdar.struts.action.AbstractPersistableController#save()
     */
    public String save() throws TdarActionException {
        String save2 = super.save();
        if (StringUtils.equals(save2,SUCCESS) && StringUtils.equalsAnyIgnoreCase(getAlternateSubmitAction(),ASSIGN_RIGHTS)) {
            return RIGHTS;
        }
        return save2;

    }

    public List<Long> getSelectedResourceIds() {
        return selectedResourceIds;
    }

    public void setSelectedResourceIds(List<Long> selectedResourceIds) {
        this.selectedResourceIds = selectedResourceIds;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getParentId() {
        return parentId;
    }

    @Override
    public List<Project> getAllSubmittedProjects() {
        return allSubmittedProjects;
    }

    private void prepareDataTableSection() {
        allSubmittedProjects = projectService.findBySubmitter(getAuthenticatedUser());
        Collections.sort(allSubmittedProjects);
        boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
        fullUserProjects = new ArrayList<Resource>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));
        fullUserProjects.removeAll(getAllSubmittedProjects());
        for (ResourceCollection c : resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser())) {
            getAllResourceCollections().add(c);
        }
        // always place current resource collection as the first option
        if (PersistableUtils.isNotTransient(getPersistable())) {
            getAllResourceCollections().remove(getPersistable());
            getAllResourceCollections().add(0, getPersistable());
        }
    }

    public void setFullUserProjects(List<Resource> projects) {
        this.fullUserProjects = projects;
    }

    @Override
    public List<Resource> getFullUserProjects() {
        if (fullUserProjects == null) {
            boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
            fullUserProjects = new ArrayList<Resource>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));
        }
        return fullUserProjects;
    }

    @Override
    public List<Status> getStatuses() {
        return new ArrayList<Status>(authorizationService.getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    @Override
    public List<ResourceType> getResourceTypes() {
        return resourceService.getAllResourceTypes();
    }

    public void setCollections(List<ResourceCollection> findAllChildCollections) {
        getLogger().info("child collections: {}", findAllChildCollections);
        this.collections = findAllChildCollections;
    }

    public List<ResourceCollection> getCollections() {
        return this.collections;
    }

    public String getParentCollectionName() {
        return parentCollectionName;
    }

    public void setParentCollectionName(String parentCollectionName) {
        this.parentCollectionName = parentCollectionName;
    }

    public ArrayList<ResourceType> getSelectedResourceTypes() {
        return selectedResourceTypes;
    }

    public void setSelectedResourceTypes(ArrayList<ResourceType> selectedResourceTypes) {
        this.selectedResourceTypes = selectedResourceTypes;
    }

    private void setupOwnerField() {
        if (PersistableUtils.isNotNullOrTransient(getOwner()) && StringUtils.isNotBlank(getOwner().getProperName())) {
            if (getOwner().getFirstName() != null && getOwner().getLastName() != null)
                setOwnerProperName(getOwner().getProperName());
        } else {
            setOwnerProperName(getAuthenticatedUser().getProperName());
        }
    }
    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public List<ResourceCollection> getAllResourceCollections() {
        return allResourceCollections;
    }

    public void setAllResourceCollections(List<ResourceCollection> allResourceCollections) {
        this.allResourceCollections = allResourceCollections;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileFileName() {
        return fileFileName;
    }

    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public String getOwnerProperName() {
        return ownerProperName;
    }

    public void setOwnerProperName(String ownerProperName) {
        this.ownerProperName = ownerProperName;
    }

    public TdarUser getOwner() {
        return owner;
    }

    public void setOwner(TdarUser owner) {
        this.owner = owner;
    }


    public ResourceCollection getParentCollection() {
        return parentCollection;
    }

    public void setParentCollection(ResourceCollection parentCollection) {
        this.parentCollection = parentCollection;
    }

    
    public ResourceCollection getAlternateParentCollection() {
        return alternateParentCollection;
    }

    public void setAlternateParentCollection(ResourceCollection alternateParentCollection) {
        this.alternateParentCollection = alternateParentCollection;
    }

    public Long getAlternateParentId() {
        return alternateParentId;
    }

    public void setAlternateParentId(Long alternateParentId) {
        this.alternateParentId = alternateParentId;
    }

    public String getAlternateParentCollectionName() {
        return alternateParentCollectionName;
    }

    public void setAlternateParentCollectionName(String alternateParentCollectionName) {
        this.alternateParentCollectionName = alternateParentCollectionName;
    }

}
