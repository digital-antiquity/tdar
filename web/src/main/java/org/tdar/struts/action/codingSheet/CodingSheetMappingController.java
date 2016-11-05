package org.tdar.struts.action.codingSheet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.ontology.OntologyNodeSuggestionGenerator;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.Preparable;


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
@HttpsOnly
public class CodingSheetMappingController extends AbstractAuthenticatableAction implements Preparable, PersistableLoadingAction<CodingSheet> {

    private static final long serialVersionUID = 5661394303022737505L;
    public static final String SAVE_MAPPING = "save-mapping";
    public static final String MAPPING = "mapping";

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient CodingSheetService codingSheetService;
    @Autowired
    private transient DataTableService dataTableService;
    
    private CodingSheet codingSheet;
    private List<OntologyNode> ontologyNodes;
    private List<CodingRule> codingRules;
    private Ontology ontology;
    private Long id;
    private SortedMap<String, List<OntologyNode>> suggestions;
    private Set<String> missingCodingKeys;

    private List<CodingRule> specialRules = new ArrayList<>();

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);

    }

    @SkipValidation
    @Action(value = MAPPING, results = {
            @Result(name = SUCCESS, location = "mapping.ftl"),
            @Result(name = INPUT, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.VIEW_RESOURCE_ID)
    })
    public String loadOntologyMappedColumns() throws TdarActionException {
        // checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        getLogger().debug("loading ontology mapped columns for {}", getPersistable());
        Ontology ontology = getCodingSheet().getDefaultOntology();
        setMissingCodingKeys(dataTableService.getMissingCodingKeys(getCodingSheet(), null));
        setOntologyNodes(ontology.getSortedOntologyNodesByImportOrder());
        getLogger().debug("{}", getOntologyNodes());
        setCodingRules(new ArrayList<CodingRule>(getCodingSheet().getSortedCodingRules()));
        // generate suggestions for all distinct column values or only those columns that aren't already mapped?
        addSpecialCodingRules();
        OntologyNodeSuggestionGenerator generator = new OntologyNodeSuggestionGenerator();
        suggestions = generator.applySuggestions(getCodingSheet().getCodingRules(), getOntologyNodes());
        // load existing ontology mappings

        return SUCCESS;
    }

    /**
     * We have a few special rules:
     * NULL , MISSING, and UNMAPPED
     * 
     * for these rules, we want to group them separately for the user
     */
    private void addSpecialCodingRules() {
        if (!TdarConfiguration.getInstance().includeSpecialCodingRules()) {
            return;
        }
        Map<String, CodingRule> codeToRuleMap = getCodingSheet().getCodeToRuleMap();
        CodingRule _null = codeToRuleMap.get(CodingRule.NULL.getCode());
        if (_null != null) {
            getCodingRules().remove(_null);
            getSpecialRules().add(_null);
        } else {
            getSpecialRules().add(CodingRule.NULL);
        }

        CodingRule _missing = codeToRuleMap.get(CodingRule.MISSING.getCode());
        if (_missing != null) {
            getCodingRules().remove(_missing);
            getSpecialRules().add(_missing);
        } else {
            getSpecialRules().add(CodingRule.MISSING);
        }

        CodingRule _unmapped = codeToRuleMap.get(CodingRule.UNMAPPED.getCode());
        if (_unmapped != null) {
            getCodingRules().remove(_unmapped);
            getSpecialRules().add(_unmapped);

        } else {
            getSpecialRules().add(CodingRule.UNMAPPED);
        }

    }

    @WriteableSession
    @PostOnly
    @SkipValidation
    @Action(value = SAVE_MAPPING,
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TDAR_REDIRECT, location = URLConstants.VIEW_RESOURCE_ID),
                    @Result(name = INPUT, location = "mapping.ftl") })
    public String saveValueOntologyNodeMapping() throws TdarActionException {
        // checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        try {
            getCodingRules().addAll(getSpecialRules());
            List<String> mappingIssues = codingSheetService.updateCodingSheetMappings(getCodingSheet(), getAuthenticatedUser(), getCodingRules());
            if (CollectionUtils.isNotEmpty(mappingIssues)) {
                addActionMessage(getText("codingSheetMappingController.could_not_map", Arrays.asList(mappingIssues)));
            }

        } catch (Throwable tde) {
            getLogger().error(tde.getMessage(), tde);
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
        return codingSheet;
    }

    public void setCodingSheet(CodingSheet codingSheet) {
        this.setPersistable(codingSheet);
    }

    @Override
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

    public CodingSheet getResource() {
        return codingSheet;
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

    @Override
    public boolean authorize() throws TdarActionException {
        return authorizationService.canEdit(getAuthenticatedUser(), codingSheet);
    }

    public boolean isEditable() {
        try {
            return authorize();
        } catch (TdarActionException tae) {
            getLogger().debug("authorization exception", tae);
        }
        return false;
    }

    @Override
    public Persistable getPersistable() {
        return codingSheet;
    }

    @Override
    public void setPersistable(CodingSheet persistable) {
        this.codingSheet = persistable;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANY_RESOURCE;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<String> getMissingCodingKeys() {
        return missingCodingKeys;
    }

    public void setMissingCodingKeys(Set<String> missingCodingKeys) {
        this.missingCodingKeys = missingCodingKeys;
    }

    public List<CodingRule> getSpecialRules() {
        return specialRules;
    }

    public void setSpecialRules(List<CodingRule> specialRules) {
        this.specialRules = specialRules;
    }

}
