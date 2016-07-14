package org.tdar.core.bean.collection;

import java.util.Set;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlTransient;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;

@Entity
public interface RightsBasedResourceCollection extends Persistable  {



    @XmlTransient
    public abstract Set<Resource> getResources();

    public abstract void setResources(Set<Resource> resources);

}
