package org.tdar.core.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.UserRightsProxy;

public interface UserRightsProxyService {

    List<UserInvite> findUserInvites(Persistable resource);

    List<UserInvite> findUserInvites(Resource resource);

    List<UserInvite> findUserInvites(ResourceCollection resourceCollection);

    List<UserInvite> findUserInvites(TdarUser user);

    void handleInvites(TdarUser authenticatedUser, List<UserInvite> invites, HasAuthorizedUsers c);

    void convertProxyToItems(List<UserRightsProxy> proxies, TdarUser authenticatedUser, List<AuthorizedUser> authorizedUsers, List<UserInvite> invites);

    UserInvite toInvite(UserRightsProxy proxy, TdarUser user);

    AuthorizedUser toAuthorizedUser(UserRightsProxy proxy);

}