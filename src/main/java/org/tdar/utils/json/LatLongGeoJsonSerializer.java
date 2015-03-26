package org.tdar.utils.json;

import java.io.IOException;

import org.tdar.core.bean.coverage.LatitudeLongitudeBox;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class LatLongGeoJsonSerializer extends StdSerializer<LatitudeLongitudeBox>{

    public LatLongGeoJsonSerializer() {
        super(LatitudeLongitudeBox.class);
    }

        @Override
        public void serialize(LatitudeLongitudeBox value, JsonGenerator jgen, SerializerProvider provider) 
          throws IOException, JsonProcessingException {
            /*
{ "type": "Feature",
    "bbox": [-180.0, -90.0, 180.0, 90.0],
    "geometry": {
      "type": "Polygon",
      "coordinates": [[
        [-180.0, 10.0], [20.0, 90.0], [180.0, -5.0], [-30.0, -90.0]
        ]]
      }
    ...
    }
    //http://www.baeldung.com/jackson-custom-serialization
    //http://geojson.org/geojson-spec.html#polygon
            */
            jgen.writeStartObject();
            jgen.writeStringField("type", "Feature");
            jgen.writeFieldName("geometry");
            jgen.writeStartObject();
            jgen.writeStringField("type", "Polygon");
            jgen.writeFieldName("coordinate");
            jgen.writeStartArray();
            writeArrayEntry(value.getMinObfuscatedLatitude(), value.getMinObfuscatedLongitude(), jgen);
            writeArrayEntry(value.getMinObfuscatedLatitude(), value.getMaxObfuscatedLongitude(), jgen);
            writeArrayEntry(value.getMaxObfuscatedLatitude(), value.getMaxObfuscatedLongitude(), jgen);
            writeArrayEntry(value.getMaxObfuscatedLatitude(), value.getMinObfuscatedLongitude(), jgen);
            writeArrayEntry(value.getMinObfuscatedLatitude(), value.getMinObfuscatedLongitude(), jgen);
            jgen.writeEndArray();
            jgen.writeEndObject();
            jgen.writeEndObject();
        }

        private void writeArrayEntry(Double lat, Double lon, JsonGenerator jgen) throws IOException, JsonGenerationException {
            jgen.writeStartArray();
            jgen.writeNumber(lat);
            jgen.writeNumber(lon);
            jgen.writeEndArray();
        }
}
