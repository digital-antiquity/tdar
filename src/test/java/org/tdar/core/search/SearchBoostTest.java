/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.search;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.search.index.boost.InformationResourceBoostStrategy;

/**
 * @author Adam Brin
 * 
 */
public class SearchBoostTest {

    @Test
    public void testInformationResourceBoostStrategy() {
        Document d = new Document();
        InformationResourceFile irFile = new InformationResourceFile();
        InformationResourceBoostStrategy strategy = new InformationResourceBoostStrategy();
        float boost = strategy.defineBoost(d);
        assertTrue("should be 1", InformationResourceBoostStrategy.BOOST_DEFAULT == boost);
        d.add(irFile);
        boost = strategy.defineBoost(d);
        assertTrue("should be 2", InformationResourceBoostStrategy.BOOST_WITH_FILE == boost);
    }
}
