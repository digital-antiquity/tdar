package org.tdar.balk.service;

import org.tdar.balk.bean.DropboxUserMapping;
import org.tdar.core.bean.entity.TdarUser;

public interface UserService {

    void saveTokenFor(TdarUser user, String token);

    DropboxUserMapping findUser(TdarUser authenticatedUser);

}