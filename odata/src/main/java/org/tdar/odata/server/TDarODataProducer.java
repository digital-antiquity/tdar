package org.tdar.odata.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDateTime;
import org.odata4j.core.OAtomEntity;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OExtension;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.exceptions.ForbiddenException;
import org.odata4j.exceptions.NotAcceptableException;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.CountResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityIdResponse;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ErrorResponseExtension;
import org.odata4j.producer.ErrorResponseExtensions;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.PropertyPathHelper;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.edm.MetadataProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.db.model.abstracts.AbstractDataRecord;
import org.tdar.odata.DataRecord;
import org.tdar.odata.bean.MetaData.EntitySet;
import org.tdar.odata.bean.MetaData.Property;
import org.tdar.odata.service.RepositoryService;
import org.tdar.utils.ResourceCitationFormatter;

/**
 * References:
 * 
 * CustomProducer
 * http://code.google.com/p/odata4j/source/browse/odata4j-fit/src/main/java/org/odata4j/producer/custom/CustomEdm.java?r=76e7612694925
 * a0039a4f128001aeea181163ba3
 * http://code.google.com/p/odata4j/source/browse/odata4j-fit/src/main/java/org/odata4j/producer/custom/CustomProducer.java?r=76e7612694925
 * a0039a4f128001aeea181163ba3
 * 
 * @author Richard Rothwell
 * 
 */

public class TDarODataProducer implements ODataProducer {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private RepositoryService repositoryService;
    private IMetaDataBuilder metaDataBuilder;

    // TODO RR: is this metadata producer implementation correct?
    // http://code.google.com/p/odata4j/source/browse/odata4j-fit/src/main/java/org/odata4j/producer/custom/CustomProducer.java?spec=svn76e7612694925a0039a4f128001aeea181163ba3&r=76e7612694925a0039a4f128001aeea181163ba3
    // private final MetadataProducer metadataProducer;

    public TDarODataProducer() {
        super();
    }

    public TDarODataProducer(RepositoryService repositoryService, IMetaDataBuilder metaDataBuilder) {
        super();
        this.repositoryService = repositoryService;
        this.metaDataBuilder = metaDataBuilder;
        // TODO RR: is this metadata producer implementation correct?
        // this.metadataProducer = new MetadataProducer(this, null);
    }

    /**
     * The metadata is rebuilt on each query instead of being instantiated once.
     * This is because each user will have their own table data entities that change over time.
     * These table data entities have custom properties.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public EdmDataServices getMetadata() {
        return metaDataBuilder.build();
    }

    @Override
    public MetadataProducer getMetadataProducer() {
        // TODO RR: is this metada producer implementation correct?
        // return this.metadataProducer;
        throw new UnsupportedOperationException("getMetadataProducer");
    }

    protected OEntity dataRecordToOEntity(EdmDataServices metadata, EdmEntitySet entitySet, AbstractDataRecord dataRecord) {
        List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
        properties.add(OProperties.int64(Property.ID, dataRecord.getId()));

        for (EdmProperty edmProperty : entitySet.getType().getProperties()) {
            if (!edmProperty.getName().equals(Property.ID))
            {
                OProperty<?> property = OProperties.simple(edmProperty.getName(), dataRecord.get(edmProperty.getName()));
                properties.add(property);
            }
        }

        OEntityKey entityKey = OEntityKey.infer(entitySet, properties);
        return OEntities.create(entitySet, entityKey, properties, Collections.<OLink> emptyList());
    }

    protected OEntity dataTableToOEntity(EdmDataServices metadata, EdmEntitySet entitySet, DataTable dataTable) {

        List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
        properties.add(OProperties.int64(Property.ID, dataTable.getId()));
        properties.add(OProperties.string(Property.NAME, dataTable.getName()));
        OEntityKey entityKey = OEntityKey.infer(entitySet, properties);

        List<OLink> links = new ArrayList<OLink>();
        // Only include links to datarecords at this time. Not associations.
        // Therefore there should be only one navigable property and that is the collection of datarecords.
        // Therefore this loop should execute just once. It's left as a loop so datatable associations can be added later.
        for (EdmNavigationProperty edmNavigationProperty : entitySet.getType().getNavigationProperties()) {
            if (edmNavigationProperty.getToRole().getMultiplicity() == EdmMultiplicity.MANY)
            {
                OLink link = null;
                boolean expanded = false;
                if (expanded)
                {
                    List<OEntity> relatedEntities = new ArrayList<OEntity>();
                    List<AbstractDataRecord> dataRecords = repositoryService.findAllDataRecordsForDataTable(dataTable);
                    for (AbstractDataRecord dataRecord : dataRecords)
                    {
                        // TODO RR: the concrete entity set derived from abstract datarecord has a name
                        // that is the same as the data table.
                        EdmEntitySet relatedEntitySet = metadata.getEdmEntitySet(EntitySet.T_DATA_RECORDS);
                        OEntity relatedEntity = dataRecordToOEntity(metadata, relatedEntitySet, dataRecord);
                        relatedEntities.add(relatedEntity);
                    }
                    link = OLinks.relatedEntitiesInline(null, edmNavigationProperty.getName(), null, relatedEntities);
                }
                else
                {
                    link = OLinks.relatedEntities(null, edmNavigationProperty.getName(), edmNavigationProperty.getName());
                }
                links.add(link);
            }
        }

        // TODO RR: check that these datatable fields are the relevant fields for the Atom feed.
        String summary = dataTable.getDescription() == null ? "None" : dataTable.getDescription();
        String authors = "None (unknown dataset)";
        Date dateUpdated = new Date();
        Dataset dataset = dataTable.getDataset();
        if (dataset != null)
        {
            ResourceCitationFormatter formatter = new ResourceCitationFormatter(dataset);
            authors = formatter.getFormattedAuthorList();
            dateUpdated = dataset.getDateUpdated();
        }
        String displayName = dataTable.getDisplayName() == null ? dataTable.getName() : dataTable.getDisplayName();
        OAtomEntity atomEntity = createAtomEntity(authors, dateUpdated, displayName, summary);
        return OEntities.create(entitySet, entityKey, properties, links, atomEntity);
    }

    // Create a dataset entity with datatables as links/navigable properties.
    protected OEntity dataSetToOEntity(EdmDataServices metadata, EdmEntitySet entitySet, Dataset dataset) {

        // No properties on dataset, only relationships to datatables and datatable associations.
        List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
        properties.add(OProperties.int64(Property.ID, dataset.getId()));
        properties.add(OProperties.string(Property.NAME, dataset.getName()));

        List<OLink> links = new ArrayList<OLink>();
        // Only include links to datatables at this time. Not associations.
        // Therefore there should be only one navigable property and that is the collection of datatables.
        // Therefore this loop should execute just once. It's left as a loop so dataset associations can be added later.
        for (EdmNavigationProperty edmNavigationProperty : entitySet.getType().getNavigationProperties()) {
            if (edmNavigationProperty.getToRole().getMultiplicity() == EdmMultiplicity.MANY)
            {
                OLink link = null;
                boolean expanded = false;
                if (expanded)
                {
                    List<OEntity> relatedEntities = new ArrayList<OEntity>();
                    Set<DataTable> dataTables = dataset.getDataTables();
                    for (DataTable dataTable : dataTables)
                    {
                        EdmEntitySet relatedEntitySet = metadata.getEdmEntitySet(EntitySet.T_DATA_TABLES);
                        OEntity relatedEntity = dataTableToOEntity(metadata, relatedEntitySet, dataTable);
                        relatedEntities.add(relatedEntity);
                    }
                    link = OLinks.relatedEntitiesInline(null, edmNavigationProperty.getName(), null, relatedEntities);
                }
                else
                {
                    link = OLinks.relatedEntities(null, edmNavigationProperty.getName(), edmNavigationProperty.getName());
                }
                links.add(link);
            }
        }

        OEntityKey entityKey = OEntityKey.infer(entitySet, properties);

        // TODO RR: check that these dataset fields are the relevant fields for the Atom feed.
        String description = dataset.getShortenedDescription();
        String summary = description == null ? (dataset.getName() + ": none") : description;
        ResourceCitationFormatter formatter = new ResourceCitationFormatter(dataset);

        OAtomEntity atomEntity = createAtomEntity(formatter.getFormattedAuthorList(), dataset.getDateUpdated(), dataset.getTitle(), summary);
        return OEntities.create(entitySet, entityKey, properties, links, atomEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    // TODO RR: establish transaction here.
    // This will ensure consistency between getMetaData and the actual entity query.
    public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {

        getLogger().info("Begin: getEntities: " + entitySetName);

        // Always recalculate metadata.
        EdmDataServices metadata = getMetadata();

        final EdmEntitySet edmEntitySet = metadata.findEdmEntitySet(entitySetName);
        if (edmEntitySet == null)
        {
            throw new NotFoundException("Unknown entity set: " + entitySetName);
        }

        List<OEntity> oEntities = new ArrayList<OEntity>();

        if (EntitySet.T_DATA_SETS.equals(entitySetName))
        {
            // entitySet is top level representing datasets

            // TODO check if datasets are owned by authenticated user
            // Note: handled by query. If no results are found what should happen?
            // Do nothing, throw a not found exception, throw an access control exception.

            // TODO RR: should have a stable ordering so OData indexing will be repeatable.
            // Note: decided to use indexing by name instead of id so this is no longer relevant.
            List<Dataset> dataSets = repositoryService.findAllOwnedDatasets();
            for (Dataset dataSet : dataSets)
            {
                OEntity entity = dataSetToOEntity(metadata, edmEntitySet, dataSet);
                oEntities.add(entity);
            }
        }
        else if (EntitySet.T_DATA_TABLES.equals(entitySetName))
        {
            Collection<DataTable> dataTables = repositoryService.findAllOwnedDataTables();
            for (DataTable dataTable : dataTables)
            {
                OEntity entity = dataTableToOEntity(metadata, edmEntitySet, dataTable);
                oEntities.add(entity);
            }
        }
        else if (EntitySet.T_DATA_RECORDS.equals(entitySetName))
        {
            throw new NotAcceptableException("An abstract entity set is not viewable: " + entitySetName);
        }
        else
        {
            DataTable dataTable = repositoryService.findOwnedDataTableByName(entitySetName);
            List<AbstractDataRecord> dataRecords = repositoryService.findAllDataRecordsForDataTable(dataTable);
            for (AbstractDataRecord dataRecord : dataRecords)
            {
                OEntity entity = dataRecordToOEntity(metadata, edmEntitySet, dataRecord);
                oEntities.add(entity);
            }
        }

        // TODO RR: check the semantics of this. Maybe just set it to null is good enough.
        String skipToken = queryInfo.skipToken;

        // TODO RR: check the semantics of this. Maybe just set it to null is good enough.
        Integer inlineCount = queryInfo.inlineCount == InlineCount.ALLPAGES ? oEntities.size() : null;

        EntitiesResponse entities = Responses.entities(oEntities, edmEntitySet, inlineCount, skipToken);

        getLogger().info("End: getEntities: " + entitySetName);

        return entities;
    }

    private OAtomEntity createAtomEntity(final String author, final Date updatedDate, final String title, final String summary)
    {
        OAtomEntity oAtomEntity = new OAtomEntity() {

            @Override
            public String getAtomEntityTitle() {
                return title;
            }

            @Override
            public String getAtomEntitySummary() {
                return summary;
            }

            @Override
            public String getAtomEntityAuthor() {
                return author;
            }

            @Override
            public LocalDateTime getAtomEntityUpdated() {
                return new LocalDateTime(updatedDate);
            }
        };
        return oAtomEntity;
    }

    @Override
    public CountResponse getEntitiesCount(String entitySetName, QueryInfo queryInfo) {
        throw new UnsupportedOperationException("getEntitiesCount");
    }

    @Override
    public EntityResponse getEntity(String entitySetName, OEntityKey entityKey, EntityQueryInfo queryInfo) {
        throw new UnsupportedOperationException("getEntity");
    }

    @Override
    public BaseResponse getNavProperty(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
        throw new UnsupportedOperationException("getNavProperty");
    }

    @Override
    public CountResponse getNavPropertyCount(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
        throw new UnsupportedOperationException("getNavPropertyCount");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("close");
    }

    @Override
    public EntityResponse createEntity(String entitySetName, OEntity entity) {
        throw new UnsupportedOperationException("createEntity");
    }

    @Override
    public EntityResponse createEntity(String entitySetName, OEntityKey entityKey, String navProp, OEntity entity) {
        throw new UnsupportedOperationException("createEntity");
    }

    @Override
    public void deleteEntity(String entitySetName, OEntityKey entityKey) {
        throw new UnsupportedOperationException("deleteEntity");
    }

    @Override
    public void mergeEntity(String entitySetName, OEntity entity) {
        throw new UnsupportedOperationException("mergeEntity");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    // TODO RR: establish transaction here.
    // This will ensure consistency between getMetaData and the actual entity query.
    // It will also ensure data integrity if the update fails
    public void updateEntity(String entitySetName, OEntity oEntity) {

        if (EntitySet.T_DATA_SETS.equals(entitySetName))
        {
            throw new ForbiddenException("The entity dataset is not allowed to be modified.");
        }
        else if (EntitySet.T_DATA_TABLES.equals(entitySetName))
        {
            throw new ForbiddenException("The entity datatable is not allowed to be modified.");
        }
        else if (EntitySet.T_DATA_RECORDS.equals(entitySetName))
        {
            // This should never happen.
            throw new NotFoundException("An abstract entity set is not updateable: " + entitySetName);
        }
        else
        {
            // Always recalculate metadata.
            EdmDataServices metadata = getMetadata();
            final EdmEntitySet edmEntitySet = metadata.findEdmEntitySet(entitySetName);
            if (edmEntitySet == null)
            {
                throw new NotFoundException("Unknown entity set: " + entitySetName);
            }
            // TODO RR: add check to verify that the entity inherits from EntitySet.T_DATA_RECORDS
            AbstractDataRecord dataRecord = populateDataRecord(oEntity);
            repositoryService.updateRecord(dataRecord);
        }
    }

    protected AbstractDataRecord populateDataRecord(OEntity oEntity) {

        // DataTable name
        String entitySetName = oEntity.getEntitySetName();
        DataTable dataTable = repositoryService.findOwnedDataTableByName(entitySetName);

        // Record id.
        OEntityKey oEntityKey = oEntity.getEntityKey();
        String key = (String) oEntityKey.asSingleValue();
        OProperty<?> keyValue = oEntity.getProperty(key);
        Long id = (Long) keyValue.getValue();

        // Record fields other than id.
        List<OProperty<?>> oProperties = oEntity.getProperties();

        // Create the value object we need.
        AbstractDataRecord dataRecord = createDataRecord(id, oProperties, dataTable);
        return dataRecord;
    }

    protected AbstractDataRecord createDataRecord(Long id, List<OProperty<?>> oProperties, DataTable dataTable) {
        final DataRecord dataRecord = new DataRecord(id, dataTable);
        for (OProperty<?> oProperty : oProperties)
        {
            String name = oProperty.getName();
            Object value = oProperty.getValue();
            dataRecord.put(name, value);
        }
        return dataRecord;
    };

    @Override
    public EntityIdResponse getLinks(OEntityId sourceEntity, String targetNavProp) {
        throw new UnsupportedOperationException("getLinks");
    }

    @Override
    public void createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
        throw new UnsupportedOperationException("createLink");
    }

    @Override
    public void updateLink(OEntityId sourceEntity, String targetNavProp, OEntityKey oldTargetEntityKey, OEntityId newTargetEntity) {
        throw new UnsupportedOperationException("updateLink");
    }

    @Override
    public void deleteLink(OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey) {
        throw new UnsupportedOperationException("deleteLink");
    }

    @Override
    public BaseResponse callFunction(EdmFunctionImport name, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
        throw new UnsupportedOperationException("callFunction");
    }

    public Object getNameSpace() {
        return this.metaDataBuilder.getNameSpace();
    }

    @Override
    public <TExtension extends OExtension<ODataProducer>> TExtension findExtension(Class<TExtension> clazz) {

        TExtension extension = null;
        if (clazz.equals(ErrorResponseExtension.class))
        {
            extension = clazz.cast(ErrorResponseExtensions.returnInnerErrors());
        }
        return extension;
    }

    // May need later to support the full query syntax.
    // TODO RR: Keeping things simple for now. Use later.
    @SuppressWarnings("unused")
    private boolean isExpanded(String navigablePropertyName, QueryInfo queryInfo) {
        if ((null == queryInfo) || (null == queryInfo.expand)) {
            return false;
        }
        PropertyPathHelper h = new PropertyPathHelper(queryInfo.select, queryInfo.expand);
        return h.isExpanded(navigablePropertyName);
    }

    private Logger getLogger() {
        return logger;
    }

}
