package org.tdar.core.bean.resource;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.RunWithTdarConfiguration;

@SuppressWarnings("static-method")
public class LicenseTypeITCase {

    @Test
    public void testImageUrlIsHttpOnHttpConfigration() {
        setConfiguration(RunWithTdarConfiguration.FAIMS);
        testEachLicenceImageUri("FAIMS default is currently http!", "http://");
    }

    
    @Test
    public void testImageUrlIsHttpsOnHttpsConfigration() {
        setConfiguration(RunWithTdarConfiguration.TDAR);
        testEachLicenceImageUri("TDAR default is currently https!", "https://");
    }


    private void testEachLicenceImageUri(final String errorMessage, final String prefix) {
        for (LicenseType lt : LicenseType.values()) {
            if (lt.getImageURI().length() > 0) {
                assertTrue(errorMessage, lt.getImageURI().startsWith(prefix));
            }
        }
    }

    private void setConfiguration(final String filePath) {
        TdarConfiguration.getInstance().setConfigurationFile(filePath);
    }

}
