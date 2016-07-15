package org.tdar.core.bean.collection;

import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;

public interface RightsBasedResourceCollection extends Persistable {


    TdarUser getOwner();
    
    boolean isTransient();

    @XmlTransient
    Set<Resource> getResources();

    void setResources(Set<Resource> resources);
    
    Set<AuthorizedUser> getAuthorizedUsers();

}
