package org.tdar.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.PairtreeFilestore;

/**
 * Very basic servlet for serving static content outside of application path. This is temporary and you should not use this.
 * Instead, you should configure your static web server to handle static content, as it does a much better job.
 */
public class StaticContentServlet extends HttpServlet {

    private static final long serialVersionUID = -4676094228284733556L;
    String basepath = TdarConfiguration.getInstance().getHostedFileStoreLocation();
    Logger logger = LoggerFactory.getLogger(getClass());

    public StaticContentServlet() {
        logger.debug("starting static content servlet");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.trace("static content request: {}", request);
        String filename = request.getPathInfo().substring(1);
        String[] idValues = request.getParameterValues("id");

        File file = getRequestedFile(filename, idValues);
        response.setHeader("Content-Type", getServletContext().getMimeType(filename));
        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        Files.copy(file.toPath(), response.getOutputStream());
    }

    /**
     * Return the File object specified by the path. If "id" specified in queryString, File path resolves to basepath+pairtreeRoot, where pairtreeRoot
     * is calculated from id value. Otherwise, file path resolves to basepath.
     * 
     * @return
     */
    public File getRequestedFile(String path, String[] parameterValues) {
        File parentFolder = new File(basepath);
        if (parameterValues != null) {
            String id = parameterValues[0];
            parentFolder = new File(parentFolder, PairtreeFilestore.toPairTree(id));
        }
        return new File(parentFolder, path);
    }

}