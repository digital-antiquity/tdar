package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.common.SolrInputDocument;
import org.geotools.geometry.jts.JTS;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.Resource;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.WKTWriter;

public class ResourceDocumentConverter extends AbstractSolrDocumentConverter {

    public static SolrInputDocument convert(Resource resource) {

        SolrInputDocument doc = convertPersistable(resource);
        doc.setField("name", resource.getName());
        doc.setField("name_autocomplete", resource.getName());
        // doc.setField("collectionIds", resource.getResourceCollectionIds());
        doc.setField("submitter.id", resource.getSubmitter().getId());
        doc.setField("description", resource.getDescription());
        doc.setField("usersWhoCanModify", resource.getUsersWhoCanModify());
        doc.setField("usersWhoCanView", resource.getUsersWhoCanView());
        // doc.setField("allSearch", resource.get);

        
//        indexCreatorInformati0on(doc, );
        
        addKeyword(doc, KeywordType.CULTURE_KEYWORD, resource.getActiveCultureKeywords());
        addKeyword(doc, KeywordType.GEOGRAPHIC_KEYWORD, resource.getActiveGeographicKeywords());
        addKeyword(doc, KeywordType.INVESTIGATION_TYPE, resource.getActiveInvestigationTypes());
        addKeyword(doc, KeywordType.MATERIAL_TYPE, resource.getActiveMaterialKeywords());
        addKeyword(doc, KeywordType.OTHER_KEYWORD, resource.getActiveOtherKeywords());
        addKeyword(doc, KeywordType.SITE_NAME_KEYWORD, resource.getActiveSiteNameKeywords());
        addKeyword(doc, KeywordType.SITE_TYPE_KEYWORD, resource.getActiveSiteTypeKeywords());
        addKeyword(doc, KeywordType.TEMPORAL_KEYWORD, resource.getActiveTemporalKeywords());
        
        indexLatitudeLongitudeBoxes(resource, doc);
        return doc;
    }

    private static void indexLatitudeLongitudeBoxes(Resource resource, SolrInputDocument doc) {
        List<String> envelops = new ArrayList<>();
        for (LatitudeLongitudeBox llb : resource.getActiveLatitudeLongitudeBoxes()) {
            Envelope env = new Envelope(llb.getMinObfuscatedLongitude(), llb.getMaxObfuscatedLongitude(), llb.getMinObfuscatedLatitude(), llb.getMinObfuscatedLongitude());
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
                HierarchicalKeyword<?> hk = (HierarchicalKeyword<?>)k;
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
