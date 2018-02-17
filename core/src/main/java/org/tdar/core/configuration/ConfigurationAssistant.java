package org.tdar.core.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * $Id$
 * 
 * Provides convenience methods to read configuration properties from a java.util.Properties
 * object.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class ConfigurationAssistant implements Serializable {

    private static final long serialVersionUID = -9093022080387404606L;
    // public static final String DEFAULT_CONFIG_PATH = "TDAR_CONFIG_PATH";

    private final Properties properties;
    private final transient static Logger logger = LoggerFactory.getLogger(ConfigurationAssistant.class);

    public ConfigurationAssistant() {
        this(new Properties());
    }

    public ConfigurationAssistant(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public void loadProperties(String resource) {
        InputStream stream = toInputStream(resource);
        try {
            properties.load(stream);
        } catch (IOException e) {
            // FIXME: try to load them manually, not via loadFromXML?
            logger.warn("Unable to load properties normally, trying loadFromXML", e);
            try {
                properties.loadFromXML(stream);
            } catch (IOException exception) {
                logger.error("Couldn't load properties file from xml either, aborting:" + resource, exception);
                throw new IllegalArgumentException("Couldn't load properties via loadFromXML - resource: "
                        + resource + " - stream: " + stream, e);
            }

        }
        IOUtils.closeQuietly(stream);
    }

    public void loadProperties(File file) {
        try {
            FileReader reader = new FileReader(file);
            properties.load(reader);
            IOUtils.closeQuietly(reader);
        } catch (IOException e) {
            // FIXME: try to load them manually, not via loadFromXML?
            logger.warn("Unable to load properties normally, trying loadFromXML", e);
        }
    }

    @SuppressWarnings("resource")
    public static InputStream toInputStream(String resource) {
        // first try to read it as a file
        InputStream stream = null;

        try {
            File file = new File(resource);
            if (file.isFile()) {
                stream = new FileInputStream(file);
            } else {
                stream = getResourceAsStream(resource);
            }
        } catch (AccessControlException e) {
            stream = getResourceAsStream(resource);
        } catch (FileNotFoundException e) {
            stream = getResourceAsStream(resource);
        }
        return stream;
    }

    public static InputStream getResourceAsStream(String path) {
        InputStream stream = ConfigurationAssistant.class.getResourceAsStream(path);
        if (stream == null) {
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        }
        if (stream == null) {
            stream = ClassLoader.getSystemResourceAsStream(path);
        }
        if (stream == null) {
            throw new RuntimeException("Couldn't load resource from system classpath or context classpath: " + path);
        }
        return stream;
    }

    public String getProperty(String key) {
        return getStringProperty(key, "");
    }

    public String getProperty(String key, String defaultValue) {
        return getStringProperty(key, defaultValue);
    }

    public String getStringProperty(String key) {
        return getStringProperty(key, "");
    }

    public String[] getStringArray(String key, String[] defaultArrayValue) {
        String property = getStringProperty(key);
        if (StringUtils.isEmpty(property)) {
            return defaultArrayValue;
        }
        return StringUtils.split(property, ',');
    }

    public String getStringProperty(String key, String defaultValue) {
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        return defaultValue;
    }

    public int getIntProperty(String key) {
        return getIntProperty(key, 0);
    }

    public int getIntProperty(String key, int defaultValue) {
        if (properties.containsKey(key)) {
            try {
                return Integer.parseInt(properties.getProperty(key));
            } catch (NumberFormatException fallthrough) {
            }
        }
        return defaultValue;
    }

    public long getLongProperty(String key, long defaultValue) {
        if (properties.containsKey(key)) {
            try {
                return Long.parseLong(properties.getProperty(key));
            } catch (NumberFormatException fallthrough) {
            }
        }
        return defaultValue;
    }

    public boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, false);
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        if (properties.containsKey(key)) {
            return "true".equalsIgnoreCase(properties.getProperty(key));
        }
        return defaultValue;
    }

    public double getDoubleProperty(String key) {
        return getDoubleProperty(key, 0.0d);
    }

    public double getDoubleProperty(String key, double defaultValue) {
        if (properties.containsKey(key)) {
            try {
                return Double.parseDouble(properties.getProperty(key));
            } catch (NumberFormatException fallthrough) {
                logger.warn("Couldn not get key as a number.", fallthrough);
            }
        }
        return defaultValue;
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

}
