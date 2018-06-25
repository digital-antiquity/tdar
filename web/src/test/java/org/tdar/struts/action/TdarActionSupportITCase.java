package org.tdar.struts.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.dataset.DatasetController;

/**
 * The aim of this test is simple: to ensure that the path from TdarActionSupport through to the property file yields the expected results for the
 * switchable.map.obfuscation setting. The FAIMS configuration file has it set to true: the TDAR configuration file doesn't even have it.
 * 
 * @author Martin Paulo
 */
@RunWith(MultipleTdarConfigurationRunner.class)
public class TdarActionSupportITCase extends AbstractAdminControllerITCase {

    /** Our proxy for the abstract TdarActionSupport class */
    private DatasetController controller;

    @Before
    public void setUpArchiveController() {
        controller = generateNewInitializedController(DatasetController.class);
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
    public void testIsSwitchableMapObfuscationOnForFaims() {
        assertTrue(controller.getConfig().isSwitchableMapObfuscation());
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR_DISABLED_OBFUSCATION })
    public void testIsSwitchableMapObfuscationOffForTdar() {
        assertFalse(controller.getConfig().isSwitchableMapObfuscation());
    }

}
