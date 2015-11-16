package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.geotools.geometry.jts.JTS;
import org.tdar.core.bean.SupportsResource;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Dataset.IntegratableOptions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.index.bridge.GeneralKeywordBuilder;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.DataUtil;
import org.tdar.utils.PersistableUtils;

import com.rometools.modules.activitystreams.types.Bookmark;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.WKTWriter;

public class ResourceDocumentConverter extends AbstractSolrDocumentConverter {

    public static SolrInputDocument convert(Resource resource, ResourceService resourceService, ResourceCollectionService resourceCollectionService) {

        SolrInputDocument doc = convertPersistable(resource);
        doc.setField(QueryFieldNames.NAME, resource.getName());
        doc.setField(QueryFieldNames.NAME_SORT, resource.getTitleSort());
        doc.setField(QueryFieldNames.NAME_AUTOCOMPLETE, resource.getName());
        doc.setField(QueryFieldNames.RESOURCE_TYPE, resource.getResourceType().name());
        doc.setField(QueryFieldNames.RESOURCE_TYPE_SORT, resource.getResourceType().getSortName());
        doc.setField(QueryFieldNames.SUBMITTER_ID, resource.getSubmitter().getId());
        doc.setField(QueryFieldNames.DESCRIPTION, resource.getDescription());
        doc.setField(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, resource.getUsersWhoCanModify());
        doc.setField(QueryFieldNames.RESOURCE_USERS_WHO_CAN_VIEW, resource.getUsersWhoCanView());

        indexCreatorInformation(doc, resource);
        indexCollectionInformation(doc, resource);
        indexTemporalInformation(doc, resource);
        Map<DataTableColumn, String> data = null;
        if (resource instanceof InformationResource) {
            InformationResource ir = (InformationResource) resource;
            doc.setField(QueryFieldNames.PROJECT_ID, ir.getProjectId());
            doc.setField(QueryFieldNames.PROJECT_TITLE, ir.getProjectTitle());
            doc.setField(QueryFieldNames.PROJECT_TITLE_SORT, ir.getProjectTitleSort());
            doc.setField(QueryFieldNames.DATE, ir.getDate());
            doc.setField(QueryFieldNames.DATE_CREATED_DECADE, ir.getDateNormalized());

            data = resourceService.getMappedDataForInformationResource(ir);
            indexTdarDataDatabaseValues(doc, data);
            if (ir.getMetadataLanguage() != null) {
                doc.setField(QueryFieldNames.METADATA_LANGUAGE, ir.getMetadataLanguage().name());
            }
            if (ir.getResourceLanguage() != null) {
                doc.setField(QueryFieldNames.RESOURCE_LANGUAGE, ir.getResourceLanguage().name());
            }

            Set<String> filenames = new HashSet<>();
            for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                filenames.add(irf.getFilename());
            }
            doc.setField(QueryFieldNames.FILENAME, filenames);
            doc.setField(QueryFieldNames.DOI, ir.getDoi());
            if (PersistableUtils.isNotNullOrTransient(ir.getResourceProviderInstitution())) {
                doc.setField(QueryFieldNames.RESOURCE_PROVIDER_ID, ir.getResourceProviderInstitution().getId());
            }

            doc.setField(QueryFieldNames.RESOURCE_ACCESS_TYPE, ir.getResourceAccessType().name());
            // getContent

        }

        if (resource instanceof Document) {
            Document doc_ = (Document) resource;
            doc.setField(QueryFieldNames.DOCUMENT_TYPE, doc_.getDocumentType().name());
            if (doc_.getDocumentSubType() != null) {
                doc.setField(QueryFieldNames.DOCUMENT_SUB_TYPE, doc_.getDocumentSubType().name());
            }
            doc.setField(QueryFieldNames.DEGREE, doc_.getDegree());
            doc.setField(QueryFieldNames.SERIES_NAME, doc_.getSeriesName());
            doc.setField(QueryFieldNames.BOOK_TITLE, doc_.getBookTitle());
            doc.setField(QueryFieldNames.JOURNAL_NAME, doc_.getJournalName());
            doc.setField(QueryFieldNames.ISBN, doc_.getIsbn());
            doc.setField(QueryFieldNames.ISSN, doc_.getIssn());
        }

        Set<Long> bookmarks = new HashSet<>();
        for (BookmarkedResource bm : resource.getBookmarkedResources()) {
            bookmarks.add(bm.getPerson().getId());
        }
        doc.setField(QueryFieldNames.BOOKMARKED_RESOURCE_PERSON_ID, bookmarks);

        if (resource instanceof Dataset) {
            Dataset dataset = (Dataset) resource;
            IntegratableOptions option = IntegratableOptions.NO;
            for (DataTable dt : dataset.getDataTables()) {
                for (DataTableColumn dtc : dt.getDataTableColumns()) {
                    if (dtc.getMappedOntology() != null) {
                        option = IntegratableOptions.YES;
                    }
                }
            }
            doc.setField(QueryFieldNames.INTEGRATABLE, option.name());

            // dataset.dataTables, DataTable.displayName , dataTableColumn.displayName

        }

        addKeyword(doc, QueryFieldNames.ACTIVE_CULTURE_KEYWORDS, KeywordType.CULTURE_KEYWORD, resource.getActiveCultureKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS, KeywordType.GEOGRAPHIC_KEYWORD, resource.getIndexedGeographicKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_INVESTIGATION_TYPES, KeywordType.INVESTIGATION_TYPE, resource.getActiveInvestigationTypes());
        addKeyword(doc, QueryFieldNames.ACTIVE_MATERIAL_KEYWORDS, KeywordType.MATERIAL_TYPE, resource.getActiveMaterialKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_OTHER_KEYWORDS, KeywordType.OTHER_KEYWORD, resource.getActiveOtherKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_SITE_NAME_KEYWORDS, KeywordType.SITE_NAME_KEYWORD, resource.getActiveSiteNameKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS, KeywordType.SITE_TYPE_KEYWORD, resource.getActiveSiteTypeKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_TEMPORAL_KEYWORDS, KeywordType.TEMPORAL_KEYWORD, resource.getActiveTemporalKeywords());

        GeneralKeywordBuilder gkb = new GeneralKeywordBuilder(resource, data);
        String text = gkb.getKeywords();
        doc.setField(QueryFieldNames.ALL, text);

        indexLatitudeLongitudeBoxes(resource, doc);

        // getActiveResourceAnnotations, getActiveSourceCollections , getActiveRelatedComparativeCollections, getActiveResourceNotes,
        doc.setField(QueryFieldNames.RESOURCE_OWNER, resource.getResourceOwner());

        // project.*

        if (resource instanceof SupportsResource) {
            SupportsResource sup = (SupportsResource) resource;
            if (PersistableUtils.isNotNullOrTransient(sup.getCategoryVariable())) {
                doc.setField(QueryFieldNames.CATEGORY_ID, sup.getCategoryVariable().getId());
            }

            if (sup instanceof Ontology) {
                Ontology ont = (Ontology) sup;
                // ontology nodes?
            }

            if (sup instanceof CodingSheet) {
                CodingSheet sheet = (CodingSheet) sup;
                // coding rules?
            }
        }

        return doc;
    }

    private static void indexTdarDataDatabaseValues(SolrInputDocument doc, Map<DataTableColumn, String> data) {
        List<String> values = new ArrayList<>();
        if (data != null) {
            for (Object key : data.keySet()) {
                if (key == null) {
                    continue;
                }
                String keyName = "";
                if (key instanceof DataTableColumn) {
                    keyName = ((DataTableColumn) key).getName();
                } else {
                    keyName = DataUtil.extractStringValue(key);
                }
                String mapValue = data.get(key);
                if (keyName == null || StringUtils.isBlank(mapValue)) {
                    continue;
                }
                values.add(keyName + ":" + mapValue);
            }
            doc.setField(QueryFieldNames.DATA_VALUE_PAIR, values);
        }
    }

    private static void indexTemporalInformation(SolrInputDocument doc, Resource resource) {
        for (CoverageDate date : resource.getActiveCoverageDates()) {
            doc.setField(QueryFieldNames.ACTIVE_END_DATE, date.getEndDate());
            doc.setField(QueryFieldNames.ACTIVE_START_DATE, date.getStartDate());
            doc.setField(QueryFieldNames.ACTIVE_COVERAGE_TYPE, date.getDateType().name());
        }
    }

    private static void indexCollectionInformation(SolrInputDocument doc, Resource resource) {
        Set<Long> collectionIds = new HashSet<Long>();
        Set<Long> directCollectionIds = new HashSet<Long>();
        Set<ResourceCollection> collections = new HashSet<>(resource.getResourceCollections());
        collections.addAll(resource.getUnmanagedResourceCollections());
        for (ResourceCollection collection : collections) {
            if (collection.isShared()) {
                directCollectionIds.add(collection.getId());
                collectionIds.addAll(collection.getParentIds());
            }
        }
        collectionIds.addAll(directCollectionIds);

        doc.setField(QueryFieldNames.RESOURCE_COLLECTION_DIRECT_SHARED_IDS, directCollectionIds);
        doc.setField(QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS, collectionIds);

    }

    private static void indexCreatorInformation(SolrInputDocument doc, Resource resource) {
        Map<String, List<Long>> types = new HashMap<>();
        List<String> roles = new ArrayList<>();
        if (resource instanceof InformationResource) {
            InformationResource informationRessource = (InformationResource) resource;
            roles.add(ResourceCreator.getCreatorRoleIdentifier(informationRessource.getResourceProviderInstitution(), ResourceCreatorRole.RESOURCE_PROVIDER));
            roles.add(ResourceCreator.getCreatorRoleIdentifier(informationRessource.getPublisher(), ResourceCreatorRole.PUBLISHER));
        }
        roles.add(ResourceCreator.getCreatorRoleIdentifier(resource.getSubmitter(), ResourceCreatorRole.SUBMITTER));
        roles.add(ResourceCreator.getCreatorRoleIdentifier(resource.getUpdatedBy(), ResourceCreatorRole.UPDATER));

        for (ResourceCreator rc : resource.getActiveResourceCreators()) {
            String key = rc.getRole().name();
            if (!types.containsKey(key)) {
                types.put(key, new ArrayList<Long>());
            }
            types.get(key).add(rc.getCreator().getId());
            roles.add(rc.getCreatorRoleIdentifier());
        }

        for (String key : types.keySet()) {
            doc.setField(key, types.get(key));
        }
        doc.setField("roles", roles);
    }

    private static void indexLatitudeLongitudeBoxes(Resource resource, SolrInputDocument doc) {
        List<String> envelops = new ArrayList<>();
        for (LatitudeLongitudeBox llb : resource.getActiveLatitudeLongitudeBoxes()) {
            Envelope env = new Envelope(llb.getMinObfuscatedLongitude(), llb.getMaxObfuscatedLongitude(), llb.getMinObfuscatedLatitude(),
                    llb.getMaxObfuscatedLatitude());
            WKTWriter wrt = new WKTWriter();
            String str = wrt.write(JTS.toGeometry(env));
            envelops.add(str);
        }
        doc.setField(QueryFieldNames.ACTIVE_LATITUDE_LONGITUDE_BOXES, envelops);
    }

    private static <K extends Keyword> void addKeyword(SolrInputDocument doc, String id, KeywordType type, Set<K> keywords) {
        Set<Long> ids = new HashSet<>();
        for (K k : keywords) {
            ids.add(k.getId());
            if (k instanceof HierarchicalKeyword) {
                HierarchicalKeyword<?> hk = (HierarchicalKeyword<?>) k;
                while (hk.getParent() != null) {
                    HierarchicalKeyword<?> parent = hk.getParent();
                    ids.add(parent.getId());
                    hk = parent;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            doc.setField(id, ids);
        }

    }
}
