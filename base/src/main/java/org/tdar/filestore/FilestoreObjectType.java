package org.tdar.filestore;

//import org.tdar.core.bean.collection.ResourceCollection;
//import org.tdar.core.bean.entity.Creator;
//import org.tdar.core.bean.resource.Resource;

public enum FilestoreObjectType {
    LOG,
    RESOURCE,
    CREATOR,
    COLLECTION, FILE;


    public String getRootDir() {
        return name().toLowerCase();
    }
}
