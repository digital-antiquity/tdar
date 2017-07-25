package org.tdar.faims.dao;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.tdar.faims.dao.AndsDoiExternalIdProviderImpl.IdentityFactory;

import au.csiro.doiclient.business.AndsDoiIdentity;

public class IdentityFactoryTest {

    private static final String FAKE_DOMAIN = "au.edu.versi";
    private static final String FAKE_APP_ID = "anti-test";
    IdentityFactory testFactory;

    @Test
    public void doesReturnTestAppIdIfInTestMode() {
        testFactory = new IdentityFactory(FAKE_APP_ID, FAKE_DOMAIN, false);
        AndsDoiIdentity appId = testFactory.getAppId();
        assertTrue(appId.getAppId().equals(IdentityFactory.TEST_PREFIX + FAKE_APP_ID));
    }

    @Test
    public void doesNotOrdinarilyReturnTestAppId() {
        testFactory = new IdentityFactory(FAKE_APP_ID, FAKE_DOMAIN, true);
        AndsDoiIdentity appId = testFactory.getAppId();
        assertTrue(appId.getAppId().equals(FAKE_APP_ID));
    }

    @Test
    public void testGetNullIdentityReturnsEmpyIdentityEvenUnderTest() {
        testFactory = new IdentityFactory(FAKE_APP_ID, FAKE_DOMAIN, true);
        AndsDoiIdentity nullId = testFactory.getNullAppId();
        assertTrue(StringUtils.isBlank(nullId.getAppId()));
        assertTrue(StringUtils.isBlank(nullId.getAuthDomain()));
    }

    @Test
    public void testGetNullIdentityOrdinarilyReturnsEmpyIdentity() {
        testFactory = new IdentityFactory(FAKE_APP_ID, FAKE_DOMAIN, false);
        AndsDoiIdentity nullId = testFactory.getNullAppId();
        assertTrue(StringUtils.isBlank(nullId.getAppId()));
        assertTrue(StringUtils.isBlank(nullId.getAuthDomain()));
    }
}
