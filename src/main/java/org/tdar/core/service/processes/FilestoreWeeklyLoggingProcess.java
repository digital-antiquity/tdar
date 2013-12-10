package org.tdar.core.service.processes;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.resource.InformationResourceFileVersionService;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.Filestore.LogType;

@Component
public class FilestoreWeeklyLoggingProcess extends ScheduledProcess.Base<HomepageGeographicKeywordCache> {

    public static final String PROBLEM_FILES_REPORT = "Problem Files Report";

    private static final long serialVersionUID = -6196804675468219433L;

    @Autowired
    private InformationResourceFileVersionService informationResourceFileVersionService;

    @Autowired
    private FreemarkerService freemarkerService;

    @Autowired
    private EmailService emailService;

    int batchCount = 0;
    boolean run = false;

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

        StringBuffer subject = new StringBuffer(PROBLEM_FILES_REPORT);
        int count = 0;
        for (InformationResourceFileVersion version : informationResourceFileVersionService.findAll()) {
            try {
                if (!filestore.verifyFile(version)) {
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
        }

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
