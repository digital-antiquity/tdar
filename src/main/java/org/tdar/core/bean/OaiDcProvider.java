package org.tdar.core.bean;

import java.util.Date;

/*
 * Implemented by objects which can publish basic information about themselves in OAI_DC format.
 */
public interface OaiDcProvider {

    public abstract Long getId();
    
    public abstract Date getDateCreated();

    public abstract Date getDateUpdated();

    public abstract String getTitle();

    public abstract String getDescription();

}
