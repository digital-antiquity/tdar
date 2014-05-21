/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean;

import java.util.Date;

import org.tdar.core.bean.entity.Person;

/**
 * This abstracts date and person information for business logic about who saved or updated what, when.
 * 
 * @author Adam Brin
 * 
 */
public interface Updatable {

    void markUpdated(Person p);

    Date getDateUpdated();
    
    Date getDateCreated();
}
