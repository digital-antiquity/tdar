package org.tdar.core.bean.billing;

import java.util.Set;

import org.tdar.core.bean.entity.TdarUser;

public interface HasUsers {

    Set<TdarUser> getAuthorizedMembers();

    void setAuthorizedMembers(Set<TdarUser> authorizedMembers);

}
