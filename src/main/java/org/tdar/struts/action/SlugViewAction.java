package org.tdar.struts.action;

import org.tdar.core.bean.resource.Addressable;

public interface SlugViewAction {

    void setSlugSuffix(String slugSuffix);

    String getSlugSuffix();

    String getSlug();

    boolean isRedirectBadSlug();

    Addressable getPersistable();

}
