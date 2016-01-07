package org.tdar.tag;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SiteTypeQueryMapper implements QueryMapper<SubjectType> {

    private Map<SubjectType, List<String>> tagTermMap;

    public Map<SubjectType, List<String>> getTagTermMap() {
        return tagTermMap;
    }

    public void setTagTermMap(Map<SubjectType, List<String>> tagTermMap) {
        this.tagTermMap = tagTermMap;
    }

    @Override
    public List<String> findMappedValues(SubjectType sub) {
        List<String> vals = tagTermMap.get(sub);
        if (vals == null) {
            return Collections.emptyList();
        }
        return vals;
    }

}
