package org.tdar.core.service.integration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.integration.IntegrationColumnPartProxy;
import org.tdar.core.dao.integration.TableDetailsProxy;
import org.tdar.db.model.abstracts.IntegrationDatabase;

import com.opensymphony.xwork2.TextProvider;

public interface DataIntegrationService {

    void setTdarDataImportDatabase(IntegrationDatabase tdarDataImportDatabase);

    /**
     * sets transient booleans on CodingRule to mark that it's mapped
     * 
     * 
     * @param column
     */
    void updateMappedCodingRules(DataTableColumn column);

    /**
     * Convert the integration context to XML for persistance in the @link PersonalFilestore and logging
     * 
     * @param integrationColumns
     * @param creator
     * @return
     * @throws Exception
     */
    String serializeIntegrationContext(List<IntegrationColumn> integrationColumns, TdarUser creator) throws Exception;

    /**
     * For a specified @link DataTableColumn, find @link CodingRule entires mapped to the @link CodingSheet and Column that actually have data in the tdardata
     * database.
     * 
     * @param column
     * @return
     */
    List<CodingRule> findMappedCodingRules(DataTableColumn column);

    /**
     * When a user maps a @link DataTableColumn to an @link Ontology without a @link CodingSheet specifically chosen, create one on-the-fly from the @link
     * OntologyNode values.
     * 
     * @param column
     * @param submitter
     * @param ontology
     * @return
     */
    CodingSheet createGeneratedCodingSheet(TextProvider provider, DataTableColumn column, TdarUser submitter, Ontology ontology);

    /**
     * @see #convertCodingSheetToCSV(CodingSheet, Collection)
     * 
     * @param sheet
     * @return
     */
    String convertCodingSheetToCSV(CodingSheet sheet);

    /**
     * Given a @link CodingSheet and a set of @link CodingRule entries, create a CSV File
     * 
     * @param sheet
     * @param rules
     * @return
     */
    String convertCodingSheetToCSV(CodingSheet sheet, Collection<CodingRule> rules);

    Map<Ontology, List<DataTable>> getIntegrationSuggestions(Collection<DataTable> bookmarkedDataTables, boolean showOnlyShared);

    Set<OntologyNode> getFilteredOntologyNodes(IntegrationColumn integrationColumn);

    /**
     * Compute the node participation for the DataTableColumns described by the specified list of ID's. The method
     * returns the results in a map of OntologyNode lists by DataTableColumn.
     *
     * @param dataTableColumnIds
     * @return
     */
    List<IntegrationColumnPartProxy> getNodeParticipationByColumn(List<Long> dataTableColumnIds);

    ModernIntegrationDataResult generateModernIntegrationResult(String integration, TextProvider provider, TdarUser authenticatedUser) throws Exception;

    /**
     * Take the result of the integration and store it in the personal filestore for retrieval
     * 
     * @param result
     * @return
     */
    PersonalFilestoreTicket storeResult(ModernIntegrationDataResult result);

    TableDetailsProxy getTableDetails(List<Long> dataTableIds);

    void saveSettingsForController(DataIntegrationWorkflow persistable, TdarUser authenticatedUser, List<UserRightsProxy> proxies);

}