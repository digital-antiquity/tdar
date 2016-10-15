/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.bulk.BulkUploadService;
import org.tdar.junit.MultipleTdarConfigurationRunner;

/**
 * @author Adam Brin
 * 
 */

@RunWith(MultipleTdarConfigurationRunner.class)
public class BulkUploadServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    BulkUploadService bulkUploadService;

    @Autowired
    ReflectionService reflectionService;


    public Map<String, Resource> setup() {
        Map<String, Resource> filenameResourceMap = new HashMap<String, Resource>();
        filenameResourceMap.put("test1.pdf", new Document());
        filenameResourceMap.put("test2.pdf", new Document());
        filenameResourceMap.put("image.jpg", new Image());
        filenameResourceMap.put("test3.pdf", new Document());
        filenameResourceMap.get("test2.pdf").setTitle("bad title");
        for (Resource r : filenameResourceMap.values()) {
            r.markUpdated(getUser());
        }
        return filenameResourceMap;
    }


}
