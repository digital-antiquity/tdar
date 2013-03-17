package org.tdar.core.bean.resource;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Indexed;

/**
 * $Id$
 * <p>
 * (What kind of files are allowed to be Images?
 * </p>
 * 
 * @author Adam Brin
 * @version $Revision: 543$
 */
@Entity
@Indexed
@Table(name = "geospatial")
@XmlRootElement(name = "geospatial")
public class Geospatial extends Dataset {

    /**
     * 
     */
    private static final long serialVersionUID = -7898164729934403851L;

    public Geospatial() {
        setResourceType(ResourceType.GEOSPATIAL);
    }

    @Override
    @Transient
    public boolean isSupportsThumbnails() {
        return true;
    }
}
