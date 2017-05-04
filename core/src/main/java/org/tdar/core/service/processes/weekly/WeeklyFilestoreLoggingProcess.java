package org.tdar.core.service.processes.weekly;

import java.lang.reflect.InvocationTargetException;
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
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.InformationResourceFileVersionProxy;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.AbstractScheduledProcess;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.Filestore.LogType;

@Component
@Scope("prototype")
public class WeeklyFilestoreLoggingProcess extends AbstractScheduledProcess {

    public static final String PROBLEM_FILES_REPORT = "Problem Files Report";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = -6196804675468219433L;

    @Autowired
    private transient InformationResourceFileService informationResourceFileService;

    @Autowired
    private transient FreemarkerService freemarkerService;

    @Autowired
    private transient EmailService emailService;

    @Autowired
    private transient ThreadPoolTaskExecutor taskExecutor;

    private boolean run = false;
    private ScrollableResults scrollableResults;
    private List<FileStoreFileProxy> missing = new ArrayList<>();
    private List<FileStoreFileProxy> other = new ArrayList<>();
    private List<FileStoreFileProxy> tainted = new ArrayList<>();
    private int count = 0;
    private Filestore filestore = getTdarConfiguration().getFilestore();

    @Override
    public void execute() {
//        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        logger.info("beginning automated verification of files");
        scrollableResults = informationResourceFileService.findScrollableVersionsForVerification();
        for (int i = 0; i < taskExecutor.getCorePoolSize(); i++) {
            taskExecutor.execute(new FilestoreVerificationTask(filestore, this));
        }

        while (taskExecutor.getActiveCount() != 0) {
            int count = taskExecutor.getActiveCount();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (count == 0) {
                taskExecutor.shutdown();
                break;
            }
        }
        report();
        run = true;
    }

    private synchronized FileStoreFileProxy setupVersionFromResult() throws IllegalAccessException, InvocationTargetException {
        Long irId = scrollableResults.getLong(0);
        Long irfId = scrollableResults.getLong(1);
        Integer latestVersion = scrollableResults.getInteger(2);
        InformationResourceFileVersionProxy versionProxy = (InformationResourceFileVersionProxy) scrollableResults.get(3);
        InformationResourceFile irf = new InformationResourceFile();
        irf.setLatestVersion(latestVersion);
        irf.setId(irfId);
        InformationResourceFileVersion version = versionProxy.generateInformationResourceFileVersion();
        version.setInformationResourceFileId(irfId);
        version.setInformationResourceId(irId);
        version.setInformationResourceFile(irf);
        return version;
    }

    public synchronized List<FileStoreFileProxy> createThreadBatch() {
        List<FileStoreFileProxy> current = new ArrayList<>();
        if (scrollableResults == null) {
            return current;
        }
        for (int i = 0; i < 10; i++) {
            if (scrollableResults != null && scrollableResults.next()) {
                try {
                    FileStoreFileProxy result = setupVersionFromResult();
                    current.add(result);
                } catch (Exception e) {
                    logger.error("error in loading from resultsset: {}", e);
                }
            }
            else {
                if (scrollableResults != null) {
                    scrollableResults.close();
                    scrollableResults = null;
                }
            }
        }
        return current;
    }

    public void report() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("other", other);
        map.put("tainted", tainted);
        map.put("missing", missing);
        Thread.yield();
        StringBuffer subject = new StringBuffer(PROBLEM_FILES_REPORT);
        int totalIssues = other.size() + tainted.size() + missing.size();

        if (totalIssues == 0) {
            subject.append(" [NONE]");
        }
        else {
            subject.append(" [" + totalIssues + "]");
        }
        map.put("siteAcronym", TdarConfiguration.getInstance().getSiteAcronym());
        map.put("baseUrl", TdarConfiguration.getInstance().getBaseUrl());
        map.put("dateRun", new Date());
        map.put("count", count);
        map.put("totalIssues", totalIssues);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String filename = "verify-" + df.format(new Date()) + ".txt";
        try {
            String message = freemarkerService.render("filestore-stats.ftl", map);
            filestore.storeLog(LogType.FILESTORE_VERIFICATION, filename, message);

            logger.debug(subject + "[ " + getTdarConfiguration().getSystemAdminEmail() + " ]");
            logger.debug(message);
            Email email = new Email();
            email.setSubject(subject.toString());
            email.setMessage(message);
            emailService.queue(email);
            logger.info("ending automated verification of files");
        } catch (Exception e) {
            logger.error("exception occurred when testing filestore", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Filestore Verification Task";
    }

    @Override
    public boolean isCompleted() {
        return run;
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    @Override
    public void cleanup() {
        missing.clear();
        tainted.clear();
        other.clear();
        count = 0;
        run = false;
    }

    public synchronized void updateCounts(List<FileStoreFileProxy> proxyList, List<FileStoreFileProxy> tainted_, List<FileStoreFileProxy> missing_) {
        count += proxyList.size();
        this.missing.addAll(missing_);
        this.tainted.addAll(tainted_);
    }

}
