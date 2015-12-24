package org.tdar.search.query.facet;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tdar.search.query.Facet;

import edu.emory.mathcs.backport.java.util.Collections;

public class FacetWrapper implements Serializable {

    private static final long serialVersionUID = -151557113111620751L;

    private Map<String, Class<?>> facetMap = new HashMap<>();
    private Map<String, List<Facet>> facetResults = new HashMap<>();

    public void facetBy(String facetField, Class<?> facetClass) {
        facetMap.put(facetField, facetClass);
    }

    public Map<String, Class<?>> getFacetMap() {
        return facetMap;
    }

    public Collection<String> getFacetFieldNames() {
        if (facetMap.isEmpty()) {
            return Collections.emptyList();
        }
        return facetMap.keySet();
    };

    public Map<String, List<Facet>> getFacetResults() {
        return facetResults;
    }

    public void setFacetResults(Map<String, List<Facet>> facetResults) {
        this.facetResults = facetResults;
    }

    public Class<?> getFacetClass(String name) {
        return facetMap.get(name);
    }

}
