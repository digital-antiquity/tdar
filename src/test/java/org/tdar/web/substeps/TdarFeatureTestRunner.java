package org.tdar.web.substeps;

import org.junit.runner.RunWith;

import com.technophobia.substeps.runner.JunitFeatureRunner;
import com.technophobia.substeps.runner.JunitFeatureRunner.SubStepsConfiguration;
import com.technophobia.webdriver.substeps.impl.BaseWebdriverSubStepImplementations;


@SubStepsConfiguration( 
        featureFile = "./target/test-classes/features",
        subStepsFile = "./target/test-classes/substeps",
        stepImplementations = {BaseWebdriverSubStepImplementations.class })
@RunWith(JunitFeatureRunner.class)
public class TdarFeatureTestRunner {
    //no op.  necessary to run feature tests from JUnit
}
