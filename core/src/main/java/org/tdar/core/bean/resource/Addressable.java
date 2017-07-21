package org.tdar.core.bean.resource;

/**
 * An interface to manage/represent things with URLs.
 * 
 * @author abrin
 * 
 */
public interface Addressable {

    Long getId();

    String getUrlNamespace();

    default String getDetailUrl() {
        return getUrlNamespace() + "/" + getId();
    }
}
