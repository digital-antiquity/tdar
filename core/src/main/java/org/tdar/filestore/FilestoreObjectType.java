package org.tdar.filestore;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.Resource;

public enum FilestoreObjectType {
        LOG,
        RESOURCE,
        CREATOR,
        COLLECTION;

        public static FilestoreObjectType fromClass(Class<?> cls) {
            if (Resource.class.isAssignableFrom(cls)) {
                return RESOURCE;
            }
            if (ResourceCollection.class.isAssignableFrom(cls)) {
                return COLLECTION;
            }
            if (Creator.class.isAssignableFrom(cls)) {
                return CREATOR;
            }
            return null;
        }
        
        public String getRootDir() {
            return name().toLowerCase();
        }
}
