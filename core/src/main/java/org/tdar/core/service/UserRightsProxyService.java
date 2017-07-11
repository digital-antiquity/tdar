package org.tdar.core.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.WrongClassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.core.service.external.EmailService;
import org.tdar.utils.PersistableUtils;

@Component
public class UserRightsProxyService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Autowired
    private GenericDao genericDao;
    
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    

    @Transactional(readOnly = true)
    public List<UserInvite> findUserInvites(Persistable resource) {
        if (resource instanceof Resource) {
            return resourceCollectionDao.findUserInvites((Resource) resource);
        }
        if (resource instanceof ResourceCollection) {
            return resourceCollectionDao.findUserInvites((ResourceCollection) resource);
        }
        if (resource instanceof TdarUser) {
            return resourceCollectionDao.findUserInvites((TdarUser) resource);
        }
        return null;

    }

    @Transactional(readOnly = true)
    public List<UserInvite> findUserInvites(Resource resource) {
        return resourceCollectionDao.findUserInvites(resource);
    }

    @Transactional(readOnly = true)
    public List<UserInvite> findUserInvites(ResourceCollection resourceCollection) {
        return resourceCollectionDao.findUserInvites(resourceCollection);
    }

    @Transactional(readOnly = true)
    public List<UserInvite> findUserInvites(TdarUser user) {
        return resourceCollectionDao.findUserInvites(user);
    }

    
    
    @Transactional(readOnly=false)
    public void handleInvites(TdarUser authenticatedUser, List<UserInvite> invites, HasAuthorizedUsers c) {
        List<UserInvite> existing = resourceCollectionDao.findUserInvites(c);
        Map<Long, UserInvite> createIdMap = PersistableUtils.createIdMap(existing);
        logger.debug("invites existing:: {}", existing);
        logger.debug("invites incoming:: {}", invites);

        if (CollectionUtils.isNotEmpty(invites)) {
            for (UserInvite invite : invites) {
                if (invite == null || invite.getUser().hasNoPersistableValues()) {
                    continue;
                }

                // existing one
                if (PersistableUtils.isNotNullOrTransient(invite.getId() )) {
                    UserInvite inv = createIdMap.get(invite.getId());
                    inv.setDateExpires(invite.getDateExpires());
                    inv.setPermissions(inv.getPermissions());
                    genericDao.saveOrUpdate(inv);
                    createIdMap.remove(invite.getId());
                    continue;
                }

                // new invite
                if (c instanceof ResourceCollection) {
                    invite.setResourceCollection((ResourceCollection) c);
                }
                if (c instanceof Resource) {
                    invite.setResource((Resource) c);
                }
                genericDao.saveOrUpdate(invite);
                emailService.sendUserInviteEmail(invite, authenticatedUser);
            }
        }
        Collection<UserInvite> toDelete = createIdMap.values();
        logger.debug("invites delete:: {}", toDelete);

        genericDao.delete(toDelete);
    }

    @Transactional(readOnly=false)
    public void convertProxyToItems(List<UserRightsProxy> proxies, TdarUser authenticatedUser, List<AuthorizedUser> authorizedUsers, List<UserInvite> invites) {
        for (UserRightsProxy proxy : proxies) {
            if (proxy == null || proxy.isEmpty()) {
                return;
            } 

            if (proxy.getEmail() != null || proxy.getInviteId() != null) {
                UserInvite invite = toInvite(proxy, authenticatedUser);
                if (invite != null) {
                    if (invite.getUser().isValidForController()) {
                        invites.add(invite);
                    } else {
                        throw new TdarValidationException("resourceCollectionService.invalid", Arrays.asList(invite.getUser()));
                    }
                }
            } else if (PersistableUtils.isNotNullOrTransient(proxy.getId())) {
                authorizedUsers.add(toAuthorizedUser(proxy));
            }
        }
    }

    @Transactional(readOnly=false)
    public UserInvite toInvite(UserRightsProxy proxy, TdarUser user) {
        UserInvite invite = new UserInvite();
        if (PersistableUtils.isNotNullOrTransient(proxy.getInviteId() )) {
            invite = genericDao.find(UserInvite.class, proxy.getInviteId());
        } else {
            invite.setId(proxy.getInviteId());
            invite.setNote(proxy.getNote());
            invite.setAuthorizer(user);
            Person person = new Person(proxy.getFirstName(), proxy.getLastName(), proxy.getEmail());
            if (person.hasNoPersistableValues()) {
                return null;
            }
            person = entityService.findOrSaveCreator(person);
            invite.setPerson(person);
        }
        invite.setDateExpires(proxy.getUntilDate());
        invite.setPermissions(proxy.getPermission());
        return invite;
    }

    @Transactional(readOnly=false)
    public AuthorizedUser toAuthorizedUser(UserRightsProxy proxy) {
        try {
            AuthorizedUser au = new AuthorizedUser();
            logger.debug("{} {} ", proxy, proxy.getId());
            TdarUser user = genericDao.find(TdarUser.class, proxy.getId());
            if (user == null && PersistableUtils.isNotNullOrTransient(proxy.getId() )) {
                throw new TdarRecoverableRuntimeException("resourceCollectionService.user_does_not_exists", Arrays.asList(proxy.getDisplayName()));
            }
            logger.debug("{} {}", user.getClass() , user);
            au.setUser(user);
            au.setGeneralPermission(proxy.getPermission());
            au.setDateExpires(proxy.getUntilDate());
            logger.debug("{} ({})", au, proxy.getDisplayName());
        return au;
        } catch (WrongClassException e) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.user_does_not_exists", Arrays.asList(proxy.getDisplayName()));
        }
    }

}
