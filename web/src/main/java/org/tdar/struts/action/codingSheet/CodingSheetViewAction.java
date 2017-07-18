package org.tdar.struts.action.codingSheet;

import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.struts.action.resource.AbstractSupportingResourceViewAction;
import org.tdar.struts_base.action.TdarActionException;

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
    @Autowired
    private CodingSheetService codingSheetService;
    private Set<String> missingCodingKeys;

    @Override
    protected void loadCustomViewMetadata() throws TdarActionException {
        // TODO Auto-generated method stub
        super.loadCustomViewMetadata();
        setMissingCodingKeys(dataTableService.getMissingCodingKeys(getPersistable(), getTablesUsingResource()));
        
    }
    
    public boolean isOkToMapOntology() {
        return codingSheetService.isOkToMapOntology(getPersistable());
    }

    public Set<String> getMissingCodingKeys() {
        return missingCodingKeys;
    }

    public void setMissingCodingKeys(Set<String> missingCodingKeys) {
        this.missingCodingKeys = missingCodingKeys;
    }

}
