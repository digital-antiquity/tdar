package org.tdar.transform.jsonld;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.SerializationService;

/**
 * Convert a Resource to a proper JSON Linked Data String
 * 
 * @author abrin
 *
 */
public class SchemaOrgResourceTransformer extends AbstractSchemaOrgMetadataTransformer {

    private static final long serialVersionUID = 3523678951801372308L;

    public String convert(SerializationService ss, Resource r) throws IOException {
        Map<String, Object> jsonLd = new HashMap<String, Object>();
        jsonLd.put(GRAPH, getGraph());
        addGraphSection(r);
        addGraphSection(r.getActiveCultureKeywords(), "tdar:cultureKeywords");
        addGraphSection(r.getActiveGeographicKeywords(), "tdar:geographicKeywords");
        addGraphSection(r.getActiveInvestigationTypes(), "tdar:investigationTypes");
        addGraphSection(r.getActiveMaterialKeywords(), "tdar:materialKeywords");
        addGraphSection(r.getActiveOtherKeywords(), "tdar:otherKeywords");
        addGraphSection(r.getActiveSiteNameKeywords(), "tdar:siteNameKeywords");
        addGraphSection(r.getActiveSiteTypeKeywords(), "tdar:siteTypeKeywords");
        addGraphSection(r.getActiveTemporalKeywords(), "tdar:temporalKeywords");
        ss.markReadOnly(r.getFirstActiveLatitudeLongitudeBox());
        addGraphSectionSpatial(r.getActiveLatitudeLongitudeBoxes());
        addGraphSectionTemporal(r.getActiveCoverageDates());
        addContextSection(jsonLd);
        String convertToJson = ss.convertToJson(jsonLd);
        getGraph().clear();
        return convertToJson;
    }

}
