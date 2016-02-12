package org.tdar.core.service;

import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.json.LatLongGeoJsonSerializer;
import org.tdar.utils.json.LatitudeLongitudeBoxWrapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

public class JacksonUtils {

    public static ObjectMapper initializeObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.registerModules(new JaxbAnnotationModule());
        Hibernate4Module hibernate4Module = new Hibernate4Module();
        hibernate4Module.enable(Hibernate4Module.Feature.FORCE_LAZY_LOADING);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LatitudeLongitudeBoxWrapper.class, new LatLongGeoJsonSerializer());
        mapper.registerModules(hibernate4Module, module);
        mapper.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
        return mapper;
    }

    public static ObjectWriter initializeObjectWriter(ObjectMapper mapper, Class<?> view) {
        ObjectWriter objectWriter = mapper.writer();
        if (view != null) {
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            objectWriter = mapper.writerWithView(view);
        }

        if (TdarConfiguration.getInstance().isPrettyPrintJson()) {
            objectWriter = objectWriter.with(new DefaultPrettyPrinter());
        }
        return objectWriter;
    }

}
