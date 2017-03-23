package org.tdar.core.service;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.ObfuscationDao;
import org.tdar.core.service.external.AuthorizationService;

/**
 * A service to help with the obfuscation of @link Persistable Beans supporting @link Obfuscatable
 * 
 * @author abrin
 * 
 */
@Service
@Transactional(readOnly = true)
public class ObfuscationService {

    protected static final transient Logger logger = LoggerFactory.getLogger(ObfuscationService.class);

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private ObfuscationDao obfuscationDao;
    
    private Boolean enabled = true;

    /**
     * Obfuscates a collection of objects based on the specified user.
     * 
     * @see #obfuscate(Obfuscatable, Person)
     * 
     * @param targets
     * @param user
     */
    @Transactional(readOnly = true)
    public void obfuscate(Collection<? extends Obfuscatable> targets, TdarUser user) {
        for (Obfuscatable target : targets) {
            obfuscate(target, user);
        }
    }

    /**
     * Due to Autowiring complexity, we expose the @link AuthenticationAndAuthorizationService here so we don't have autowiring issues in services like
     * the @link
     * SearchService
     * 
     * @return
     */
    public AuthorizationService getAuthenticationAndAuthorizationService() {
        return authService;
    }

    /**
     * we're going to manipulate the record, so, we detach the items from the session before
     * we muck with them... then we'll pass it on. If we don't detach, then hibernate may try
     * to persist the changes.
     * Before we detach from the session, though, we have to make sure any lazily-initialized
     * properties and collections are initialized, because without a session, these properties
     * can't be initialized. So first we'll serialize the object (and discard the serialization),
     * purely as a means of fully loading the properties for the final serialization later.
     * 
     * @param target
     * @param user
     */
    @Transactional(readOnly = true)
    public void obfuscate(Obfuscatable target, TdarUser user) {

        obfuscationDao.obfuscate(target,user, authService);
    }

    public boolean isWritableSession() {
        return genericDao.isSessionWritable();
    }

    @Autowired(required=false)
    @Qualifier("obfuscationEnabled")
    public void setObfuscationEnabled(Boolean enabled) {
        this.enabled = enabled;
        logger.trace("set enabled: {} ", enabled);
    }

    
    public boolean obfuscationInterceptorEnabled() {
        logger.trace("get enabled: {} ", enabled);
        return enabled;
    }

}
