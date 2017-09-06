package org.tdar.core.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.external.AuthorizationService;

public interface ObfuscationService {

    /**
     * Obfuscates a collection of objects based on the specified user.
     * 
     * @see #obfuscate(Obfuscatable, Person)
     * 
     * @param targets
     * @param user
     */
    void obfuscate(Collection<? extends Obfuscatable> targets, TdarUser user);

    /**
     * Due to Autowiring complexity, we expose the @link AuthenticationAndAuthorizationService here so we don't have autowiring issues in services like
     * the @link
     * SearchService
     * 
     * @return
     */
    AuthorizationService getAuthenticationAndAuthorizationService();

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
    void obfuscate(Obfuscatable target, TdarUser user);

    void obfuscateObject(Object obj, TdarUser user);

    boolean isWritableSession();

    void setObfuscationEnabled(Boolean enabled);

    boolean obfuscationInterceptorEnabled();

}