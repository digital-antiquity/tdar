package org.tdar.core.bean.resource;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

@SuppressWarnings("static-method")
public class LicenseTypeTestCase {

    @Test
    public void testImageUrlIsHttpOnHttpConfigration() {
        assertTrue("FAIMS default is currently http!", LicenseType.CREATIVE_COMMONS_ATTRIBUTION.getImageURI().contains("http://"));
    }

    @Test
    public void testImageUrlIsHttpsOnHttpsConfigration() {
        assertTrue("TDAR default is currently https!", LicenseType.CREATIVE_COMMONS_ATTRIBUTION.getSecureImageURI().contains("https://"));
    }

}
