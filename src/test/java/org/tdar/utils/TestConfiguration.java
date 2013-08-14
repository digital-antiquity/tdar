package org.tdar.utils;

import java.io.File;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.ConfigurationAssistant;

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

    public int getPort() {
        return assistant.getIntProperty("tdar.http.port", 8180);
    }

    public String getAdminUsername() {
        return assistant.getStringProperty("tdar.admin.username", "admin@tdar.org");
    }

    public String getAdminPassword() {
        return assistant.getStringProperty("tdar.admin.password", "admin");
    }

    public Long getAdminUserId() {
        return assistant.getLongProperty("tdar.admin.id", 8093L);
    }

    public String getUsername() {
        return assistant.getStringProperty("tdar.user.username", "test@tdar.org");
    }

    public String getPassword() {
        return assistant.getStringProperty("tdar.user.password", "test");
    }

    public Long getUserId() {
        return assistant.getLongProperty("tdar.user.id", 8092L);
    }

    public String getEditorUsername() {
        return assistant.getStringProperty("tdar.editor.username", "editor@tdar.org");
    }

    public String getEditorPassword() {
        return assistant.getStringProperty("tdar.editor.password", "editor");
    }

    public Long getEditorUserId() {
        return assistant.getLongProperty("tdar.editor.id", 8093L);
    }

    public String getBaseUrl() {
        return String.format("http://%s:%s/", getHostName(), getPort());
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
        return String.format("https://%s:%s/", getHostName(), getHttpsPort());
    }

    public String getChromeDriverPath() {
        return assistant.getStringProperty("tdar.chromedriver.path","/Applications/chromedriver");
    }

    public String getIEDriverPath() {
        return assistant.getStringProperty("tdar.iedriver.path","c:\\opt\\workspace\\IEDriverServer.exe");
    }


    public ConfigurationAssistant getAssistant() {
        return assistant;
    }

}
