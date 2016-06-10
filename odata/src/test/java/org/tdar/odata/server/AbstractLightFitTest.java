package org.tdar.odata.server;

import org.springframework.test.context.ContextConfiguration;
import org.tdar.core.bean.entity.TdarUser;

@ContextConfiguration(locations = { "classpath:/org/tdar/odata/server/AbstractLightFitTest-context.xml" })
public abstract class AbstractLightFitTest extends AbstractFitTest {

    @Override
    protected void createTestScenario() {
        // Define any old user for the test.
        TdarUser person = new TdarUser("Rabbit", "Tobiath", "rabitto@cornsblog.org");
        person.setUsername("rabitto");
        getTestingServer().setPerson(person);
    }
}