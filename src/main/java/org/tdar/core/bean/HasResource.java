package org.tdar.core.bean;

import org.tdar.core.bean.resource.Resource;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public interface HasResource<R extends Resource> extends Validatable {

    public R getResource();

    public void setResource(R resource);

}
