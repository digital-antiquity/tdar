package org.tdar;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.tdar.configuration.PooledDataSourceWrapper;

//@ComponentScan(basePackages = { "org.tdar" })
@EnableTransactionManagement()
//@Configuration()
@PropertySource(value = "hibernate.properties", ignoreResourceNotFound = true)
public class BaseConfiguration {


    @Autowired
    protected Environment env;

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
