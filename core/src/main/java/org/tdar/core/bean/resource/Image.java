package org.tdar.core.bean.resource;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * $Id$
 * <p>
 * Represnts an image object in TDAR
 * </p>
 * 
 * @author Adam Brin
 * @version $Revision: 543$
 */
@Entity
// @Indexed
@Table(name = "image")
@XmlRootElement(name = "image")
public class Image extends InformationResource {

    private static final long serialVersionUID = 8408005825415291619L;

    public Image() {
        setResourceType(ResourceType.IMAGE);
    }

    @Override
    @Transient
    public boolean isSupportsThumbnails() {
        return true;
    }

    @Override
    @Transient
    public boolean isHasBrowsableImages() {
        return true;
    }
}
