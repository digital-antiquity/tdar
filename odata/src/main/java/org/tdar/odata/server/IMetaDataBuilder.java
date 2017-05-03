package org.tdar.odata.server;

import org.odata4j.edm.EdmDataServices;

public interface IMetaDataBuilder {

    EdmDataServices build();

    String getNameSpace();

}