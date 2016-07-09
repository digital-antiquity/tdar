package org.tdar.search.query.builder;

import org.apache.commons.collections4.CollectionUtils;
import org.tdar.search.bean.SearchParameters;
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
public class MultiCoreQueryBuilder extends QueryBuilder {

    public MultiCoreQueryBuilder() {
//        setTypeLimit(LookupSource.RESOURCE.name());
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

}
