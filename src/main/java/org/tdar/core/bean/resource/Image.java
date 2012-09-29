package org.tdar.core.bean.resource;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Indexed;
import org.tdar.core.configuration.JSONTransient;

import com.thoughtworks.xstream.annotations.XStreamAlias;

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
@Table(name = "image")
@XStreamAlias("image")
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
}
