package org.tdar.core.dao;

import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.external.AuthorizationService;

@Component
public class ObfuscationDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    public void obfuscate(Obfuscatable target, TdarUser user, AuthorizationService authService) {

        if ((target == null) || target.isObfuscated()) {
            logger.trace("target is already obfuscated or null: {} ({}}", target, user);
            return;
        }

        if (authService != null && authService.isEditor(user)) {
            // logger.debug("user is editor: {} ({}}", target, user);
            return;
        }

        // don't obfuscate someone for themself
        if ((target instanceof Person) && Objects.equals(user, target)) {
            logger.trace("not obfuscating person: {}", user);
            return;
        }

        if (TdarConfiguration.getInstance().shouldShowExactLocationToThoseWhoCanEdit()) {
            if (target instanceof Resource && authService != null &&
                    authService.canViewConfidentialInformation(user, (Resource) target)) {
                return;
            }
        }

        genericDao.markReadOnly(target);
        Set<Obfuscatable> obfuscateList = handleObfuscation(target);
        if (CollectionUtils.isNotEmpty(obfuscateList)) {
            for (Obfuscatable subTarget : obfuscateList) {
                obfuscate(subTarget, user, authService);
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

    public void obfuscateObject(Object obj, TdarUser user, AuthorizationService authService) {
        // because of generic type arguments, the following (duplicate) instance-of checks are necessary in cases where system
        // returns type of List<I> but we can't figure out what
        if (obj == null) {
            return;
        }

        if (Iterable.class.isAssignableFrom(obj.getClass())) {
            for (Object obj_ : (Iterable<?>) obj) {
                if (obj_ instanceof Obfuscatable) {
                    obfuscate((Obfuscatable) obj_, user, authService);
                } else {
                    logger.trace("trying to obfsucate something we shouldn't {}", obj.getClass());
                }
            }
        } else {
            if (obj instanceof Obfuscatable) {
                try {
                    obfuscate((Obfuscatable) obj, user, authService);
                } catch (LazyInitializationException e) {
                    if (genericDao.isSessionWritable()) {
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

}
