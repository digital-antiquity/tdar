package org.tdar.core.service;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import freemarker.template.Configuration;

/**
 * Enables the use of Freemarker for non-ftl files, such as emails
 * 
 * @author jtdevos
 * 
 */
@Service
public class FreemarkerServiceImpl implements FreemarkerService  {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Configuration freemarkerConfiguration;

    /* (non-Javadoc)
     * @see org.tdar.core.service.FreemarkerService#render(java.lang.String, java.util.Map)
     */
    @Override
    public String render(String templateName, Map<?,?> dataModel) throws IOException {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(templateName), dataModel);
        } catch (Exception e) {
            logger.error("Unable to process template " + templateName, e);
            throw new TdarRecoverableRuntimeException(e);
        }
    }

}
