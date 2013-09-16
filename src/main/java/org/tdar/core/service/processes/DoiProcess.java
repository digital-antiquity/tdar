package org.tdar.core.service.processes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.dao.external.pid.ExternalIDProvider;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.EmailService;
import org.tdar.utils.Pair;

@Component
public class DoiProcess extends ScheduledBatchProcess<InformationResource> {

    public static final String SUBJECT = " DOI Creation Info";

    private static final long serialVersionUID = 6004534173920064945L;

    public static final String DELETED = "deleted";
    public static final String UPDATED = "updated";
    public static final String CREATED = "created";
    public static final String DOI_KEY = "DOI";

    @Autowired
    private UrlService urlService;
    @Autowired
    private DatasetDao datasetDao;
    @Autowired
    private EmailService emailService;

    private List<ExternalIDProvider> allServices;
    private Map<String, List<Pair<Long, String>>> batchResults = new HashMap<String, List<Pair<Long, String>>>();

    public DoiProcess() {
        initializeBatchResults();
    }

    private void initializeBatchResults() {
        batchResults.put(DELETED, new ArrayList<Pair<Long, String>>());
        batchResults.put(UPDATED, new ArrayList<Pair<Long, String>>());
        batchResults.put(CREATED, new ArrayList<Pair<Long, String>>());
    }

    public String getDisplayName() {
        return "DOI Update Process";
    }

    public Class<InformationResource> getPersistentClass() {
        return InformationResource.class;
    }

    @Override
    public List<Long> findAllIds() {
        return datasetDao.findRecentlyUpdatedItemsInLastXDaysForExternalIdLookup(2);
    }

    @Override
    public void execute() {
        ExternalIDProvider idProvider = getProvider();
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
        ExternalIDProvider idProvider = getProvider();
        if (resource.getStatus() == Status.ACTIVE) {
            if (StringUtils.isEmpty(resource.getExternalId())) {
                Map<String, String> createdIds = idProvider.create(resource, urlService.absoluteUrl(resource));
                resource.setExternalId(createdIds.get(DOI_KEY));
                datasetDao.saveOrUpdate(resource);
                batchResults.get(CREATED).add(new Pair<Long, String>(resource.getId(), resource.getExternalId()));
            } else {
                idProvider.modify(resource, urlService.absoluteUrl(resource), resource.getExternalId());
                batchResults.get(UPDATED).add(new Pair<Long, String>(resource.getId(), resource.getExternalId()));
            }
            logger.debug("setting external id {} for {} ", resource.getExternalId(), resource.getId());
        } else {
            if (StringUtils.isNotEmpty(resource.getExternalId())) {
                idProvider.delete(resource, urlService.absoluteUrl(resource), resource.getExternalId());
                batchResults.get(DELETED).add(new Pair<Long, String>(resource.getId(), resource.getExternalId()));
            }
        }
    }

    @Override
    protected void batchCleanup() {
        long total = 0;
        Map<String, Object> map = new HashMap<String, Object>();
        if (batchResults != null) {
            map.put(DoiProcess.CREATED, batchResults.get(DoiProcess.CREATED));
            map.put(DoiProcess.UPDATED, batchResults.get(DoiProcess.UPDATED));
            map.put(DoiProcess.DELETED, batchResults.get(DoiProcess.DELETED));
            total += batchResults.get(DoiProcess.CREATED).size();
            total += batchResults.get(DoiProcess.UPDATED).size();
            total += batchResults.get(DoiProcess.DELETED).size();
        }
        map.put("total", total);
        map.put("date", new Date());

        if (total > 0) {
            logger.info("sending email");
            emailService.sendTemplate("doi-daily.ftl", map, emailService.getTdarConfiguration().getSiteAcronym() + SUBJECT);
        }
        batchResults.clear();
        initializeBatchResults();
    }

    public Map<String, List<Pair<Long, String>>> getBatchResults() {
        return batchResults;
    }

    public List<ExternalIDProvider> getAllServices() {
        return allServices;
    }

    @Autowired
    public void setAllServices(List<ExternalIDProvider> providers) {
        allServices = providers;
        Iterator<ExternalIDProvider> iterator = allServices.iterator();
        while (iterator.hasNext()) {
            ExternalIDProvider provider = iterator.next();
            if (provider.isConfigured()) {
                logger.debug("enabling {} provider: {} will use first", getClass().getSimpleName(), provider.getClass().getSimpleName());
            } else {
                logger.debug("disabling unconfigured {} provider: {}", getClass().getSimpleName(), provider.getClass().getSimpleName());
                iterator.remove();
            }
        }
    }

    public ExternalIDProvider getProvider() {
        if (CollectionUtils.isEmpty(allServices)) {
            logger.warn("no available provider found for DoiProcess");
            return null;
        }
        return allServices.get(0);
    }

    @Override
    public boolean isEnabled() {
        ExternalIDProvider idProvider = getProvider();
        return idProvider != null && idProvider.isConfigured();
    }
}
