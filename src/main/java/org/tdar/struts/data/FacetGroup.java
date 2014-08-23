package org.tdar.struts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.PluralLocalizable;
import org.tdar.search.query.FacetValue;

@SuppressWarnings("rawtypes")
public class FacetGroup<C extends Enum> implements Serializable {

    private static final long serialVersionUID = -4830192828726636840L;

    public FacetGroup(Class<C> facetClass, String facetField, ArrayList<FacetValue> facets, Enum facetEnum) {
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

    public List<FacetValue> getFacets() {
        return facets;
    }

    public void setFacets(List<FacetValue> facets) {
        this.facets = facets;
    }

    public void add(String result, int count) {
        @SuppressWarnings("unchecked")
        C f = (C) facetEnum.valueOf(facetClass, result);
        FacetValue facet = new FacetValue();
        facet.setCount(count);
        facet.setKey(f.name());
        facet.setValue(f.name());
        facet.setPluralKey(f.name());
        if (f instanceof Localizable) {
            facet.setKey(((Localizable) f).getLocaleKey());
            facet.setPluralKey(facet.getKey());
        }
        if (f instanceof PluralLocalizable) {
            facet.setPluralKey(((PluralLocalizable) f).getPluralLocaleKey());
        }
        getFacets().add(facet);
    }

    public Enum getFacetEnum() {
        return facetEnum;
    }

    public void setFacetEnum(Enum facetEnum) {
        this.facetEnum = facetEnum;
    }

    private String facetField;
    private Enum facetEnum;
    private Class<C> facetClass;
    private List<FacetValue> facets = new ArrayList<FacetValue>();
}
