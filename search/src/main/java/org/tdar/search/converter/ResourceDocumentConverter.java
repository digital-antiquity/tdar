package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.geotools.geometry.jts.JTS;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.SupportsResource;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.IntegratableOptions;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.search.bean.ObjectType;
import org.tdar.search.index.GeneralKeywordBuilder;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.SiteCodeExtractor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.WKTWriter;

public class ResourceDocumentConverter extends AbstractSolrDocumentConverter {

    /*
     * See solr/configsets/default/conf/resources-schema.xml
     */

    @SuppressWarnings("unused")
    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    public static SolrInputDocument convert(Resource resource) {
        // Create a new solr document.
        SolrInputDocument doc = convertPersistable(resource);
        Map<DataTableColumn, String> data = null;

        // Set index data for basic fields.
        doc.setField(QueryFieldNames.NAME, resource.getName());
        doc.setField(QueryFieldNames.NAME_SORT, Sortable.getTitleSort(resource.getName()));
        addRequiredField(resource, doc);
        if (resource.getSubmitter() != null) {
            doc.setField(QueryFieldNames.SUBMITTER_ID, resource.getSubmitter().getId());
        }
        doc.setField(QueryFieldNames.DESCRIPTION, resource.getDescription());

        // Index the creator data
        indexCreatorInformation(doc, resource);

        // parse out the collection inforamation and add it to the document.
        Map<String, Object> indexCollectionInformation = indexCollectionInformation(doc, resource);
        indexAll(doc, indexCollectionInformation);

        indexTemporalInformation(doc, resource);

        if (resource instanceof Project) {
            doc.setField(QueryFieldNames.PROJECT_TITLE_SORT, Sortable.getTitleSort(resource.getTitle()));
            doc.setField(QueryFieldNames.TOTAL_FILES, 0);

        }

        if (resource instanceof InformationResource) {
            InformationResource ir = (InformationResource) resource;
            Map<String, Object> indexProjectInformation = indexProjectInformation(doc, ir);
            indexAll(doc, indexProjectInformation);
            if (ir.getPublisher() != null) {
                doc.setField(QueryFieldNames.PUBLISHER_ID, ir.getPublisher().getId());
            }
            doc.setField(QueryFieldNames.DATE, ir.getDate());

            doc.setField(QueryFieldNames.DATE_CREATED_DECADE, ir.getDateNormalized());

            if (ir.getMetadataLanguage() != null) {
                doc.setField(QueryFieldNames.METADATA_LANGUAGE, ir.getMetadataLanguage().name());
            }
            if (ir.getResourceLanguage() != null) {
                doc.setField(QueryFieldNames.RESOURCE_LANGUAGE, ir.getResourceLanguage().name());
            }

            Set<String> filenames = new HashSet<>();
            Set<Long> fileIds = new HashSet<>();
            int total = 0;
            for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                filenames.add(irf.getFilename());
                fileIds.add(irf.getId());

                if (!irf.isDeleted() && !irf.isPartOfComposite()) {
                    total++;
                }
            }
            if (ir.getResourceType().allowsMultipleFiles()) {
                doc.setField(QueryFieldNames.TOTAL_FILES, total);
            } else {
                doc.setField(QueryFieldNames.TOTAL_FILES, 1);
            }
            doc.setField(QueryFieldNames.FILENAME, filenames);
            doc.setField(QueryFieldNames.FILE_IDS, fileIds);
            doc.setField(QueryFieldNames.DOI, ir.getDoi());
            if (PersistableUtils.isNotNullOrTransient(ir.getResourceProviderInstitution())) {
                doc.setField(QueryFieldNames.RESOURCE_PROVIDER_ID, ir.getResourceProviderInstitution().getId());
            }

            doc.setField(QueryFieldNames.RESOURCE_ACCESS_TYPE, ir.getResourceAccessType().name());
        }

        if (resource instanceof Document) {
            Document doc_ = (Document) resource;
            doc.setField(QueryFieldNames.DOCUMENT_TYPE, doc_.getDocumentType().name());
            if (doc_.getDocumentSubType() != null) {
                doc.setField(QueryFieldNames.DOCUMENT_SUB_TYPE, doc_.getDocumentSubType().name());
            }
            doc.setField(QueryFieldNames.DEGREE, doc_.getDegree());
            doc.setField(QueryFieldNames.SERIES_NAME, doc_.getSeriesName());
            doc.setField(QueryFieldNames.SERIES_NUMBER, doc_.getSeriesNumber());
            doc.setField(QueryFieldNames.PUBLISHER_LOCATION, doc_.getPublisherLocation());
            doc.setField(QueryFieldNames.COPY_LOCATION, doc_.getCopyLocation());
            doc.setField(QueryFieldNames.START_PAGE, doc_.getStartPage());
            doc.setField(QueryFieldNames.END_PAGE, doc_.getEndPage());

            doc.setField(QueryFieldNames.BOOK_TITLE, doc_.getBookTitle());
            doc.setField(QueryFieldNames.JOURNAL_NAME, doc_.getJournalName());
            doc.setField(QueryFieldNames.JOURNAL_NUMBER, doc_.getJournalNumber());
            doc.setField(QueryFieldNames.ISBN, doc_.getIsbn());
            doc.setField(QueryFieldNames.ISSN, doc_.getIssn());
        }

        if (resource instanceof Dataset) {
            Dataset dataset = (Dataset) resource;
            IntegratableOptions option = IntegratableOptions.NOT_INTEGRATABLE;
            for (DataTable dt : dataset.getDataTables()) {
                for (DataTableColumn dtc : dt.getDataTableColumns()) {
                    if (dtc.getMappedOntology() != null) {
                        option = IntegratableOptions.INTEGRATABLE;
                    }
                }
            }
            doc.setField(QueryFieldNames.INTEGRATABLE, option.name());

            // dataset.dataTables, DataTable.displayName , dataTableColumn.displayName
        }

        Set<Long> bookmarks = new HashSet<>();
        for (BookmarkedResource bm : resource.getBookmarkedResources()) {
            bookmarks.add(bm.getPerson().getId());
        }
        doc.setField(QueryFieldNames.BOOKMARKED_RESOURCE_PERSON_ID, bookmarks);

        addKeyword(doc, QueryFieldNames.ACTIVE_CULTURE_KEYWORDS, KeywordType.CULTURE_KEYWORD, resource.getActiveCultureKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS, KeywordType.GEOGRAPHIC_KEYWORD, resource.getIndexedGeographicKeywords());
        Set<String> geoCodes = new HashSet<>();
        resource.getIndexedGeographicKeywords().forEach(geo -> {
            if (geo.isActive() || geo.isDuplicate()) {
                if (StringUtils.isNotBlank(geo.getCode())) {
                    geoCodes.add(geo.getCode());
                }
            }
        });
        doc.addField(QueryFieldNames.ACTIVE_GEOGRAPHIC_ISO, geoCodes);
        addKeyword(doc, QueryFieldNames.ACTIVE_INVESTIGATION_TYPES, KeywordType.INVESTIGATION_TYPE, resource.getActiveInvestigationTypes());
        addKeyword(doc, QueryFieldNames.ACTIVE_MATERIAL_KEYWORDS, KeywordType.MATERIAL_TYPE, resource.getActiveMaterialKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_OTHER_KEYWORDS, KeywordType.OTHER_KEYWORD, resource.getActiveOtherKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_SITE_NAME_KEYWORDS, KeywordType.SITE_NAME_KEYWORD, resource.getActiveSiteNameKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS, KeywordType.SITE_TYPE_KEYWORD, resource.getActiveSiteTypeKeywords());
        addKeyword(doc, QueryFieldNames.ACTIVE_TEMPORAL_KEYWORDS, KeywordType.TEMPORAL_KEYWORD, resource.getActiveTemporalKeywords());

        doc.addField(QueryFieldNames.SITE_CODE, extractSiteCodeTokens(resource));

        GeneralKeywordBuilder gkb = new GeneralKeywordBuilder(resource, data);
        String text = gkb.getKeywords();
        doc.setField(QueryFieldNames.ALL, text);
        doc.setField(QueryFieldNames.ALL_PHRASE, text);

        indexLatitudeLongitudeBoxes(resource, doc);

        // getActiveSourceCollections , getActiveRelatedComparativeCollections, getActiveResourceNotes,
        doc.setField(QueryFieldNames.RESOURCE_OWNER, resource.getResourceOwner());

        Set<String> concatenatedAnnotations = new HashSet<>();
        for (ResourceAnnotation ann : resource.getActiveResourceAnnotations()) {
            if (ann != null && ann.isValid()) {
                concatenatedAnnotations.add(formatResourceAnnotation(ann.getResourceAnnotationKey().getLabel(), ann.getValue()));
            }
        }
        doc.setField(QueryFieldNames.RESOURCE_ANNOTATION, concatenatedAnnotations);
        // project.*

        if (resource instanceof SupportsResource) {
            SupportsResource sup = (SupportsResource) resource;
            if (PersistableUtils.isNotNullOrTransient(sup.getCategoryVariable())) {
                doc.setField(QueryFieldNames.CATEGORY_ID, sup.getCategoryVariable().getId());
            }

            if (sup instanceof Ontology) {
                // Ontology ont = (Ontology) sup;
                // ontology nodes?
            }

            if (sup instanceof CodingSheet) {
                // CodingSheet sheet = (CodingSheet) sup;
                // coding rules?
            }
        }

        return doc;
    }

    public static String formatResourceAnnotation(String key, String value) {
        return String.format("%s-$$-%s", StringUtils.lowerCase(key), StringUtils.lowerCase(value));
    }

    private static void indexAll(SolrInputDocument doc, Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            doc.setField(entry.getKey(), entry.getValue());
        }

    }

    private static Map<String, Object> indexProjectInformation(SolrInputDocument doc, InformationResource ir) {
        Map<String, Object> map = new HashMap<>();
        if (ir.getProject() != null) {
            map.put(QueryFieldNames.PROJECT_ID, ir.getProject().getId());
            map.put(QueryFieldNames.PROJECT_TITLE, ir.getProjectTitle());
            doc.setField(QueryFieldNames.PROJECT_TITLE_SORT, Sortable.getTitleSort(ir.getProjectTitle()) + " - " + Sortable.getTitleSort(ir.getTitle()));

        }
        return map;
    }

    private static void addRequiredField(Resource resource, SolrInputDocument doc) {
        doc.setField(QueryFieldNames.RESOURCE_TYPE, resource.getResourceType().name());
        ObjectType objectType = ObjectType.from(resource.getResourceType());
        doc.setField(QueryFieldNames.OBJECT_TYPE, objectType.name());
        doc.setField(QueryFieldNames.OBJECT_TYPE_SORT, objectType.getSortName());
        doc.setField(QueryFieldNames.GENERAL_TYPE, LookupSource.RESOURCE.name());
    }

    private static HashSet<String> extractSiteCodeTokens(Resource resource) {
        HashSet<String> kwds = new HashSet<>();
        kwds.addAll(SiteCodeExtractor.extractSiteCodeTokens(resource.getTitle(), true));
        kwds.addAll(SiteCodeExtractor.extractSiteCodeTokens(resource.getDescription(), true));
        for (SiteNameKeyword kwd : resource.getActiveSiteNameKeywords()) {
            kwds.addAll(SiteCodeExtractor.extractSiteCodeTokens(kwd.getLabel(), true));
        }
        for (OtherKeyword kwd : resource.getActiveOtherKeywords()) {
            kwds.addAll(SiteCodeExtractor.extractSiteCodeTokens(kwd.getLabel(), true));
        }
        return kwds;
    }

    private static void indexTemporalInformation(SolrInputDocument doc, Resource resource) {
        for (CoverageDate date : resource.getActiveCoverageDates()) {
            doc.setField(QueryFieldNames.ACTIVE_END_DATE, date.getEndDate());
            doc.setField(QueryFieldNames.ACTIVE_START_DATE, date.getStartDate());
            doc.setField(QueryFieldNames.ACTIVE_COVERAGE_TYPE, date.getDateType().name());
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * Adds the Creator Information to the index for a given resource.
     * 
     * @param doc
     * @param resource
     */
    private static void indexCreatorInformation(SolrInputDocument doc, Resource resource) {
        List<String> crids = new ArrayList<>();
        Map<ResourceCreatorRole, HashSet<Creator<? extends Creator<?>>>> map = new HashMap<>();
        for (ResourceCreatorRole r : ResourceCreatorRole.values()) {
            map.put(r, new HashSet<>());
        }
        if (resource instanceof InformationResource) {
            InformationResource informationRessource = (InformationResource) resource;
            map.get(ResourceCreatorRole.RESOURCE_PROVIDER).add(informationRessource.getResourceProviderInstitution());
            map.get(ResourceCreatorRole.PUBLISHER).add(informationRessource.getPublisher());
        }
        doc.setField(QueryFieldNames.RESOURCE_CREATOR_ROLE_IDS, PersistableUtils.extractIds(resource.getPrimaryCreators()));

        map.get(ResourceCreatorRole.SUBMITTER).add(resource.getSubmitter());
        map.get(ResourceCreatorRole.UPDATER).add(resource.getUpdatedBy());
        map.get(ResourceCreatorRole.UPLOADER).add(resource.getUploader());
        Set<String> roles = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (ResourceCreator rc : resource.getActiveResourceCreators()) {
            if (!rc.getCreator().isActive() && !rc.getCreator().isDuplicate()) {
                continue;
            }
            map.get(rc.getRole()).add(rc.getCreator());
        }

        for (ResourceCreatorRole role_ : map.keySet()) {
            Set<Creator<? extends Creator<?>>> creators = map.get(role_);
            creators.removeAll(Collections.singleton(null));
            if (CollectionUtils.isEmpty(creators)) {
                continue;
            }
            roles.add(role_.name());
            Set<Long> typeIds = new HashSet<>();
            for (Creator<?> creator : creators) {
                if (creator == null) {
                    continue;
                }
                if (!creator.isActive() && !creator.isDuplicate()) {
                    continue;
                }
                names.add(creator.getProperName());
                typeIds.add(creator.getId());
                crids.add(ResourceCreator.getCreatorRoleIdentifier(creator, role_));
                // if (CollectionUtils.isNotEmpty(creator.getSynonyms())) {
                // for (Creator syn : (Collection<Creator>)creator.getSynonyms()) {
                // names.add(syn.getProperName());
                // crids.add(ResourceCreator.getCreatorRoleIdentifier(syn, role_));
                // typeIds.add(syn.getId());
                // }
                // }
            }
            doc.setField(role_.name(), typeIds);
        }

        doc.setField(QueryFieldNames.RESOURCE_CREATORS_PROPER_NAME, names);
        doc.setField(QueryFieldNames.CREATOR_ROLE, roles);
        doc.setField(QueryFieldNames.CREATOR_ROLE_IDENTIFIER, crids);
    }

    private static void indexLatitudeLongitudeBoxes(Resource resource, SolrInputDocument doc) {
        List<String> envelops = new ArrayList<>();
        List<Integer> scales = new ArrayList<>();
        List<Long> llibId = new ArrayList<>();
        for (LatitudeLongitudeBox llb : resource.getActiveLatitudeLongitudeBoxes()) {
            if (llb.getObfuscatedNorth() == null || llb.getObfuscatedEast() == null) {
                logger.error("LLB is null... shouldn't be for tDAR Id {}", resource.getId());
                llb.obfuscateAll();
            }
            Envelope env = new Envelope(llb.getObfuscatedWest(), llb.getObfuscatedEast(), llb.getObfuscatedSouth(),
                    llb.getObfuscatedNorth());
            llibId.add(llb.getId());
            WKTWriter wrt = new WKTWriter();
            String str = wrt.write(JTS.toGeometry(env));
            envelops.add(str);
            scales.add(llb.getScale());
        }
        doc.setField(QueryFieldNames.ACTIVE_LATITUDE_LONGITUDE_BOXES_IDS, llibId);
        doc.setField(QueryFieldNames.ACTIVE_LATITUDE_LONGITUDE_BOXES, envelops);
        doc.setField(QueryFieldNames.SCALE, scales);
    }

    private static <K extends Keyword> void addKeyword(SolrInputDocument doc, String prefix, KeywordType type, Set<K> keywords) {
        Set<Long> ids = new HashSet<>();
        Set<String> labels = new HashSet<>();
        for (K k : keywords) {
            if (!k.isActive() && !k.isDuplicate()) {
                continue;
            }

            ids.add(k.getId());
            labels.add(k.getLabel());
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
            doc.setField(prefix, ids);
            doc.setField(prefix + "_label", labels);
        }

    }

    /**
     * Map a Resource's Collection data to a Solr document
     * 
     * @param doc
     * @param resource
     * @return
     */
    public static Map<String, Object> indexCollectionInformation(SolrInputDocument doc, Resource resource) {
        // Instantiate and parsed out Resource Collection data from the resource.
        CollectionDataExtractor rightsExtractor = new CollectionDataExtractor(resource);

        // Map the parsed data to the document.
        Map<String, Object> map = new HashMap<>();
        map.put(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, rightsExtractor.getUsersWhoCanModify());
        map.put(QueryFieldNames.RESOURCE_USERS_WHO_CAN_VIEW, rightsExtractor.getUsersWhoCanView());

        map.put(QueryFieldNames.RESOURCE_COLLECTION_DIRECT_MANAGED_IDS, rightsExtractor.getDirectManagedCollectionIds());
        map.put(QueryFieldNames.RESOURCE_COLLECTION_DIRECT_UNMANAGED_IDS, rightsExtractor.getDirectUnmanagedCollectionIds());

        // There aren't Query fields to map the collection names.
        // map.put(QueryFieldNames.RESOURCE_LIST_COLLECTION_NAME, rightsExtractor.getListCollectionNames());
        // map.put(QueryFieldNames.RESOURCE_LIST_COLLECTION_IDS, rightsExtractor.getListCollectionIds());

        map.put(QueryFieldNames.RESOURCE_COLLECTION_MANAGED_IDS, rightsExtractor.getManagedCollectionIds());
        map.put(QueryFieldNames.RESOURCE_COLLECTION_UNMANAGED_IDS, rightsExtractor.getUnmanagedCollectionIds());
        // is used in a keyword search allowing us to boost relevancy on collection name
        map.put(QueryFieldNames.RESOURCE_COLLECTION_NAME, rightsExtractor.getCollectionNames());
        map.put(QueryFieldNames.RESOURCE_COLLECTION_IDS, rightsExtractor.getAllCollectionIds());

        return map;
    }

    public static SolrInputDocument replaceCollectionFields(Resource r) {
        SolrInputDocument doc = ResourceDocumentConverter.convertPersistable(r);
        Map<String, Object> map = ResourceDocumentConverter.indexCollectionInformation(doc, r);
        addRequiredField(r, doc);
        replaceField(doc, map, QueryFieldNames.RESOURCE_COLLECTION_DIRECT_MANAGED_IDS);
        replaceField(doc, map, QueryFieldNames.RESOURCE_COLLECTION_MANAGED_IDS);
        // replaceField(doc, map, QueryFieldNames.RESOURCE_LIST_COLLECTION_IDS);
        // replaceField(doc, map, QueryFieldNames.RESOURCE_LIST_COLLECTION_DIRECT_IDS);
        // replaceField(doc, map, QueryFieldNames.RESOURCE_LIST_COLLECTION_NAME);
        replaceField(doc, map, QueryFieldNames.RESOURCE_COLLECTION_IDS);
        replaceField(doc, map, QueryFieldNames.RESOURCE_COLLECTION_NAME);
        replaceField(doc, map, QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY);
        replaceField(doc, map, QueryFieldNames.RESOURCE_USERS_WHO_CAN_VIEW);

        return doc;
    }

    private static void replaceField(SolrInputDocument doc, Map<String, Object> map, String fieldName) {
        Map<String, Object> partialUpdate = new HashMap<>();
        partialUpdate.put("set", map.get(fieldName));
        doc.setField(fieldName, partialUpdate);
    }

    public static SolrInputDocument replaceProjectFields(InformationResource r) {
        SolrInputDocument doc = ResourceDocumentConverter.convertPersistable(r);
        Map<String, Object> map = ResourceDocumentConverter.indexProjectInformation(doc, r);
        addRequiredField(r, doc);
        replaceField(doc, map, QueryFieldNames.PROJECT_ID);
        replaceField(doc, map, QueryFieldNames.PROJECT_TITLE);
        replaceField(doc, map, QueryFieldNames.PROJECT_TITLE_SORT);
        return doc;
    }

}
