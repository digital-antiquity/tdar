package org.tdar.core.bean.resource;

import java.util.Set;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.AuthorizedUser;

public interface HasAuthorizedUsers extends Persistable {

    public Set<AuthorizedUser> getAuthorizedUsers();

}
