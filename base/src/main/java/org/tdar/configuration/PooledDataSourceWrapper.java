package org.tdar.configuration;

import java.beans.PropertyVetoException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PooledDataSourceWrapper {
    public transient Logger logger = LoggerFactory.getLogger(getClass());
    private ComboPooledDataSource dataSource;
    private Environment env;

    public ComboPooledDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(ComboPooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Configure the dataSource by using the dataSource prefix (tdardata, tdarmetadata, tdargis); for jpa properties, the defaults of javax.persistance.jdbc.
     * can be swapped in.
     * 
     * @param prefix
     * @return
     * @throws PropertyVetoException
     */
    public PooledDataSourceWrapper(String prefix, Environment env) throws PropertyVetoException {
        this.env = env;
        dataSource = new ComboPooledDataSource();
        
        String driver_ = ".persistence.jdbc.driver";
        String url_ = ".persistence.jdbc.url";
        String user_ = ".persistence.jdbc.user";
        String password_ = ".persistence.jdbc.password";
        dataSource.setDriverClass(getProperty(prefix, driver_));
        setupAndLogJdbcConnectionString(prefix, dataSource, url_);
        dataSource.setUser(getProperty(prefix, user_));
        dataSource.setPassword(getProperty(prefix, password_));
        
        dataSource.setAcquireIncrement(env.getProperty(prefix + ".acquireIncrement", Integer.class, 5));
        dataSource.setPreferredTestQuery(env.getProperty(prefix + ".preferredTestQuery", String.class, "select 1"));
        dataSource.setMaxIdleTime(env.getProperty(prefix + ".maxIdleTime", Integer.class, 600));
        dataSource.setIdleConnectionTestPeriod(env.getProperty(prefix + ".idleConnectionTestPeriod", Integer.class, 300));
        dataSource.setMaxStatements(env.getProperty(prefix + ".maxStatements", Integer.class, 100));
        dataSource.setTestConnectionOnCheckin(env.getProperty(prefix + ".testConnectionOnCheckin", Boolean.class, true));
        dataSource.setMaxPoolSize(getChainedOptionalProperty(prefix, ".maxConnections", 10));
        dataSource.setMinPoolSize(getChainedOptionalProperty(prefix, ".minConnections", 1));
    }

    private void setupAndLogJdbcConnectionString(String prefix, ComboPooledDataSource ds, String url_) {
        String property = getProperty(prefix, url_);
        String prefix_ = "tdar";
        String appPrefix = System.getProperty("appPrefix");
        if (StringUtils.isNotBlank(appPrefix)) {
            prefix_ = appPrefix;
        }
        if (property.contains("?")) {
            property += "&ApplicationName=" + prefix_;
        } else {
            property += "?ApplicationName=" + prefix_;
        }
        ds.setJdbcUrl(property);
        logger.debug(prefix_ + " JDBC Connection (" + prefix + "):" + property);
    }

    private int getChainedOptionalProperty(String prefix, String key, Integer deflt) {
        String appPrefix = System.getProperty("appPrefix");
        String prefix_ = prefix;
        if (StringUtils.isNotBlank(appPrefix)) {
            prefix_ = appPrefix + "." + prefix;
        }

        Integer val = env.getProperty(prefix_ + key, Integer.class);
        if (val != null) {
            logger.debug(prefix_ + key + ": " + val);
            return val;
        }

        return env.getProperty(prefix + key, Integer.class, deflt);
    }

    /**
     * Allow for the override of the default connection properties (good for postGIS)
     * 
     * @param prefix
     * @param val_
     * @return
     */
    private String getProperty(String prefix, String val_) {
        String val = env.getProperty(prefix + val_);
        if (val == null) {
            val = env.getRequiredProperty("javax" + val_);
        } else {
            logger.debug("{} --> {}", prefix + val_, val);
        }
        return val;
    }
}
