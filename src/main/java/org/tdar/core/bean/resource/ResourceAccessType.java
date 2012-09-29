/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;

/**
 * @author Adam Brin
 * 
 */
public enum ResourceAccessType implements HasLabel {
    CITATION("Citation Only"),
    PUBLICALLY_ACCESSIBLE("Publicly Accessible Files"),
    PARTIALLY_RESTRICTED("Some Files Restricted"),
    RESTRICTED("Restricted Files");

    private String label;

    ResourceAccessType(String label) {
        this.setLabel(label);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
