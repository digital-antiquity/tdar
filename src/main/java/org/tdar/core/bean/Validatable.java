/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean;

import org.tdar.core.configuration.JSONTransient;

/**
 * @author Adam Brin
 * 
 */
public interface Validatable {

    /*
     * This method is used to test whether something is "valid" from the controller perspective,
     * it may be missing some components that require validitity for Hibernate...
     * by default isValidForController should be more permissive, not less than isValid
     */
    @JSONTransient
    public boolean isValidForController();

    /*
     * This is a low-level validation method... it should check for nulls and things hibernate won't like
     */
    @JSONTransient
    public boolean isValid();

}
