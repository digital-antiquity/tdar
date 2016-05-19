package org.tdar.junit;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdar.core.configuration.TdarAppConfiguration;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ReflectionService;

@ContextConfiguration(classes = TdarAppConfiguration.class)
public class MultipleTdarConfigurationRunner extends SpringJUnit4ClassRunner {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public MultipleTdarConfigurationRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    private Description describeTest(FrameworkMethod method) {
        Description description = Description.createSuiteDescription(testName(method), method.getAnnotations());
        try {
            RunWithTdarConfiguration annotation = ReflectionService.getAnnotationFromMethodOrClass(method.getMethod(), RunWithTdarConfiguration.class);

            if (annotation == null) {
                return description;
            }
            String[] configs = annotation.runWith();
            logger.info("RunWith: {} -- {}", testName(method), configs);

            for (int i = 0; i < configs.length; i++) {
                description.addChild(Description.createTestDescription(getTestClass().getJavaClass(), testName(method) + "[" + configs[i] + "] "));
            }

        } catch (Exception e) {
            logger.error(":", e);
        }
        return description;
    }


    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        Description description = describeTest(method);
        String testName = testName(method);
        final String currentConfig = TdarConfiguration.getInstance().getConfigurationFile();
        RunWithTdarConfiguration annotation = ReflectionService.getAnnotationFromMethodOrClass(method.getMethod(), RunWithTdarConfiguration.class);

        if ((annotation != null) &&
                (method.getAnnotation(Ignore.class) == null)) {
            String[] configs = annotation.runWith();

            if (configs.length > 0) {
                for (int i = 0; i < configs.length; i++) {
                    logger.info(String.format("#############     Running %s with config [%s]      #############", testName, configs[i]));
                    setConfiguration(method, configs[i]);
                    runLeaf(methodBlock(method), description.getChildren().get(i), notifier);
                }
            }
        } else {
            super.runChild(method, notifier);
        }
        setConfiguration(method, currentConfig);
    }

    @SuppressWarnings("deprecation")
    protected void setConfiguration(final FrameworkMethod method, String config) {
        TdarConfiguration.getInstance().setConfigurationFile(config);
    }

}