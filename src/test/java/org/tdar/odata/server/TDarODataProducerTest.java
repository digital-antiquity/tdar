package org.tdar.odata.server;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Ignore;
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
import org.tdar.odata.server.MetaData.EntitySet;

@RunWith(JMock.class)
public class TDarODataProducerTest {

    private Mockery context = new Mockery();

    @Test
    public void testConstructorDoesNotBuildMetadata() {
        
        final IMetaDataBuilder metadataBuilder = context.mock(IMetaDataBuilder.class);
        context.checking(new Expectations() {{
        }});
                
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
        }});
        
        new TDarODataProducer(repositoryService, metadataBuilder);
    }

    @Test
    public void testGetMetadataDelegatesToBuilder() {
        final EdmDataServices edmDataServices = EdmDataServices.newBuilder().build();
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
        }});

        final IMetaDataBuilder metadataBuilder = context.mock(IMetaDataBuilder.class);
        context.checking(new Expectations() {{
            oneOf(metadataBuilder).build(); will(returnValue(edmDataServices));
        }});
                
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metadataBuilder);
        producer.getMetadata();
    }

//    @Test
//    public void testGetMetadataProducer() {
//        fail("Not yet implemented");
//    }
    

    @Test(expected=NotFoundException.class)
    public void testGetEntitiesForTDataSetsRequestButNoSetsAvailable() {
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
        }});
 
        final EdmDataServices edmDataServices = EdmDataServices.newBuilder().build();
        final IMetaDataBuilder metadataBuilder = context.mock(IMetaDataBuilder.class);
        context.checking(new Expectations() {{
            oneOf(metadataBuilder).build(); will(returnValue(edmDataServices));
        }});
        
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metadataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_SETS, queryInfo);
        
        assertEquals(0, entitiesResponse.getEntities().size());
    }

    @Test
    public void testGetEntitiesForTDataSetsRequestWithOneDataSetAvailable() {
        
        final List<Dataset> ownedDataSets = new ArrayList<Dataset>();
        
        Dataset dataSet = new Dataset();
        dataSet.setTitle("Grecian urns");
        
        DataTable dataTable = new DataTable();
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn = new DataTableColumn() {{setName("id");}};
        dataTable.setDataTableColumns(Arrays.asList(dataTableColumn));
        dataTable.setDataset(dataSet);
        final List<DataTable> dataTables = Arrays.asList(dataTable);
        Set<DataTable> ownedDataTables = new HashSet<DataTable>(dataTables);
        dataSet.setDataTables(ownedDataTables);
        
        ownedDataSets.add(dataSet);
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(dataTables));
            oneOf(repositoryService).findAllOwnedDatasets(); will(returnValue(ownedDataSets));
        }});
 
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
        
        Dataset dataSet0 = new Dataset();
        dataSet0.setTitle("Grecian urns");
        
        DataTable dataTable0 = new DataTable();
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn0 = new DataTableColumn() {{setName("id");}};
        dataTable0.setDataTableColumns(Arrays.asList(dataTableColumn0));
        dataTable0.setDataset(dataSet0);
        
        Dataset dataSet1 = new Dataset();
        dataSet1.setTitle("Hand Axes");
        
        DataTable dataTable1 = new DataTable();
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn1 = new DataTableColumn() {{setName("id");}};
        dataTable1.setDataTableColumns(Arrays.asList(dataTableColumn1));
        dataTable1.setDataset(dataSet1);
        
        dataTables.add(dataTable0);
        dataTables.add(dataTable1);

        ownedDataSets.add(dataSet0);
        ownedDataSets.add(dataSet1);
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(dataTables));
            oneOf(repositoryService).findAllOwnedDatasets(); will(returnValue(ownedDataSets));
        }});
 
        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_SETS, queryInfo);
        
        assertEquals(2, entitiesResponse.getEntities().size());
    }
    
    @Test
    public void testDataSetToOEntityProducesAnEntity() {
        
        final List<DataTable> dataTables = new ArrayList<DataTable>();

        Dataset dataSet = new Dataset();
        dataSet.setTitle("Grecian Urns");
        
        DataTable dataTable = new DataTable();
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn0 = new DataTableColumn() {{setName("id");}};
        dataTable.setDataTableColumns(Arrays.asList(dataTableColumn0));
        dataTable.setDataset(dataSet); 

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(dataTables));
        }});

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet(EntitySet.T_DATA_SETS);

        dataTables.add(dataTable);
 
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        OEntity dataSetOEntity = producer.dataSetToOEntity(metaData , entitySet, dataSet);
        assertEquals(2, dataSetOEntity.getProperties().size());      
    }
    
    // TODO RR: repeat test to use expanded form.
    @Test
    public void testDataSetToOEntityProducesALink() {
        
        final List<DataTable> dataTables = new ArrayList<DataTable>();

        Dataset dataSet = new Dataset();
        dataSet.setTitle("Grecian Urns");
        
        DataTable dataTable0 = new DataTable();
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn0 = new DataTableColumn() {{setName("id");}};
        dataTable0.setDataTableColumns(Arrays.asList(dataTableColumn0));
        dataTables.add(dataTable0);
        
        DataTable dataTable1 = new DataTable();
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn1 = new DataTableColumn() {{setName("id");}};
        dataTable1.setDataTableColumns(Arrays.asList(dataTableColumn1));
        dataTables.add(dataTable1);
        
        dataSet.setDataTables(new HashSet<DataTable>(dataTables));

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(dataTables));
        }});
 
        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet(EntitySet.T_DATA_SETS);
        
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        OEntity dataSetOEntity = producer.dataSetToOEntity(metaData , entitySet, dataSet);
        // Always 1 datatables link.
        assertEquals(1, dataSetOEntity.getLinks().size());      
        assertEquals("TDataTables", dataSetOEntity.getLink("TDataTables", OLink.class).getHref());      
    }

    @Test(expected=NotFoundException.class)
    public void testGetEntitiesForTDataTablesRequestButNoDataTablesAvailable() {
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
        }});
 
        final EdmDataServices edmDataServices = EdmDataServices.newBuilder().build();
        final IMetaDataBuilder metadataBuilder = context.mock(IMetaDataBuilder.class);
        context.checking(new Expectations() {{
            oneOf(metadataBuilder).build(); will(returnValue(edmDataServices));
        }});
        
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metadataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_TABLES, queryInfo);
        
        assertEquals(0, entitiesResponse.getEntities().size());
    }

    @Test
    public void testGetEntitiesForTDataTablesRequestWithOneDataTableAvailable() {
        
        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();
        
        DataTable dataTable = new DataTable();
        dataTable.setName("Pompeii:Insula of Julia Felix");
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn1 = new DataTableColumn() {{setName("id");}};
        dataTable.setDataTableColumns(Arrays.asList(dataTableColumn1));
        ownedDataTables.add(dataTable);
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            // Once for build metadata  and once for getEntities
            exactly(2).of(repositoryService).findAllOwnedDataTables(); will(returnValue(ownedDataTables));
        }});
 
        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_TABLES, queryInfo);
        
        assertEquals(1, entitiesResponse.getEntities().size());
    }

    @Test
    public void testGetEntitiesForTDataTablesRequestWithTwoDataTablesAvailable() {
        
        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();
        
        DataTable dataTable0 = new DataTable();
        dataTable0.setName("Italy/Pompeii: Insula of Julia Felix");
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn0 = new DataTableColumn() {{setName("id");}};
        dataTable0.setDataTableColumns(Arrays.asList(dataTableColumn0));
        ownedDataTables.add(dataTable0);
        
        DataTable dataTable1 = new DataTable();
        dataTable1.setName("Britain/Silchester: Calleva Atrebatum");
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn1 = new DataTableColumn() {{setName("id");}};
        dataTable1.setDataTableColumns(Arrays.asList(dataTableColumn1));
        ownedDataTables.add(dataTable1);
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            // Once for build metadata  and once for getEntities
           exactly(2).of(repositoryService).findAllOwnedDataTables(); will(returnValue(ownedDataTables));
        }});
 
        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_TABLES, queryInfo);
        
        assertEquals(2, entitiesResponse.getEntities().size());
    }
    
    @Test
    public void testDataTableToOEntityProducesAnEntity() {

        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();
        
        DataTable dataTable0 = new DataTable();
        dataTable0.setName("Italy/Pompeii: Insula of Julia Felix");
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn0 = new DataTableColumn() {{setName("id");}};
        dataTable0.setDataTableColumns(Arrays.asList(dataTableColumn0));
        ownedDataTables.add(dataTable0);
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            exactly(1).of(repositoryService).findAllOwnedDataTables(); will(returnValue(ownedDataTables));
        }});

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
        
        DataTable dataTable0 = new DataTable();
        dataTable0.setName("Italy/Pompeii: Insula of Julia Felix");
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn0 = new DataTableColumn() {{setName("id");}};
        dataTable0.setDataTableColumns(Arrays.asList(dataTableColumn0));
        ownedDataTables.add(dataTable0);

        Dataset dataSet = new Dataset();        
        dataTable0.setDataset(dataSet);
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            exactly(1).of(repositoryService).findAllOwnedDataTables(); will(returnValue(ownedDataTables));
        }});
 
        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet(EntitySet.T_DATA_TABLES);
        
        TDarODataProducer producer = new TDarODataProducer(null, metaDataBuilder);
        OEntity dataTableOEntity = producer.dataTableToOEntity(metaData, entitySet, dataTable0);
        // Always 1 datarecords link since it is a collection.
        assertEquals(1, dataTableOEntity.getLinks().size());      
        assertEquals("TDataRecords", dataTableOEntity.getLink("TDataRecords", OLink.class).getHref());      
    }

    @Test(expected=NotAcceptableException.class)
    public void testGetEntitiesForTDataRecordsRequestForAbstractDataRecords() {
        
        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();   
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            // By getMetaData
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(ownedDataTables));            
        }});
 
        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities(EntitySet.T_DATA_RECORDS, queryInfo);
        
        assertEquals(0, entitiesResponse.getEntities().size());
    }

    @Test(expected=NotFoundException.class)
    public void testGetEntitiesForTDataRecordsRequestButNoDataRecordsAvailable() {
        
        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();   
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            // By getMetaData
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(ownedDataTables));            
        }});
 
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
        
        final DataTable dataTable = new DataTable();
        dataTable.setName("Italy/Pompeii: Insula of Julia Felix");
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn = new DataTableColumn() {{setName("id");}};
        dataTable.setDataTableColumns(Arrays.asList(dataTableColumn));

        AbstractDataRecord dataRecord = new AbstractDataRecord(12345L, dataTable );
        ownedDataRecords.add(dataRecord);
        
        ownedDataTables.add(dataTable);
        
        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            // By getMetaData
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(ownedDataTables));            
            oneOf(repositoryService).findOwnedDataTableByName("Italy/Pompeii: Insula of Julia Felix_s"); will(returnValue(dataTable));            
            oneOf(repositoryService).findAllDataRecordsForDataTable(dataTable); will(returnValue(ownedDataRecords));
        }});
 
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
        
        final DataTable dataTable = new DataTable();
        dataTable.setName("Italy/Pompeii: Insula of Julia Felix");
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn = new DataTableColumn() {{setName("id");}};
        dataTable.setDataTableColumns(Arrays.asList(dataTableColumn));
        
        AbstractDataRecord dataRecord0 = new AbstractDataRecord(12345L, dataTable);
        ownedDataRecords.add(dataRecord0);
        
        AbstractDataRecord dataRecord1 = new AbstractDataRecord(45678L, dataTable);
        ownedDataRecords.add(dataRecord1);
 
        ownedDataTables.add(dataTable);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            // By getMetaData
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(ownedDataTables));            
            oneOf(repositoryService).findOwnedDataTableByName("Italy/Pompeii: Insula of Julia Felix_s"); will(returnValue(dataTable));            
            oneOf(repositoryService).findAllDataRecordsForDataTable(dataTable); will(returnValue(ownedDataRecords));
        }});
 
        final MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        QueryInfo queryInfo = new QueryInfo();
        EntitiesResponse entitiesResponse = producer.getEntities("Italy/Pompeii: Insula of Julia Felix_s", queryInfo);
        
        assertEquals(2, entitiesResponse.getEntities().size());
    }
    
    @Test
    public void testDataRecordToOEntityProducesAnEntity() {
        
        final List<DataTable> ownedDataTables = new ArrayList<DataTable>();   
        
        DataTable dataTable = new DataTable();
        dataTable.setName("Italy/Pompeii: Insula of Julia Felix");
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn = new DataTableColumn() {{setName("id");}};
        dataTable.setDataTableColumns(Arrays.asList(dataTableColumn));
        
        AbstractDataRecord dataRecord0 = new AbstractDataRecord(12345L, dataTable);
        
        ownedDataTables.add(dataTable);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            // metaDataBuilder.build()
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(ownedDataTables));            
        }});

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet("Italy/Pompeii: Insula of Julia Felix_s");
        
        TDarODataProducer producer = new TDarODataProducer(null, metaDataBuilder);
        OEntity dataRecordOEntity = producer.dataRecordToOEntity(metaData , entitySet, dataRecord0);

        assertEquals(1, dataRecordOEntity.getProperties().size());      
    }
    
    @Test
    public void testDataRecordToOEntityProducesEntityWithMultipleProperties() {
        
        final DataTable dataTable = new DataTable();
        dataTable.setName("Italy/Pompeii: Insula of Julia Felix");
                
        AbstractDataRecord dataRecord0 = new AbstractDataRecord(12345L, dataTable){{put("title","Hand axe 4321");}};
        
        List<DataTableColumn> dataTableColumns = new ArrayList<DataTableColumn>();
        
        DataTableColumn dataTableColumn0 = new DataTableColumn();
        dataTableColumn0.setName("id");
        dataTableColumn0.setColumnDataType(DataTableColumnType.BIGINT);
        dataTableColumns.add(dataTableColumn0);
        
        DataTableColumn dataTableColumn1 = new DataTableColumn();
        dataTableColumn1.setName("title");
        dataTableColumn1.setColumnDataType(DataTableColumnType.VARCHAR);
        dataTableColumns.add(dataTableColumn1);
        
        dataTable.setDataTableColumns(dataTableColumns);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(Arrays.asList(dataTable)));            
        }});
 
        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet("Italy/Pompeii: Insula of Julia Felix_s");
        
        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        OEntity dataRecordOEntity = producer.dataRecordToOEntity(metaData , entitySet, dataRecord0);

        assertEquals(2, dataRecordOEntity.getProperties().size());      
    }

    // TODO RR: repeat test to use expanded form.
    @Test
    public void testDataRecordToOEntityProducesNoLink() {
        
        final DataTable dataTable = new DataTable();
        dataTable.setName("Italy/Pompeii: Insula of Julia Felix");
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn = new DataTableColumn() {{setName("id");}};
        dataTable.setDataTableColumns(Arrays.asList(dataTableColumn));

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {{
            oneOf(repositoryService).findAllOwnedDataTables(); will(returnValue(Arrays.asList(dataTable)));            
        }});

        AbstractDataRecord dataRecord = new AbstractDataRecord(76543L, dataTable);        
 
        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);
        EdmDataServices metaData = metaDataBuilder.build();
        EdmEntitySet entitySet = metaData.findEdmEntitySet("Italy/Pompeii: Insula of Julia Felix_s");
        
        TDarODataProducer producer = new TDarODataProducer(null, metaDataBuilder);
        OEntity dataTableOEntity = producer.dataRecordToOEntity(metaData, entitySet, dataRecord);

        assertEquals(0, dataTableOEntity.getLinks().size());      
    }

//    @Test
//    public void testGetEntitiesCount() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetEntity() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetNavProperty() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetNavPropertyCount() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testClose() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testCreateEntityStringOEntity() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testCreateEntityStringOEntityKeyStringOEntity() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testDeleteEntity() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testMergeEntity() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testUpdateEntity() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetLinks() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testCreateLink() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testUpdateLink() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testDeleteLink() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testCallFunction() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetNameSpace() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetEntityTypes() {
//        fail("Not yet implemented");
//    }

}
