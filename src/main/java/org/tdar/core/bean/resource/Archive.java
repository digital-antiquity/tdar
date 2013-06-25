package org.tdar.core.bean.resource;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Indexed;

/**
 * A compressed archive. If from FAIMS the hope is that it will unpacked and its constituent parts imported as separate documents.
 * @author Martin Paulo
 */
@Entity
@Indexed
@Table(name = "archive")
@XmlRootElement(name = "archive")
public class Archive extends InformationResource {

    private static final long serialVersionUID = -3052481706474354766L;
    
    public Archive() {
        setResourceType(ResourceType.ARCHIVE);
    }

}
