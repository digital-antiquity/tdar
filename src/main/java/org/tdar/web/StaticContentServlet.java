package org.tdar.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;

/**
 * Very basic servlet for serving static content outside of application path. This is temporary and you should not use this.
 * Instead, you should configure your static web server to handle static content, as it does a much better job.
 */
@WebServlet("/hosted/*")
public class StaticContentServlet extends HttpServlet {

    private static final long serialVersionUID = -4676094228284733556L;
    String basepath = TdarConfiguration.getInstance().getHostedFileStoreLocation();
    Logger logger = LoggerFactory.getLogger(getClass());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.trace("static content request: {}", request);
        String filename = request.getPathInfo().substring(1);
        File file = new File(basepath, filename);
        response.setHeader("Content-Type", getServletContext().getMimeType(filename));
        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        Files.copy(file.toPath(), response.getOutputStream());
    }

}