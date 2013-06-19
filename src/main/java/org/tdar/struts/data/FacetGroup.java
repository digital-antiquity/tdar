package org.tdar.struts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.Facetable;

public class FacetGroup<C extends Facetable> implements Serializable {

    private static final long serialVersionUID = -4830192828726636840L;

    public FacetGroup(Class<C> facetClass, String facetField, ArrayList<C> facets, Facetable facetEnum) {
        this.facetClass = facetClass;
        this.facetField = facetField;
        this.facets = facets;
        this.setFacetEnum(facetEnum);
    }

    public String getFacetField() {
        return facetField;
    }

    public void setFacetField(String facetField) {
        this.facetField = facetField;
    }

    public Class<C> getFacetClass() {
        return facetClass;
    }

    public void setFacetClass(Class<C> facetClass) {
        this.facetClass = facetClass;
    }

    public List<C> getFacets() {
        return facets;
    }

    public void setFacets(List<C> facets) {
        this.facets = facets;
    }

    public void add(String result, int count) {
        C f = (C) facetEnum.getValueOf(result);
        f.setCount(count);
        getFacets().add(f);
    }

    public Facetable getFacetEnum() {
        return facetEnum;
    }

    public void setFacetEnum(Facetable facetEnum) {
        this.facetEnum = facetEnum;
    }

    private String facetField;
    private Facetable facetEnum;
    private Class<C> facetClass;
    private List<C> facets = new ArrayList<C>();
}
