/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean;

import java.util.Date;

import org.tdar.core.bean.entity.TdarUser;

/**
 * This abstracts date and person information for business logic about who saved or updated what, when.
 * 
 * @author Adam Brin
 * 
 */
public interface Updatable {

    void markUpdated(TdarUser p);

    Date getDateUpdated();

    Date getDateCreated();

}
