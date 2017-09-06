package org.tdar.core.service;

import java.io.IOException;
import java.util.Map;

public interface FreemarkerService {

    /**
     * Given a template name and an object model, render the FTL to the string.
     * 
     * @param templateName
     * @param dataModel
     * @return
     * @throws IOException
     */
    String render(String templateName, Map<?, ?> dataModel) throws IOException;

}