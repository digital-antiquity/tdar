package org.tdar.odata.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odata4j.exceptions.NotAuthorizedException;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.Accessible;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.db.model.abstracts.AbstractDataRecord;
import org.tdar.db.model.abstracts.RowOperations;
import org.tdar.odata.DataRecord;

@RunWith(JMock.class)
@Ignore
public class ODataRepositoryServiceTest {

    Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    @Test
    public final void testFindAllOwnedDatasetsForNoAuthorisedDataSet() {

        final boolean isAuthorised = false;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final Dataset dataSet = new Dataset();
        final List<Dataset> dataSets = new ArrayList<Dataset>();
        dataSets.add(dataSet);

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
                oneOf(genericService).findAll(Dataset.class);
                will(returnValue(dataSets));
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setGenericService(genericService);
        repositoryService.setAuthorisationService(authService);

        List<Dataset> actualDataRecords = repositoryService.findAllOwnedDatasets();
        assertEquals(0, actualDataRecords.size());
    }

    private TdarUser setupPerson() {
        final TdarUser person = new TdarUser("Fred", "Nerk", null);
        person.setUsername("fnerk");
        return person;
    }

    @Test
    public final void testFindAllOwnedDatasetsForOneAuthorisedDataSet() {

        final boolean isAuthorised = true;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final Dataset dataSet = new Dataset();
        final List<Dataset> dataSets = new ArrayList<Dataset>();
        dataSets.add(dataSet);

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
                oneOf(genericService).findAll(Dataset.class);
                will(returnValue(dataSets));
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setGenericService(genericService);
        repositoryService.setAuthorisationService(authService);

        List<Dataset> actualDataRecords = repositoryService.findAllOwnedDatasets();
        assertEquals(1, actualDataRecords.size());
    }

    @Test
    public final void testFindAllOwnedDataTablesForNoAuthorisedDataTable() {
        final boolean isAuthorised = false;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final List<Dataset> dataSets = new ArrayList<Dataset>();

        final Dataset dataSet = new Dataset();
        DataTable dataTable = new DataTable();
        dataTable.setDataset(dataSet);
        dataSet.setDataTables(new HashSet<DataTable>(Arrays.asList(dataTable)));

        dataSets.add(dataSet);

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
                oneOf(genericService).findAll(Dataset.class);
                will(returnValue(dataSets));
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setGenericService(genericService);
        repositoryService.setAuthorisationService(authService);

        List<DataTable> actualDataTables = repositoryService.findAllOwnedDataTables();
        assertEquals(0, actualDataTables.size());
    }

    @Test
    public final void testFindAllOwnedDataTablesForOneAuthorisedDataTable() {
        final boolean isAuthorised = true;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final List<Dataset> dataSets = new ArrayList<Dataset>();

        final Dataset dataSet = new Dataset();
        DataTable dataTable = new DataTable();
        dataTable.setDataset(dataSet);
        dataSet.setDataTables(new HashSet<DataTable>(Arrays.asList(dataTable)));

        dataSets.add(dataSet);

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
                oneOf(genericService).findAll(Dataset.class);
                will(returnValue(dataSets));
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setGenericService(genericService);
        repositoryService.setAuthorisationService(authService);

        List<DataTable> actualDataTables = repositoryService.findAllOwnedDataTables();
        assertEquals(1, actualDataTables.size());
    }

    @Test
    public final void testFindAllOwnedDataTablesForOneAuthorisedAndOneUnauthorisedDataTables() {
        final boolean isAuthorised0 = true;
        final boolean isAuthorised1 = false;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final List<Dataset> dataSets = new ArrayList<Dataset>();

        final Dataset authorisedDataSet = new Dataset();
        dataSets.add(authorisedDataSet);
        DataTable authorisedDataTable = new DataTable();
        authorisedDataTable.setDataset(authorisedDataSet);
        authorisedDataSet.setDataTables(new HashSet<DataTable>(Arrays.asList(authorisedDataTable)));

        final Dataset unAuthorisedDataSet = new Dataset();
        dataSets.add(unAuthorisedDataSet);
        DataTable unAuthorisedDataTable = new DataTable();
        unAuthorisedDataTable.setDataset(unAuthorisedDataSet);
        unAuthorisedDataSet.setDataTables(new HashSet<DataTable>(Arrays.asList(unAuthorisedDataTable)));

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, authorisedDataSet);
                will(returnValue(isAuthorised0));
                oneOf(authService).canView(person, unAuthorisedDataSet);
                will(returnValue(isAuthorised1));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
                oneOf(genericService).findAll(Dataset.class);
                will(returnValue(dataSets));
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setGenericService(genericService);
        repositoryService.setAuthorisationService(authService);

        List<DataTable> actualDataTables = repositoryService.findAllOwnedDataTables();
        assertEquals(1, actualDataTables.size());
    }

    @Test
    public final void testFindOwnedDataTableByNameForAuthorisedDataTable() {

        final boolean isAuthorised = true;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final List<Dataset> dataSets = new ArrayList<Dataset>();

        final Dataset dataSet = new Dataset();
        DataTable dataTable = new DataTable();
        dataTable.setName("Bone carvings");
        dataTable.setDataset(dataSet);
        dataSet.setDataTables(new HashSet<DataTable>(Arrays.asList(dataTable)));

        dataSets.add(dataSet);

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
                oneOf(genericService).findAll(Dataset.class);
                will(returnValue(dataSets));
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setGenericService(genericService);
        repositoryService.setAuthorisationService(authService);

        DataTable actualDataTable = repositoryService.findOwnedDataTableByName("Bone carvings");
        assertNotNull(actualDataTable);
        assertEquals("Bone carvings", actualDataTable.getName());
    }

    @Test
    public final void testFindOwnedDataTableByNameForNoAuthorisedDataTable() {

        final boolean isAuthorised = false;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final List<Dataset> dataSets = new ArrayList<Dataset>();

        final Dataset dataSet = new Dataset();
        DataTable dataTable = new DataTable();
        dataTable.setName("Bone carvings");
        dataTable.setDataset(dataSet);
        dataSet.setDataTables(new HashSet<DataTable>(Arrays.asList(dataTable)));

        dataSets.add(dataSet);

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
                oneOf(genericService).findAll(Dataset.class);
                will(returnValue(dataSets));
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setGenericService(genericService);
        repositoryService.setAuthorisationService(authService);

        DataTable actualDataTable = repositoryService.findOwnedDataTableByName("Bone carvings");
        assertNull(actualDataTable);
    }

    @Test
    public final void testFindAllOwnedDataRecordsForUnauthorisedDataRecords() {

        final boolean isAuthorised = false;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final List<Dataset> dataSets = new ArrayList<Dataset>();

        final Dataset dataSet = new Dataset();
        dataSets.add(dataSet);

        final DataTable dataTable = new DataTable();
        dataTable.setName("Flint scrapers");
        dataTable.setDataset(dataSet);
        final Set<DataTable> dataTables = new HashSet<DataTable>();
        dataTables.add(dataTable);
        dataSet.setDataTables(dataTables);

        dataSet.setDataTables(dataTables);

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
                oneOf(genericService).findAll(Dataset.class);
                will(returnValue(dataSets));
            }
        });

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final DataRecord dataRecord = new DataRecord(1234L, dataTable);
        final Set<DataRecord> dataRecords = new HashSet<DataRecord>();
        dataRecords.add(dataRecord);

        final RowOperations databaseService = context.mock(RowOperations.class);
        context.checking(new Expectations() {
            {
                // This wont happen if authorisation is not available for a dataset.
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setDatabaseService(databaseService);
        repositoryService.setAuthorisationService(authService);
        repositoryService.setGenericService(genericService);

        List<AbstractDataRecord> actualDataRecords = repositoryService.findAllOwnedDataRecords();
        assertEquals(0, actualDataRecords.size());
    }

    @Test
    public final void testFindAllOwnedDataRecordsForAuthorisedDataRecords() {

        final boolean isAuthorised = true;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final Dataset dataSet = new Dataset();
        final List<Dataset> dataSets = new ArrayList<Dataset>();
        dataSets.add(dataSet);

        final DataTable dataTable = new DataTable();
        dataTable.setName("Flint scrapers");
        dataTable.setDataset(dataSet);
        final Set<DataTable> dataTables = new HashSet<DataTable>();
        dataTables.add(dataTable);

        dataSet.setDataTables(dataTables);

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
                oneOf(genericService).findAll(Dataset.class);
                will(returnValue(dataSets));
            }
        });

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final DataRecord dataRecord = new DataRecord(1234L, dataTable);
        final Set<DataRecord> dataRecords = new HashSet<DataRecord>();
        dataRecords.add(dataRecord);

        final RowOperations databaseService = context.mock(RowOperations.class);
        context.checking(new Expectations() {
            {
                oneOf(databaseService).findAllRows(dataTable);
                will(returnValue(dataRecords));
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setDatabaseService(databaseService);
        repositoryService.setAuthorisationService(authService);
        repositoryService.setGenericService(genericService);

        List<AbstractDataRecord> actualDataRecords = repositoryService.findAllOwnedDataRecords();
        assertEquals(1, actualDataRecords.size());
    }

    @Test(expected = NotAuthorizedException.class)
    public final void testAllFindDataRecordsForDataTableForUnauthorisedDataTable() {

        final boolean isAuthorised = false;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final Dataset dataSet = new Dataset();
        final DataTable dataTable = new DataTable();
        dataTable.setName("Flint scrapers");
        dataTable.setDataset(dataSet);

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
            }
        });

        final DataRecord dataRecord = new DataRecord(1234L, dataTable);
        final Set<DataRecord> dataRecords = new HashSet<DataRecord>();
        dataRecords.add(dataRecord);

        final RowOperations databaseService = context.mock(RowOperations.class);
        context.checking(new Expectations() {
            {
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setDatabaseService(databaseService);
        repositoryService.setAuthorisationService(authService);
        repositoryService.setGenericService(genericService);

        repositoryService.findAllDataRecordsForDataTable(dataTable);
    }

    @Test
    public final void testAllFindDataRecordsForDataTableForAuthorisedDataTable() {

        final boolean isAuthorised = true;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final Dataset dataSet = new Dataset();
        final DataTable dataTable = new DataTable();
        dataTable.setName("Flint scrapers");
        dataTable.setDataset(dataSet);

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canView(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
            }
        });

        final DataRecord dataRecord = new DataRecord(1234L, dataTable);
        final Set<DataRecord> dataRecords = new HashSet<DataRecord>();
        dataRecords.add(dataRecord);

        final RowOperations databaseService = context.mock(RowOperations.class);
        context.checking(new Expectations() {
            {
                oneOf(databaseService).findAllRows(dataTable);
                will(returnValue(dataRecords));
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setDatabaseService(databaseService);
        repositoryService.setAuthorisationService(authService);
        repositoryService.setGenericService(genericService);

        List<AbstractDataRecord> actualDataRecords = repositoryService.findAllDataRecordsForDataTable(dataTable);
        assertEquals(1, actualDataRecords.size());
    }

    @Test(expected = NotAuthorizedException.class)
    public final void testUpdateRecordDoesNotSaveUnauthorisedDataViaDatabaseService() {

        final boolean isAuthorised = false;

        SessionData sessionData = new SessionData();
        final TdarUser person = setupPerson();
        sessionData.setTdarUser(person);

        final Dataset dataSet = new Dataset();
        final DataTable dataTable = new DataTable();
        dataTable.setName("Flint scrapers");
        dataTable.setDataset(dataSet);

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canEdit(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
            }
        });

        final DataRecord dataRecord = new DataRecord(1234L, dataTable);

        final RowOperations databaseService = context.mock(RowOperations.class);
        context.checking(new Expectations() {
            {
                // Expect no save.
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setDatabaseService(databaseService);
        repositoryService.setAuthorisationService(authService);
        repositoryService.setGenericService(genericService);

        repositoryService.updateRecord(dataRecord);
    }

    @Test
    public final void testUpdateRecordSavesAuthorisedDataViaDatabaseService() {

        final boolean isAuthorised = true;

        final TdarUser person = setupPerson();
        SessionData sessionData = new SessionData();
        sessionData.setTdarUser(person);

        final Dataset dataSet = new Dataset();
        final DataTable dataTable = new DataTable();
        dataTable.setName("Flint scrapers");
        dataTable.setDataset(dataSet);

        final Accessible authService = context.mock(Accessible.class);
        context.checking(new Expectations() {
            {
                oneOf(authService).canEdit(person, dataSet);
                will(returnValue(isAuthorised));
            }
        });

        final GenericService genericService = context.mock(GenericService.class);
        context.checking(new Expectations() {
            {
                oneOf(genericService).findByProperty(Person.class, "username", "fnerk");
                will(returnValue(person));
            }
        });

        final DataRecord dataRecord = new DataRecord(1234L, dataTable);

        final RowOperations databaseService = context.mock(RowOperations.class);
        context.checking(new Expectations() {
            {
                oneOf(databaseService).editRow(dataTable, dataRecord.getId(), dataRecord.asMap());
            }
        });

        ODataRepositoryService repositoryService = new ODataRepositoryService();
        repositoryService.setSessionData(sessionData);
        repositoryService.setDatabaseService(databaseService);
        repositoryService.setAuthorisationService(authService);
        repositoryService.setGenericService(genericService);

        repositoryService.updateRecord(dataRecord);
    }

}
