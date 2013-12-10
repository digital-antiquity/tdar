/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.search.index.boost;

import org.hibernate.search.engine.BoostStrategy;
import org.tdar.core.bean.resource.InformationResource;

/**
 * @author Adam Brin
 * 
 */
public class InformationResourceBoostStrategy implements BoostStrategy {

    public static final float BOOST_DEFAULT = -1.0f;
    public static final float BOOST_WITH_FILE = 2.0f;

    @Override
    public float defineBoost(Object value) {
        if (value instanceof InformationResource) {
            InformationResource resource = (InformationResource) value;
            if (resource.getInformationResourceFiles().size() > 0) {
                return BOOST_WITH_FILE;
            }
        }
        return BOOST_DEFAULT;
    }
}