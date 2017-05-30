package org.tdar.odata.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.odata4j.exceptions.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.Accessible;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.core.service.external.session.SessionDataAware;
import org.tdar.db.model.abstracts.AbstractDataRecord;
import org.tdar.db.model.abstracts.RowOperations;
import org.tdar.utils.MessageHelper;

/**
 * Provide core methods needed to support the OData Data editing protocol
 * 
 * @author
 * 
 */
@Service
public class ODataRepositoryService implements RepositoryService, SessionDataAware {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Accessible authService;

    @Autowired
    private EntityService entityService;

    @Autowired
    @Qualifier("target")
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
        TdarUser authenticatedUser = genericService.find(TdarUser.class, getSessionData().getTdarUserId());
        TdarUser knownPerson = entityService.findByUsername(authenticatedUser.getUsername());
        if (knownPerson != null) {
            List<ResourceType> types = new ArrayList<>();
            types.add(ResourceType.DATASET);
            for (Resource resource : authService.findEditableResources(knownPerson, false, types)) {
                ownedDatasets.add((Dataset) resource);
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
        for (DataTable table : dataTables) {
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
        TdarUser authenticatedUser = genericService.find(TdarUser.class, getSessionData().getTdarUserId());
        TdarUser knownPerson = entityService.findByUsername(authenticatedUser.getUsername());
        if (knownPerson != null) {
            Dataset dataset = dataTable.getDataset();
            assert dataset != null;
            if (!authService.canView(authenticatedUser, dataset)) {
                // We use the OData exception since it is serialised and sends an appropriate HTTP status code to the client.
                throw new NotAuthorizedException(MessageHelper.getMessage("odataRepositoryService.user_not_allowed_view",
                        Arrays.asList(authenticatedUser, dataset)));
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
        TdarUser authenticatedUser = genericService.find(TdarUser.class, getSessionData().getTdarUserId());
        TdarUser knownPerson = entityService.findByUsername(authenticatedUser.getUsername());
        if (knownPerson != null) {
            DataTable dataTable = dataRecord.getDataTable();
            assert dataTable != null;
            Dataset dataset = dataTable.getDataset();
            assert dataset != null;
            if (!authService.canEdit(authenticatedUser, dataset)) {
                // We use the OData exception since it is serialised and sends an appropriate HTTP status code to the client.
                throw new NotAuthorizedException(MessageHelper.getMessage("odataRepositoryService.user_not_allowed_edit",
                        Arrays.asList(authenticatedUser, dataset)));
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
