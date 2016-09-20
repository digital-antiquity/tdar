package org.tdar.balk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.balk.bean.DropboxUserMapping;
import org.tdar.balk.dao.UserDao;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.GenericDao;
import org.tdar.utils.dropbox.DropboxClient;

@Component
public class UserService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired 
    private GenericDao genericDao;

    @Autowired 
    private UserDao userDao;

    @Transactional(readOnly=false)
    public void saveTokenFor(TdarUser user, String token) throws Exception {
        DropboxUserMapping mapping = userDao.findUserForUsername(user);
        mapping.setToken(token);
        DropboxClient client = new DropboxClient(mapping);
        mapping.setEmail(client.getUserAccount().getEmail());
        genericDao.saveOrUpdate(mapping);
    }
}
