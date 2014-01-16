package org.tdar.core.bean;

import java.util.Date;

import org.tdar.core.bean.resource.Addressable;

/**
 * Implemented by objects which can publish basic information about themselves in OAI_DC format.
 */
public interface OaiDcProvider extends Addressable {

    abstract Date getDateCreated();

    abstract Date getDateUpdated();

    abstract String getTitle();

    abstract String getDescription();

}
