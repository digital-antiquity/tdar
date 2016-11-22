package org.tdar.search.query.facet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;



public class FacetWrapper implements Serializable {

    private static final long serialVersionUID = -151557113111620751L;

    private Map<String, Class<?>> facetMap = new HashMap<>();
    private Map<String, List<Facet>> facetResults = new HashMap<>();
    private Map<String, String> filters = new HashMap<>();
    private String facetPivotJson = null;
    private boolean mapFacet = false;
    public void facetBy(String facetField, Class<?> facetClass) {
        facetMap.put(facetField, facetClass);
    }
    
    public <T> void facetBy(String facetField, Class<T> facetClass, ArrayList<T> selectedResourceTypes) {
        facetBy(facetField, facetClass);
        filters.put(facetField, StringUtils.join(selectedResourceTypes, " "));
    }


    public Map<String, Class<?>> getFacetMap() {
        return facetMap;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getFacetFieldNames() {
        if (facetMap.isEmpty()) {
            return Collections.emptySet();
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

    public String getFilter(String facet) {
        return filters.get(facet);

    }

    public boolean isMapFacet() {
        return mapFacet;
    }

    public void setMapFacet(boolean mapFacet) {
        this.mapFacet = mapFacet;
    }

    public String getFacetPivotJson() {
        return facetPivotJson;
    }

    public void setFacetPivotJson(String facetPivotJson) {
        this.facetPivotJson = facetPivotJson;
    }


}
