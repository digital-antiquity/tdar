package org.tdar.core.service.processes;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.EmailService;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.Filestore.LogType;
import org.tdar.filestore.Filestore.ObjectType;

@Component
public class WeeklyFilestoreLoggingProcess extends ScheduledProcess.Base<HomepageGeographicKeywordCache> {

    public static final String PROBLEM_FILES_REPORT = "Problem Files Report";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = -6196804675468219433L;

    @Autowired
    private transient GenericService genericService;

    @Autowired
    private transient FreemarkerService freemarkerService;

    @Autowired
    private transient EmailService emailService;

    private boolean run = false;

    @Override
    public void execute() {
        run = true;
        logger.info("beginning automated verification of files");
        Filestore filestore = getTdarConfiguration().getFilestore();
        List<InformationResourceFileVersion> missing = new ArrayList<InformationResourceFileVersion>();
        List<InformationResourceFileVersion> other = new ArrayList<InformationResourceFileVersion>();
        List<InformationResourceFileVersion> tainted = new ArrayList<InformationResourceFileVersion>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("other", other);
        map.put("tainted", tainted);
        map.put("missing", missing);
        Thread.yield();
        StringBuffer subject = new StringBuffer(PROBLEM_FILES_REPORT);
        int count = 0;
        ScrollableResults scrollableResults = genericService.findAllScrollable(InformationResourceFileVersion.class);

        while (scrollableResults.next()) {
            Object item = scrollableResults.get(0);
            InformationResourceFileVersion version = (InformationResourceFileVersion)item;
            try {
                if (!filestore.verifyFile(ObjectType.RESOURCE, version)) {
                    count++;
                    tainted.add(version);
                }
            } catch (FileNotFoundException e) {
                count++;
                missing.add(version);
                logger.debug("file not found ", e);
            } catch (Exception e) {
                count++;
                tainted.add(version);
                logger.debug("other error ", e);
            }
            if ((count % 10) == 0) {
                Thread.yield();
                if ((count % 10_000) == 0) {
                    try {
                        Thread.sleep(1_000l);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        scrollableResults.close();

        if (count == 0) {
            subject.append(" [NONE]");
        }
        else {
            subject.append(" [" + count + "]");
        }
        map.put("siteAcronym", TdarConfiguration.getInstance().getSiteAcronym());
        map.put("baseUrl", TdarConfiguration.getInstance().getBaseUrl());
        map.put("dateRun", new Date());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String filename = "verify-" + df.format(new Date()) + ".txt";
        try {
            String message = freemarkerService.render("filestore-stats.ftl", map);
            filestore.storeLog(LogType.FILESTORE_VERIFICATION, filename, message);

            logger.debug(subject + "[ " + getTdarConfiguration().getSystemAdminEmail() + " ]");
            logger.debug(message);
            emailService.send(message, subject.toString());
            logger.info("ending automated verification of files");
        } catch (Exception e) {
            logger.error("eception occurred when testing filestore", e);
        }
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Filestore Verification Task";
    }

    @Override
    public Class<HomepageGeographicKeywordCache> getPersistentClass() {
        return null;
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
