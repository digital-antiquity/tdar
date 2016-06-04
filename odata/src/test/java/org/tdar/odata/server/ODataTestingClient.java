package org.tdar.odata.server;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumers;
import org.odata4j.consumer.behaviors.BasicAuthenticationBehavior;

public class ODataTestingClient {

    public void retrieveEntities()
    {
        ODataConsumer.Builder builder = ODataConsumers.newBuilder(Constant.SERVICE_URL);
        builder.setClientBehaviors(new BasicAuthenticationBehavior(Constant.TEST_USER_NAME, Constant.TEST_PASSWORD));
        ODataConsumer c = builder.build();
        /* List<OEntity> templates = */c.getEntities("VirtualMachineTemplates").execute().toList();
    }
}
