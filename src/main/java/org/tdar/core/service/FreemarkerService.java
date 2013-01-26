package org.tdar.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import freemarker.template.Configuration;



@Service
public class FreemarkerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Configuration freemarkerConfiguration;
    
    
    public String render(String templatePath, Object dataModel) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(
                    freemarkerConfiguration.getTemplate(templatePath), dataModel);
        } catch (Exception e) {
            logger.error("Unable to process template " + templatePath, e);
            throw new TdarRecoverableRuntimeException(e);
        }
    }

}
