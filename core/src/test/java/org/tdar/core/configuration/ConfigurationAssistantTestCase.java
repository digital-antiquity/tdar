package org.tdar.core.configuration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

/**
 * Put in place simply to ensure that the map obfuscation settings are indeed going to result in a fail safe outcome if they not set correctly in the
 * configuration file.
 * 
 * @author Martin Paulo
 */
public class ConfigurationAssistantTestCase {

    private static final String SWITCHABLE_MAP_OBFUSCATION_KEY = "switchable.map.obfuscation";
    ConfigurationAssistant configurationAssistant;

    private static ConfigurationAssistant configureAssistant(String key, String value) {
        Properties properties = new Properties();
        properties.put(key, value);
        return new ConfigurationAssistant(properties);
    }

    @Test
    public void thatBooleanWillBeFalseIfKeyNotSet() {
        configurationAssistant = configureAssistant("", "");
        assertFalse(configurationAssistant.getBooleanProperty(SWITCHABLE_MAP_OBFUSCATION_KEY));
    }

    @Test
    public void thatBooleanWillBeFalseIfKeyWronglySet() {
        configurationAssistant = configureAssistant(SWITCHABLE_MAP_OBFUSCATION_KEY, "bob");
        assertFalse(configurationAssistant.getBooleanProperty(SWITCHABLE_MAP_OBFUSCATION_KEY));
    }

    @Test
    public void thatBooleanWillBeFalseIfKeyIsFalse() {
        configurationAssistant = configureAssistant(SWITCHABLE_MAP_OBFUSCATION_KEY, "false");
        assertFalse(configurationAssistant.getBooleanProperty(SWITCHABLE_MAP_OBFUSCATION_KEY));
    }

    @Test
    public void thatBooleanWillBeTrueIfKeyIsTrue() {
        configurationAssistant = configureAssistant(SWITCHABLE_MAP_OBFUSCATION_KEY, "true");
        assertTrue(configurationAssistant.getBooleanProperty(SWITCHABLE_MAP_OBFUSCATION_KEY));
    }

}
