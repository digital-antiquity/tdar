package org.tdar.core.bean;

import java.util.Date;

import org.tdar.core.bean.resource.Addressable;

/**
 * Implemented by objects which can publish basic information about themselves in OAI_DC format.
 */
public interface OaiDcProvider extends Addressable {

    Date getDateCreated();

    Date getDateUpdated();

    String getTitle();

    String getDescription();

}
