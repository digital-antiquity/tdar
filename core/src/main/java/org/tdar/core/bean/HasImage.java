package org.tdar.core.bean;

import org.tdar.core.bean.resource.file.VersionType;

public interface HasImage extends Persistable {

    Integer getMaxWidth();

    Integer getMaxHeight();

    VersionType getMaxSize();

    void setMaxWidth(Integer width);

    void setMaxHeight(Integer height);

    void setMaxSize(VersionType type);
}
