package org.tdar.odata.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.producer.ODataProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdar.core.bean.resource.Dataset;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "Placeholder-context.xml" })
@DirtiesContext
public class TDarProducerFactoryITCase extends AbstractLightFitTest {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InitialisableRepositoryService repositoryService;

    @Autowired
    private TDarODataProducerFactory oDataProducerFactory;

    @Test
    public void testCreateSetsNameSpaceOfProducerFromPropertiesFile() {
        TDarODataProducer oDataProducer = (TDarODataProducer) oDataProducerFactory.create(null);
        assertEquals("tDAR", oDataProducer.getNameSpace());
    }

    @Test
    public void testCreateSetsEntityTypesOfProducerFromRepositoryService() {
        TDarODataProducer oDataProducer = (TDarODataProducer) oDataProducerFactory.create(null);
        List<String> entityTypeNames = getEntityTypeNames(oDataProducer);
        assertEquals(4, entityTypeNames.size());
        assertTrue(entityTypeNames.contains("TDataRecord"));
        assertTrue(entityTypeNames.contains("TDataTable"));
        assertTrue(entityTypeNames.contains("TDataSet"));
        assertTrue(entityTypeNames.contains("Pompeii:Insula of Julia Felix"));
    }

    private List<String> getEntityTypeNames(ODataProducer producer) {
        List<String> entityTypeNames = new ArrayList<String>();
        Iterable<EdmEntityType> entityTypes = producer.getMetadata().getEntityTypes();
        for (EdmEntityType entityType : entityTypes)
        {
            entityTypeNames.add(entityType.getName());
        }
        return entityTypeNames;
    }

    @Override
    protected void createTestScenario() {
        super.createTestScenario();

        Dataset dataset = createTestDataset();

        repositoryService.save(dataset.getDataTables().iterator().next());
        repositoryService.saveOwnedDatasetByName(Constant.GRECIAN_URNS_DATASET_NAME, dataset);
        repositoryService.saveOwnedDatasets(Arrays.asList(dataset));

        repositoryService.saveOwnedDataTables(dataset.getDataTables());
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
