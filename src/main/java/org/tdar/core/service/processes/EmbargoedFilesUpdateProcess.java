package org.tdar.core.service.processes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.resource.InformationResourceFileService;

@Component
public class EmbargoedFilesUpdateProcess extends AbstractScheduledBatchProcess<InformationResourceFile> {

    private static final String SITE_ACRONYM = TdarConfiguration.getInstance().getSiteAcronym();

    private static final long serialVersionUID = 5134091457634096415L;

    private static final String SUBJECT = SITE_ACRONYM + ": Expiration Warning";

    private static final String SUBJECT_WARNING = SITE_ACRONYM + ": Embargo about to expire";
    private static final String SUBJECT_EXPIRED = SITE_ACRONYM + ": Expired";

    @Autowired
    private transient EmailService emailService;

    @Autowired
    private transient InformationResourceFileService informationResourceFileService;

    @Override
    public String getDisplayName() {
        return "Embargoed Files Process";
    }

    @Override
    public int getBatchSize() {
        return 10000;
    }

    @Override
    public Class<InformationResourceFile> getPersistentClass() {
        return InformationResourceFile.class;
    }

    @Override
    public void execute() {
        List<InformationResourceFile> expired = informationResourceFileService.findAllExpiredEmbargoFiles();
        List<InformationResourceFile> toExpire = informationResourceFileService.findAllEmbargoFilesExpiringTomorrow();

        if (CollectionUtils.isEmpty(expired) && CollectionUtils.isEmpty(toExpire)) {
            return;
        }
        logger.debug("expired: {} toExpire: {}", CollectionUtils.size(expired), CollectionUtils.size(toExpire));
        if (CollectionUtils.isNotEmpty(toExpire)) {
            sendExpirationNotices(toExpire);
        }

        if (CollectionUtils.isNotEmpty(expired)) {
            expire(expired);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("expired", expired);
        map.put("toExpired", toExpire);
        Email email = new Email();
        email.setSubject(SUBJECT);
        email.setUserGenerated(false);
        if (CollectionUtils.isNotEmpty(expired) || CollectionUtils.isNotEmpty(toExpire)) {
            emailService.queueWithFreemarkerTemplate("embargo/expiration-admin.ftl", map, email);
        }
    }

    private void expire(List<InformationResourceFile> expired) {
        for (InformationResourceFile file : expired) {
            file.setRestriction(FileAccessRestriction.PUBLIC);
            genericDao.saveOrUpdate(file);
            sendEmail(file, true);
        }

    }

    private void sendEmail(InformationResourceFile file, boolean isExpiration) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("file", file);
        Email email = new Email();
        TdarUser submitter = file.getInformationResource().getSubmitter();
        map.put("submitter", submitter);
        map.put("siteAcronym", SITE_ACRONYM);
        map.put("resource", file.getInformationResource());
        map.put("resourceUrl", file.getInformationResource().getAbsoluteUrl());
        email.setTo(submitter.getEmail());

        email.setUserGenerated(false);
        if (isExpiration) {
            email.setSubject(SUBJECT_EXPIRED);
            emailService.queueWithFreemarkerTemplate("embargo/expiration.ftl", map, email);
        } else {
            email.setSubject(SUBJECT_WARNING);
            emailService.queueWithFreemarkerTemplate("embargo/expiration-warning.ftl", map, email);
        }

    }

    private void sendExpirationNotices(List<InformationResourceFile> toExpire) {
        for (InformationResourceFile file : toExpire) {
            sendEmail(file, false);
        }
    }

    @Override
    public void process(InformationResourceFile account) throws Exception {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
