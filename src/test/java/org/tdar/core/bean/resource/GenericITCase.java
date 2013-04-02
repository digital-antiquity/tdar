/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.service.GenericService;

/**
 * @author Adam Brin
 * 
 */
public class GenericITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;

    public static Integer INVESTIATION_TYPE_COUNT = 20;

    @Test
    public void testCount() {
        Number count = genericService.count(InvestigationType.class);
        assertEquals(Integer.valueOf(count.intValue()), INVESTIATION_TYPE_COUNT);
        List<InvestigationType> allInvestigationTypes = genericService.findAll(InvestigationType.class);
        assertEquals(INVESTIATION_TYPE_COUNT, Integer.valueOf(allInvestigationTypes.size()));
    }

}
