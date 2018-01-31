/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * Describes the aggregate restrictions on all of the files on the InformationResource.
 * 
 * @author Adam Brin
 * 
 */
public enum ResourceAccessType implements HasLabel, Localizable {
    CITATION("Citation Only"), PUBLICALLY_ACCESSIBLE("Publicly Accessible Files"), PARTIALLY_RESTRICTED("Some Files Restricted"), RESTRICTED(
            "Restricted Files");

    private String label;

    ResourceAccessType(String label) {
        this.setLabel(label);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
