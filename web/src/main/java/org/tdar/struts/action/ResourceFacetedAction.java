package org.tdar.struts.action;

import java.util.List;

import org.tdar.search.query.facet.Facet;

public interface ResourceFacetedAction {

    List<Facet> getResourceTypeFacets();

}
