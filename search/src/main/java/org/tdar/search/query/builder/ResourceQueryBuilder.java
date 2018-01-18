package org.tdar.search.query.builder;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.search.service.CoreNames;

import com.opensymphony.xwork2.TextProvider;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class ResourceQueryBuilder extends QueryBuilder implements HasCreator {

    private boolean creatorCreatedEmphasized;

    public ResourceQueryBuilder() {
        setTypeLimit(LookupSource.RESOURCE.name());
    }

    @Override
    public String getCoreName() {
        return CoreNames.RESOURCES;
    }

    public void appendIfNotEmpty(SearchParameters params, TextProvider support_) {
        if (params != null) {
            QueryPartGroup queryPartGroup = params.toQueryPartGroup(support_);
            if (!queryPartGroup.isEmpty()) {
                append(queryPartGroup);
            }
        }
        if (CollectionUtils.isNotEmpty(params.getFilters())) {
            appendFilter(params.getFilters());
        }

    }

    private boolean deemphasizeSupporting = true;
    private Set<String> boostType = null;

    public boolean isDeemphasizeSupporting() {
        return deemphasizeSupporting;
    }

    public void setDeemphasizeSupporting(boolean deemphasizeSupporting) {
        this.deemphasizeSupporting = deemphasizeSupporting;
    }

    public Set<String> getBoostType() {
        return boostType;
    }

    public void setBoostType(Set<String> matches) {
        this.boostType = matches;
    }

    public boolean isCreatorCreatedEmphasized() {
        return creatorCreatedEmphasized;
    }

    public void setCreatorCreatedEmphasized(boolean creatorCreatedEmphasized) {
        this.creatorCreatedEmphasized = creatorCreatedEmphasized;
    }

}
