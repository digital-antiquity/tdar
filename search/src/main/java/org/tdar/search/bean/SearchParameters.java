package org.tdar.search.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.IntegratableOptions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.search.index.LookupSource;
import org.tdar.search.index.analyzer.SiteCodeExtractor;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.part.AnnotationQueryPart;
import org.tdar.search.query.part.ContentQueryPart;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.GeneralSearchResourceQueryPart;
import org.tdar.search.query.part.HydrateableKeywordQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.search.query.part.RangeQueryPart;
import org.tdar.search.query.part.SkeletonPersistableQueryPart;
import org.tdar.search.query.part.SpatialQueryPart;
import org.tdar.search.query.part.TitleQueryPart;
import org.tdar.search.query.part.entity.CreatorOwnerQueryPart;
import org.tdar.search.query.part.entity.CreatorQueryPart;
import org.tdar.search.query.part.resource.TemporalQueryPart;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.StringPair;
import org.tdar.utils.range.DateRange;
import org.tdar.utils.range.StringRange;

import com.opensymphony.xwork2.TextProvider;

/**
 * This class is meant to capture a group of search terms.
 * 
 * 
 * @author jimdevos
 * 
 */
public class SearchParameters {

    public SearchParameters() {
    }

    public SearchParameters(Operator operator) {
        setOperator(operator);
    }

    
    private List<String> filters = new ArrayList<>();
    private boolean explore = false;
    // user specified status that they do not have permissions to search for. probably because they are not logged in.

    private static final Operator defaultOperator = Operator.AND;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private List<SearchFieldType> fieldTypes = new ArrayList<SearchFieldType>();
    private Operator operator = defaultOperator;

    // managed keywords. Ultimately we need list of list of id, but struts only knows how to construct list of list of strings
    private List<List<String>> materialKeywordIdLists = new ArrayList<>();
    private List<List<String>> approvedSiteTypeIdLists = new ArrayList<>();
    private List<List<String>> approvedCultureKeywordIdLists = new ArrayList<>();
    private List<List<String>> investigationTypeIdLists = new ArrayList<>();

    // freeform keywords
    private List<String> otherKeywords = new ArrayList<>();
    private List<String> siteNames = new ArrayList<>();
    private List<String> uncontrolledCultureKeywords = new ArrayList<>();
    private List<String> uncontrolledMaterialKeywords = new ArrayList<>();
    private List<String> temporalKeywords = new ArrayList<>();
    private List<String> geographicKeywords = new ArrayList<>();
    private List<String> uncontrolledSiteTypes = new ArrayList<>();
    private boolean latScaleUsed = true;
    private List<String> allFields = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    private List<String> descriptions = new ArrayList<>();
    private List<String> contents = new ArrayList<>();
    private List<String> filenames = new ArrayList<>();

    private boolean join = false;
    private ResourceCreatorProxy creatorOwner;
    private Set<ResourceCreatorRole> creatorOwnerRoles = new HashSet<>();
    private List<ResourceCreatorProxy> resourceCreatorProxies = new ArrayList<>();
    // private List<String> creatorRoleIdentifiers = new ArrayList<String>();

    private List<Resource> sparseProjects = new ArrayList<Resource>();
    private List<ResourceCollection> collections = new ArrayList<>();

    private List<Long> resourceIds = new ArrayList<Long>();

    private List<DateRange> registeredDates = new ArrayList<>();
    private List<DateRange> updatedDates = new ArrayList<>();
    private List<CoverageDate> coverageDates = new ArrayList<>();
    private List<StringRange> createdDates = new ArrayList<>();
    private List<StringPair> annotations = new ArrayList<>();
    private List<Integer> creationDecades = new ArrayList<>();

    // parameters. don't render these in the form view.
    private String startingLetter;

    // reserved terms
    // We don't allow users (at the moment) to have total control over certain fields: maps, resourceTypes, statuses, and documentTypes
    // Instead, we use a single searchParameters instance to hold these terms, which will be populated via the "narrow your search" section
    // as well by selecting a faceted search.
    private List<LatitudeLongitudeBox> latitudeLongitudeBoxes = new ArrayList<LatitudeLongitudeBox>();
    private List<ResourceType> resourceTypes = new ArrayList<>();
    private List<ObjectType> objectTypes = new ArrayList<>();
    private List<CollectionResourceSection> collectionTypes = new ArrayList<CollectionResourceSection>();
    private List<LookupSource> types = new ArrayList<>();
    private List<IntegratableOptions> integratableOptions = new ArrayList<IntegratableOptions>();
    private List<DocumentType> documentTypes = new ArrayList<DocumentType>();
    private List<ResourceAccessType> resourceAccessTypes = new ArrayList<ResourceAccessType>();

    public List<SearchFieldType> getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(List<SearchFieldType> fieldTypes) {
        this.fieldTypes = fieldTypes;
    }

    public Operator getOperator() {
        if (operator == null) {
            operator = defaultOperator;
        }
        return operator;
    }

    public boolean isOr() {
        return getOperator() == Operator.OR;
    }

    public void setOperator(Operator operator) {
        if (operator != null) {
            this.operator = operator;
        }
    }

    public List<List<String>> getMaterialKeywordIdLists() {
        return materialKeywordIdLists;
    }

    public void setMaterialKeywordIdLists(List<List<String>> materialKeywordIdLists) {
        this.materialKeywordIdLists = materialKeywordIdLists;
    }

    public List<List<String>> getApprovedSiteTypeIdLists() {
        return approvedSiteTypeIdLists;
    }

    public void setApprovedSiteTypeIdLists(List<List<String>> approvedSiteTypeIdLists) {
        this.approvedSiteTypeIdLists = approvedSiteTypeIdLists;
    }

    public List<List<String>> getApprovedCultureKeywordIdLists() {
        return approvedCultureKeywordIdLists;
    }

    public void setApprovedCultureKeywordIdLists(List<List<String>> approvedCultureKeywordIdLists) {
        this.approvedCultureKeywordIdLists = approvedCultureKeywordIdLists;
    }

    public List<String> getAllFields() {
        return allFields;
    }

    public void setAllFields(List<String> allFields) {
        this.allFields = allFields;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public void setResourceTypes(List<ResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }
    
    public List<ResourceType> getResourceTypes() {
        return resourceTypes;
    }

    public List<String> getOtherKeywords() {
        return otherKeywords;
    }

    public void setOtherKeywords(List<String> otherKeywords) {
        this.otherKeywords = otherKeywords;
    }

    public List<String> getSiteNames() {
        return siteNames;
    }

    public void setSiteNames(List<String> siteNames) {
        this.siteNames = siteNames;
    }

    public List<String> getUncontrolledCultureKeywords() {
        return uncontrolledCultureKeywords;
    }

    public void setUncontrolledCultureKeywords(List<String> uncontrolledCultureKeywords) {
        this.uncontrolledCultureKeywords = uncontrolledCultureKeywords;
    }

    public List<String> getTemporalKeywords() {
        return temporalKeywords;
    }

    public void setTemporalKeywords(List<String> temporalKeywords) {
        this.temporalKeywords = temporalKeywords;
    }

    public List<String> getGeographicKeywords() {
        return geographicKeywords;
    }

    public void setGeographicKeywords(List<String> geographicKeywords) {
        this.geographicKeywords = geographicKeywords;
    }

    public List<String> getUncontrolledSiteTypes() {
        return uncontrolledSiteTypes;
    }

    public void setUncontrolledSiteTypes(List<String> uncontrolledSiteTypes) {
        this.uncontrolledSiteTypes = uncontrolledSiteTypes;
    }

    public List<LatitudeLongitudeBox> getLatitudeLongitudeBoxes() {
        return latitudeLongitudeBoxes;
    }

    public void setLatitudeLongitudeBoxes(List<LatitudeLongitudeBox> latitudeLongitudeBoxs) {
        this.latitudeLongitudeBoxes = latitudeLongitudeBoxs;
    }

    public List<ResourceCreatorProxy> getResourceCreatorProxies() {
        return resourceCreatorProxies;
    }

    public void setResourceCreatorProxies(List<ResourceCreatorProxy> resourceCreatorInstitutions) {
        this.resourceCreatorProxies = resourceCreatorInstitutions;
    }

    public List<List<String>> getInvestigationTypeIdLists() {
        return investigationTypeIdLists;
    }

    public void setInvestigationTypeIdLists(List<List<String>> investigationTypeIdLists) {
        this.investigationTypeIdLists = investigationTypeIdLists;
    }

    public List<DateRange> getRegisteredDates() {
        return registeredDates;
    }

    public void setRegisteredDates(List<DateRange> registeredDates) {
        this.registeredDates = registeredDates;
    }

    public List<DateRange> getUpdatedDates() {
        return updatedDates;
    }

    public void setUpdatedDates(List<DateRange> updatedDates) {
        this.updatedDates = updatedDates;
    }

    public List<CoverageDate> getCoverageDates() {
        return coverageDates;
    }

    public void setCoverageDates(List<CoverageDate> coverageDates) {
        this.coverageDates = coverageDates;
    }

    public List<Long> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<Long> resourceIds) {
        this.resourceIds = resourceIds;
    }

    TextProvider support = MessageHelper.getInstance();
    private List<String> actionMessages = new ArrayList<>();

    // FIXME: where appropriate need to make sure we pass along the operator to any sub queryPart groups
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public QueryPartGroup toQueryPartGroup(TextProvider support_) {
        if (support_ != null) {
            this.support = support_;
        }
        QueryPartGroup queryPartGroup = new QueryPartGroup(getOperator());
        queryPartGroup.append(new FieldQueryPart<Long>(QueryFieldNames.ID, support.getText("searchParameter.id"), Operator.OR, getResourceIds()));
        if (CollectionUtils.isNotEmpty(getTypes())) {
            queryPartGroup.append(new FieldQueryPart<LookupSource>(QueryFieldNames.GENERAL_TYPE,support_.getText("searchParameter.general_type"), Operator.OR, getTypes()));
        }
        if (CollectionUtils.isNotEmpty(getCollectionTypes())) {
            queryPartGroup.append(new FieldQueryPart<CollectionResourceSection>(QueryFieldNames.COLLECTION_TYPE, support_.getText("searchParameter.collection_type"), Operator.OR, getCollectionTypes()));
        }
        queryPartGroup.append(new GeneralSearchResourceQueryPart(this.getAllFields(), getOperator()));
        queryPartGroup.append(new TitleQueryPart(this.getTitles(), getOperator()));
        queryPartGroup.append(new FieldQueryPart<String>(QueryFieldNames.DESCRIPTION, support.getText("searchParameters.description"), getOperator(), this.getDescriptions()));

        queryPartGroup.append(new ContentQueryPart(support.getText("searchParameter.file_contents"), getOperator(),contents));
        FieldQueryPart<String> filenamePart = new FieldQueryPart<String>(QueryFieldNames.FILENAME, support.getText("searchParameter.file_name"), getOperator(), filenames);
        filenamePart.setPhraseFormatters(Arrays.asList(PhraseFormatter.ESCAPE_QUOTED));
        queryPartGroup.append(filenamePart);

        if (creatorOwner != null) {
            if (CollectionUtils.isNotEmpty(getCreatorOwnerRoles())) {
                setCreatorOwnerRoles(ResourceCreatorRole.getResourceCreatorRolesForProfilePage(creatorOwner.getActualCreatorType()));
            }
            if (PersistableUtils.isNotNullOrTransient(creatorOwner.getPerson())) {
                queryPartGroup.append(new CreatorOwnerQueryPart(creatorOwner.getPerson(), getCreatorOwnerRoles()));
            }
            if (PersistableUtils.isNotNullOrTransient(creatorOwner.getInstitution())) {
                queryPartGroup.append(new CreatorOwnerQueryPart(creatorOwner.getInstitution(), getCreatorOwnerRoles()));
            }
        }

        // freeform keywords
        appendKeywordQueryParts(queryPartGroup, KeywordType.OTHER_KEYWORD, Arrays.asList(this.getOtherKeywords()));
        if (CollectionUtils.isNotEmpty(siteNames)) {
            QueryPartGroup subgroup = new QueryPartGroup(Operator.OR);
            for (String q : siteNames) {
                if (StringUtils.isNotBlank(q) && SiteCodeExtractor.matches(q)) {
                    FieldQueryPart<String> siteCodePart = new FieldQueryPart<String>(QueryFieldNames.SITE_CODE, q);
                    siteCodePart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
                    siteCodePart.setDisplayName(support.getText("searchParameters.site_code"));
                    subgroup.append(siteCodePart.setBoost(5f));
                }
            }
            appendKeywordQueryParts(subgroup, KeywordType.SITE_NAME_KEYWORD, Arrays.asList(siteNames));
            queryPartGroup.append(subgroup);
        }

        appendKeywordQueryParts(queryPartGroup, KeywordType.CULTURE_KEYWORD, Arrays.asList(this.getUncontrolledCultureKeywords()));
        appendKeywordQueryParts(queryPartGroup, KeywordType.MATERIAL_TYPE, Arrays.asList(this.getUncontrolledMaterialKeywords()));
        appendKeywordQueryParts(queryPartGroup, KeywordType.TEMPORAL_KEYWORD, Arrays.asList(this.getTemporalKeywords()));
        appendKeywordQueryParts(queryPartGroup, KeywordType.GEOGRAPHIC_KEYWORD, Arrays.asList(this.getGeographicKeywords()));
        appendKeywordQueryParts(queryPartGroup, KeywordType.SITE_TYPE_KEYWORD, Arrays.asList(this.getUncontrolledSiteTypes()));

        // managed keywords (in the form of lists of lists of ids)
        appendKeywordQueryParts(queryPartGroup, KeywordType.MATERIAL_TYPE, this.getMaterialKeywordIdLists());
        appendKeywordQueryParts(queryPartGroup, KeywordType.SITE_TYPE_KEYWORD, this.getApprovedSiteTypeIdLists());
        appendKeywordQueryParts(queryPartGroup, KeywordType.INVESTIGATION_TYPE, this.getInvestigationTypeIdLists());
        appendKeywordQueryParts(queryPartGroup, KeywordType.CULTURE_KEYWORD, this.getApprovedCultureKeywordIdLists());

        queryPartGroup.append(constructSkeletonQueryPart(QueryFieldNames.PROJECT_ID, support.getText("searchParameter.project"), "project.", Resource.class,
                getOperator(), getProjects()));

        for (ResourceType rt : getResourceTypes()) {
            if (rt != null) {
                getObjectTypes().add(ObjectType.from(rt));
            }
        }

        appendFieldQueryPart(queryPartGroup, QueryFieldNames.OBJECT_TYPE, support.getText("searchParameter.object_type"), getObjectTypes(), Operator.OR,
                Arrays.asList(ObjectType.values()));
        appendFieldQueryPart(queryPartGroup, QueryFieldNames.INTEGRATABLE, support.getText("searchParameter.integratable"), getIntegratableOptions(),
                Operator.OR,
                Arrays.asList(IntegratableOptions.values()));

        queryPartGroup.append(new FieldQueryPart<DocumentType>(QueryFieldNames.DOCUMENT_TYPE, support.getText("searchParameter.document_type"), Operator.OR,
                getDocumentTypes()));
        queryPartGroup.append(new FieldQueryPart<ResourceAccessType>(QueryFieldNames.RESOURCE_ACCESS_TYPE, support
                .getText("searchParameter.resource_access_type"), Operator.OR,
                getResourceAccessTypes()));

        queryPartGroup.append(new RangeQueryPart(QueryFieldNames.DATE_CREATED, support.getText("searchParameter.date_created"), getOperator(),
                getRegisteredDates()));
        queryPartGroup.append(new RangeQueryPart(QueryFieldNames.DATE_UPDATED, support.getText("searchParameter.date_updated"), getOperator(),
                getUpdatedDates()));
        queryPartGroup.append(new RangeQueryPart(QueryFieldNames.DATE, support.getText("searchParameter.date"), getOperator(), getCreatedDates()));

        queryPartGroup.append(new AnnotationQueryPart(QueryFieldNames.RESOURCE_ANNOTATION, support.getText("searchParameter.annotation"), getOperator(), getAnnotations()));

        queryPartGroup.append(new TemporalQueryPart(getCoverageDates(), getOperator()));
        SpatialQueryPart spatialQueryPart = new SpatialQueryPart(getLatitudeLongitudeBoxes());
        if (!latScaleUsed) {
            spatialQueryPart.ignoreScale(true);
        }
//        getFilters().add(spatialQueryPart.getFilter());
        queryPartGroup.append(spatialQueryPart);
        // NOTE: I AM "SHARED" the autocomplete will supply the "public"

        queryPartGroup.append(constructSkeletonQueryPart(QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS,
                support.getText("searchParameter.resource_collection"), "resourceCollections.",
                ResourceCollection.class, getOperator(), getCollections()));
        CreatorQueryPart<Creator> cqp = new CreatorQueryPart<>(QueryFieldNames.CREATOR_ROLE_IDENTIFIER, Creator.class, null, resourceCreatorProxies);
        getActionMessages().addAll(cqp.getActionMessages());
        queryPartGroup.append(cqp);

        // explore: decade
        queryPartGroup.append(new FieldQueryPart<>(QueryFieldNames.DATE_CREATED_DECADE, Operator.OR, getCreationDecades()));

        // explore: title starts with
        if (startingLetter != null) {
            FieldQueryPart<String> part = new FieldQueryPart<String>(QueryFieldNames.NAME_SORT, startingLetter.toLowerCase());
            part.setDisplayName(support.getText("searchParameter.title_starts_with", Arrays.asList(startingLetter)));
            part.setPhraseFormatters(PhraseFormatter.WILDCARD);
            queryPartGroup.append(part);
        }

        return queryPartGroup;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <P extends Persistable> SkeletonPersistableQueryPart constructSkeletonQueryPart(String fieldName, String label, String prefix, Class<P> cls,
            Operator operator, List<P> values) {
        if (CollectionUtils.isEmpty(values)){
            return null;
        }
        SkeletonPersistableQueryPart q = new SkeletonPersistableQueryPart(fieldName, label, cls, values);
        logger.debug("{} {} {} ", cls, prefix, values);
        if ((HasName.class.isAssignableFrom(cls) || ResourceCollection.class.isAssignableFrom(cls)) && StringUtils.isNotBlank(prefix)) {
            TitleQueryPart tqp = new TitleQueryPart();
            tqp.setPrefix(prefix);
            for (Persistable p : values) {
                HasName name = (HasName) p;
                if ((name != null) && StringUtils.isNotBlank(name.getName())) {
                    tqp.add(name.getName());
                }
            }
            q.setTransientFieldQueryPart(tqp);
            setOperator(operator);
        }
        return q;
    }

    protected <C> void appendFieldQueryPart(QueryPartGroup queryPartGroup, String fieldName, String fieldDisplayName, List<C> incomingList, Operator operator,
            List<C> fullList) {
        Set<C> emptyCheck = new HashSet<C>(fullList);
        for (C item : incomingList) {
            emptyCheck.remove(item);
        }
        if (emptyCheck.isEmpty()) {
            return;
        }
        queryPartGroup.append(new FieldQueryPart<C>(fieldName, fieldDisplayName, operator, incomingList));
    }

    protected <K extends Keyword> void appendKeywordQueryParts(QueryPartGroup group, KeywordType type, List<List<String>> idListList) {
        for (List<String> strings : idListList) {
            if (CollectionUtils.isNotEmpty(strings)) {
                group.append(createKeywordQueryPart(type, strings));
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <I, K extends Keyword> HydrateableKeywordQueryPart createKeywordQueryPart(KeywordType type, List<I> values) {
        List<K> kwdValues = new ArrayList<K>();
        for (I value : values) {
            if (value == null) {
                continue;
            }
            try {
                K keyword = (K) type.getKeywordClass().newInstance();
                if (value instanceof Keyword) {
                    keyword = (K) value;
                } else if (StringUtils.isNotBlank(value.toString()) && StringUtils.isNumeric(value.toString())) {
                    Long id = Long.valueOf(value.toString());
                    if ((id != null) && (id > -1)) {
                        keyword.setId(id);
                    }
                } else {
                    keyword.setLabel(value.toString());
                }
                logger.trace("kwd: {}", keyword);
                kwdValues.add(keyword);
            } catch (Exception e) {
                throw new TdarRecoverableRuntimeException(e);
            }
        }
        HydrateableKeywordQueryPart hydrateableKeywordQueryPart = new HydrateableKeywordQueryPart(type, kwdValues);
        hydrateableKeywordQueryPart.setIncludeChildren(!explore);
        hydrateableKeywordQueryPart.setDisplayName(support.getText(type.getLocaleKey()));
        return hydrateableKeywordQueryPart;
    }

    public List<DocumentType> getDocumentTypes() {
        return documentTypes;
    }

    public List<ResourceAccessType> getResourceAccessTypes() {
        return resourceAccessTypes;
    }

    // convenience getter for freemarker
    public boolean isMatchingAnyTerms() {
        return getOperator() == Operator.OR;
    }

    // convenience getter for freemarker
    public boolean isMatchingAllTerms() {
        return getOperator() == Operator.AND;
    }

    @Override
    public String toString() {
        try {
            return toQueryPartGroup(null).toString();
        } catch (Exception e) {
            logger.error("error in toString()", e);
            return super.toString();
        }
    }

    public List<StringRange> getCreatedDates() {
        return createdDates;
    }

    public void setCreatedDates(List<StringRange> createdDates) {
        this.createdDates = createdDates;
    }

    public List<Integer> getCreationDecades() {
        return creationDecades;
    }

    public void setCreationDecades(List<Integer> creationDecades) {
        this.creationDecades = creationDecades;
    }

    public String getStartingLetter() {
        return startingLetter;
    }

    public void setStartingLetter(String startingLetter) {
        this.startingLetter = startingLetter;
    }

    public List<Resource> getProjects() {
        return sparseProjects;
    }

    public void setProjects(List<Resource> projects) {
        sparseProjects = projects;
    }

    public List<ResourceCollection> getCollections() {
        return collections;
    }

    public void setCollections(List<ResourceCollection> resourceCollections) {
        collections = resourceCollections;
    }

    public List<ResourceCollection> getShares() {
        return collections;
    }

    public List<String> getContents() {
        return contents;
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
    }

    public boolean isExplore() {
        return explore;
    }

    public void setExplore(boolean explore) {
        this.explore = explore;
    }

    public List<IntegratableOptions> getIntegratableOptions() {
        return integratableOptions;
    }

    public void setIntegratableOptions(List<IntegratableOptions> integratableOptions) {
        this.integratableOptions = integratableOptions;
    }

    public List<String> getFilenames() {
        return filenames;
    }

    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }

    public List<List<? extends Persistable>> getSparseLists() {
        List<List<? extends Persistable>> lists = new ArrayList<List<? extends Persistable>>();
        lists.add(sparseProjects);
        lists.add(collections);
        return lists;
    }

    public ResourceCreatorProxy getCreatorOwner() {
        return creatorOwner;
    }

    public void setCreatorOwner(ResourceCreatorProxy creatorOwner) {
        this.creatorOwner = creatorOwner;
    }

    public List<String> getUncontrolledMaterialKeywords() {
        return uncontrolledMaterialKeywords;
    }

    public void setUncontrolledMaterialKeywords(List<String> uncontrolledMaterialKeywords) {
        this.uncontrolledMaterialKeywords = uncontrolledMaterialKeywords;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public boolean isJoin() {
        return join;
    }

    public void setJoin(boolean join) {
        this.join = join;
    }

    public boolean isLatScaleUsed() {
        return latScaleUsed;
    }

    public void setLatScaleUsed(boolean latScaleUsed) {
        this.latScaleUsed = latScaleUsed;
    }

    public Set<ResourceCreatorRole> getCreatorOwnerRoles() {
        return creatorOwnerRoles;
    }

    public void setCreatorOwnerRoles(Set<ResourceCreatorRole> creatorOwnerRoles) {
        this.creatorOwnerRoles = creatorOwnerRoles;
    }

    public List<LookupSource> getTypes() {
        return types;
    }

    public void setTypes(List<LookupSource> types) {
        this.types = types;
    }

    public List<CollectionResourceSection> getCollectionTypes() {
        return collectionTypes;
    }

    public void setCollectionTypes(List<CollectionResourceSection> collectionTypes) {
        this.collectionTypes = collectionTypes;
    }

    public List<ObjectType> getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(List<ObjectType> objectTypes) {
        this.objectTypes = objectTypes;
    }

    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    public List<StringPair> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<StringPair> annotations) {
        this.annotations = annotations;
    }

    public List<String> getActionMessages() {
        return actionMessages;
    }

    public void setActionMessages(List<String> actionMessages) {
        this.actionMessages = actionMessages;
    }

}
