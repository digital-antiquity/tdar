package org.tdar.core.bean.resource;

/**
 * The type of data in the ResourceAnnotation value
 * 
 * @author abrin
 * 
 */
public enum ResourceAnnotationDataType {

    NUMERIC,
    STRING,
    FORMAT_STRING;

    public boolean isFormatString() {
        return this == FORMAT_STRING;
    }
}
