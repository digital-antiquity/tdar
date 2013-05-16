package org.tdar.core.bean;

import java.util.Date;

import org.tdar.core.bean.resource.Addressable;

/*
 * Implemented by objects which can publish basic information about themselves in OAI_DC format.
 */
public interface OaiDcProvider extends Addressable {

    public abstract Date getDateCreated();

    public abstract Date getDateUpdated();

    public abstract String getTitle();

    public abstract String getDescription();

}
