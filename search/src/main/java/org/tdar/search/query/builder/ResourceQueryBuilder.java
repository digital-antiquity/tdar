package org.tdar.search.query.builder;

import org.apache.commons.collections4.CollectionUtils;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.search.service.CoreNames;
import org.tdar.search.service.SearchParameters;

import com.opensymphony.xwork2.TextProvider;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class ResourceQueryBuilder extends QueryBuilder {

    public ResourceQueryBuilder() {
        this.setClasses(LookupSource.RESOURCE.getClasses());
    }
    
//    public void addResourceOmits(List<String> omits) {
//        omits.add("auto");
//        omits.add("keywordType");
//        omits.add("endDate");
//        omits.add("submitter");
//        omits.add("updatedBy");
//        omits.add("startDate");
//        omits.add("pairedValue");
//        omits.add("resourceNotes.type");
//        omits.add(".id");
//    }

//    protected Map<String, Class<? extends Analyzer>> createPartialLabelOverrides() {
//        Map<String, Class<? extends Analyzer>> map = super.createPartialLabelOverrides();
//        // indicate we just want to use the default analyzer for labels that end with the following strings
//        map.put("keywordType", null);
//        map.put("endDate", null);
//        map.put("submitter", null);
//        map.put("updatedBy", null);
//        map.put("startDate", null);
//        map.put("pairedValue", null);
//        map.put("resourceNotes.type", null);
//        map.put(".id", null);
//        return map;
//    }


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
