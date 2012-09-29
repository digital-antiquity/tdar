package org.tdar.core.configuration;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.PairtreeFilestore;

/**
 * $Id$
 * 
 * Configuration file for various tDAR webapp properties.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
// @Component
public class TdarConfiguration {

    public final static String DEFAULT_SMTP_HOST = "smtp.asu.edu";
    private static final String SYSTEM_ADMIN_EMAIL = "tdar-svn@lists.asu.edu";

    public static final int DEFAULT_AUTHORITY_MANAGEMENT_DUPE_LIST_MAX_SIZE = 10;

    private final transient Logger logger = Logger.getLogger(getClass());

    private ConfigurationAssistant assistant;

    private Filestore filestore;

    private final static TdarConfiguration INSTANCE = new TdarConfiguration();

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
        return assistant.getStringProperty("base.url", "http://core.tdar.org/");
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

    /**
     * @return
     */
    public Set<String> getStopWords() {
        Set<String> toReturn = new HashSet<String>();
        toReturn.add("the");
        toReturn.add("a");
        toReturn.add("null");
        toReturn.add("of");
        toReturn.add("in");
        toReturn.add("for");
        toReturn.add("and");
        return toReturn;
    }

    
    public String getRecapchaServer() {
        return assistant.getProperty("recaptcha.url");        
    }

    public String getRecapchaPrivateKey() {
        return assistant.getProperty("recaptcha.privateKey");        
    }

    public String getRecapchaPublicKey() {
        return assistant.getProperty("recaptcha.publicKey");        
    }
}
