package org.tdar.core.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.tdar.core.dao.FileSystemResourceDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import freemarker.template.Configuration;

@Service
public class FreemarkerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Configuration freemarkerConfiguration;

    @Autowired
    private FileSystemResourceDao fileDao;

    public String render(String templateName, Object dataModel) throws IOException {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(templateName), dataModel);
        } catch (Exception e) {
            logger.error("Unable to process template " + templateName, e);
            throw new TdarRecoverableRuntimeException(e);
        }
    }

}
