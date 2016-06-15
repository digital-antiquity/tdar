package org.tdar.utils.json;

import java.io.IOException;

import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.service.JacksonUtils;
import org.tdar.core.service.UrlService;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class LatLongGeoJsonSerializer extends StdSerializer<LatitudeLongitudeBoxWrapper> {

    private static final long serialVersionUID = -4871387688721312659L;

    public LatLongGeoJsonSerializer() {
        super(LatitudeLongitudeBoxWrapper.class);
    }

    @Override
    public void serialize(LatitudeLongitudeBoxWrapper value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        /*
         * { "type": "Feature",
         * "bbox": [-180.0, -90.0, 180.0, 90.0],
         * "geometry": {
         * "type": "Polygon",
         * "coordinates": [[
         * [-180.0, 10.0], [20.0, 90.0], [180.0, -5.0], [-30.0, -90.0]
         * ]]
         * }
         * ...
         * }
         * //http://www.baeldung.com/jackson-custom-serialization
         * //http://geojson.org/geojson-spec.html#polygon
         */
        ObjectMapper mapper = JacksonUtils.initializeObjectMapper();
        ObjectWriter objectWriter = JacksonUtils.initializeObjectWriter(mapper, value.getJsonView());
        
        jgen.writeStartObject();
        jgen.writeStringField("type", "Feature");
        writeGeometry(value, jgen);
        writeProperties(value, jgen, objectWriter);
        jgen.writeEndObject();
    }

    private void writeProperties(LatitudeLongitudeBoxWrapper value, JsonGenerator jgen, ObjectWriter objectWriter) throws IOException {
        jgen.writeFieldName("properties");
        jgen.writeStartObject();
        jgen.writeStringField("title", value.getResource().getTitle());
        jgen.writeNumberField("id", value.getResource().getId());
        jgen.writeStringField("resourceType", value.getResource().getResourceType().name());
        jgen.writeStringField("status", value.getResource().getStatus().name());
        jgen.writeStringField("detailUrl", UrlService.absoluteUrl(value.getResource()));
        if (value.getResource() instanceof InformationResource && value.getResource().isHasBrowsableImages()) {
            InformationResource ir = ((InformationResource) value.getResource());
            InformationResourceFileVersion t = null;
            for (InformationResourceFile irf : ir.getActiveInformationResourceFiles()) {
                t = irf.getLatestThumbnail();
                if (t != null) {
                    break;
                }
            }
            jgen.writeStringField("thumbnailUrl", UrlService.thumbnailUrl(t));
        }
        jgen.writeFieldName("resource");
        objectWriter.writeValue(jgen, value.getResource());
//        jgen.writeObjectField("resource", value.getResource());
        jgen.writeEndObject();
    }

    private void writeGeometry(LatitudeLongitudeBoxWrapper value, JsonGenerator jgen) throws IOException, JsonGenerationException {
        jgen.writeFieldName("geometry");
        jgen.writeStartObject();
        if (value.isSpatial()) {
            switch (value.getMode()) {
                case ENVELOPE:
                    jgen.writeStringField("type", "Polygon");
                    jgen.writeFieldName("coordinates");
                    jgen.writeStartArray();
                    jgen.writeStartArray();
                    writeArrayEntry(value.getMinLatitude(), value.getMinLongitude(), jgen);
                    writeArrayEntry(value.getMinLatitude(), value.getMaxLongitude(), jgen);
                    writeArrayEntry(value.getMaxLatitude(), value.getMaxLongitude(), jgen);
                    writeArrayEntry(value.getMaxLatitude(), value.getMinLongitude(), jgen);
                    writeArrayEntry(value.getMinLatitude(), value.getMinLongitude(), jgen);
                    jgen.writeEndArray();
                    jgen.writeEndArray();
                    break;
                case POINT:
                    // "geometry": {"type": "Point", "coordinates": [102.0, 0.5]},
                    jgen.writeStringField("type", "Point");
                    jgen.writeFieldName("coordinate");
                    jgen.writeStartArray();
                    writeArrayEntry(value.getCenterLatitude(), value.getCenterLongitude(), jgen);
                    jgen.writeEndArray();
    
                default:
                    break;
            }
        }
        jgen.writeEndObject();
    }

    private void writeArrayEntry(Double lat, Double lon, JsonGenerator jgen) throws IOException, JsonGenerationException {
        jgen.writeStartArray();
        jgen.writeNumber(lat);
        jgen.writeNumber(lon);
        jgen.writeEndArray();
    }
}
