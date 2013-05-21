package org.tdar.core.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.geotools.resources.image.ImageUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.PairtreeFilestore;

/**
 * $Id$
 * 
 * Configuration file for various tDAR webapp properties.
 * 
 * FIXME: convert to a Spring managed bean (and by extension, Filestore)
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class TdarConfiguration {

    public static final List<String> STOP_WORDS = Arrays.asList("the", "and", "a", "to", "of", "in", "i", "is", "that", "it", "on", "you", "this", "for",
            "but", "with", "are", "have", "be", "at", "or", "as", "was", "so", "if", "out", "not");
    private static final String JIRA_LINK = "https://dev.tdar.org/jira/s/en_USgh0sw9-418945332/844/18/1.2.9/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=959f12a3";
    private static final String DESCRIPTION = "tDAR is an international digital archive and repository that houses data about archaeological investigations, research, resources, and scholarship.  tDAR provides researchers new avenues to discover and integrate information relevant to topics they are studying.   Users can search tDAR for digital documents, data sets, images, GIS files, and other data resources from archaeological projects spanning the globe.  For data sets, users also can use data integration tools in tDAR to simplify and illuminate comparative research.";
    private static final String SECURITY_EXCEPTION_COULD_NOT_CREATE_PERSONAL_FILESTORE_HOME_DIRECTORY = "Security Exception: could not create personal filestore home directory";
    private static final String MESSAGE_QUEUE_IS_ENABLED_BUT_A_USERNAME_AND_PASSWORD_IS_NOT_DEFINED = "Message Queue is enabled, but a username and password is not defined";
    public static final String COULDN_T_CREATE_TEMPORARY_DIRECTORY_AT = "Couldn't create temporary directory at : ";
    public static final int DEFAULT_SCHEDULED_PROCESS_START_ID = 0;
    public static final int DEFAULT_SCHEDULED_PROCESS_END_ID = 400000;

    public static final String DEFAULT_HOSTNAME = "core.tdar.org";
    public static final int DEFAULT_PORT = 80; // we use this in test
    public static final String DEFAULT_SMTP_HOST = "localhost";
    private static final String SYSTEM_ADMIN_EMAIL = "tdar-svn@lists.asu.edu";

    public static final int DEFAULT_AUTHORITY_MANAGEMENT_DUPE_LIST_MAX_SIZE = 10;
    public static final int DEFAULT_AUTHORITY_MANAGEMENT_MAX_AFFECTED_RECORDS = 100;

    public static final int DEFAULT_SEARCH_EXCEL_EXPORT_RECORD_MAX = 1000;

    private final transient static Logger logger = LoggerFactory.getLogger(TdarConfiguration.class);

    private ConfigurationAssistant assistant;

    private Filestore filestore;

    private Set<String> stopWords = new HashSet<String>();
    private String configurationFile;

    private final static TdarConfiguration INSTANCE = new TdarConfiguration();
    public static final String PRODUCTION = "production";
    private static final int USE_DEFAULT_EXCEL_ROWS = -1;

    private TdarConfiguration() {
        this("/tdar.properties");
    }

    public void setConfigurationFile(String configurationFile) {
        assistant = new ConfigurationAssistant();
        assistant.loadProperties(configurationFile);
        this.configurationFile = configurationFile;
        filestore = loadFilestore();
        initPersonalFilestorePath();
        testQueue();
        initializeStopWords();
        if (ImageUtilities.isMediaLibAvailable()) {
            logger.info("JAI ImageIO available and configured");
        } else {
            logger.error("JAI-ImageIO is not properly installed with Native Libraries\n\nInstructions for Installation: http://docs.geoserver.org/latest/en/user/production/java.html");
        }
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    private TdarConfiguration(String configurationFile) {
        System.setProperty("java.awt.headless", "true");
        setConfigurationFile(configurationFile);
    }

    private void initializeStopWords() {
        try {
            stopWords.addAll(IOUtils.readLines(new FileInputStream(assistant.getStringProperty("lucene.stop.words.file"))));
        } catch (Exception e) {
            stopWords.addAll(STOP_WORDS);
        }
    }

    /**
     * @return
     */
    private Filestore loadFilestore() {
        String filestoreClassName = assistant.getStringProperty("file.store.class",
                PairtreeFilestore.class.getCanonicalName());
        Filestore filestore = null;

        File filestoreLoc = new File(getFileStoreLocation());
        try {
            if (!filestoreLoc.exists()) {
                filestoreLoc.mkdirs();
            }
        } catch (Exception e) {
            logger.error("could not create filestore path:" + filestoreLoc.getAbsolutePath(), e);
        }

        try {
            Class<?> filestoreClass = Class.forName(filestoreClassName);
            filestore = (Filestore) filestoreClass.getConstructor(String.class).newInstance(getFileStoreLocation());
        } catch (Exception e) {
            String msg = "Could not instantiate Filestore: " + e.getMessage();
            logger.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
        logger.info("instantiating filestore: {}", filestore.getClass().getCanonicalName());
        return filestore;
    }

    // verify that the personal filestore location exists, attempt to make it if it doesn't, and System.exit() if that fails
    private void initPersonalFilestorePath() {
        File personalFilestoreHome = new File(getPersonalFileStoreLocation());
        String msg = null;
        boolean pathExists = true;
        try {
            logger.info("initializing personal filestore at {}", getPersonalFileStoreLocation());
            if (!personalFilestoreHome.exists()) {
                pathExists = personalFilestoreHome.mkdirs();
                if (!pathExists) {
                    msg = "Could not create personal filestore at " + getPersonalFileStoreLocation();
                }
            }
        } catch (SecurityException ex) {
            logger.error(SECURITY_EXCEPTION_COULD_NOT_CREATE_PERSONAL_FILESTORE_HOME_DIRECTORY, ex);
            pathExists = false;
        }
        if (!pathExists) {
            throw new IllegalStateException(msg);
        }

    }

    public static TdarConfiguration getInstance() {
        return INSTANCE;
    }

    public String getSitemapDir() {
        return String.format("%s/%s", getPersonalFileStoreLocation(), "sitemap");
    }

    public String getBaseUrl() {
        String base = "http://" + getHostName();
        if (getPort() != 80) {
            base += ":" + getPort();
        }
        return base;
    }

    public String getBaseSecureUrl() {
        String base = "https://" + getHostName();
        if (getHttpsPort() != 443) {
            base += ":" + getHttpsPort();
        }
        return base;
    }

    public String getHostName() {
        return assistant.getStringProperty("app.hostname", DEFAULT_HOSTNAME);
    }

    public String getEmailHostName() {
        return assistant.getStringProperty("app.email.hostname", getHostName());
    }

    public int getPort() {
        return assistant.getIntProperty("app.port", DEFAULT_PORT);
    }

    public String getHelpUrl() {
        return assistant.getStringProperty("help.url", "http://dev.tdar.org/confluence/display/TDAR/User+Documentation");
    }

    public String getAboutUrl() {
        return assistant.getStringProperty("about.url", "http://www.tdar.org");
    }

    public String getCommentsUrl() {
        return assistant.getStringProperty("comments.url", "mailto:comments@tdar.org");
    }

    public String getFileStoreLocation() {
        return assistant.getStringProperty("file.store.location",
                "/home/tdar/filestore");
    }

    public String getPersonalFileStoreLocation() {
        return assistant.getStringProperty("personal.file.store.location", "/home/tdar/pfs");
    }

    public String getWebRoot() {
        return assistant.getStringProperty("web.root", "src/main/webapp");
    }

    public String getSmtpHost() {
        return assistant.getStringProperty("mail.smtp.host", DEFAULT_SMTP_HOST);
    }

    public String getSystemAdminEmail() {
        return assistant.getStringProperty("sysadmin.email", SYSTEM_ADMIN_EMAIL);
    }

    public String getBillingAdminEmail() {
        return assistant.getStringProperty("billing.admin.email", SYSTEM_ADMIN_EMAIL);
    }

    public String getServerEnvironmentStatus() {
        return assistant.getStringProperty("server.environment", "test");

    }

    public boolean getPrivacyControlsEnabled() {
        return assistant.getBooleanProperty("privacy.controls.enabled", false);

    }

    public boolean getLicenseEnabled() {
        return assistant.getBooleanProperty("licenses.enabled", false);
    }

    public boolean getCopyrightMandatory() {
        return assistant.getBooleanProperty("copyright.fields.enabled", false);
    }

    // TODO: make mapping props vendor neutral where possible (e.g. lat/long)
    public double getMapDefaultLat() {
        return assistant.getDoubleProperty("google.map.defaultLatitude", 40.00);

    }

    public double getMapDefaultLng() {
        return assistant.getDoubleProperty("google.map.defaultLongitude", -97.00);

    }

    public File getTempDirectory() {
        File file = new File(assistant.getStringProperty("tmp.dir", System.getProperty("java.io.tmpdir")));
        if (file.exists() && file.isDirectory()) {
            return file;
        }
        if (!file.mkdirs()) {
            String msg = COULDN_T_CREATE_TEMPORARY_DIRECTORY_AT + file.getAbsolutePath();
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }
        return file;
    }

    public Filestore getFilestore() {
        return filestore;
    }

    public boolean shouldRunPeriodicEvents() {
        return assistant.getBooleanProperty("run.periodic.events", false);
    }

    /**
     * @return
     */
    public boolean useExternalMessageQueue() {
        return assistant.getBooleanProperty("message.queue.enabled", false);
    }

    public String getMessageQueueURL() {
        return assistant.getStringProperty("message.queue.server", "dev.tdar.org");
    }

    public String getMessageQueueUser() {
        return assistant.getStringProperty("message.queue.user");
    }

    public String getMessageQueuePwd() {
        return assistant.getStringProperty("message.queue.pwd");
    }

    public String getQueuePrefix() {
        return assistant.getStringProperty("message.queue.prefix", getDefaultQueuePrefix());
    }

    /**
     * @return
     */
    public String getDefaultQueuePrefix() {
        String host = "";
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            host = localMachine.getHostName();
        } catch (UnknownHostException e) {
            logger.debug("unknownhost: ", e);
        }
        return host + ".";
    }

    private void testQueue() {
        if (useExternalMessageQueue() && (StringUtils.isEmpty(getMessageQueuePwd()) || StringUtils.isEmpty(getMessageQueueUser()))) {
            throw new IllegalStateException(MESSAGE_QUEUE_IS_ENABLED_BUT_A_USERNAME_AND_PASSWORD_IS_NOT_DEFINED);
        }
    }

    /**
     * @return
     */
    public int getScrollableFetchSize() {
        return assistant.getIntProperty("scrollableResult.fetchSize", 100);
    }

    /**
     * @return
     */
    public int getIndexerFlushSize() {
        return assistant.getIntProperty("tdar.indexer.flushEvery", 500);
    }

    public int getAuthorityManagementDupeListMaxSize() {
        return assistant.getIntProperty("tdar.authorityManagement.dupeListMaxSize", DEFAULT_AUTHORITY_MANAGEMENT_DUPE_LIST_MAX_SIZE);
    }

    public int getAuthorityManagementMaxAffectedRecords() {
        return assistant.getIntProperty("tdar.authorityManagement.maxAffectedRecords", DEFAULT_AUTHORITY_MANAGEMENT_MAX_AFFECTED_RECORDS);
    }

    /**
     * @return the theme directory
     */
    public String getThemeDir() {
        String dir = assistant.getStringProperty("app.theme.dir", "includes/themes/tdar/");
        if (dir.startsWith("/")) {
            dir = dir.substring(1);
        }
        return dir;
    }

    /**
     * @return
     */
    public Set<String> getStopWords() {
        return stopWords;
    }

    public String getRecaptchaUrl() {
        return assistant.getStringProperty("recaptcha.host");
    }

    public String getGoogleMapsApiKey() {
        return assistant.getStringProperty("googlemaps.apikey", "ABQIAAAA9NaKjBJpcVyUYJMRSYQl8xS0DQCUA87cCG9n-o92VKwf-4ptwhSBrQY9Wnb4P_utINrjb3QZf1KuBw");
    }

    public String getRecaptchaPrivateKey() {
        return assistant.getStringProperty("recaptcha.privateKey");
    }

    public String getRecaptchaPublicKey() {
        return assistant.getStringProperty("recaptcha.publicKey");
    }

    public String getRepositoryName() {
        return assistant.getStringProperty("oai.repository.name", "the Digital Archaeological Record");
    }

    public boolean enableTdarFormatInOAI() {
        return assistant.getBooleanProperty("oai.repository.enableTdarMetadataFormat", true);
    }

    public String getRepositoryNamespaceIdentifier() {
        return assistant.getStringProperty("oai.repository.namespace-identifier", "tdar.org");
    }

    public boolean getEnableEntityOai() {
        return assistant.getBooleanProperty("oai.repository.enableEntities", false);
    }

    // TODO: remove feature toggle when feature complete
    public boolean getLeftJoinDataIntegrationFeatureEnabled() {
        return assistant.getBooleanProperty("featureEnabled.leftJoinDataIntegration", false);
    }

    public String getSystemDescription() {
        return assistant
                .getStringProperty("oai.repository.description", DESCRIPTION);
    }

    public int getScheduledProcessStartId() {
        return assistant.getIntProperty("scheduled.startId", DEFAULT_SCHEDULED_PROCESS_START_ID);
    }

    public int getScheduledProcessBatchSize() {
        return assistant.getIntProperty("scheduled.batchSize", 100);
    }

    public Integer getScheduledProcessEndId() {
        return assistant.getIntProperty("scheduled.endId", DEFAULT_SCHEDULED_PROCESS_END_ID);
    }

    public String getGoogleAnalyticsId() {
        return assistant.getStringProperty("google.analytics", "");
    }

    public int getSessionCountLimitForBackgroundTasks() {
        return assistant.getIntProperty("scheduled.maxProcess", 6);
    }

    public int getSearchExcelExportRecordMax() {
        return assistant.getIntProperty("search.excel.export.recordMax", DEFAULT_SEARCH_EXCEL_EXPORT_RECORD_MAX);
    }

    public boolean isProductionEnvironment() {
        return PRODUCTION.equals(getServerEnvironmentStatus());
    }

    public int getMaxSpreadSheetRows() {
        return assistant.getIntProperty("excel.export.rowMax", USE_DEFAULT_EXCEL_ROWS);
    }

    public String getCulturalTermsHelpURL() {
        return assistant.getStringProperty("help.url.cultural", "http://dev.tdar.org/confluence/display/TDAR/Cultural+Terms");
    }

    public String getInvestigationTypesHelpURL() {
        return assistant.getStringProperty("help.url.investigation", "http://dev.tdar.org/confluence/display/TDAR/Investigation+Types");
    }

    public String getMaterialTypesHelpURL() {
        return assistant.getStringProperty("help.url.material", "http://dev.tdar.org/confluence/display/TDAR/Material+Types");
    }

    public String getSiteTypesHelpURL() {
        return assistant.getStringProperty("help.url.site", "http://dev.tdar.org/confluence/display/TDAR/Site+Types");
    }

    public String getMobileImportURL() {
        return assistant.getStringProperty("mobile.upload.url", "/");
    }
    
    /*
     * Returns the collectionId to use for finding featured resources within
     * 
     * @default -1 -- used to say any colleciton
     */
    public Long getFeaturedCollectionId() {
        return assistant.getLongProperty("featured.collection.id", -1);
    }

    public String getDocumentationUrl() {
        return assistant.getStringProperty("help.baseurl", "http://dev.tdar.org/confluence/display/TDAR/User+Documentation");
    }

    public String getBugReportUrl() {
        return assistant.getStringProperty("bugreport.url", "http://dev.tdar.org/jira");
    }

    public String getCommentUrl() {
        return assistant.getStringProperty("comment.url", "mailto:comments@tdar.org");
    }

    public String getSiteAcronym() {
        return assistant.getStringProperty("site.acronym", "tDAR");
    }

    public String getSiteName() {
        return assistant.getStringProperty("site.name", "the Digital Archaeological Record");
    }

    public LicenseType getDefaultLicenseType() {
        return LicenseType.valueOf(assistant.getStringProperty("default.license.type", LicenseType.CREATIVE_COMMONS_ATTRIBUTION.name()));
    }

    public Boolean isRPAEnabled() {
        return assistant.getBooleanProperty("rpa.enabled", true);
    }

    public String getContactEmail() {
        return assistant.getStringProperty("app.contact.email", "info@digitalantiquity.org");
    }

    public String getNewsRssFeed() {
        return assistant.getStringProperty("news.rssFeed", "http://www.tdar.org/feed/");
    }

    public String getNewsUrl() {
        return assistant.getStringProperty("news.url", "http://www.tdar.org/news/");
    }

    public int getEmbargoPeriod() {
        return assistant.getIntProperty("embargo.period", 5);
    }

    public boolean isOdataEnabled() {
        return assistant.getBooleanProperty("odata.enabled", false);
    }

    public boolean isPayPerIngestEnabled() {
        return assistant.getBooleanProperty("pay.per.contribution.enabled", false);
    }

    public boolean isHttpsEnabled() {
        return assistant.getBooleanProperty("https.enabled", false);
    }

    public Integer getHttpsPort() {
        return assistant.getIntProperty("https.port", 443);
    }

    public Integer getMaxUploadFilesPerRecord() {
        return assistant.getIntProperty("upload.maxFilesPerResource", 50);
    }

    public Boolean getShowJiraLink() {
        return assistant.getBooleanProperty("jira.link.show", true);
    }

    public String getJiraScriptLink() {
        return assistant.getStringProperty("jira.link", JIRA_LINK);
    }
    
    public boolean isViewRowSupported() {
        return assistant.getBooleanProperty("view.row.suppported", true);
    }

    public File getFremarkerTemplateDirectory() {
        return new File(assistant.getStringProperty("freemarker.templatedir", "includes/email/"));
    }

    public List<Long> getUserIdsToIgnoreInLargeTasks() {
        String users = assistant.getStringProperty("userids.to.ignore");
        List<Long> userIds = new ArrayList<Long>();
        for (String userid : users.split("[|,;]")) {
            try {
                userIds.add(Long.parseLong(userid));
            } catch (Exception e) {
                logger.warn("skipping: {} {}", userid, e);
            }
        }
        return userIds;
    }
}
