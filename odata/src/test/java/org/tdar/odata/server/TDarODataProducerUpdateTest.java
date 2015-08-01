package org.tdar.odata.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.exceptions.ForbiddenException;
import org.odata4j.exceptions.NotFoundException;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.db.model.abstracts.AbstractDataRecord;
import org.tdar.odata.DataRecord;
import org.tdar.odata.bean.MetaData.EntitySet;
import org.tdar.odata.service.RepositoryService;

@RunWith(JMock.class)
public class TDarODataProducerUpdateTest {

    private Mockery context = new Mockery();

    @Test(expected = ForbiddenException.class)
    public void testUpdateEntitiesForTDataSetsRequestThrowsException() {

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
            }
        });

        final IMetaDataBuilder metaDataBuilder = context.mock(IMetaDataBuilder.class);
        context.checking(new Expectations() {
            {
            }
        });

        final OEntity oEntity = context.mock(OEntity.class);
        context.checking(new Expectations() {
            {
            }
        });

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        producer.updateEntity(EntitySet.T_DATA_SETS, oEntity);
    }

    @Test(expected = ForbiddenException.class)
    public void testUpdateEntitiesForTDataTablesRequestThrowsException() {

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
            }
        });

        final IMetaDataBuilder metaDataBuilder = context.mock(IMetaDataBuilder.class);
        context.checking(new Expectations() {
            {
            }
        });

        final OEntity oEntity = context.mock(OEntity.class);
        context.checking(new Expectations() {
            {
            }
        });

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        producer.updateEntity(EntitySet.T_DATA_TABLES, oEntity);
    }

    @Test
    public void testUpdateEntitiesForTDataRecordsRequestUpdatesRecord() {

        String concreteDataRecordEntityName = "Italy/Pompeii: Insula of Julia Felix_s";

        Dataset dataset = AbstractFitTest.createTestDataset();
        final DataTable dataTable = dataset.getDataTables().iterator().next();
        dataTable.setName("Italy/Pompeii: Insula of Julia Felix");
        final AbstractDataRecord dataRecord = new DataRecord(1234L, dataTable);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(Arrays.asList(dataTable)));
                oneOf(repositoryService).findOwnedDataTableByName("Italy/Pompeii: Insula of Julia Felix_s");
                will(returnValue(dataTable));
                oneOf(repositoryService).updateRecord(dataRecord);
            }
        });

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);

        final OEntityKey oEntityKey = OEntityKey.create("id");
        final OProperty<?> oPropertyId = OProperties.int64("id", 1234L);
        final OProperty<?> oPropertyOther = OProperties.string("title", "Blue glaze urn #234");
        final List<OProperty<?>> oProperties = new ArrayList<OProperty<?>>();
        oProperties.add(oPropertyId);
        oProperties.add(oPropertyOther);
        final OEntity oEntity = context.mock(OEntity.class);
        context.checking(new Expectations() {
            {
                oneOf(oEntity).getEntityKey();
                will(returnValue(oEntityKey));
                oneOf(oEntity).getProperty("id");
                will(returnValue(oPropertyId));
                oneOf(oEntity).getProperties();
                will(returnValue(oProperties));
                oneOf(oEntity).getEntitySetName();
                will(returnValue("Italy/Pompeii: Insula of Julia Felix_s"));
            }
        });

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder) {
            @Override
            protected AbstractDataRecord createDataRecord(Long id, List<OProperty<?>> oProperties, DataTable dataTable) {
                for (OProperty<?> oProperty : oProperties)
                {
                    String name = oProperty.getName();
                    Object value = oProperty.getValue();
                    dataRecord.put(name, value);
                }
                return dataRecord;
            };
        };
        producer.updateEntity(concreteDataRecordEntityName, oEntity);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateEntitiesForAbstractTDataRecordsRequestUpdatesRecord() {

        String abstractDataRecordEntityName = EntitySet.T_DATA_RECORDS;

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
            }
        });

        MetaDataBuilder metaDataBuilder = new MetaDataBuilder("tDAR", repositoryService);

        final OEntity oEntity = context.mock(OEntity.class);
        context.checking(new Expectations() {
            {
            }
        });

        TDarODataProducer producer = new TDarODataProducer(repositoryService, metaDataBuilder);
        producer.updateEntity(abstractDataRecordEntityName, oEntity);
    }

}
