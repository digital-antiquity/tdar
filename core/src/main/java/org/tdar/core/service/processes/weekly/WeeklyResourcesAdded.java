package org.tdar.core.service.processes.weekly;

import java.util.ArrayList;
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
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.AbstractScheduledProcess;
import org.tdar.core.service.search.SearchService;
import org.tdar.utils.MessageHelper;

@Component
@Scope("prototype")
public class WeeklyResourcesAdded extends AbstractScheduledProcess {

    private static final long serialVersionUID = -121034534408651405L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient SearchService searchService;

    @Autowired
    private transient EmailService emailService;

    private boolean run = false;

    @Override
    public void execute() {
        DateTime time = DateTime.now().minusDays(7);
        Collection<? extends Resource> resources  = new ArrayList<>();
        try {
         resources = searchService.findRecentResourcesSince(time.toDate(), null, MessageHelper.getInstance());
        } catch(Exception e) {
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
