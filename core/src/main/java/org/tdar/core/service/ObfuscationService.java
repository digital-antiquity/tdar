package org.tdar.core.service;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
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

        if ((target == null) || target.isObfuscated()) {
            logger.trace("target is already obfuscated or null: {} ({}}", target, user);
            return;
        }

        if (authService.isEditor(user)) {
            // logger.debug("user is editor: {} ({}}", target, user);
            return;
        }

        // don't obfuscate someone for themself
        if ((target instanceof Person) && Objects.equals(user, target)) {
            logger.trace("not obfuscating person: {}", user);
            return;
        }

        if (TdarConfiguration.getInstance().shouldShowExactLocationToThoseWhoCanEdit()) {
            if (target instanceof Resource && authService.canViewConfidentialInformation(user, (Resource) target)) {
                return;
            }
        }

        genericDao.markReadOnly(target);
        Set<Obfuscatable> obfuscateList = handleObfuscation(target);
        if (CollectionUtils.isNotEmpty(obfuscateList)) {
            for (Obfuscatable subTarget : obfuscateList) {
                obfuscate(subTarget, user);
            }
        }
    }

    /**
     * Ultimately, this should be replaced with a Vistor pattern for obfuscation, but right now, it handles the obfuscation by calling @link
     * Obfuscatable.obfuscate()
     * 
     * @param target
     * @return
     */
    @SuppressWarnings("deprecation")
    private Set<Obfuscatable> handleObfuscation(Obfuscatable target) {
        logger.trace("obfuscating: {} [{}]", target.getClass(), target.getId());
        target.setObfuscated(true);
        return target.obfuscate();
    }

    public void obfuscateObject(Object obj, TdarUser user) {
        // because of generic type arguments, the following (duplicate) instance-of checks are necessary in cases where system
        // returns type of List<I> but we can't figure out what
        if (obj == null) {
            return;
        }

        if (Iterable.class.isAssignableFrom(obj.getClass())) {
            for (Object obj_ : (Iterable<?>) obj) {
                if (obj_ instanceof Obfuscatable) {
                    obfuscate((Obfuscatable) obj_, user);
                } else {
                    logger.trace("trying to obfsucate something we shouldn't {}", obj.getClass());
                }
            }
        } else {
            if (obj instanceof Obfuscatable) {
                try {
                    obfuscate((Obfuscatable) obj, user);
                } catch (LazyInitializationException e) {
                    if (isWritableSession()) {
                        logger.warn("failed obfuscation of {} for user: {} ", obj, user);
                    } else {
                        logger.debug("failed obfuscation for non-writable sessions due to LazyInitException {}", obj);
                    }
                }
            } else {
                logger.trace("trying to obfsucate something we shouldn't {}", obj.getClass());
            }
        }
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
