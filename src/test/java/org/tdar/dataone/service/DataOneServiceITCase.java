package org.tdar.dataone.service;

import java.net.URISyntaxException;

import org.dspace.foresite.OREException;
import org.dspace.foresite.ORESerialiserException;
import org.dspace.foresite.ResourceMapDocument;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.GenericService;

public class DataOneServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private DataOneService service;

    @Autowired
    private GenericService genericService;

    @Test
    @Rollback
    public void testOaiORE() throws OREException, URISyntaxException, ORESerialiserException {
        Document doc = genericService.find(Document.class, 4287L);
        ResourceMapDocument mapDocument = service.createAggregationForResource(doc);
        logger.debug(mapDocument.toString());
    }
}
