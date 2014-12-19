package org.tdar.core.dao.integration;

import java.util.ArrayList;
import java.util.List;

public class DatasetSearchFilter extends AbstractIntegrationSearchFilter {

    private static final long serialVersionUID = 55621693166486755L;
    private List<Long> ontologyIds = new ArrayList<>();

    public DatasetSearchFilter(int maxResults, int firstResult) {
        super(maxResults, firstResult);
    }

    public List<Long> getOntologyIds() {
        return ontologyIds;
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

    public boolean isHasOntologies() {
        return !getOntologyIds().isEmpty();
    }
}
