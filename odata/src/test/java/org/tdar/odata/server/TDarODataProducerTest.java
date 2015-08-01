package org.tdar.odata.server;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.NotAcceptableException;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.QueryInfo;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.tdar.db.model.abstracts.AbstractDataRecord;
import org.tdar.odata.DataRecord;
import org.tdar.odata.bean.MetaData.EntitySet;
import org.tdar.odata.service.RepositoryService;

@RunWith(JMock.class)
public class TDarODataProducerTest {

    private Mockery context = new Mockery();

    @Test
    public void testConstructorDoesNotBuildMetadata() {

        final IMetaDataBuilder metadataBuilder = context.mock(IMetaDataBuilder.class);
        context.checking(new Expectations() {
            {
            }
        });

        final RepositoryService repositoryService = setupMockRepositoryService();

        new TDarODataProducer(repositoryService, metadataBuilder);
    }

    @Test
    public void testGetMetadataDelegatesToBuilder() {
        final EdmDataServices edmDataServices = EdmDataServices.newBuilder().build();

        final RepositoryService repositoryService = setupMockRepositoryService();

        final IMetaDataBuilder metadataBuilder = setupMockMetadataBuilder(edmDataServices);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metadataBuilder);
        producer.getMetadata();
    }

    // @Test
    // public void testGetMetadataProducer() {
    // fail("Not yet implemented");
    // }

    @Test(expected = NotFoundException.class)
    public void testGetEntitiesForTDataSetsRequestButNoSetsAvailable() {

        final RepositoryService repositoryService = setupMockRepositoryService();

        final EdmDataServices edmDataServices = EdmDataServices.newBuilder().build();
        final IMetaDataBuilder metadataBuilder = setupMockMetadataBuilder(edmDataServices);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metadataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_SETS, queryInfo);

        assertEquals(0, entitiesResponse.getEntities().size());
    }

    private IMetaDataBuilder setupMockMetadataBuilder(final EdmDataServices edmDataServices) {
        final IMetaDataBuilder metadataBuilder = context.mock(IMetaDataBuilder.class);
        context.checking(new Expectations() {
            {
                oneOf(metadataBuilder).build();
                will(returnValue(edmDataServices));
            }
        });
        return metadataBuilder;
    }

    private RepositoryService setupMockRepositoryService() {
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
            }
        });
        return repositoryService;
    }

    public Dataset setupTestDataset(String name) {
        Dataset dataSet = new Dataset();
        dataSet.setTitle(name);

        DataTable dataTable = new DataTable();
        dataTable.setName(name);
        DataTableColumn dataTableColumn = new DataTableColumn();
        dataTableColumn.setName("id");

        dataTable.getDataTableColumns().add(dataTableColumn);
        dataTable.setDataset(dataSet);
        dataSet.getDataTables().add(dataTable);
        return dataSet;
    }

    @Test
    public void testGetEntitiesForTDataSetsRequestWithOneDataSetAvailable() {

        final List<Dataset> ownedDataSets = new ArrayList<Dataset>();
        final Dataset dataSet = setupTestDataset("Grecian urns");
        ownedDataSets.add(dataSet);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(dataSet.getDataTables()));
                oneOf(repositoryService).findAllOwnedDatasets();
                will(returnValue(ownedDataSets));
            }
        });

        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_SETS, queryInfo);

        assertEquals(1, entitiesResponse.getEntities().size());
    }

    @Test
    public void testGetEntitiesForTDataSetsRequestWithTwoDataSetsAvailable() {

        final List<Dataset> ownedDataSets = new ArrayList<Dataset>();
        final List<DataTable> dataTables = new ArrayList<DataTable>();

        final Dataset dataSet0 = setupTestDataset("Grecian urns");
        final Dataset dataSet1 = setupTestDataset("Hand Axes");

        dataTables.addAll(dataSet0.getDataTables());
        dataTables.addAll(dataSet1.getDataTables());

        ownedDataSets.add(dataSet0);
        ownedDataSets.add(dataSet1);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(dataTables));
                oneOf(repositoryService).findAllOwnedDatasets();
                will(returnValue(ownedDataSets));
            }
        });

        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_SETS, queryInfo);

        assertEquals(2, entitiesResponse.getEntities().size());
    }

    @Test
    public void testDataSetToOEntityProducesAnEntity() {

        final List<DataTable> dataTables = new ArrayList<DataTable>();
        final Dataset dataSet = setupTestDataset("Grecian urns");

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(dataTables));
            }
        });

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet(EntitySet.T_DATA_SETS);

        dataTables.addAll(dataSet.getDataTables());

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        OEntity dataSetOEntity = producer.dataSetToOEntity(metaData, entitySet, dataSet);
        assertEquals(2, dataSetOEntity.getProperties().size());
    }

    // TODO RR: repeat test to use expanded form.
    @Test
    public void testDataSetToOEntityProducesALink() {

        final Dataset dataSet0 = setupTestDataset("Grecian urns");

        DataTable dataTable1 = new DataTable();
        DataTableColumn dataTableColumn1 = new DataTableColumn();
        dataTableColumn1.setName("id");
        dataTable1.getDataTableColumns().add(dataTableColumn1);
        dataSet0.getDataTables().add(dataTable1);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(dataSet0.getDataTables()));
            }
        });

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet(EntitySet.T_DATA_SETS);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        OEntity dataSetOEntity = producer.dataSetToOEntity(metaData, entitySet, dataSet0);
        // Always 1 datatables link.
        assertEquals(1, dataSetOEntity.getLinks().size());
        assertEquals("TDataTables", dataSetOEntity.getLink("TDataTables", OLink.class).getHref());
    }

    @Test(expected = NotFoundException.class)
    public void testGetEntitiesForTDataTablesRequestButNoDataTablesAvailable() {

        final RepositoryService repositoryService = setupMockRepositoryService();

        final EdmDataServices edmDataServices = EdmDataServices.newBuilder().build();
        final IMetaDataBuilder metadataBuilder = setupMockMetadataBuilder(edmDataServices);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metadataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_TABLES, queryInfo);

        assertEquals(0, entitiesResponse.getEntities().size());
    }

    @Test
    public void testGetEntitiesForTDataTablesRequestWithOneDataTableAvailable() {

        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();

        Dataset dataset = AbstractFitTest.createTestDataset();
        ownedDataTables.addAll(dataset.getDataTables());

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                // Once for build metadata and once for getEntities
                exactly(2).of(repositoryService).findAllOwnedDataTables();
                will(returnValue(ownedDataTables));
            }
        });

        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_TABLES, queryInfo);

        assertEquals(1, entitiesResponse.getEntities().size());
    }

    public DataTable setupDataTableWithIdColumn(String name) {
        DataTable dataTable0 = new DataTable();
        dataTable0.setName(name);
        DataTableColumn dataTableColumn0 = new DataTableColumn();
        dataTableColumn0.setName("id");
        dataTable0.getDataTableColumns().add(dataTableColumn0);
        return dataTable0;
    }

    @Test
    public void testGetEntitiesForTDataTablesRequestWithTwoDataTablesAvailable() {

        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();

        DataTable dataTable0 = setupDataTableWithIdColumn("Italy/Pompeii: Insula of Julia Felix");
        DataTable dataTable1 = setupDataTableWithIdColumn("Britain/Silchester: Calleva Atrebatum");
        ownedDataTables.add(dataTable0);
        ownedDataTables.add(dataTable1);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                // Once for build metadata and once for getEntities
                exactly(2).of(repositoryService).findAllOwnedDataTables();
                will(returnValue(ownedDataTables));
            }
        });

        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_TABLES, queryInfo);

        assertEquals(2, entitiesResponse.getEntities().size());
    }

    @Test
    public void testDataTableToOEntityProducesAnEntity() {

        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();
        DataTable dataTable0 = setupDataTableWithIdColumn("Italy/Pompeii: Insula of Julia Felix");
        ownedDataTables.add(dataTable0);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                exactly(1).of(repositoryService).findAllOwnedDataTables();
                will(returnValue(ownedDataTables));
            }
        });

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet(EntitySet.T_DATA_TABLES);

        TDarODataProducer producer = new TDarODataProducer(null, null);
        OEntity dataTableToOEntity = producer.dataTableToOEntity(metaData, entitySet, dataTable0);

        assertEquals(2, dataTableToOEntity.getProperties().size());

    }

    // TODO RR: repeat test to use expanded form.
    @Test
    public void testDataTableToOEntityProducesALink() {

        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();

        Dataset dataSet = setupTestDataset("Italy/Pompeii: Insula of Julia Felix");
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                exactly(1).of(repositoryService).findAllOwnedDataTables();
                will(returnValue(ownedDataTables));
            }
        });

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet(EntitySet.T_DATA_TABLES);

        TDarODataProducer producer = new TDarODataProducer(null, metaDataBuilder);
        OEntity dataTableOEntity = producer.dataTableToOEntity(metaData, entitySet, dataSet.getDataTables().iterator().next());
        // Always 1 datarecords link since it is a collection.
        assertEquals(1, dataTableOEntity.getLinks().size());
        assertEquals("TDataRecords", dataTableOEntity.getLink("TDataRecords", OLink.class).getHref());
    }

    @Test(expected = NotAcceptableException.class)
    public void testGetEntitiesForTDataRecordsRequestForAbstractDataRecords() {

        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                // By getMetaData
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(ownedDataTables));
            }
        });

        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_RECORDS, queryInfo);

        assertEquals(0, entitiesResponse.getEntities().size());
    }

    @Test(expected = NotFoundException.class)
    public void testGetEntitiesForTDataRecordsRequestButNoDataRecordsAvailable() {

        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                // By getMetaData
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(ownedDataTables));
            }
        });

        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities("Italy/Pompeii: Insula of Julia Felix_s", queryInfo);

        assertEquals(0, entitiesResponse.getEntities().size());
    }

    @Test
    public void testGetEntitiesForTDataRecordsRequestWithOneDataRecordAvailable() {

        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();
        final List<AbstractDataRecord> ownedDataRecords = new ArrayList<AbstractDataRecord>();

        final DataTable dataTable = setupDataTableWithIdColumn("Italy/Pompeii: Insula of Julia Felix");

        AbstractDataRecord dataRecord = new DataRecord(12345L, dataTable);
        ownedDataRecords.add(dataRecord);

        ownedDataTables.add(dataTable);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                // By getMetaData
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(ownedDataTables));
                oneOf(repositoryService).findOwnedDataTableByName("Italy/Pompeii: Insula of Julia Felix_s");
                will(returnValue(dataTable));
                oneOf(repositoryService).findAllDataRecordsForDataTable(dataTable);
                will(returnValue(ownedDataRecords));
            }
        });

        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities("Italy/Pompeii: Insula of Julia Felix_s", queryInfo);

        assertEquals(1, entitiesResponse.getEntities().size());
    }

    @Test
    public void testGetEntitiesForTDataRecordsRequestWithTwoDataRecordsAvailable() {

        final List<AbstractDataRecord> ownedDataRecords = new ArrayList<AbstractDataRecord>();
        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();
        final DataTable dataTable = setupDataTableWithIdColumn("Italy/Pompeii: Insula of Julia Felix");

        AbstractDataRecord dataRecord0 = new DataRecord(12345L, dataTable);
        ownedDataRecords.add(dataRecord0);

        AbstractDataRecord dataRecord1 = new DataRecord(45678L, dataTable);
        ownedDataRecords.add(dataRecord1);

        ownedDataTables.add(dataTable);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                // By getMetaData
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(ownedDataTables));
                oneOf(repositoryService).findOwnedDataTableByName("Italy/Pompeii: Insula of Julia Felix_s");
                will(returnValue(dataTable));
                oneOf(repositoryService).findAllDataRecordsForDataTable(dataTable);
                will(returnValue(ownedDataRecords));
            }
        });

        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities("Italy/Pompeii: Insula of Julia Felix_s", queryInfo);

        assertEquals(2, entitiesResponse.getEntities().size());
    }

    @Test
    public void testDataRecordToOEntityProducesAnEntity() {

        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();
        DataTable dataTable = setupDataTableWithIdColumn("Italy/Pompeii: Insula of Julia Felix");
        // new DataTable();
        // dataTable.setName("Italy/Pompeii: Insula of Julia Felix");
        // @SuppressWarnings("serial")
        // DataTableColumn dataTableColumn = new DataTableColumn() {
        // {
        // setName("id");
        // }
        // };
        // dataTable.setDataTableColumns(Arrays.asList(dataTableColumn));

        AbstractDataRecord dataRecord0 = new DataRecord(12345L, dataTable);

        ownedDataTables.add(dataTable);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                // metaDataBuilder.build()
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(ownedDataTables));
            }
        });

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet("Italy/Pompeii: Insula of Julia Felix_s");

        TDarODataProducer producer = new TDarODataProducer(null, metaDataBuilder);
        OEntity dataRecordOEntity = producer.dataRecordToOEntity(metaData, entitySet, dataRecord0);

        assertEquals(1, dataRecordOEntity.getProperties().size());
    }

    @Test
    public void testDataRecordToOEntityProducesEntityWithMultipleProperties() {

        final DataTable dataTable = setupDataTableWithIdColumn("Italy/Pompeii: Insula of Julia Felix");

        AbstractDataRecord dataRecord0 = new DataRecord(12345L, dataTable) {
            {
                put("title", "Hand axe 4321");
            }
        };

        DataTableColumn dataTableColumn1 = new DataTableColumn();
        dataTableColumn1.setName("title");
        dataTableColumn1.setColumnDataType(DataTableColumnType.VARCHAR);
        dataTable.getDataTableColumns().add(dataTableColumn1);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(Arrays.asList(dataTable)));
            }
        });

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet("Italy/Pompeii: Insula of Julia Felix_s");

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        OEntity dataRecordOEntity = producer.dataRecordToOEntity(metaData, entitySet, dataRecord0);

        assertEquals(2, dataRecordOEntity.getProperties().size());
    }

    // TODO RR: repeat test to use expanded form.
    @Test
    public void testDataRecordToOEntityProducesNoLink() {

        Dataset dataset = AbstractFitTest.createTestDataset();
        final DataTable dataTable = dataset.getDataTables().iterator().next();
        dataTable.setName("Italy/Pompeii: Insula of Julia Felix");
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(Arrays.asList(dataTable)));
            }
        });

        AbstractDataRecord dataRecord = new DataRecord(76543L, dataTable);

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet("Italy/Pompeii: Insula of Julia Felix_s");

        TDarODataProducer producer = new TDarODataProducer(null, metaDataBuilder);
        OEntity dataTableOEntity = producer.dataRecordToOEntity(metaData, entitySet, dataRecord);

        assertEquals(0, dataTableOEntity.getLinks().size());
    }

    // @Test
    // public void testGetEntitiesCount() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetEntity() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetNavProperty() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetNavPropertyCount() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testClose() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testCreateEntityStringOEntity() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testCreateEntityStringOEntityKeyStringOEntity() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testDeleteEntity() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testMergeEntity() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testUpdateEntity() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetLinks() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testCreateLink() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testUpdateLink() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testDeleteLink() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testCallFunction() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetNameSpace() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetEntityTypes() {
    // fail("Not yet implemented");
    // }

}
