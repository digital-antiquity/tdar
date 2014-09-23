package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * <p>
 * Resource annotations can be:
 * <ul>
 * <li>identifiers (e.g., )</li>
 * <li>general notes (e.g., )</li>
 * <li>redaction notes (e.g., )</li>
 * </ul>
 * </p>
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public enum ResourceAnnotationType implements HasLabel, Localizable {

    IDENTIFIER("Resource Identifier");

    private final String label;

    private ResourceAnnotationType(String label) {
        this.label = label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getLabel() {
        return label;
    }
}
