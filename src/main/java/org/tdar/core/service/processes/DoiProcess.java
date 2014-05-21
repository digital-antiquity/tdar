package org.tdar.core.service.processes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.dao.external.pid.ExternalIDProvider;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.core.service.AbstractConfigurableService;
import org.tdar.core.service.ConfigurableService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.EmailService;
import org.tdar.utils.Pair;

@Component
public class DoiProcess extends ScheduledBatchProcess<InformationResource> {

    public static final String SUBJECT = " DOI Creation Info";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final long serialVersionUID = 6004534173920064945L;

    public static final String DELETED = "deleted";
    public static final String UPDATED = "updated";
    public static final String CREATED = "created";
    public static final String DOI_KEY = "DOI";

    @Autowired
    private transient UrlService urlService;
    @Autowired
    private transient DatasetDao datasetDao;
    @Autowired
    private transient EmailService emailService;

    private ConfigurableService<ExternalIDProvider> providers = new AbstractConfigurableService<ExternalIDProvider>() {
        @Override
        public boolean isServiceRequired() {
            // no need to check that the provider used is null, because this service will not be enabled if no provider is found.
            return false;
        }
    };

    /**
     * Used in testing
     * 
     * @return the providers
     */
    public ConfigurableService<ExternalIDProvider> getProviders() {
        return providers;
    }

    private Map<String, List<Pair<Long, String>>> batchResults = new HashMap<>();

    public DoiProcess() {
        initializeBatchResults();
    }

    private void initializeBatchResults() {
        batchResults.put(DELETED, new ArrayList<Pair<Long, String>>());
        batchResults.put(UPDATED, new ArrayList<Pair<Long, String>>());
        batchResults.put(CREATED, new ArrayList<Pair<Long, String>>());
    }

    @Override
    public String getDisplayName() {
        return "DOI Update Process";
    }

    @Override
    public Class<InformationResource> getPersistentClass() {
        return InformationResource.class;
    }

    @Override
    public List<Long> findAllIds() {
        return datasetDao.findRecentlyUpdatedItemsInLastXDaysForExternalIdLookup(2);
    }

    @Override
    public void execute() {
        ExternalIDProvider idProvider = providers.getProvider();
        try {
            idProvider.connect();
            processBatch(getNextBatch());
            idProvider.logout();
        } catch (IOException e) {
            logger.error("connection issues with idProvider " + idProvider, e);
            throw new TdarRuntimeException(e);
        }
    }

    @Override
    public void process(InformationResource resource) throws Exception {
        ExternalIDProvider idProvider = providers.getProvider();
        if (resource.getStatus() == Status.ACTIVE) {
            if (StringUtils.isEmpty(resource.getExternalId())) {
                Map<String, String> createdIds = idProvider.create(resource, urlService.absoluteUrl(resource));
                resource.setExternalId(createdIds.get(DOI_KEY));
                datasetDao.saveOrUpdate(resource);
                batchResults.get(CREATED).add(new Pair<>(resource.getId(), resource.getExternalId()));
            } else {
                idProvider.modify(resource, urlService.absoluteUrl(resource), resource.getExternalId());
                batchResults.get(UPDATED).add(new Pair<>(resource.getId(), resource.getExternalId()));
            }
            logger.debug("setting external id {} for {} ", resource.getExternalId(), resource.getId());
        } else {
            if (StringUtils.isNotEmpty(resource.getExternalId())) {
                idProvider.delete(resource, urlService.absoluteUrl(resource), resource.getExternalId());
                batchResults.get(DELETED).add(new Pair<>(resource.getId(), resource.getExternalId()));
            }
        }
    }

    @Override
    protected void batchCleanup() {
        if (batchResults != null) {
            long total = 0;
            Map<String, Object> map = new HashMap<>();
            map.put(DoiProcess.CREATED, batchResults.get(DoiProcess.CREATED));
            map.put(DoiProcess.UPDATED, batchResults.get(DoiProcess.UPDATED));
            map.put(DoiProcess.DELETED, batchResults.get(DoiProcess.DELETED));
            total += batchResults.get(DoiProcess.CREATED).size();
            total += batchResults.get(DoiProcess.UPDATED).size();
            total += batchResults.get(DoiProcess.DELETED).size();
            map.put("total", total);
            map.put("date", new Date());
            if (total > 0) {
                logger.info("sending email");
                emailService.sendWithFreemarkerTemplate("doi-daily.ftl", map, emailService.getTdarConfiguration().getSiteAcronym() + SUBJECT);
            }
            batchResults.clear();
            initializeBatchResults();
        }
    }

    public Map<String, List<Pair<Long, String>>> getBatchResults() {
        return batchResults;
    }

    @Autowired
    private void setAllServices(List<ExternalIDProvider> providers) {
        ((AbstractConfigurableService<ExternalIDProvider>) this.providers).setAllServices(providers);
    }

    @Override
    public boolean isEnabled() {
        ExternalIDProvider idProvider = providers.getProvider();
        return (idProvider != null) && idProvider.isConfigured();
    }
}
