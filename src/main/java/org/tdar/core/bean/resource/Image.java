package org.tdar.core.bean.resource;

import javax.persistence.Entity;
import javax.persistence.Table;
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
@Table(name = "image")
@XmlRootElement(name = "image")
public class Image extends InformationResource {


    private static final long serialVersionUID = 8408005825415291619L;
    
    public Image() {
	setResourceType(ResourceType.IMAGE);
    }

}
