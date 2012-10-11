/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean;

import org.tdar.core.bean.entity.Person;

/**
 * @author Adam Brin
 *
 */
public interface Updatable {

    public void markUpdated(Person p);
}
