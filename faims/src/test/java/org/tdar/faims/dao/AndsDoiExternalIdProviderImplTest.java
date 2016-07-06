package org.tdar.faims.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.tdar.faims.dao.AndsDoiExternalIdProviderImpl.IdentityFactory;
import org.tdar.core.configuration.ConfigurationAssistant;

public class AndsDoiExternalIdProviderImplTest {

    private static final String MINIMAL_PROPERTIES = "ands.doi.minimal.properties";
    private static final String TEST_PROPERTIES = "ands.doi.test.properties";
    private AndsDoiExternalIdProviderImpl andsDoiProvider;

    @Test
    public void isNotConfiguredOrEnabledOnJunkProperties() {
        andsDoiProvider = new AndsDoiExternalIdProviderImpl("junk.properties");
        assertFalse(andsDoiProvider.isConfigured());
        assertFalse(andsDoiProvider.isEnabled());
    }

    @Test
    public void isEnabledAndConfiguredOnMinimalProperties() {
        andsDoiProvider = new AndsDoiExternalIdProviderImpl(MINIMAL_PROPERTIES);
        assertTrue(andsDoiProvider.isConfigured());
        assertTrue(andsDoiProvider.isEnabled());
    }

    @Test
    public void defaultsToTestMode() {
        ConfigurationAssistant assistant = new ConfigurationAssistant();
        assistant.loadProperties(MINIMAL_PROPERTIES);
        assertFalse(assistant.getProperties().containsKey(AndsDoiExternalIdProviderImpl.IS_PRODUCTION_SERVER_KEY));
        andsDoiProvider = new AndsDoiExternalIdProviderImpl(MINIMAL_PROPERTIES);
        assertTrue(andsDoiProvider.getAppId().startsWith(IdentityFactory.TEST_PREFIX));
    }

    @Test
    public void testReadsProductionFlag() {
        ConfigurationAssistant assistant = new ConfigurationAssistant();
        assistant.loadProperties(TEST_PROPERTIES);
        assertTrue(assistant.getProperties().containsKey(AndsDoiExternalIdProviderImpl.IS_PRODUCTION_SERVER_KEY));
        assertTrue(assistant.getBooleanProperty(AndsDoiExternalIdProviderImpl.IS_PRODUCTION_SERVER_KEY));
        andsDoiProvider = new AndsDoiExternalIdProviderImpl(TEST_PROPERTIES);
        assertFalse(andsDoiProvider.getAppId().startsWith(IdentityFactory.TEST_PREFIX));
    }
}
