package org.tdar.junit;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdar.core.configuration.TdarConfiguration;

public class MultipleTdarConfigurationRunner extends SpringJUnit4ClassRunner {

    public MultipleTdarConfigurationRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        // if (method.getAnnotation(RunWithTdarConfiguration.class) != null &&
        // method.getAnnotation(Ignore.class) == null) {
        // return describeTest(method);
        // }
        return super.describeChild(method);
    }

    private Description describeTest(FrameworkMethod method) {
        String[] configs = method.getAnnotation(RunWithTdarConfiguration.class).runWith();
        Description description = Description.createSuiteDescription(testName(method), method.getAnnotations());

        for (int i = 0; i < configs.length; i++) {
            description.addChild(Description.createTestDescription(getTestClass().getJavaClass(),  testName(method) + "[" + configs[i] + "] "));
        }

        return description;
    }

    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        Description description = describeTest(method);

        final String currentConfig = TdarConfiguration.getInstance().getConfigurationFile();
        if (method.getAnnotation(RunWithTdarConfiguration.class) != null &&
                method.getAnnotation(Ignore.class) == null) {
            String[] configs = method.getAnnotation(RunWithTdarConfiguration.class).runWith();

            if (configs.length > 0) {
                for (int i = 0; i < configs.length; i++) {
                    System.out.println(configs[i]);
                    TdarConfiguration.getInstance().setConfigurationFile(configs[i]);
                    runLeaf(methodBlock(method), description.getChildren().get(i), notifier);
                }
            }
        }
        TdarConfiguration.getInstance().setConfigurationFile(currentConfig);
        super.runChild(method, notifier);
    }

}