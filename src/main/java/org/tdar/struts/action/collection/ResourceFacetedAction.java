package org.tdar.struts.action.collection;

import java.util.ArrayList;

import org.tdar.search.query.FacetValue;

public interface ResourceFacetedAction {

    ArrayList<FacetValue> getResourceTypeFacets();

}
