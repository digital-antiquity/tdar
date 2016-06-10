package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.Collections;
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
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.search.index.GeneralKeywordBuilder;
import org.tdar.search.index.analyzer.SiteCodeExtractor;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.PersistableUtils;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.WKTWriter;

public class ResourceDocumentConverter extends AbstractSolrDocumentConverter {

    @SuppressWarnings("unused")
    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    public static SolrInputDocument convert(Resource resource) {

        SolrInputDocument doc = convertPersistable(resource);
        doc.setField(QueryFieldNames.NAME, resource.getName());
        doc.setField(QueryFieldNames.NAME_SORT, resource.getTitleSort());
        addRequiredField(resource, doc);
        doc.setField(QueryFieldNames.SUBMITTER_ID, resource.getSubmitter().getId());
        doc.setField(QueryFieldNames.DESCRIPTION, resource.getDescription());
        indexCreatorInformation(doc, resource);
        indexCollectionInformation(doc, resource);
        indexTemporalInformation(doc, resource);
        Map<DataTableColumn, String> data = null;
        if (resource instanceof Project) {
            doc.setField(QueryFieldNames.PROJECT_TITLE_SORT, ((Project) resource).getTitleSort());
            doc.setField(QueryFieldNames.TOTAL_FILES, 0);

        }
        if (resource instanceof InformationResource) {
            InformationResource ir = (InformationResource) resource;
            if (ir.getProject() != null) {
                doc.setField(QueryFieldNames.PROJECT_ID, ir.getProject().getId());
                doc.setField(QueryFieldNames.PROJECT_TITLE, ir.getProjectTitle());
                doc.setField(QueryFieldNames.PROJECT_TITLE_SORT, ir.getProjectTitleSort());
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
            if (ir.getResourceType().allowsMultipleFIles()) {
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

        // getActiveResourceAnnotations, getActiveSourceCollections , getActiveRelatedComparativeCollections, getActiveResourceNotes,
        doc.setField(QueryFieldNames.RESOURCE_OWNER, resource.getResourceOwner());

        // project.*

        if (resource instanceof SupportsResource) {
            SupportsResource sup = (SupportsResource) resource;
            if (PersistableUtils.isNotNullOrTransient(sup.getCategoryVariable())) {
                doc.setField(QueryFieldNames.CATEGORY_ID, sup.getCategoryVariable().getId());
            }

            if (sup instanceof Ontology) {
//                Ontology ont = (Ontology) sup;
                // ontology nodes?
            }

            if (sup instanceof CodingSheet) {
//                CodingSheet sheet = (CodingSheet) sup;
                // coding rules?
            }
        }

        return doc;
    }


    private static void addRequiredField(Resource resource, SolrInputDocument doc) {
        doc.setField(QueryFieldNames.RESOURCE_TYPE, resource.getResourceType().name());
        doc.setField(QueryFieldNames.RESOURCE_TYPE_SORT, resource.getResourceType().getSortName());
    }

    
    private static HashSet<String> extractSiteCodeTokens(Resource resource) {
        HashSet<String> kwds = new HashSet<>();
        kwds.addAll(SiteCodeExtractor.extractSiteCodeTokens(resource.getTitle()));
        kwds.addAll(SiteCodeExtractor.extractSiteCodeTokens(resource.getDescription()));
        for (SiteNameKeyword kwd : resource.getActiveSiteNameKeywords()) {
            kwds.addAll(SiteCodeExtractor.extractSiteCodeTokens(kwd.getLabel()));
        }
        for (OtherKeyword kwd : resource.getActiveOtherKeywords()) {
            kwds.addAll(SiteCodeExtractor.extractSiteCodeTokens(kwd.getLabel()));
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

    public static void indexCollectionInformation(SolrInputDocument doc, Resource resource) {
        ResourceRightsExtractor rightsExtractor = new ResourceRightsExtractor(resource);
        doc.setField(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, rightsExtractor.getUsersWhoCanModify());
        doc.setField(QueryFieldNames.RESOURCE_USERS_WHO_CAN_VIEW, rightsExtractor.getUsersWhoCanView());

        doc.setField(QueryFieldNames.RESOURCE_COLLECTION_DIRECT_SHARED_IDS, rightsExtractor.getDirectCollectionIds());
        doc.setField(QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS, rightsExtractor.getCollectionIds());
        doc.setField(QueryFieldNames.RESOURCE_COLLECTION_IDS, rightsExtractor.getAllCollectionIds());
        doc.setField(QueryFieldNames.RESOURCE_COLLECTION_NAME, rightsExtractor.getCollectionNames());
    }

    @SuppressWarnings("unchecked")
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


    public static SolrInputDocument replaceCollectionFields(Resource r) {
        SolrInputDocument doc = ResourceDocumentConverter.convertPersistable(r);
        ResourceDocumentConverter.indexCollectionInformation(doc, r);
        addRequiredField(r, doc);
        replaceField(doc, QueryFieldNames.RESOURCE_COLLECTION_DIRECT_SHARED_IDS);
        replaceField(doc, QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS);
        replaceField(doc, QueryFieldNames.RESOURCE_COLLECTION_IDS);
        replaceField(doc, QueryFieldNames.RESOURCE_COLLECTION_NAME);
        replaceField(doc, QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY);
        replaceField(doc, QueryFieldNames.RESOURCE_USERS_WHO_CAN_VIEW);
        return doc;
    }

    private static void replaceField(SolrInputDocument doc, String fieldName) {
        Map<String, Object> partialUpdate = new HashMap<>();
        partialUpdate.put("set", doc.getField(fieldName).getValues());
        doc.setField(fieldName, partialUpdate);
    }


}
