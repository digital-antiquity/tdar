package org.tdar.dataone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.AbstractConfigurationFile;
import org.tdar.core.configuration.ConfigurationAssistant;
import org.tdar.core.exception.ConfigurationFileException;

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
public class DataOneConfiguration extends AbstractConfigurationFile {
    private final transient static Logger logger = LoggerFactory.getLogger(DataOneConfiguration.class);

    private ConfigurationAssistant assistant;
    private final static DataOneConfiguration INSTANCE = new DataOneConfiguration();

    private String configurationFile;

    public String getConfigurationFile() {
        return configurationFile;
    }

    private DataOneConfiguration() {
        this("/dataOne.properties");
    }

    private DataOneConfiguration(String configurationFile) {
        System.setProperty("java.awt.headless", "true");
        setConfigurationFile(configurationFile);
    }

    String TDAR_DOI = "doi:10.6067";
    String MN_NAME = "urn:node:TDAR";
    String MN_NAME_TEST = "urn:node:TestTDAR";

    public String getMemberNodeIdentifier() {
        return assistant.getStringProperty("member.node.id", MN_NAME);
    }

    public String getDoiPrefix() {
        return assistant.getStringProperty("tdar.doi.prefix", TDAR_DOI);
    }

    @Deprecated
    public void setConfigurationFile(String configurationFile) {
        assistant = new ConfigurationAssistant();
        try {
            assistant.loadProperties(configurationFile);
            this.configurationFile = configurationFile;
        } catch (ConfigurationFileException cfe) {
            logger.warn("could not load dataOne.properties (using defaults)");
        } catch (Exception e) {
            logger.error("could not load dataOne.properties (using defaults)", e);
        }
    }

    public static DataOneConfiguration getInstance() {
        return INSTANCE;
    }

    @Override
    protected ConfigurationAssistant getAssistant() {
        return assistant;
    }

    public String getContactSubject() {
        return assistant.getStringProperty("contact.subject", "CN=Adam Brin A29701,O=Arizona State University,C=US,DC=cilogon,DC=org");
    }

    public String getSubject() {
        return assistant.getStringProperty("dataone.subject", "CN="+getMemberNodeIdentifier()+",DC=dataone,DC=org");
    }

    public boolean isProduction() {
        return false;
    }
}
