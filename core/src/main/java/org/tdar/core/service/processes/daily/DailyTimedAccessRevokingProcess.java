package org.tdar.core.service.processes.daily;

import java.util.ArrayList;
import java.util.Collection;
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
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.collection.CollectionRevisionLog;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.AbstractScheduledProcess;

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
public class DailyTimedAccessRevokingProcess extends AbstractScheduledProcess {

    private static final long serialVersionUID = 7534566757094920406L;
    public TdarConfiguration config = TdarConfiguration.getInstance();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Autowired
    private GenericDao genericDao;

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
    public void execute() {
        DateTime now = DateTime.now();
        Collection<HasAuthorizedUsers> toProcess = resourceCollectionDao.findExpiringUsers(now.toDate());
        logger.debug("{}", toProcess);
        for (HasAuthorizedUsers persistable : toProcess) {
            String name = getName(persistable);
            List<AuthorizedUser> toRemove = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            evaluateAuthorizedUsersForCollection(persistable, now, name, toRemove, sb);

            if (CollectionUtils.isNotEmpty(toRemove)) {
                persistable.getAuthorizedUsers().removeAll(toRemove);

                genericDao.saveOrUpdate(persistable);
//                genericDao.delete(persistable);
                TdarUser user = genericDao.find(TdarUser.class, TdarConfiguration.getInstance().getAdminUserId());
                if (persistable instanceof ResourceCollection) {
                    CollectionRevisionLog crl = new CollectionRevisionLog(sb.toString(), (ResourceCollection) persistable, user, RevisionLogType.EDIT);
                    crl.setResourceCollection((ResourceCollection) persistable);
                    genericDao.saveOrUpdate(crl);
                }
                if (persistable instanceof Resource) {
                    ResourceRevisionLog crl = new ResourceRevisionLog(sb.toString(), (Resource) persistable, user, RevisionLogType.EDIT);
                    crl.setResource((Resource) persistable);
                    genericDao.saveOrUpdate(crl);
                }
                logger.debug("result: {}", persistable.getAuthorizedUsers());
                publisher.publishEvent(new TdarEvent(persistable, EventType.CREATE_OR_UPDATE));
            }
        }
        sendNotifications();
    }

    private void evaluateAuthorizedUsersForCollection(HasAuthorizedUsers persistable, DateTime now, String name, List<AuthorizedUser> toRemove,
            StringBuilder sb) {
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

    private String getName(HasAuthorizedUsers persistable) {
        String name = "No name";
        if (persistable instanceof HasName) {
            name = String.format("<a href=\"%s%s\">%s</a> (%s)", config.getBaseSecureUrl(), ((Addressable)persistable).getDetailUrl(), ((HasName) persistable).getName(), persistable.getId());
        }
        return name;
    }

    protected void sendNotifications() {
        Set<String> adminNotes = new HashSet<>();
        for (TdarUser owner : ownerNotes.keySet()) {
            Email email = emailService.createMessage(EmailType.ACCESS_EXPIRE_OWNER_NOTIFICATION, owner.getEmail());
            List<String> notes = ownerNotes.getOrDefault(owner, new ArrayList<>());
            email.setUserGenerated(false);
            email.addData("user", owner);
            email.addData("notes", notes);
            logger.debug("owner notes: {}", notes);
            emailService.renderAndQueueMessage(email);
            adminNotes.addAll(notes);
        }

        for (TdarUser owner : userNotes.keySet()) {
            Email email = emailService.createMessage(EmailType.ACCESS_EXPIRE_USER_NOTIFICATION, owner.getEmail());
            email.setUserGenerated(false);
            email.addData("user", owner);
            email.addData("notes", userNotes.getOrDefault(owner, new ArrayList<>()));
            logger.debug("user notes: {}", userNotes);
            emailService.renderAndQueueMessage(email);
        }

        if (CollectionUtils.isNotEmpty(adminNotes)) {
            Email email = emailService.createMessage(EmailType.ACCESS_EXPIRE_ADMIN_NOTIFICATION, TdarConfiguration.getInstance().getSystemAdminEmail());
            email.setUserGenerated(false);
            email.addData("notes", adminNotes);
            logger.debug("admin notes: {}", adminNotes);
            emailService.renderAndQueueMessage(email);
        }
    }

}
