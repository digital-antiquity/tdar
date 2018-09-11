package org.tdar.core.configuration;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.tdar.configuration.PooledDataSourceWrapper;

public class IntegrationAppConfiguration extends SimpleAppConfiguration {

    private static final long serialVersionUID = 6361299741688277624L;

    @Bean
    @Qualifier("tdarDataTx")
    public DataSourceTransactionManager dataTransactionManager(
            @Qualifier("tdarDataImportDataSource") DataSource dataSource) throws PropertyVetoException {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        return dataSourceTransactionManager;
    }

    @Bean(name = "tdarDataImportDataSource")
    public DataSource tdarDataDataSource() {
        try {
            return new PooledDataSourceWrapper("tdardata", env).getDataSource();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
