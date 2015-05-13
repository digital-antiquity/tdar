package org.tdar.struts.action.codingSheet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.struts.action.resource.AbstractResourceViewAction;
import org.tdar.utils.PersistableUtils;


@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/coding-sheet")
public class CodingSheetViewAction extends AbstractResourceViewAction<CodingSheet> {

    /**
     * 
     */
    private static final long serialVersionUID = 3034924577588283512L;

    public boolean isOkToMapOntology() {
        CodingSheet persistable = (CodingSheet)getPersistable();
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

}
