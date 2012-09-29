package org.tdar.core.service.processes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.ExternalIDProvider;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.service.AbstractConfigurableService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.EmailService;
import org.tdar.utils.Pair;

@Component
public class DoiProcess extends AbstractConfigurableService<ExternalIDProvider> implements ScheduledProcess<InformationResource> {

    private static final long serialVersionUID = 6004534173920064945L;

    public static final String DELETED = "DELETED";
    public static final String UPDATED = "UPDATED";
    public static final String CREATED = "CREATED";
    public static final String DOI_KEY = "DOI";

    @Autowired
    private UrlService urlService;
    @Autowired
    private DatasetDao datasetDao;
    @Autowired
    private GenericDao genericDao;
    @Autowired
    private EmailService emailService;

    private Map<String, List<Pair<Long, String>>> batchResults = new HashMap<String, List<Pair<Long, String>>>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private transient Long lastId = -1L;

    public DoiProcess() {
        initializeBatchResults();
    }

    private void initializeBatchResults() {
        batchResults.put(DELETED, new ArrayList<Pair<Long, String>>());
        batchResults.put(UPDATED, new ArrayList<Pair<Long, String>>());
        batchResults.put(CREATED, new ArrayList<Pair<Long, String>>());
    }

    public ExternalIDProvider getIdProvider() {
        return getProvider();
    }

    public boolean isConfigured() {
        return true;
    }

    public String getDisplayName() {
        return "DOI Update Process";
    }

    public Class<InformationResource> getPersistentClass() {
        return InformationResource.class;
    }

    public List<Long> getPersistableIdQueue() {
        return datasetDao.findRecentlyUpdatedItemsInLastXDaysForExternalIdLookup(2);
    }

    public void processBatch(List<Long> ids) throws ClientProtocolException, IOException {
        // FIXME: can this guard go into isConfigured instead?
        ExternalIDProvider idProvider = getIdProvider();
        if (idProvider == null || !idProvider.isConfigured()) {
            return;
        }
        getProvider().connect();
        for (InformationResource resource : genericDao.findAll(getPersistentClass(), ids)) {
            try {
                process(resource);
            } catch (Exception e) {
                logger.error("Error while creating a DOI for resource " + resource, e);
            }
        }
        getIdProvider().logout();
    }

    public void process(InformationResource resource) throws Exception {
        ExternalIDProvider idProvider = getProvider();
        if (resource.getStatus() == Status.ACTIVE) {
            if (StringUtils.isEmpty(resource.getExternalId())) {
                Map<String, String> createdIds = idProvider.create(resource, urlService.absoluteUrl(resource));
                resource.setExternalId(createdIds.get(DOI_KEY));
                genericDao.saveOrUpdate(resource);
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

    public void cleanup() {
        StringBuilder sb = new StringBuilder();
        long total = 0;
        if (batchResults != null) {
            sb.append(DoiProcess.CREATED).append(":").append(batchResults.get(DoiProcess.CREATED)).append("\r\n");
            sb.append(DoiProcess.UPDATED).append(":").append(batchResults.get(DoiProcess.UPDATED)).append("\r\n");
            sb.append(DoiProcess.DELETED).append(":").append(batchResults.get(DoiProcess.DELETED)).append("\r\n");
            total += batchResults.get(DoiProcess.CREATED).size();
            total += batchResults.get(DoiProcess.UPDATED).size();
            total += batchResults.get(DoiProcess.DELETED).size();
        }
        if (sb.length() > 0 && total > 0) {
            logger.info("sending email");
            emailService.send(sb.toString(), "tDAR: DOI Creation Info", TdarConfiguration.getInstance().getSystemAdminEmail());
        }
        batchResults.clear();
        initializeBatchResults();
    }

    @Override
    public boolean isShouldRunOnce() {
        return false;
    }

    public String toString() {
        return getDisplayName();
    }

    @Override
    public Long getLastId() {
        return lastId;
    }

    @Override
    public void setLastId(Long lastId) {
        this.lastId = lastId;
    }

    public Map<String, List<Pair<Long, String>>> getBatchResults() {
        return batchResults;
    }

    @Override
    public int getBatchSize() {
        return TdarConfiguration.getInstance().getScheduledProcessBatchSize();
    }
}
