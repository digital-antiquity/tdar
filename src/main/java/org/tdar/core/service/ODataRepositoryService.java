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
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.external.Accessible;
import org.tdar.odata.server.AbstractDataRecord;
import org.tdar.odata.server.RepositoryService;
import org.tdar.utils.MessageHelper;
import org.tdar.web.SessionData;
import org.tdar.web.SessionDataAware;

/**
 * Provide core methods needed to support the OData Data editing protocol
 * 
 * @author 
 *
 */
@Service
public class ODataRepositoryService implements RepositoryService, SessionDataAware {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private Accessible authService;

    @Autowired
    private EntityService entityService;
    
    @Autowired
    private RowOperations databaseService;

    @Autowired
    private GenericService genericService;

    @Autowired
    private SessionData sessionData;

    @Override
    public SessionData getSessionData() {
        return sessionData;
    }

    @Override
    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }

    /**
     * Find all @link Dataset entries that user has access to
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Dataset> findAllOwnedDatasets() {
        List<Dataset> ownedDatasets = new ArrayList<Dataset>();
        Person authenticatedUser = getSessionData().getPerson();
        Person knownPerson = entityService.findByUsername(authenticatedUser.getUsername());
        if (knownPerson != null) {
            List<ResourceType> types = new ArrayList<>();
            types.add(ResourceType.DATASET);
            for (Resource resource : authService.findEditableResources(knownPerson, false, types)) {
                ownedDatasets.add((Dataset)resource);
            }
        }
        return ownedDatasets;
    }


    /**
     * Find All @link DataTable entries that a user has access to
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<DataTable> findAllOwnedDataTables() {
        List<DataTable> ownedDataTables = new ArrayList<DataTable>();
        for (Dataset dataset : findAllOwnedDatasets()) {
            ownedDataTables.addAll(dataset.getDataTables());
        }
        return ownedDataTables;
    }

    /**
     * Find a @link DataTable by name
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DataTable findOwnedDataTableByName(String entitySetName) {
        assert entitySetName != null;
        assert entitySetName.length() > 0;
        List<DataTable> dataTables = findAllOwnedDataTables();
        for (DataTable table :dataTables) {
            if (entitySetName.equals(table.getName())) {
                return table;
            }
        }
        return null;
    }

    /**
     * Find all Rows of data from @link DataTable and @link Dataset entities that user has access to
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<AbstractDataRecord> findAllOwnedDataRecords() {

        List<AbstractDataRecord> ownedDataRecords = new ArrayList<AbstractDataRecord>();
        List<DataTable> dataTables = findAllOwnedDataTables();
        for (DataTable dataTable : dataTables) {
            Set<AbstractDataRecord> dataTableRecords = databaseService.findAllRows(dataTable);
            ownedDataRecords.addAll(dataTableRecords);
        }
        return ownedDataRecords;
    }

    /**
     * Find all Rows of data from @link DataTable entities that user has access to
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<AbstractDataRecord> findAllDataRecordsForDataTable(DataTable dataTable) {

        List<AbstractDataRecord> dataRecords = new ArrayList<AbstractDataRecord>();
        Person authenticatedUser = getSessionData().getPerson();
        Person knownPerson = entityService.findByUsername(authenticatedUser.getUsername());
        if (knownPerson != null) {
            Dataset dataset = dataTable.getDataset();
            assert dataset != null;
            if (!authService.canView(authenticatedUser, dataset)) {
                // We use the OData exception since it is serialised and sends an appropriate HTTP status code to the client.
                throw new NotAuthorizedException(MessageHelper.getMessage("odataRepositoryService.user_not_allowed_view", authenticatedUser, dataset));
            }
            dataRecords.addAll(databaseService.findAllRows(dataTable));
        }
        return dataRecords;
    }

    /**
     * Update a row
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateRecord(AbstractDataRecord dataRecord) {
        Person authenticatedUser = getSessionData().getPerson();
        Person knownPerson = entityService.findByUsername(authenticatedUser.getUsername());
        if (knownPerson != null) {
            DataTable dataTable = dataRecord.getDataTable();
            assert dataTable != null;
            Dataset dataset = dataTable.getDataset();
            assert dataset != null;
            if (!authService.canEdit(authenticatedUser, dataset)) {
                // We use the OData exception since it is serialised and sends an appropriate HTTP status code to the client.
                throw new NotAuthorizedException(MessageHelper.getMessage("odataRepositoryService.user_not_allowed_edit", authenticatedUser, dataset));
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
