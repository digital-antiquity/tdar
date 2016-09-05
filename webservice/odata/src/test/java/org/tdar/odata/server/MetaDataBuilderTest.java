package org.tdar.odata.server;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityType;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.odata.service.RepositoryService;

@RunWith(JMock.class)
public class MetaDataBuilderTest {

    private Mockery context = new Mockery();

    @Test
    public void testBuild() {
        final List<DataTable> dataTables = new ArrayList<DataTable>();

        Dataset dataSet = new Dataset();
        dataSet.setTitle("Grecian Urns");

        DataTable dataTable = new DataTable();

        DataTableColumn dataTableColumn0 = new DataTableColumn();
        dataTableColumn0.setName("id");
        dataTable.getDataTableColumns().add(dataTableColumn0);
        dataTable.setDataset(dataSet);

        final RepositoryService repositoryService = context.mock(RepositoryService.class);
        context.checking(new Expectations() {
            {
                oneOf(repositoryService).findAllOwnedDataTables();
                will(returnValue(dataTables));
            }
        });

        IMetaDataBuilder builder = new MetaDataBuilder("tDAR-test", repositoryService);
        EdmDataServices metadata = builder.build();

        Iterable<EdmEntityType> entityTypes = metadata.getEntityTypes();
        boolean[] isPresents = new boolean[3];
        for (EdmEntityType edmEntityType : entityTypes) {
            if (edmEntityType.getName().equals("TDataRecord")) {
                isPresents[0] = true;
            }
            else if (edmEntityType.getName().equals("TDataTable")) {
                isPresents[1] = true;
            }
            else if (edmEntityType.getName().equals("TDataSet")) {
                isPresents[2] = true;
            }
        }
        assertTrue(isPresents[0]);
        assertTrue(isPresents[1]);
        assertTrue(isPresents[2]);
    }

}
