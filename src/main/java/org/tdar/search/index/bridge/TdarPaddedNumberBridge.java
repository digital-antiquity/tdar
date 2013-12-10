package org.tdar.search.index.bridge;

import org.hibernate.search.bridge.StringBridge;
import org.tdar.search.index.TdarIndexNumberFormatter;

/**
 * 
 * $Id$
 * 
 * A {@link StringBridge} which is used to format numerical strings
 * as sortable for the index using a {@link TdarIndexNumberFormatter}.
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class TdarPaddedNumberBridge implements StringBridge {

    @Override
    public String objectToString(Object object) {
        if (object == null)
            return "";
        Number n = (Number) object;
        return TdarIndexNumberFormatter.format(n);
    }
}
