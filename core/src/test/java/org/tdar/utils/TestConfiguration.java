package org.tdar.utils;

import java.io.File;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.ConfigurationAssistant;
import org.tdar.core.dao.external.auth.MockAuthenticationProvider;

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
public class TestConfiguration {
    private final transient static Logger logger = LoggerFactory.getLogger(TestConfiguration.class);

    private ConfigurationAssistant assistant;
    private final static TestConfiguration INSTANCE = new TestConfiguration();

    private String configurationFile;

    private TestConfiguration() {
        this(System.getProperty("test.config", "test.properties"));
        Properties sysprop = System.getProperties();
        Properties properties = assistant.getProperties();
        /*
         * Theoretically allow the configuration Assistant access to specifically overriden properties without polluting it with all of System.properties
         */
        for (Entry<Object, Object> entry : properties.entrySet()) {
            try {
                String key = (String) entry.getKey();
                if (sysprop.contains(key)) {
                    properties.put(key, sysprop.getProperty(key));
                    logger.debug("overriding [{}] with [{}]", key, sysprop.getProperty(key));
                }
            } catch (Exception e) {
                logger.error("{}", e);
            }
        }

        for (Entry<Object, Object> entry : sysprop.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith("tdar.")) {
                properties.put(key, sysprop.getProperty(key));
                logger.debug("overriding [{}] with [{}]", key, sysprop.getProperty(key));
            }
        }
    }

    /*
     * Do not use this except for via the @MultipleTdarConfigurationRunner
     */
    private void setConfigurationFile(String configurationFile) {
        assistant = new ConfigurationAssistant();
        if (StringUtils.isNotBlank(configurationFile)) {
            File config = new File(configurationFile);
            if (config.exists()) {
                assistant.loadProperties(configurationFile);
                this.configurationFile = configurationFile;
            }
        }
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    private TestConfiguration(String configurationFile) {
        setConfigurationFile(configurationFile);
    }

    public static TestConfiguration getInstance() {
        return INSTANCE;
    }

    public String getHostName() {
        return assistant.getStringProperty("tdar.host.name", "localhost");
    }

    public boolean isHttpsEnabled() {
        return assistant.getBooleanProperty("tdar.https.enabled", true);
    }

    public int getHttpsPort() {
        return assistant.getIntProperty("tdar.https.port", 8143);
    }

    public String getContext() {
        return assistant.getStringProperty("tdar.context.path", "");
    }

    public int getPort() {
        return assistant.getIntProperty("tdar.http.port", 8180);
    }

    public String getAdminUsername() {
        return assistant.getStringProperty("tdar.admin.username", MockAuthenticationProvider.ADMIN_USERNAME);
    }

    public String getAdminPassword() {
        return assistant.getStringProperty("tdar.admin.password", MockAuthenticationProvider.ADMIN_PASSWORD);
    }

    public Long getAdminUserId() {
        return assistant.getLongProperty("tdar.admin.id", 8093L);
    }

    public String getUsername() {
        return assistant.getStringProperty("tdar.user.username", MockAuthenticationProvider.USERNAME);
    }

    public String getPassword() {
        return assistant.getStringProperty("tdar.user.password", MockAuthenticationProvider.PASSWORD);
    }

    public Long getUserId() {
        return assistant.getLongProperty("tdar.user.id", 8092L);
    }

    public String getEditorUsername() {
        return assistant.getStringProperty("tdar.editor.username", MockAuthenticationProvider.EDITOR_USERNAME);
    }

    public String getEditorPassword() {
        return assistant.getStringProperty("tdar.editor.password", MockAuthenticationProvider.EDITOR_PASSWORD);
    }

    public Long getEditorUserId() {
        return assistant.getLongProperty("tdar.editor.id", 8094L);
    }

    public String getBillingAdminUsername() {
        return assistant.getStringProperty("tdar.billing.username", MockAuthenticationProvider.BILLING_USERNAME);
    }

    public String getBillingAdminPassword() {
        return assistant.getStringProperty("tdar.billing.password", MockAuthenticationProvider.BILLING_PASSWORD);
    }

    public Long getBillingAdminUserId() {
        return assistant.getLongProperty("tdar.billing.id", 8095L);
    }

    public String getBaseUrl() {
        return String.format("http://%s:%s%s/", getHostName(), getPort(), getContext());
    }

    public String getUsername(String user) {
        if (StringUtils.isBlank(user)) {
            return getUsername();
        }
        return user;
    }

    public String getPassword(String pass) {
        if (StringUtils.isBlank(pass)) {
            return getPassword();
        }
        return pass;
    }

    public String getBaseSecureUrl() {
        return String.format("https://%s:%s%s/", getHostName(), getHttpsPort(), getContext());
    }

    public String getChromeApplicationPath() {
        String deflt = "Google Chrome";
        if (isUnix()) {
            deflt = "/usr/bin/google-chrome-stable";
        }
        if (isMac()) {
            deflt = "/Applications/Google Chrome.app";
        }
        return assistant.getStringProperty("tdar.chrome.path", deflt);

    }

    public String getChromeDriverPath() {
        String deflt = "chromedriver";
        if (isUnix()) {
            deflt = "/usr/local/bin/chromedriver";
        }
        if (isMac()) {
            deflt = "/Applications/chromedriver";
        }
        return assistant.getStringProperty("tdar.chromedriver.path", deflt);
    }

    public String getIEDriverPath() {
        return assistant.getStringProperty("tdar.iedriver.path", "c:\\opt\\workspace\\IEDriverServer.exe");
    }

    public int getMaxAPIFindAll() {
        return assistant.getIntProperty("test.findall.max", 10);
    }

    public int getWaitInt() {
        return 1;
    }

    public static boolean isWindows() {
        return (OS.CURRENT == OS.WINDOWS);
    }

    public static boolean isMac() {
        return (OS.CURRENT == OS.OSX);
    }

    public static boolean isUnix() {
        return (OS.CURRENT == OS.UNIX || OS.CURRENT == OS.LINUX);
    }

    public static StackTraceElement getCallerInfo() {
        StackTraceElement stackTraceElement = null;
        try {
            stackTraceElement = Thread.currentThread().getStackTrace()[3];

        } catch (SecurityException ex) {
            stackTraceElement = new StackTraceElement("Nice ", "try, ", "buddy. ", 42);
        }
        return stackTraceElement;
    }

    /**
     * Convenience wrapper for SystemUtils, which is a convenience wrapper for system.os.name.
     *
     * OS.CURRENT is an alias to enum value for the detected OS.
     *
     * metaKey indicates the key used by the current browser to execute menu hotkey command (e.g. CTRL+N opens window on Windows,
     * CMD+N opens window on OSX)
     */
    public enum OS {
        WINDOWS,
        LINUX,
        OSX,
        UNIX,
        TRS_80;


        public static OS CURRENT;
        static {
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX)
                CURRENT = OSX;
            else if (SystemUtils.IS_OS_WINDOWS)
                CURRENT = WINDOWS;
            else if (SystemUtils.IS_OS_LINUX)
                CURRENT = LINUX;
            else if (SystemUtils.IS_OS_UNIX)
                CURRENT = UNIX;
            else
                CURRENT = TRS_80;
        }

        OS() {
        }
    }
}
