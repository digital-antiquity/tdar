package org.tdar;

import org.junit.Assert;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.WebTestCase;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

public class MultipleWebTdarConfigurationRunner extends MultipleTdarConfigurationRunner {

    public MultipleWebTdarConfigurationRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    protected final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);

    @Override
    protected void setConfiguration(FrameworkMethod method, String config) {
        super.setConfiguration(method, config);
        Class<?> testClass = getTestClass().getJavaClass();
        if (WebTestCase.class.isAssignableFrom(testClass)) {
            try {
                // if we tried to change the baseUrl, this could break stuff
                String url = TdarConfiguration.getInstance().getBaseUrl() + "/admin/switchContext/denied?configurationFile=" + config;
                logger.info("LOADING CONFIG : " + url);
                webClient.getPage(url);
            } catch (Exception e) {
                logger.warn("Exception {}", e);
                Assert.fail(e.getMessage());
            }
        }

    }
}
