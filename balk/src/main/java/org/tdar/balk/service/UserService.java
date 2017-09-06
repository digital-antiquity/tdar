package org.tdar.balk.service;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.balk.bean.DropboxUserMapping;
import org.tdar.core.bean.entity.TdarUser;

public interface UserService {

    void saveTokenFor(TdarUser user, String token);

    DropboxUserMapping findUser(TdarUser authenticatedUser);

}