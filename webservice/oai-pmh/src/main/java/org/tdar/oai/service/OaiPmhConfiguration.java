package org.tdar.oai.service;

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
public class OaiPmhConfiguration extends AbstractConfigurationFile {
    private final transient static Logger logger = LoggerFactory.getLogger(OaiPmhConfiguration.class);

    private ConfigurationAssistant assistant;
    private final static OaiPmhConfiguration INSTANCE = new OaiPmhConfiguration();

    private String configurationFile;


    public String getConfigurationFile() {
        return configurationFile;
    }

    private OaiPmhConfiguration() {
        this("/oai-pmh.properties");
    }

    private OaiPmhConfiguration(String configurationFile) {
        System.setProperty("java.awt.headless", "true");
        setConfigurationFile(configurationFile);
    }


    public boolean enableTdarFormatInOAI() {
        return assistant.getBooleanProperty("oai.repository.enableTdarMetadataFormat", false);
    }

    public String getRepositoryNamespaceIdentifier() {
        return assistant.getStringProperty("oai.repository.namespace-identifier", "tdar.org");
    }

    public boolean getEnableEntityOai() {
        return assistant.getBooleanProperty("oai.repository.enableEntities", false);
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
            logger.error("could not load dataOne.properties (using defaults)",e);
        }
    }

    public static OaiPmhConfiguration getInstance() {
        return INSTANCE;
    }

    @Override
    protected ConfigurationAssistant getAssistant() {
    	return assistant;
    }
}
