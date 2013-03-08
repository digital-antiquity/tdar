package org.tdar.core.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.FileSystemResourceDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import freemarker.template.Configuration;

@Service
public class FreemarkerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Configuration freemarkerConfiguration;

    @Autowired
    FileSystemResourceDao fileDao;

    public String render(String templateName, Object dataModel) throws IOException {
        return render(TdarConfiguration.getInstance().getFremarkerTemplateDirectory(), templateName, dataModel);
    }

    public String render(File baseDir, String templateName, Object dataModel) throws IOException {
        File template = new File(baseDir, templateName);
        if (!template.exists()) {
            template = fileDao.loadTemplate(template.getPath());
        }
        return render(template, dataModel);
    }

    private String render(File template, Object dataModel) throws FileNotFoundException {
        if (!template.exists()) {
            throw new FileNotFoundException("Template File not found: "+ template);
        }
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(
                    freemarkerConfiguration.getTemplate(template.getAbsolutePath()), dataModel);
        } catch (Exception e) {
            logger.error("Unable to process template " + template.getAbsolutePath(), e);
            throw new TdarRecoverableRuntimeException(e);
        }
    }

}
