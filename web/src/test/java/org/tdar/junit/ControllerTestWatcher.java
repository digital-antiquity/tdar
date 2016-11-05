package org.tdar.junit;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;


/**
 * Collects tdar-specific test annotations.
 * Created by jimdevos on 2/25/16.
 */
public class ControllerTestWatcher extends TestWatcher{
    private IgnoreActionErrors actionErrorsAnnotation = null;

    @Override
    protected void starting(Description description) {
        actionErrorsAnnotation = description.getAnnotation(IgnoreActionErrors.class);
    }

    public boolean isIgnoringActionErrors() {
        return actionErrorsAnnotation != null;
    }

}
