package org.tdar.core.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.FileSystemResourceDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import freemarker.template.Configuration;

/**
 * Enables the use of Freemarker for non-ftl files, such as emails
 * 
 * @author jtdevos
 *
 */
@Service
public class FreemarkerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Configuration freemarkerConfiguration;

    @Autowired
    private FileSystemResourceDao fileDao;

    /**
     * Given a template name and an object model, render the FTL to the string. 
     * 
     * @param templateName
     * @param dataModel
     * @return
     * @throws IOException
     */
    public String render(String templateName, Object dataModel) throws IOException {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(templateName), dataModel);
        } catch (Exception e) {
            logger.error("Unable to process template " + templateName, e);
            throw new TdarRecoverableRuntimeException(e);
        }
    }

    /**
     * Given a template name and an object model, render the FTL to the string. 
     * 
     * @param templateName
     * @param dataModel
     * @return
     * @throws IOException
     */
    public File renderWithCache(String fileName, String templateName, Object dataModel) throws IOException {
        File cacheDir = new File(TdarConfiguration.getInstance().getFileCacheDirectory());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File cacheFile = new File(cacheDir, fileName);
        DateTime dateTime = new DateTime(cacheFile.lastModified());
        if (cacheFile.exists()) {
            if (dateTime.plusDays(1).isBeforeNow()) {
                cacheFile.delete();
            } else {
                return cacheFile;
            }
        }
        try {
            FileUtils.writeStringToFile(cacheFile, FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(templateName), dataModel));
        } catch (Exception e) {
            logger.error("Unable to process template " + templateName, e);
            throw new TdarRecoverableRuntimeException(e);
        }
        return cacheFile;
    }

}
