package org.tdar.search.dao;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.service.ObfuscationService;
import org.tdar.search.bean.SolrSearchObject;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.utils.PersistableUtils;

@Component
public class ProjectionTransformer<I extends Indexable> {

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DatasetDao datasetDao;

    public boolean isProjected(SolrSearchObject<I> results) {
        if (CollectionUtils.isEmpty(results.getDocumentList())) {
            return true;
        }
        SolrDocument doc = results.getDocumentList().get(0);


        // we only start storing this properly in obsidian & we only project it in resources
        if (doc.getFieldValue(QueryFieldNames.SUBMITTER_ID) != null) {
            return true;
        }
        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public I transformResource(SearchResultHandler<I> resultHandler, SolrDocument doc, I r, ObfuscationService obfuscationService) {
        Resource r_ = (Resource) r;
        // set basic fields
        r_.setStatus(Status.valueOf((String) doc.getFieldValue(QueryFieldNames.STATUS)));
        r_.setTitle((String) doc.getFieldValue(QueryFieldNames.NAME));
        r_.setDescription((String) doc.getFieldValue(QueryFieldNames.DESCRIPTION));

        // set collections
        Collection<Long> collectionIds = (Collection<Long>) (Collection) doc.getFieldValues(QueryFieldNames.RESOURCE_COLLECTION_IDS);
        for (ResourceCollection rc : datasetDao.findAll(ResourceCollection.class, collectionIds)) {
            if(rc instanceof SharedCollection) {
                r_.getSharedCollections().add((SharedCollection) rc);
            }
            if(rc instanceof InternalCollection) {
                r_.getInternalCollections().add((InternalCollection) rc);
            }

        }

        // handle submitter
        Long submitterId = (Long) doc.getFieldValue(QueryFieldNames.SUBMITTER_ID);
        r_.setSubmitter(datasetDao.find(TdarUser.class, submitterId));

        // only display for map
        Collection<Long> llIds = (Collection<Long>) (Collection)doc.getFieldValues(QueryFieldNames.ACTIVE_LATITUDE_LONGITUDE_BOXES_IDS);
        List<LatitudeLongitudeBox> findAll = null;
        DisplayOrientation orientation = resultHandler.getOrientation();
        if (orientation == null) {
            orientation = DisplayOrientation.LIST_FULL;
        }
        if (orientation == DisplayOrientation.MAP) {
            findAll = datasetDao.findAll(LatitudeLongitudeBox.class,llIds);
            r_.getLatitudeLongitudeBoxes().addAll(findAll);
        }

        // creators
        Collection<Long> cIds = (Collection<Long>) (Collection)doc.getFieldValues(QueryFieldNames.RESOURCE_CREATOR_ROLE_IDS);
        logger.trace("{}: creator: {}", r_.getId(), cIds);
        if (orientation == DisplayOrientation.LIST_FULL ) {
            r_.getResourceCreators().addAll(datasetDao.findAll(ResourceCreator.class, cIds));
        }

        if (r_ instanceof InformationResource) {
            // add file info
            InformationResource ir = (InformationResource) r_;

            String fieldValue = (String)doc.getFieldValue(QueryFieldNames.RESOURCE_ACCESS_TYPE);
            if (fieldValue != null) {
                ir.setTransientAccessType(ResourceAccessType.valueOf(fieldValue));
            }
            Collection<Long> fileIds = (Collection<Long>) (Collection)doc.getFieldValues(QueryFieldNames.FILE_IDS);
            if (orientation == DisplayOrientation.GRID || orientation == DisplayOrientation.MAP) {
                ir.getInformationResourceFiles().addAll(datasetDao.findAll(InformationResourceFile.class,fileIds));
            }

            // setup project
            String ptitle = (String) doc.getFieldValue(QueryFieldNames.PROJECT_TITLE);
            ir.setDate((Integer)doc.getFieldValue(QueryFieldNames.DATE));
            if (StringUtils.isNotBlank(ptitle)) {
                Project project = new Project();
                project.setTitle(ptitle);
                project.setId((Long) doc.getFieldValue(QueryFieldNames.PROJECT_ID));
                ir.setProject(project);
            }

            if (ir.isInheritingSpatialInformation()) {
                if (findAll == null) {
                    findAll = datasetDao.findAll(LatitudeLongitudeBox.class,llIds);
                }
                ir.getProject().getLatitudeLongitudeBoxes().addAll(findAll);
            }
        }
        obfuscationService.getAuthenticationAndAuthorizationService().applyTransientViewableFlag(r_,
                resultHandler.getAuthenticatedUser(), collectionIds);
        if (CONFIG.obfuscationInterceptorDisabled()
                && PersistableUtils.isNullOrTransient(resultHandler.getAuthenticatedUser())) {
            obfuscationService.obfuscate((Obfuscatable) r_, resultHandler.getAuthenticatedUser());
        }

        return (I)r_;
    }

}
