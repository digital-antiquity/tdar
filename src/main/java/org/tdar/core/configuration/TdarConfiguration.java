package org.tdar.core.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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

    public static final int DEFAULT_SCHEDULED_PROCESS_START_ID = 0;
    public static final int DEFAULT_SCHEDULED_PROCESS_END_ID = 400000;
    public static final String DEFAULT_HOSTNAME = "core.tdar.org";
    public static final String DEFAULT_HOST_URL = "http://core.tdar.org";
	public static final int DEFAULT_PORT = 80; // we use this in test
    public final static String DEFAULT_SMTP_HOST = "localhost";
    private static final String SYSTEM_ADMIN_EMAIL = "tdar-svn@lists.asu.edu";

    public static final int DEFAULT_AUTHORITY_MANAGEMENT_DUPE_LIST_MAX_SIZE = 10;
    public static final int DEFAULT_AUTHORITY_MANAGEMENT_MAX_AFFECTED_RECORDS = 100;

    public static final int DEFAULT_SEARCH_EXCEL_EXPORT_RECORD_MAX = 1000;
    
    private final transient static Logger logger = Logger.getLogger(TdarConfiguration.class);

    private ConfigurationAssistant assistant;

    private Filestore filestore;

    private Set<String> stopWords = null;

    private final static TdarConfiguration INSTANCE = new TdarConfiguration();
    public static final String PRODUCTION = "production";
    private String baseHost;

    private TdarConfiguration() {
        this("/tdar.properties");
    }

    private TdarConfiguration(String configurationFile) {
        assistant = new ConfigurationAssistant();
        assistant.loadProperties(configurationFile);
        filestore = loadFilestore();
        initPersonalFilestorePath();
        testQueue();
        System.setProperty("java.awt.headless", "true");
    }

    /**
     * @return
     */
    private Filestore loadFilestore() {
        String filestoreClass = assistant.getStringProperty("file.store.class",
                PairtreeFilestore.class.getCanonicalName());
        Filestore filestore_ = null;

        File filestoreLoc = new File(getFileStoreLocation());
        try {
            if (!filestoreLoc.exists()) {
                filestoreLoc.mkdirs();
            }
        } catch (Exception e) {
            logger.error("could not create filestore path:" + filestoreLoc.getAbsolutePath(), e);
        }

        try {
            Class<?> class_ = Class.forName(filestoreClass);
            filestore_ = (Filestore) class_.getConstructor(
                    new Class<?>[] { String.class }).newInstance(
                    getFileStoreLocation());
        } catch (Exception e) {
            String msg = "Could not instantiate Filestore: " + e.getMessage();
            logger.fatal(msg, e);
            throw new IllegalStateException(msg, e);
        }
        ;

        logger.info("instantiating filestore: " + filestore_.getClass().getCanonicalName());

        return filestore_;
    }

    // verify that the personal filestore location exists, attempt to make it if it doesn't, and System.exit() if that fails
    private void initPersonalFilestorePath() {
        File personalFilestoreHome = new File(getPersonalFileStoreLocation());
        String msg = null;
        boolean pathExists = true;
        try {
            logger.info("initializing personal filestore at " + getPersonalFileStoreLocation());
            if (!personalFilestoreHome.exists()) {
                pathExists = personalFilestoreHome.mkdirs();
                if (!pathExists) {
                    msg = "Could not create personal filestore at " + getPersonalFileStoreLocation();
                }
            }
        } catch (SecurityException ex) {
            msg = "Security Exception: could not create personal filestore home directory";
            logger.fatal(ex);
            pathExists = false;
        }
        if (!pathExists) {
            throw new IllegalStateException(msg);
        }

    }

    public static TdarConfiguration getInstance() {
        return INSTANCE;
    }

	public String getBaseUrl() {
		String base = "http://" + getHostName();
	    if (getPort() != 80) {
	        base += ":" + getPort();
	    }
	    return base;
	}
	public String getBaseHost() {
        return getHostName();
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
        return assistant.getStringProperty("smtp.host", DEFAULT_SMTP_HOST);
    }

    public String getSystemAdminEmail() {
        return assistant.getStringProperty("sysadmin.email", SYSTEM_ADMIN_EMAIL);
    }

    public String getServerEnvironmentStatus() {
        return assistant.getStringProperty("server.environment", "test");

    }

    public boolean getPrivacyControlsEnabled() {
        return assistant.getBooleanProperty("privacy.controls.enabled", false);

    }

    public File getTempDirectory() {
        File file = new File(assistant.getStringProperty("tmp.dir",
                getFileStoreLocation() + "/tmp"));
        if (file.exists() && file.isDirectory()) {
            return file;
        }
        if (!file.mkdirs()) {
            logger.warn("Couldn't create temporary directory at : "
                    + file.getAbsolutePath());
            throw new IllegalStateException(
                    "Couldn't create temporary directory at : "
                            + file.getAbsolutePath());
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
            e.printStackTrace();
        }
        return host + ".";
    }

    private void testQueue() {
        if (useExternalMessageQueue() && (StringUtils.isEmpty(getMessageQueuePwd()) || StringUtils.isEmpty(getMessageQueueUser()))) {
            throw new IllegalStateException("Message Queue is enabled, but a username and password is not defined");
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
     * @return the theme
     */
    public String getThemeDir() {
        return assistant.getStringProperty("app.theme.dir", "/includes/themes/tdar/");
    }

    /**
     * @return
     */
    public Set<String> getStopWords() {
        if (stopWords == null) {
            stopWords = new HashSet<String>(Arrays.asList(new String[] {
                    "the", "and", "a", "to", "of", "in", "i", "is", "that", "it", "on", "you", "this", "for",
                    "but", "with", "are", "have", "be", "at", "or", "as",
                    "was", "so", "if", "out", "not" }));
            try {
                stopWords.clear(); // resetting to use provided file
                stopWords.addAll(IOUtils.readLines(new FileInputStream(assistant.getStringProperty("lucene.stop.words.file"))));
            } catch (Exception e) {
            }
        }
        return stopWords;
    }

    public String getRecaptchaUrl() {
        return assistant.getStringProperty("recaptcha.host");
    }

    public String getRecaptchaPrivateKey() {
        return assistant.getStringProperty("recaptcha.privateKey");
    }

    public String getRecaptchaPublicKey() {
        return assistant.getStringProperty("recaptcha.publicKey");
    }

    public String getRepositoryName() {
        return assistant.getStringProperty("oai.repository.name", "tDAR - the Digital Archaeological Record");
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

    public String getSystemDescription() {
        return assistant
                .getStringProperty(
                        "oai.repository.description",
                        "tDAR is an international digital archive and repository that houses data about archaeological investigations, research, resources, and scholarship.  tDAR provides researchers new avenues to discover and integrate information relevant to topics they are studying.   Users can search tDAR for digital documents, data sets, images, GIS files, and other data resources from archaeological projects spanning the globe.  For data sets, users also can use data integration tools in tDAR to simplify and illuminate comparative research.");
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

}
