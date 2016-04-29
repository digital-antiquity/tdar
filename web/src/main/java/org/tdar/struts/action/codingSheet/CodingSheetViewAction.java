package org.tdar.struts.action.codingSheet;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.resource.AbstractSupportingResourceViewAction;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/coding-sheet")
public class CodingSheetViewAction extends AbstractSupportingResourceViewAction<CodingSheet> {

    private static final long serialVersionUID = 3034924577588283512L;

    @Override
    public Class<CodingSheet> getPersistableClass() {
        return CodingSheet.class;
    }

    @Autowired
    private transient DataTableService dataTableService;
    private Set<String> missingCodingKeys;

    @Override
    protected void loadCustomViewMetadata() throws TdarActionException {
        // TODO Auto-generated method stub
        super.loadCustomViewMetadata();
        setMissingCodingKeys(dataTableService.getMissingCodingKeys(getPersistable(), getTablesUsingResource()));
        
    }
    
    public boolean isOkToMapOntology() {
        CodingSheet persistable = (CodingSheet) getPersistable();
        if (persistable.getResourceType().isCodingSheet()) {
            Ontology defaultOntology = persistable.getDefaultOntology();
            if (PersistableUtils.isNullOrTransient(defaultOntology) || CollectionUtils.isNotEmpty(defaultOntology.getFilesWithProcessingErrors())) {
                getLogger().debug("cannot map, ontology issues, null or transient");
                return false;
            }
            if (CollectionUtils.isEmpty(persistable.getCodingRules()) || CollectionUtils.isNotEmpty(persistable.getFilesWithProcessingErrors())) {
                getLogger().debug("cannot map, coding sheet has errors or no rules");
                return false;
            }
            return true;
        }
        return false;
    }

    public Set<String> getMissingCodingKeys() {
        return missingCodingKeys;
    }

    public void setMissingCodingKeys(Set<String> missingCodingKeys) {
        this.missingCodingKeys = missingCodingKeys;
    }

}
