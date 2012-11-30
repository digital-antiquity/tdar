/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;
import org.tdar.search.query.QueryFieldNames;

/**
 * @author Adam Brin
 * 
 */
public enum ResourceAccessType implements HasLabel, Facetable {
    CITATION("Citation Only"),
    PUBLICALLY_ACCESSIBLE("Publicly Accessible Files"),
    PARTIALLY_RESTRICTED("Some Files Restricted"),
    RESTRICTED("Restricted Files");

    private String label;
    private transient Integer count;

    ResourceAccessType(String label) {
        this.setLabel(label);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getLuceneFieldName() {
        return QueryFieldNames.RESOURCE_ACCESS_TYPE;
    }

    @Override
    public ResourceAccessType getValueOf(String val) {
        return valueOf(val);
    }
}
