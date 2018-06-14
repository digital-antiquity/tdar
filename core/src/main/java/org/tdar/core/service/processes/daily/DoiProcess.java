package org.tdar.core.service.processes.daily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.pid.ExternalIDProvider;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;
import org.tdar.utils.Pair;

@Component
@Scope("prototype")
public class DoiProcess extends AbstractScheduledBatchProcess<Resource> {

    public static final String SUBJECT = " DOI Creation Info";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final long serialVersionUID = 6004534173920064945L;

    public static final String DELETED = "deleted";
    public static final String UPDATED = "updated";
    public static final String CREATED = "created";
    private static final String ERRORS = "errors";
    public static final String DOI_KEY = "DOI";

    @Autowired
    private transient DatasetDao datasetDao;
    @Autowired
    private transient EmailService emailService;

    private ExternalIDProvider provider;

    private Map<String, List<Pair<Long, String>>> batchResults = new HashMap<>();

    public DoiProcess() {
        initializeBatchResults();
    }

    private void initializeBatchResults() {
        for (String key : Arrays.asList(DELETED, UPDATED, CREATED, ERRORS)) {
            batchResults.put(key, new ArrayList<Pair<Long, String>>());
        }
    }

    @Override
    public String getDisplayName() {
        return "DOI Update Process";
    }

    @Override
    public Class<Resource> getPersistentClass() {
        return Resource.class;
    }

    @Override
    public List<Long> findAllIds() {
        return datasetDao.findRecentlyUpdatedItemsInLastXDaysForExternalIdLookup(2);
    }

    @Override
    public void execute() {
        if (!provider.isConfigured()) {
            return;
        }
        try {
            provider.connect();
            super.execute();
            provider.logout();
        } catch (Throwable e) {
            logger.debug(ExceptionUtils.getFullStackTrace(e));
            logger.error("connection issues with provider " + provider, e);
            throw new TdarRuntimeException(e);
        }
    }

    @Override
    public void process(Resource resource) throws Exception {
        logger.trace("processing: {}", resource);
        if (resource.getStatus() == Status.ACTIVE) {
            if (StringUtils.isEmpty(resource.getExternalId())) {
                Map<String, String> createdIds = provider.create(resource, UrlService.absoluteUrl(resource));
                String externalId = createdIds.get(DOI_KEY);
                if (StringUtils.isNotBlank(externalId)) {
                    resource.setExternalId(externalId);
                    datasetDao.saveOrUpdate(resource);
                    batchResults.get(CREATED).add(new Pair<>(resource.getId(), resource.getExternalId()));
                } else {
                    batchResults.get(ERRORS).add(new Pair<>(resource.getId(), resource.getExternalId()));
                }
            } else {
                provider.modify(resource, UrlService.absoluteUrl(resource), resource.getExternalId());
                batchResults.get(UPDATED).add(new Pair<>(resource.getId(), resource.getExternalId()));
            }
            logger.debug("setting external id {} for {} ", resource.getExternalId(), resource.getId());
        } else {
            if (StringUtils.isNotEmpty(resource.getExternalId())) {
                provider.delete(resource, UrlService.absoluteUrl(resource), resource.getExternalId());
                batchResults.get(DELETED).add(new Pair<>(resource.getId(), resource.getExternalId()));
            }
        }
    }

    @Override
    protected void batchCleanup() {
        if (batchResults != null) {
            long total = 0;
            Map<String, Object> map = new HashMap<>();
            for (String key : Arrays.asList(DELETED, UPDATED, CREATED, ERRORS)) {
                map.put(key, batchResults.get(key));
                total += batchResults.get(key).size();
            }
            map.put("total", total);
            map.put("date", new Date());
            if (total > 0) {
                logger.info("sending email");
                Email email = new Email();
                email.setUserGenerated(false);
                email.setSubject(TdarConfiguration.getInstance().getSiteAcronym() + SUBJECT);
                emailService.queueWithFreemarkerTemplate("doi-daily.ftl", map, email);
            }
            batchResults.clear();
            initializeBatchResults();
        }
    }

    public Map<String, List<Pair<Long, String>>> getBatchResults() {
        return batchResults;
    }

    @Override
    public boolean isEnabled() {
        return (provider != null) && provider.isConfigured();
    }

    public ExternalIDProvider getProvider() {
        return provider;
    }

    @Autowired(required = false)
    @Qualifier("DoiProvider")
    public void setProvider(ExternalIDProvider provider) {
        this.provider = provider;
        if (provider != null) {
            logger.debug("DOI Provider: {}", provider.getClass().getSimpleName());
        } else {
            logger.debug("DOI Provider: NOT CONFIGURED");
        }
    }
}
