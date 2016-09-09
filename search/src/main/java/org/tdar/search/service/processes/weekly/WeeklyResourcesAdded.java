package org.tdar.search.service.processes.weekly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.AbstractScheduledProcess;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.SearchService;
import org.tdar.utils.MessageHelper;

@Component
@Scope("prototype")
public class WeeklyResourcesAdded extends AbstractScheduledProcess {

    private static final String MM_DD_YYYY = "MM/dd/yyyy";
    private static final long serialVersionUID = -121034534408651405L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient SearchService<Resource> searchService;
    @Autowired
    private transient SearchIndexService searchIndexService;

    @Autowired
    private transient EmailService emailService;

    @Autowired
    private transient GenericService genericService;

    private boolean run = false;

    @Override
    public void execute() {
        DateTime time = DateTime.now().minusDays(7);
        Collection<? extends Resource> resources = new ArrayList<>();
        ResourceCollection collection = new ResourceCollection(CollectionType.SHARED);
        try {
            MessageHelper messageHelper = MessageHelper.getInstance();

            resources = searchService.findRecentResourcesSince(time.toDate(), null, messageHelper);
            collection.markUpdated(genericService.find(TdarUser.class, config.getAdminUserId()));
            collection.setHidden(true);
            collection.setSystemManaged(true);
            collection.setName(messageHelper.getText("weekly_collection.name", Arrays.asList(time.toString(MM_DD_YYYY), new DateTime().toString(MM_DD_YYYY))));
            collection.setDescription(messageHelper.getText("weekly_collection.description",
                    Arrays.asList(time.toString(MM_DD_YYYY), new DateTime().toString(MM_DD_YYYY), config.getSiteAcronym())));
            collection.setSortBy(SortOption.RESOURCE_TYPE);
            genericService.saveOrUpdate(collection);
            for (Resource r : resources) {
            	collection.getResources().add(r);
            	r.getResourceCollections().add(collection);
                genericService.saveOrUpdate(r);
            }
            collection.getResources().addAll(resources);
            genericService.saveOrUpdate(collection);
            searchIndexService.indexCollection(resources);
        } catch (Exception e) {
            logger.error("issue in recent resources report", e);
        }
        if (CollectionUtils.isNotEmpty(resources)) {
            Email email = new Email();
            email.setDate(new Date());
            email.setFrom(config.getDefaultFromEmail());
            email.setTo(config.getContactEmail());
            email.setSubject(String.format("There are %s new resources in %s", resources.size(), config.getSiteAcronym()));
            email.setUserGenerated(false);
            Map<String, Object> dataModel = initDataModel();
            dataModel.put("resources", resources);
            dataModel.put("totalResources", resources.size());
            dataModel.put("collection", collection);
            dataModel.put("collectionUrl",UrlService.absoluteUrl(collection));
            emailService.queueWithFreemarkerTemplate("email_recent_resources.ftl", dataModel, email);
        }

        run = true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Weekly Resources Added Task";
    }

    @Override
    public boolean isCompleted() {
        return run;
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

}
