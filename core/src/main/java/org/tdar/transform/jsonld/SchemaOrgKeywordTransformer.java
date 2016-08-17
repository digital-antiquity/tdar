package org.tdar.transform.jsonld;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.service.SerializationService;

/**
 * Convert a Keyword to a proper schema.org JSON LinkedData String
 * @author abrin
 *
 */
public class SchemaOrgKeywordTransformer extends AbstractSchemaOrgMetadataTransformer {

    private static final long serialVersionUID = 8735863754368415427L;

    public String convert(SerializationService serializationService, Keyword keyword) throws IOException {
        Map<String, Object> jsonLd = new HashMap<String, Object>();
        addGraphSection(new HashSet<>(Arrays.asList(keyword)),"keywords");
        jsonLd.put(GRAPH, getGraph().get(0).get("keywords"));
        addContextSection(jsonLd);
        String json = serializationService.convertToJson(jsonLd);
        getGraph().clear();
        return json;
    }

}
