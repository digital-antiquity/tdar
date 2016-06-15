package org.tdar.search.query.facet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.util.StringUtils;
import org.tdar.core.bean.resource.ResourceType;

import edu.emory.mathcs.backport.java.util.Collections;

public class FacetWrapper implements Serializable {

    private static final long serialVersionUID = -151557113111620751L;

    private Map<String, Class<?>> facetMap = new HashMap<>();
    private Map<String, List<Facet>> facetResults = new HashMap<>();
    private Map<String, String> filters = new HashMap<>();

    public void facetBy(String facetField, Class<?> facetClass) {
        facetMap.put(facetField, facetClass);
    }
    
	public void facetBy(String facetField, Class<ResourceType> facetClass, ArrayList<ResourceType> selectedResourceTypes) {
		facetBy(facetField, facetClass);
		filters.put(facetField, StringUtils.join(" ", selectedResourceTypes));
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

	public String getFilter(String facet) {
		return filters.get(facet);
		
	}


}
