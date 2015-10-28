package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrInputDocument;
import org.geotools.geometry.jts.JTS;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.index.bridge.GeneralKeywordBuilder;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.DataUtil;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.WKTWriter;

public class ResourceDocumentConverter extends AbstractSolrDocumentConverter {

    public static SolrInputDocument convert(Resource resource, ResourceService resourceService, ResourceCollectionService resourceCollectionService) {

        SolrInputDocument doc = convertPersistable(resource);
        doc.setField("name", resource.getName());
        doc.setField("name_autocomplete", resource.getName());
        doc.setField("submitter.id", resource.getSubmitter().getId());
        doc.setField("description", resource.getDescription());
        doc.setField("usersWhoCanModify", resource.getUsersWhoCanModify());
        doc.setField("usersWhoCanView", resource.getUsersWhoCanView());

        indexCreatorInformation(doc, resource);
        indexCollectionInformation(doc, resource);
        indexTemporalInformation(doc, resource);

        addKeyword(doc, KeywordType.CULTURE_KEYWORD, resource.getActiveCultureKeywords());
        addKeyword(doc, KeywordType.GEOGRAPHIC_KEYWORD, resource.getActiveGeographicKeywords());
        addKeyword(doc, KeywordType.INVESTIGATION_TYPE, resource.getActiveInvestigationTypes());
        addKeyword(doc, KeywordType.MATERIAL_TYPE, resource.getActiveMaterialKeywords());
        addKeyword(doc, KeywordType.OTHER_KEYWORD, resource.getActiveOtherKeywords());
        addKeyword(doc, KeywordType.SITE_NAME_KEYWORD, resource.getActiveSiteNameKeywords());
        addKeyword(doc, KeywordType.SITE_TYPE_KEYWORD, resource.getActiveSiteTypeKeywords());
        addKeyword(doc, KeywordType.TEMPORAL_KEYWORD, resource.getActiveTemporalKeywords());

        
        Map<DataTableColumn, String> data = null;
        if (resource instanceof InformationResource) {
            data = resourceService.getMappedDataForInformationResource((InformationResource) resource);
            indexTdarDataDatabaseValues(doc, data);
        }

        GeneralKeywordBuilder gkb = new GeneralKeywordBuilder(resource, data);
        String text = gkb.getKeywords();
        doc.addField("searchAll", text);

        indexLatitudeLongitudeBoxes(resource, doc);
        return doc;
    }
    

    private static void indexTdarDataDatabaseValues(SolrInputDocument doc, Map<DataTableColumn, String> data) {
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
//                luceneOptions.addFieldToDocument(QueryFieldNames.DATA_VALUE_PAIR, keyName + ":" + mapValue, doc);
            }
        }
    }


    private static void indexTemporalInformation(SolrInputDocument doc, Resource resource) {
        // TODO Auto-generated method stub

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

        doc.addField(QueryFieldNames.RESOURCE_COLLECTION_DIRECT_SHARED_IDS, directCollectionIds);
        doc.addField(QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS, collectionIds);

    }

    private static void indexCreatorInformation(SolrInputDocument doc, Resource resource) {
        Map<String, List<Long>> types = new HashMap<>();
        List<String> roles = new ArrayList<>();
        for (ResourceCreator rc : resource.getActiveResourceCreators()) {
            String key = rc.getCreatorType().name();
            if (!types.containsKey(key)) {
                types.put(key, new ArrayList<Long>());
            }
            types.get(key).add(rc.getCreator().getId());
            roles.add(rc.getCreatorRoleIdentifier());
        }
        
        for (String key : types.keySet()) {
            doc.addField(key, types.get(key));
        }
        doc.addField("roles", roles);
    }

    private static void indexLatitudeLongitudeBoxes(Resource resource, SolrInputDocument doc) {
        List<String> envelops = new ArrayList<>();
        for (LatitudeLongitudeBox llb : resource.getActiveLatitudeLongitudeBoxes()) {
            Envelope env = new Envelope(llb.getMinObfuscatedLongitude(), llb.getMaxObfuscatedLongitude(), llb.getMinObfuscatedLatitude(),
                    llb.getMinObfuscatedLongitude());
            WKTWriter wrt = new WKTWriter();
            String str = wrt.write(JTS.toGeometry(env));
            envelops.add(str);
        }
        doc.setField("latitudeLongitudeBoxes", envelops);
    }

    private static <K extends Keyword> void addKeyword(SolrInputDocument doc, KeywordType type, Set<K> keywords) {
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
            doc.addField(type.name(), ids);
        }

    }
}
