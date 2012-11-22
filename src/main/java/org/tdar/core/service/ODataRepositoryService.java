package org.tdar.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.odata4j.exceptions.NotAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.external.Accessible;
import org.tdar.odata.server.AbstractDataRecord;
import org.tdar.odata.server.RepositoryService;
import org.tdar.web.SessionData;
import org.tdar.web.SessionDataAware;

@Service
public class ODataRepositoryService implements RepositoryService, SessionDataAware {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private Accessible authService;

    @Autowired
    private RowOperations databaseService;

    @Autowired
    private GenericService genericService;

    @Autowired
    private SessionData sessionData;

    public SessionData getSessionData() {
        return sessionData;
    }

    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }

    /**
     * We could probably return a set here as Datasets have unique names.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Dataset> findAllOwnedDatasets() {
        // TODO RR: check if authenticatedUser is a persisted entity.
        List<Dataset> ownedDatasets = new ArrayList<Dataset>();
        Person authenticatedUser = getSessionData().getPerson();
        Person knownPerson = genericService.findByProperty(Person.class, "username", authenticatedUser.getUsername());
        if (knownPerson != null) {
            List<Dataset> knownDatasets = genericService.findAll(Dataset.class);
            if (knownDatasets.size() > 0) {
                for (Dataset knownDataset : knownDatasets) {
                    if (authService.canView(knownPerson, knownDataset)) {
                        ownedDatasets.add(knownDataset);
                    }
                }
            }
        }
        return ownedDatasets;
    }

    /**
     * We could probably return a set here as DataTables have unique names.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<DataTable> findAllOwnedDataTables() {
        // TODO RR: check if authenticatedUser is a persisted entity.
        List<DataTable> ownedDataTables = new ArrayList<DataTable>();
        Person authenticatedUser = getSessionData().getPerson();
        Person knownPerson = genericService.findByProperty(Person.class, "username", authenticatedUser.getUsername());
        if (knownPerson != null) {
            // TODO RR: A database query that returns owned datasets directly rather than filter in code.
            List<Dataset> knownDatasets = genericService.findAll(Dataset.class);
            for (Dataset dataSet : knownDatasets) {
                if (authService.canView(knownPerson, dataSet)) {
                    Set<DataTable> dataTables = dataSet.getDataTables();
                    assert dataTables != null;
                    // session..initialize(dataTables);
                    ownedDataTables.addAll(dataTables);
                }
            }
        }
        return ownedDataTables;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DataTable findOwnedDataTableByName(String entitySetName) {
        assert entitySetName != null;
        assert entitySetName.length() > 0;

        // TODO RR: check if authenticatedUser is a persisted entity.
        DataTable nullDataTable = null;
        Person authenticatedUser = getSessionData().getPerson();
        Person knownPerson = genericService.findByProperty(Person.class, "username", authenticatedUser.getUsername());
        if (knownPerson != null) {
            // TODO RR: A database query that returns owned datatable by name directly rather than filter in code.
            List<Dataset> knownDatasets = genericService.findAll(Dataset.class);
            for (Dataset dataSet : knownDatasets) {
                if (authService.canView(knownPerson, dataSet)) {
                    Set<DataTable> dataTables = dataSet.getDataTables();
                    assert dataTables != null;
                    for (DataTable ownedDataTable : dataTables) {
                        if (entitySetName.equals(ownedDataTable.getName())) {
                            return ownedDataTable;
                        }
                    }
                }
            }
        }
        return nullDataTable;
    }

    /**
     * We return a list since AbstractDataRecord will only obey the set contract if it has a unique id.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<AbstractDataRecord> findAllOwnedDataRecords() {

        List<AbstractDataRecord> ownedDataRecords = new ArrayList<AbstractDataRecord>();

        Person authenticatedUser = getSessionData().getPerson();
        Person knownPerson = genericService.findByProperty(Person.class, "username", authenticatedUser.getUsername());
        if (knownPerson != null) {
            // TODO RR: A database query that returns owned datasets directly rather than filter in code.
            List<Dataset> knownDatasets = genericService.findAll(Dataset.class);
            for (Dataset dataSet : knownDatasets) {
                if (authService.canView(knownPerson, dataSet)) {
                    Set<DataTable> dataTables = dataSet.getDataTables();
                    assert dataTables != null;
                    for (DataTable dataTable : dataTables) {
                        Set<AbstractDataRecord> dataTableRecords = databaseService.findAllRows(dataTable);
                        ownedDataRecords.addAll(dataTableRecords);
                    }
                }
            }
        }
        return ownedDataRecords;
    }

    /**
     * We return a list since AbstractDataRecord will only obey the set contract if it has a unique id.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<AbstractDataRecord> findAllDataRecordsForDataTable(DataTable dataTable) {

        List<AbstractDataRecord> dataRecords = new ArrayList<AbstractDataRecord>();
        Person authenticatedUser = getSessionData().getPerson();
        Person knownPerson = genericService.findByProperty(Person.class, "username", authenticatedUser.getUsername());
        if (knownPerson != null) {
            Dataset dataset = dataTable.getDataset();
            assert dataset != null;
            if (!authService.canView(authenticatedUser, dataset)) {
                // We use the OData exception since it is serialised and sends an appropriate HTTP status code to the client.
                throw new NotAuthorizedException(String.format("User %s is not permitted to view the dataset %s", authenticatedUser, dataset));
            }
            dataRecords.addAll(databaseService.findAllRows(dataTable));
        }
        return dataRecords;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateRecord(AbstractDataRecord dataRecord) {
        Person authenticatedUser = getSessionData().getPerson();
        Person knownPerson = genericService.findByProperty(Person.class, "username", authenticatedUser.getUsername());
        if (knownPerson != null) {
            DataTable dataTable = dataRecord.getDataTable();
            assert dataTable != null;
            Dataset dataset = dataTable.getDataset();
            assert dataset != null;
            if (!authService.canEdit(authenticatedUser, dataset)) {
                // We use the OData exception since it is serialised and sends an appropriate HTTP status code to the client.
                throw new NotAuthorizedException(String.format("User %s is not permitted to edit the dataset %s", authenticatedUser, dataset));
            }
            Map<?, ?> data = dataRecord.asMap();
            databaseService.editRow(dataTable, dataRecord.getId(), data);
        }
    }

    public void setDatabaseService(RowOperations databaseService) {
        this.databaseService = databaseService;
    }

    public void setAuthorisationService(Accessible authService) {
        this.authService = authService;
    }

    public void setGenericService(GenericService genericService) {
        this.genericService = genericService;
    }

    protected Logger getLogger() {
        return logger;
    }
}
