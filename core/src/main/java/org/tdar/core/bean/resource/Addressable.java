package org.tdar.core.bean.resource;

/**
 * An interface to manage/represent things with URLs.
 * 
 * @author abrin
 * 
 */
public interface Addressable {

    public abstract Long getId();

    public abstract String getUrlNamespace();

    public abstract String getDetailUrl();
}
