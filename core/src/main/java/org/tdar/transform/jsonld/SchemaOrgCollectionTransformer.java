package org.tdar.transform.jsonld;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.UrlService;

/**
 * Convert a Keyword to a proper schema.org JSON LinkedData String
 * @author abrin
 *
 */
public class SchemaOrgCollectionTransformer extends AbstractSchemaOrgMetadataTransformer {


    private static final long serialVersionUID = -1871001958288643018L;

    public String convert(SerializationService serializationService, VisibleCollection collection) throws IOException {
        Map<String,Object> jsonLd = new HashMap<>();
        getGraph().add(jsonLd);
        jsonLd.put(NAME, collection.getTitle());
        jsonLd.put(SCHEMA_DESCRIPTION, collection.getDescription());
        add(jsonLd, "schema:url", UrlService.absoluteUrl(collection));
        
        addContextSection(jsonLd);
        String json = serializationService.convertToJson(jsonLd);
        getGraph().clear();
        return json;
    }

}
