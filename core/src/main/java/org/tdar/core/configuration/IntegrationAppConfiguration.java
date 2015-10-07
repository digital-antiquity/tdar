package org.tdar.core.configuration;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

public class IntegrationAppConfiguration extends SimpleAppConfiguration {

	private static final long serialVersionUID = 6361299741688277624L;

	@Bean
	@Qualifier("tdarDataTx")
	public DataSourceTransactionManager dataTransactionManager(
			@Qualifier("tdarDataImportDataSource") DataSource dataSource) throws PropertyVetoException {
		DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
		return dataSourceTransactionManager;
	}
/*
    @Bean(name = "tdarDataDataSource")
    public DataSource tdarDataDataSource() {
        try {
            ComboPooledDataSource ds = new ComboPooledDataSource();
            ds.setDriverClass(env.getRequiredProperty("javax.persistence.jdbc.driver"));
            ds.setJdbcUrl(env.getRequiredProperty("javax.persistence.jdbc.url"));
            ds.setUser(env.getRequiredProperty("javax.persistence.jdbc.user"));
            ds.setPassword(env.getRequiredProperty("javax.persistence.jdbc.password"));
            ds.setAcquireIncrement(5);
            ds.setIdleConnectionTestPeriod(60);
            ds.setMaxPoolSize(env.getRequiredProperty("tdardata.maxConnections", Integer.class));
            ds.setMaxStatements(50);
            ds.setMinPoolSize(env.getRequiredProperty("tdardata.minConnections", Integer.class));
            return ds;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
*/

}
