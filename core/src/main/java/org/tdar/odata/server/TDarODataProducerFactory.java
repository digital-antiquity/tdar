package org.tdar.odata.server;

import java.util.Properties;

import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.ODataProducerDelegate;
import org.odata4j.producer.ODataProducerFactory;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.odata.service.RepositoryService;

// TODO RR: this Factory can go away. It does not do enough. 
// The TDarODataProducer should be instantiated directly by Spring.
public class TDarODataProducerFactory implements ODataProducerFactory {

    public static final String TDAR_NAMESPACE_PROPNAME = "tdar.datastore.namespace";
    public static final String TDAR_NAMESPACE_PROPDEFAULT = "tDAR-default";

    private TDarODataProducer producer;

    public TDarODataProducerFactory(final TDarODataProducer producer) {
        super();
        if (TdarConfiguration.getInstance().isOdataEnabled()) {
            this.producer = producer;
        }
    }

    public TDarODataProducerFactory(final RepositoryService repositoryService, final IMetaDataBuilder metaDataBuilder) {
        super();
        if (TdarConfiguration.getInstance().isOdataEnabled()) {
            this.producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        }
    }

    @Override
    public ODataProducer create(Properties properties) {
        if (TdarConfiguration.getInstance().isOdataEnabled()) {
            return producer;
        }
        return new ODataProducerDelegate() {

            @Override
            public ODataProducer getDelegate() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
}
