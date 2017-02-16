package org.tdar.core.service.processes.daily;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.collection.CollectionRevisionLog;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.TimedAccessRestriction;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;

/**
 * $Id$
 * 
 * ScheduledProcess to remove authorized Users that have expired
 * 
 * 
 * @author <a href='mailto:adam.brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */

@Component
@Scope("prototype")
public class DailyTimedAccessRevokingProcess extends AbstractScheduledBatchProcess<TimedAccessRestriction> {

    private static final long serialVersionUID = 7534566757094920406L;
    public TdarConfiguration config = TdarConfiguration.getInstance();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private EmailService emailService;

    @Override
    public String getDisplayName() {
        return "Timed Access Revoking Porcess";

    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    @Override
    public boolean shouldRunAtStartup() {
        return true;
    }

    /**
     * This ScheduledProcess is finished to completion after execute().
     */
    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    Map<TdarUser, List<String>> ownerNotes = new HashMap<>();
    Map<TdarUser, List<String>> userNotes = new HashMap<>();

    @Override
    public void process(TimedAccessRestriction persistable) throws Exception {
        DateTime now = DateTime.now();
        if (now.isAfter(new DateTime(persistable.getUntil()))) {
            StringBuilder sb = new StringBuilder();
            ResourceCollection collection = persistable.getCollection();
            Set<AuthorizedUser> authorizedUsers = collection.getAuthorizedUsers();
            Iterator<AuthorizedUser> iter = authorizedUsers.iterator();
            List<Long> idsToRemove = new ArrayList<>();
            while (iter.hasNext()) {
                AuthorizedUser au = iter.next();
                TdarUser user = persistable.getUser();
                // if the access restriction was created prior to the creation of the user, the invite might exist
                if (user == null && persistable.getInvite() != null) {
                    user = persistable.getInvite().getUser();
                    persistable.getInvite().setPermissions(null);
                    genericDao.saveOrUpdate(persistable.getInvite());
                    logger.debug("disabling invite for {}", persistable.getInvite());
                }

                if (Objects.equals(au.getUser(), user)) {
                    String name = "";
                    if (collection instanceof HasName) {
                        name = ((HasName) collection).getName();
                    }
                    ownerNotes.putIfAbsent(persistable.getCreatedBy(), new ArrayList<>());
                    userNotes.putIfAbsent(persistable.getUser(), new ArrayList<>());
                    String note = String.format("%s - %s", name, user.getName());
                    ownerNotes.get(persistable.getCreatedBy()).add(note);
                    userNotes.get(persistable.getUser()).add(note);
                    String msg = String.format("disabling authorized user  (%s) for %s", au, collection);
                    logger.debug(msg);
                    sb.append(msg);
                    sb.append("\n");
                    idsToRemove.add(au.getId());
                    iter.remove();
                }
            }
            genericDao.saveOrUpdate(collection);
            genericDao.delete(persistable);
            TdarUser user = genericDao.find(TdarUser.class, TdarConfiguration.getInstance().getAdminUserId());
            CollectionRevisionLog crl = new CollectionRevisionLog(sb.toString(), collection, user, RevisionLogType.EDIT);
            crl.setResourceCollection(collection);
            genericDao.saveOrUpdate(crl);
            logger.debug("result: {}", collection.getAuthorizedUsers());
            publisher.publishEvent(new TdarEvent(collection, EventType.CREATE_OR_UPDATE));
        }
    }

    @Override
    protected void batchCleanup() {

        Set<String> adminNotes = new HashSet<>();
        for (TdarUser owner : ownerNotes.keySet()) {
            Email email = new Email();
            email.setUserGenerated(false);
            email.setTo(owner.getEmail());
            email.setSubject(TdarConfiguration.getInstance().getSiteAcronym() + " Expired User from Collection");
            Map<String, Object> map = new HashMap<>();
            map.put("user", owner);
            List<String> notes = ownerNotes.get(owner);
            map.put("notes", notes);
            emailService.queueWithFreemarkerTemplate("expire/expire-owner.ftl", map, email);
            adminNotes.addAll(notes);
        }

        for (TdarUser owner : userNotes.keySet()) {
            Email email = new Email();
            email.setUserGenerated(false);
            email.setTo(owner.getEmail());
            email.setSubject(TdarConfiguration.getInstance().getSiteAcronym() + " Expired Access To Collection");
            Map<String, Object> map = new HashMap<>();
            map.put("user", owner);
            map.put("notes", ownerNotes.get(owner));
            emailService.queueWithFreemarkerTemplate("expire/expire-user.ftl", map, email);
        }

        if (CollectionUtils.isNotEmpty(adminNotes)) {
            Email email = new Email();
            email.setTo(TdarConfiguration.getInstance().getSystemAdminEmail());
            email.setUserGenerated(false);
            email.setSubject(TdarConfiguration.getInstance().getSiteAcronym() + " Expired User Access To Collection");
            Map<String, Object> map = new HashMap<>();
            map.put("notes", adminNotes);
            emailService.queueWithFreemarkerTemplate("expire/expire-admin.ftl", map, email);
        }
    }

    @Override
    public Class<TimedAccessRestriction> getPersistentClass() {
        return TimedAccessRestriction.class;
    }

}
