package org.tdar.core.dao.integration.search;

import java.util.ArrayList;
import java.util.List;

public class DatasetSearchFilter extends AbstractIntegrationSearchFilter {

    private static final long serialVersionUID = 55621693166486755L;
    private List<Long> ontologyIds = new ArrayList<>();
    private boolean ableToIntegrate = false;

    public List<Long> getOntologyIds() {
        return ontologyIds;
    }

    @SuppressWarnings("unused")
    @Deprecated()
    //"ignore, required for hibernate"
    private void setPaddedOntologyIds(List<Long> n) {
        
    }

    public List<Long> getPaddedOntologyIds() {
        if (ontologyIds.isEmpty()) {
            return paddedIdList();
        }
        return ontologyIds;
    }

    public void setOntologyIds(List<Long> ontologyIds) {
        this.ontologyIds = ontologyIds;
    }

    public boolean isAbleToIntegrate() {
        return ableToIntegrate;
    }

    public void setAbleToIntegrate(boolean ableToIntegrate) {
        this.ableToIntegrate = ableToIntegrate;
    }

    public boolean isHasOntologies() {
        return !getOntologyIds().isEmpty();
    }

    @SuppressWarnings("unused")
    @Deprecated
    //"ignore, required for hibernate"
    private void setHasOntologies(boolean b) {
        
    }
}
