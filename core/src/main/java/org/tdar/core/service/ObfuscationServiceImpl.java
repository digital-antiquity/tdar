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
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.base.ObfuscationDao;
import org.tdar.core.service.external.AuthorizationService;

/**
 * A service to help with the obfuscation of @link Persistable Beans supporting @link Obfuscatable
 * 
 * @author abrin
 * 
 */
@Service
@Transactional(readOnly = true)
public class ObfuscationServiceImpl implements ObfuscationService  {

    protected static final transient Logger logger = LoggerFactory.getLogger(ObfuscationService.class);

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private ObfuscationDao obfuscationDao;
    
    private Boolean enabled = true;

    /* (non-Javadoc)
     * @see org.tdar.core.service.ObfuscationService#obfuscate(java.util.Collection, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public void obfuscate(Collection<? extends Obfuscatable> targets, TdarUser user) {
        for (Obfuscatable target : targets) {
            obfuscate(target, user);
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.ObfuscationService#getAuthenticationAndAuthorizationService()
     */
    @Override
    public AuthorizationService getAuthenticationAndAuthorizationService() {
        return authService;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.ObfuscationService#obfuscate(org.tdar.core.bean.Obfuscatable, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public void obfuscate(Obfuscatable target, TdarUser user) {
        obfuscationDao.obfuscate(target,user, authService);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.ObfuscationService#obfuscateObject(java.lang.Object, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public void obfuscateObject(Object obj, TdarUser user) {
        obfuscationDao.obfuscateObject(obj,user, authService);
    }
    
    /* (non-Javadoc)
     * @see org.tdar.core.service.ObfuscationService#isWritableSession()
     */
    @Override
    public boolean isWritableSession() {
        return genericDao.isSessionWritable();
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.ObfuscationService#setObfuscationEnabled(java.lang.Boolean)
     */
    @Override
    @Autowired(required=false)
    @Qualifier("obfuscationEnabled")
    public void setObfuscationEnabled(Boolean enabled) {
        this.enabled = enabled;
        logger.trace("set enabled: {} ", enabled);
    }

    
    /* (non-Javadoc)
     * @see org.tdar.core.service.ObfuscationService#obfuscationInterceptorEnabled()
     */
    @Override
    public boolean obfuscationInterceptorEnabled() {
        logger.trace("get enabled: {} ", enabled);
        return enabled;
    }

}
