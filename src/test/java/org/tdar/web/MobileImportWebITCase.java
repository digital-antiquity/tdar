package org.tdar.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

@RunWith(MultipleTdarConfigurationRunner.class)
public class MobileImportWebITCase extends AbstractAuthenticatedWebTestCase {

    private static final String SITE_ARCHIVE = "Site Archive";

    @Test
    public void testMobileImportDoesNotAppearOnNonFaimsSite() {
        gotoPage("/resource/add");
        assertTextNotPresentIgnoreCase(SITE_ARCHIVE);
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
    public void testMobileImportAppearsOnFaimsSite() {
        gotoPage("/resource/add");
        assertTextPresentIgnoreCase(SITE_ARCHIVE);
    }

}
