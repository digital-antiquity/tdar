package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.ArrayList;
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
import org.tdar.URLConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.ontology.OntologyNodeSuggestionGenerator;
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

    public static final String SAVE_MAPPING = "save-mapping";
    public static final String MAPPING = "mapping";

    private static final long serialVersionUID = 377533801938016848L;

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
     * @throws TdarActionException
     */
    @Override
    protected String save(CodingSheet codingSheet) throws TdarActionException {
        if (!Persistable.Base.isNullOrTransient(ontology)) {
            // load the full hibernate entity and set it back on the incoming column
            ontology = getGenericService().find(Ontology.class, ontology.getId());
        }

        getCodingSheetService().reconcileOntologyReferencesOnRulesAndDataTableColumns(codingSheet, ontology);

        super.saveBasicResourceMetadata();
        super.saveInformationResourceProperties();
        super.saveCategories();

        // getGenericService().saveOrUpdate(codingSheet);
        handleUploadedFiles();
        return SUCCESS;
    }

    @Override
    protected FileProxy createUploadedFileProxy(String fileTextInput) throws IOException {
        String filename = getPersistable().getTitle() + ".csv";
        // ensure csv conversion
        return new FileProxy(filename, FileProxy.createTempFileFromString(fileTextInput), VersionType.UPLOADED);
    }

    @SkipValidation
    @Action(value = MAPPING, results = {
            @Result(name = SUCCESS, location = "mapping.ftl"),
            @Result(name = INPUT, type = "redirect", location = URLConstants.VIEW_RESOURCE_ID)
    })
    public String loadOntologyMappedColumns() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        getLogger().debug("loading ontology mapped columns for {}", getPersistable());
        Ontology ontology = getCodingSheet().getDefaultOntology();
        setOntologyNodes(ontology.getSortedOntologyNodesByImportOrder());
        logger.debug("{}", getOntologyNodes());
        setCodingRules(new ArrayList<CodingRule>(getCodingSheet().getSortedCodingRules()));

        // generate suggestions for all distinct column values or only those columns that aren't already mapped?
        OntologyNodeSuggestionGenerator generator  = new OntologyNodeSuggestionGenerator();
        suggestions = generator.applySuggestions(getCodingSheet().getCodingRules(), getOntologyNodes());
        // load existing ontology mappings

        return SUCCESS;
    }

    @WriteableSession
    @SkipValidation
    @Action(value = SAVE_MAPPING, results = {
            @Result(name = SUCCESS, type = REDIRECT, location = URLConstants.VIEW_RESOURCE_ID),
            @Result(name = INPUT, location = "mapping.ftl") })
    public String saveValueOntologyNodeMapping() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        try {
            getLogger().debug("saving coding rule -> ontology node mappings for {} - this will generate a new default coding sheet!", getCodingSheet());
            for (CodingRule transientRule : getCodingRules()) {
                OntologyNode ontologyNode = transientRule.getOntologyNode();
                getLogger().debug(" matching column values: {} -> node ids {}", transientRule, ontologyNode);

                CodingRule rule = getCodingSheet().getCodingRuleById(transientRule.getId());
                Ontology ontology = getCodingSheet().getDefaultOntology();

                if (ontologyNode != null) {
                    rule.setOntologyNode(ontology.getOntologyNodeById(ontologyNode.getId()));
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
