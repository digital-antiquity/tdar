package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.parser.CodingSheetParserException;
import org.tdar.struts.WriteableSession;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FileProxy;

/**
 * $Id$
 * 
 * <p>
 * Manages requests to create/delete/edit an CodingSheet and its associated metadata.
 * </p>
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/coding-sheet")
public class CodingSheetController extends AbstractSupportingInformationResourceController<CodingSheet> {

    private static final long serialVersionUID = 377533801938016848L;

    private List<CodingSheet> allSubmittedCodingSheets;

    private List<OntologyNode> ontologyNodes;

    private List<CodingRule> codingRules;

    private Ontology ontology;

    private SortedMap<String, List<OntologyNode>> suggestions;

    @Override
    protected void loadCustomMetadata() throws TdarActionException {
        super.loadCustomMetadata();
        setOntology(getCodingSheet().getDefaultOntology());
    };

    /**
     * Save basic metadata of the registering concept.
     * 
     * @param concept
     */
    @Override
    protected String save(CodingSheet codingSheet) {
        if (!Persistable.Base.isNullOrTransient(ontology)) {
            // load the full hibernate entity and set it back on the incoming column
            ontology = getGenericService().find(Ontology.class, ontology.getId());
        }

        getCodingSheetService().reconcileOntologyReferencesOnRulesAndDataTableColumns(codingSheet, ontology);

        super.saveBasicResourceMetadata();
        super.saveInformationResourceProperties();
        super.saveCategories();

        getGenericService().saveOrUpdate(codingSheet);
        handleUploadedFiles();
        // datatables associated with this coding sheet need to be updated
        refreshAssociatedData(codingSheet);
        return SUCCESS;
    }

    // retranslate associated datatables, and recreate translated files
    private void refreshAssociatedData(CodingSheet codingSheet) {
        if (codingSheet.getAssociatedDataTableColumns() != null && codingSheet.getAssociatedDataTableColumns().size() > 0) {
            getDatasetService().translate(codingSheet.getAssociatedDataTableColumns(), codingSheet);
            List<DataTable> dataTables = getDataTableService().findDataTablesUsingResource(getPersistable());
            for (DataTable dataTable : dataTables) {
                getDatasetService().createTranslatedFile(dataTable.getDataset());
            }
        }
    }

    @Override
    protected FileProxy createUploadedFileProxy(String fileTextInput) throws IOException {
        String filename = getPersistable().getTitle() + ".csv";
        // ensure csv conversion
        return new FileProxy(filename, FileProxy.createTempFileFromString(fileTextInput), VersionType.UPLOADED);
    }

    @Override
    protected void processUploadedFiles() throws IOException {
        // 1. save metadata for coding sheet file
        // 1.1 Create CodingSheet object, and save the metadata
        Collection<InformationResourceFileVersion> files = getPersistable().getLatestVersions(VersionType.UPLOADED);
        getLogger().debug("processing uploaded coding sheet files: {}", files);

        if (files.size() != 1) {
            getLogger().warn("Unexpected number of files associated with this coding sheet, expected 1 got " + files.size());
            return;
        }

        /*
         * two cases, either:
         * 1) 1 file uploaded (csv | tab | xls)
         * 2) tab entry into form (2 files uploaded 1 archival, 2 not)
         */

        InformationResourceFileVersion toProcess = files.iterator().next();
        if (files.size() > 1) {
            for (InformationResourceFileVersion file : files) {
                if (file.isArchival())
                    toProcess = file;
            }
        }
        // should always be 1 based on the check above
        getLogger().debug("adding coding rules");
        try {
            getCodingSheetService().parseUpload(getPersistable(), toProcess);
            getGenericService().saveOrUpdate(getPersistable());
        } catch (CodingSheetParserException e) {
            toProcess.getInformationResourceFile().setStatus(FileStatus.PROCESSING_ERROR);
            getGenericService().saveOrUpdate(toProcess.getInformationResourceFile());
            addActionError(e.getMessage());
        }
    }

    @SkipValidation
    @Action(value = "mapping", results = {
            @Result(name = SUCCESS, location = "mapping.ftl"),
            @Result(name = INPUT, type = "redirect", location = "view?id=${resource.id}")
    })
    public String loadOntologyMappedColumns() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        getLogger().debug("loading ontology mapped columns for {}", getPersistable());
        Ontology ontology = getCodingSheet().getDefaultOntology();
        setOntologyNodes(ontology.getSortedOntologyNodesByImportOrder());
        logger.debug("{}", getOntologyNodes());
        setCodingRules(new ArrayList<CodingRule>(getCodingSheet().getSortedCodingRules()));
        // List<String> distinctColumnValues = getDistinctColumnValues();
        // generate suggestions for all distinct column values or only those
        // columns that aren't already mapped?
        suggestions = getOntologyService().applySuggestions(getCodingSheet().getCodingRules(), getOntologyNodes());
        // load existing ontology mappings

        return SUCCESS;
    }

    @WriteableSession
    @SkipValidation
    @Action(value = "save-mapping", results = {
            @Result(name = SUCCESS, type = "redirect", location = "view?id=${resource.id}"),
            @Result(name = INPUT, location = "mapping.ftl") })
    public String saveValueOntologyNodeMapping() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        try {
            getLogger().debug("saving coding rule -> ontology node mappings for {} - this will generate a new default coding sheet!");
            for (CodingRule transientRule : getCodingRules()) {
                getLogger().debug(" matching column values: {} -> node ids {}", transientRule, transientRule.getOntologyNode());
                CodingRule rule = getCodingSheet().getCodingRuleById(transientRule.getId());
                Ontology ontology = getCodingSheet().getDefaultOntology();
                if (transientRule.getOntologyNode() != null) {
                    OntologyNode node = ontology.getOntologyNodeById(transientRule.getOntologyNode().getId());
                    rule.setOntologyNode(node);
                }
            }
            getGenericService().save(getCodingSheet().getCodingRules());
        } catch (Throwable tde) {
            logger.error(tde.getMessage(), tde);
            addActionErrorWithException(tde.getMessage(), tde);
            return INPUT;
        }
        return SUCCESS;
    }

    public List<CodingRule> getCodingRules() {
        return codingRules;
    }

    /**
     * Returns all coding sheets submitted by the currently authenticated user.
     * 
     * @return all coding sheets submitted by the currently authenticated user.
     */
    public List<CodingSheet> getAllSubmittedCodingSheets() {
        if (allSubmittedCodingSheets == null) {
            allSubmittedCodingSheets = getCodingSheetService().findBySubmitter(getAuthenticatedUser());
        }
        return allSubmittedCodingSheets;
    }

    /**
     * Get the current concept.
     * 
     * @return
     */
    public CodingSheet getCodingSheet() {
        return getPersistable();
    }

    public void setCodingSheet(CodingSheet codingSheet) {
        this.setPersistable(codingSheet);
    }

    @Override
    public String deleteCustom() {
        List<Resource> related = getRelatedResources();
        if (related.size() > 0) {
            String titles = StringUtils.join(related, ',');
            String message = "please remove the mappings before deleting: " + titles;
            addActionErrorWithException("this resource is still mapped to the following datasets", new TdarRecoverableRuntimeException(message));
            return ERROR;
        }
        return SUCCESS;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.CODING_SHEET);
    }

    public Class<CodingSheet> getPersistableClass() {
        return CodingSheet.class;
    }

    public void setCodingRules(List<CodingRule> codingRules) {
        this.codingRules = codingRules;
    }

    public List<OntologyNode> getOntologyNodes() {
        return ontologyNodes;
    }

    public void setOntologyNodes(List<OntologyNode> ontologyNodes) {
        this.ontologyNodes = ontologyNodes;
    }

    /**
     * @return the suggestions
     */
    public SortedMap<String, List<OntologyNode>> getSuggestions() {
        return suggestions;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }
}
