/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean;

import java.util.Date;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.joda.time.DateTime;
import org.tdar.core.bean.entity.TdarUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @XmlTransient
    @Transient
    @JsonIgnore
    public default boolean isNew() {
        if (getDateCreated() == null) {
            return false;
        }
        
        if (DateTime.now().minusDays(7).isBefore(getDateCreated().getTime())) {
            return true;
        }
        return false;
    }

}
