package org.tdar.core.bean.resource.sensory;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
import java.util.LinkedHashSet;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.configuration.TdarConfiguration;

public class SensoryDataITCase extends AbstractIntegrationTestCase {

    @Test
    @Rollback
    public void testSimpleCreate() {
        // create a sensorydata document w/ some scans and images, then try to retrieve it.

        SensoryData sensoryData = new SensoryData();

        sensoryData.setTitle("test sensory data");
        sensoryData.setDescription("test description");
        TdarUser submitter = genericService.find(TdarUser.class, 1L);
        sensoryData.markUpdated(submitter);
        sensoryData.setDate(1234);
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            Creator<?> copyrightHolder = submitter;
            sensoryData.setCopyrightHolder(copyrightHolder);
        }
        // add some scans
        SensoryDataScan scan = new SensoryDataScan();
        scan.setFilename("filename");
        scan.setScanNotes("here are some scanning notes");

        LinkedHashSet<SensoryDataImage> images = new LinkedHashSet<SensoryDataImage>();
        for (int i = 0; i < 10; i++) {
            SensoryDataImage image = new SensoryDataImage();
            image.setFilename("file" + i);
            image.setDescription("this is a description" + i);
            images.add(image);
        }
        sensoryData.setSensoryDataImages(images);
        sensoryData.setSensoryDataScans(new LinkedHashSet<SensoryDataScan>());
        sensoryData.getSensoryDataScans().add(scan);
        genericService.save(sensoryData);
        Assert.assertNotSame("expecting hibernate to set the ID for this resource", new Long(-1L), sensoryData.getId());
        Long id = sensoryData.getId();

        // okay, try to get it back
        sensoryData = null;
        sensoryData = genericService.find(SensoryData.class, id);
        Assert.assertSame("expecting one scans for this 3dobject", 1, sensoryData.getSensoryDataScans().size());
        Assert.assertSame("expecting 10 image details for this 3dobject", 10, sensoryData.getSensoryDataImages().size());
    }

}
