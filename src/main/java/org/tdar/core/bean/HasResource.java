package org.tdar.core.bean;

import org.tdar.core.bean.resource.Resource;

/**
 * $Id$
 * 
 * interface to manage relationship between a one to many and a resource. It's used to enable the "saveHasResource() function"
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public interface HasResource<R extends Resource> extends Validatable, Persistable {

}
