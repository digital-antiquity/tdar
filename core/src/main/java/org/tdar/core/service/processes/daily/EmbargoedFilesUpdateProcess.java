package org.tdar.core.service.processes.daily;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.AbstractScheduledProcess;
import org.tdar.core.service.resource.InformationResourceFileService;

@Component
@Scope("prototype")
public class EmbargoedFilesUpdateProcess extends AbstractScheduledProcess {

    private static final String SITE_ACRONYM = TdarConfiguration.getInstance().getSiteAcronym();
    private static final String TEMPLATE_ADMIN = "embargo/expiration-admin.ftl";
    private static final String SUBJECT_ADMIN = SITE_ACRONYM + ": Admin Embargo Expiration Warning";
    private static final String BASE_URL = TdarConfiguration.getInstance().getBaseUrl();

    private static final long serialVersionUID = 5134091457634096415L;


    private static final String TEMPLATE_WARNING = "embargo/expiration-warning.ftl";
    private static final String SUBJECT_WARNING = SITE_ACRONYM + ": Embargo about to expire";
    private static final String TEMPLATE_EXPIRED = "embargo/expiration.ftl";
    private static final String SUBJECT_EXPIRED = SITE_ACRONYM + ": Embargo Expired";

    @Autowired
    private transient EmailService emailService;
    @Autowired
    private transient GenericDao genericDao;

    @Autowired
    private transient InformationResourceFileService informationResourceFileService;

    private boolean completed;

    @Override
    public String getDisplayName() {
        return "Embargoed Files Process";
    }

    @Override
    public void execute() {
        List<InformationResourceFile> expired = informationResourceFileService.findAllExpiredEmbargoFiles();
        List<InformationResourceFile> toExpire = informationResourceFileService.findAllEmbargoFilesExpiringTomorrow();

        if (CollectionUtils.isEmpty(expired) && CollectionUtils.isEmpty(toExpire)) {
            completed = true;
            return;
        }
        logger.debug("expired: {} : {}", CollectionUtils.size(expired), expired);
        logger.debug("expired: {} : {}", CollectionUtils.size(toExpire), toExpire);

        sendAdminEmail(expired, toExpire);
        
        sendExpirationNotices(toExpire);

        if (CollectionUtils.isNotEmpty(expired)) {
            Map<TdarUser, Set<InformationResourceFile>> expiredMap = createMap(expired);
            expire(expiredMap);
        }

        completed = true;
    }

    private void sendAdminEmail(List<InformationResourceFile> expired, List<InformationResourceFile> toExpire) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("expired", expired);
        map.put("toExpire", toExpire);
        map.put("siteAcronym", SITE_ACRONYM);
        map.put("baseUrl", BASE_URL);
        Email email = new Email();
        email.setSubject(SUBJECT_ADMIN);
        email.setUserGenerated(false);
        emailService.queueWithFreemarkerTemplate(TEMPLATE_ADMIN, map, email);
    }

    private Map<TdarUser, Set<InformationResourceFile>> createMap(Collection<InformationResourceFile> toExpire) {
        Map<TdarUser, Set<InformationResourceFile>> expiredMap = new HashMap<>();
        for (InformationResourceFile file : toExpire) {
            InformationResource r = file.getInformationResource();
            TdarUser submitter = r.getSubmitter();
            if (!expiredMap.containsKey(submitter)) {
                expiredMap.put(submitter, new HashSet<>());
            }
            if (r.isActive() || r.isDraft()) {
                expiredMap.get(submitter).add(file);
            }
        }
        return expiredMap;
    }

    private void expire(Map<TdarUser, Set<InformationResourceFile>> expiredMap) {
        TdarUser admin = genericDao.find(TdarUser.class, config.getAdminUserId());
        for (TdarUser submitter : expiredMap.keySet()) {
            Set<InformationResourceFile> expired = expiredMap.get(submitter);
            for (InformationResourceFile file : expired) {
                file.setRestriction(FileAccessRestriction.PUBLIC);
                file.setDateMadePublic(null);
                genericDao.saveOrUpdate(file);
                String msg = String.format("Embargo on file: %s (%s) expired and was set to PUBLIC access", file.getFilename(), file.getId());
                ResourceRevisionLog rrl = new ResourceRevisionLog(msg, file.getInformationResource(), admin, RevisionLogType.EDIT);
                genericDao.saveOrUpdate(rrl);

            }
            sendEmail(submitter, expired, SUBJECT_EXPIRED, TEMPLATE_EXPIRED);
        }
    }

    private void sendEmail(TdarUser submitter, Set<InformationResourceFile> files, String subject, String template) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("files", files);
        Email email = new Email();
        map.put("submitter", submitter);
        map.put("siteAcronym", SITE_ACRONYM);
        map.put("baseUrl", BASE_URL);
        email.setTo(submitter.getEmail());

        email.setUserGenerated(false);
        email.setSubject(subject);
        emailService.queueWithFreemarkerTemplate(template, map, email);
    }

    private void sendExpirationNotices(Collection<InformationResourceFile> toExpire) {
        if (CollectionUtils.isEmpty(toExpire)) {
            return;
        }
        Map<TdarUser, Set<InformationResourceFile>> toExpiredMap = createMap(toExpire);
        for (TdarUser submitter : toExpiredMap.keySet()) {
            Set<InformationResourceFile> expired = toExpiredMap.get(submitter);
            sendEmail(submitter, expired, SUBJECT_WARNING, TEMPLATE_WARNING);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public boolean isSingleRunProcess() {
        // TODO Auto-generated method stub
        return false;
    }
}
