package org.tdar.core.service.processes.daily;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class DailyTimedAccessRevokingProcess extends AbstractScheduledBatchProcess<ResourceCollection> {

    private static final long serialVersionUID = 7534566757094920406L;
    public TdarConfiguration config = TdarConfiguration.getInstance();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private EmailService emailService;

    @Override
    public String getDisplayName() {
        return "Timed Access Revoking Process";

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
    public void process(ResourceCollection persistable) throws Exception {
        DateTime now = DateTime.now();
        String name = getCollectionName(persistable);
        List<AuthorizedUser> toRemove = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        evaluateAuthorizedUsersForCollection(persistable, now, name, toRemove, sb);

        if (CollectionUtils.isNotEmpty(toRemove)) {
            persistable.getAuthorizedUsers().removeAll(toRemove);

            genericDao.saveOrUpdate(persistable);
            genericDao.delete(persistable);
            TdarUser user = genericDao.find(TdarUser.class, TdarConfiguration.getInstance().getAdminUserId());
            CollectionRevisionLog crl = new CollectionRevisionLog(sb.toString(), (ResourceCollection)persistable, user, RevisionLogType.EDIT);
            crl.setResourceCollection((ResourceCollection)persistable);
            genericDao.saveOrUpdate(crl);
            logger.debug("result: {}", persistable.getAuthorizedUsers());
            publisher.publishEvent(new TdarEvent(persistable, EventType.CREATE_OR_UPDATE));
        }
    }

    private void evaluateAuthorizedUsersForCollection(ResourceCollection persistable, DateTime now, String name, List<AuthorizedUser> toRemove, StringBuilder sb) {
        for (AuthorizedUser au : persistable.getAuthorizedUsers()) {

            Date dateExpires = au.getDateExpires();
            if (dateExpires != null && now.isAfter(new DateTime(dateExpires))) {
                ownerNotes.putIfAbsent(au.getCreatedBy(), new ArrayList<>());
                userNotes.putIfAbsent(au.getUser(), new ArrayList<>());
                ownerNotes.get(au.getCreatedBy()).add(String.format("- %s ; removed access for %s", name, au.getUser().getName()));
                userNotes.get(au.getUser()).add(String.format("- removed access to %s", name));
                String msg = String.format("disabling authorized user (%s) for %s", au, name);
                logger.debug(msg);
                sb.append(msg);
                sb.append("\n");
                toRemove.add(au);
            }
        }
    }

    private String getCollectionName(ResourceCollection persistable) {
        String name = "";
        if (persistable instanceof HasName) {
            name = String.format("%s (%s)", ((HasName) persistable).getName(), persistable.getId());
//        } else if ( persistable instanceof InternalCollection) {
//            InternalCollection ic = (InternalCollection)persistable;
//            if (CollectionUtils.isNotEmpty(ic.getResources())) {
//            Resource next = ic.getResources().iterator().next();
//            name = String.format("%s (%s)", next.getName(), next.getId());
//            }
        }
        return name;
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
            List<String> notes = ownerNotes.getOrDefault(owner, new ArrayList<>());
            map.put("notes", notes);
            emailService.queueWithFreemarkerTemplate("expire/expire_owner.ftl", map, email);
            logger.debug("user notes: {}", notes);
            adminNotes.addAll(notes);
        }

        for (TdarUser owner : userNotes.keySet()) {
            Email email = new Email();
            email.setUserGenerated(false);
            email.setTo(owner.getEmail());
            email.setSubject(TdarConfiguration.getInstance().getSiteAcronym() + " Expired Access To Collection");
            Map<String, Object> map = new HashMap<>();
            map.put("user", owner);
            map.put("notes", ownerNotes.getOrDefault(owner, new ArrayList<>()));
            logger.debug("owner notes: {}", ownerNotes);
            emailService.queueWithFreemarkerTemplate("expire/expire_user.ftl", map, email);
        }

        if (CollectionUtils.isNotEmpty(adminNotes)) {
            Email email = new Email();
            email.setTo(TdarConfiguration.getInstance().getSystemAdminEmail());
            email.setUserGenerated(false);
            email.setSubject(TdarConfiguration.getInstance().getSiteAcronym() + " Expired User Access To Collection");
            Map<String, Object> map = new HashMap<>();
            map.put("notes", adminNotes);
            logger.debug("admin notes: {}", adminNotes);
            emailService.queueWithFreemarkerTemplate("expire/expire_admin.ftl", map, email);
        }
    }

    @Override
    public Class<ResourceCollection> getPersistentClass() {
        return ResourceCollection.class;
    }

}
